package ru.itmo.iandolzhanskii.sd.hw12.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(initializers = {StocksClientApplicationTests.Initializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class StocksClientApplicationTests {

    private static final DockerImageName STOCKS_SERVER_IMAGE = DockerImageName.parse("sd_hw12_server:0.0.1-SNAPSHOT");
    private static final Integer STOCKS_SERVER_PORT = 8082;
    private static final Integer STOCKS_CLIENT_PORT = 8080;

    private static PostgreSQLContainer<?> stocksServerPostgresContainer;
    private static GenericContainer<?> stocksServerContainer;
    private static PostgreSQLContainer<?> stocksClientPostgresContainer;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            Network network = Network.newNetwork();

            stocksServerPostgresContainer = new PostgreSQLContainer<>("postgres")
                .withNetwork(network)
                .withExposedPorts(5432)
                .withNetworkAliases("stocks-server-db")
                .withDatabaseName("stocks-server")
                .withUsername("stocks-server")
                .withPassword("stocks-server");
            stocksServerPostgresContainer.start();

            stocksServerContainer = new GenericContainer<>(STOCKS_SERVER_IMAGE)
                .withEnv(Map.of(
                    "PORT", STOCKS_SERVER_PORT.toString(),
                    "DATASOURCE_URL", "jdbc:postgresql://stocks-server-db:5432/stocks-server?loggerLevel=OFF",
                    "DATASOURCE_USERNAME", stocksServerPostgresContainer.getUsername(),
                    "DATASOURCE_PASSWORD", stocksServerPostgresContainer.getPassword()
                ))
                .withNetwork(network)
                .withExposedPorts(STOCKS_SERVER_PORT)
                .withNetworkAliases("stocks-server");
            stocksServerContainer.start();

            stocksClientPostgresContainer = new PostgreSQLContainer<>("postgres")
                .withNetwork(network)
                .withNetworkAliases("stocks-client-db")
                .withExposedPorts(5432)
                .withDatabaseName("stocks-client")
                .withUsername("stocks-client")
                .withPassword("stocks-client");
            stocksClientPostgresContainer.start();

            System.setProperty("PORT", STOCKS_CLIENT_PORT.toString());
            System.setProperty(
                "DATASOURCE_URL",
                "jdbc:postgresql://localhost:" + stocksClientPostgresContainer.getMappedPort(5432) +
                    "/stocks-client"
            );
            System.setProperty("DATASOURCE_USERNAME", stocksClientPostgresContainer.getUsername());
            System.setProperty("DATASOURCE_PASSWORD", stocksClientPostgresContainer.getPassword());
            System.setProperty("STOCKS_SERVER_HOST", stocksServerContainer.getHost());
            System.setProperty(
                "STOCKS_SERVER_PORT",
                stocksServerContainer.getMappedPort(STOCKS_SERVER_PORT).toString()
            );
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    public void tearDown() {
        silentClose(stocksServerContainer);
        silentClose(stocksServerPostgresContainer);
        silentClose(stocksClientPostgresContainer);
    }

    @Test
    @DisplayName("Создание пользователя")
    public void createNewUser() throws Exception {
        mockMvc.perform(post("/user")
            .queryParam("userName", "John")
        )
            .andExpect(status().isOk())
            .andExpect(content().json(loadFile("response/user_0.json")));

        mockMvc.perform(get("/user/1"))
            .andExpect(status().isOk())
            .andExpect(content().json(loadFile("response/user_0.json")));
    }

    @Test
    @DisplayName("Изменение баланса пользователя")
    public void createNewUserAndChangeBalance() throws Exception {
        createUser();

        mockMvc.perform(put("/user/1/balance")
            .queryParam("balanceUsdDelta", "1000")
        )
            .andExpect(status().isOk())
            .andExpect(content().json(loadFile("response/user_1000.json")));

        mockMvc.perform(put("/user/1/balance")
            .queryParam("balanceUsdDelta", "-2000")
        )
            .andExpect(status().isBadRequest());

        mockMvc.perform(put("/user/1/balance")
            .queryParam("balanceUsdDelta", "-1000")
        )
            .andExpect(status().isOk())
            .andExpect(content().json(loadFile("response/user_0.json")));
    }

    @Test
    @DisplayName("Динамическое изменение цены")
    public void makeProfit() throws Exception {
        createUser();
        createCompany();
        createStock(4);
        changeUserBalance();

        mockMvc.perform(post("/user/1/stocks/buy")
            .queryParam("symbol", "COMP")
            .queryParam("amount", "2")
        )
            .andExpect(status().isOk())
            .andExpect(content().json(loadFile("response/user_1000_COMP_2.json")));
        mockMvc.perform(post("/user/1/stocks/buy")
            .queryParam("symbol", "COMP")
            .queryParam("amount", "2")
        )
            .andExpect(status().isOk())
            .andExpect(content().json(loadFile("response/user_1000_COMP_4.json")));

        changeStockPrice();

        mockMvc.perform(get("/user/1"))
            .andExpect(status().isOk())
            .andExpect(content().json(loadFile("response/user_4000_COMP_4.json")));

        mockMvc.perform(post("/user/1/stocks/sell")
            .queryParam("symbol", "COMP")
            .queryParam("amount", "4")
        )
            .andExpect(status().isOk())
            .andExpect(content().json(loadFile("response/user_4000.json")));
    }

    @Test
    @DisplayName("Несуществующий пользователь")
    public void missingUser() throws Exception {
        mockMvc.perform(get("/user/123"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Несуществующие акции")
    public void missingCompany() throws Exception {
        createUser();

        mockMvc.perform(post("/user/1/stocks/buy")
            .queryParam("symbol", "NULL")
            .queryParam("amount", "123")
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Недостаточно средств")
    public void notEnoughFunds() throws Exception {
        createUser();
        createCompany();
        createStock(100);
        changeUserBalance();

        mockMvc.perform(post("/user/1/stocks/buy")
            .queryParam("symbol", "COMP")
            .queryParam("amount", "5")
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Недостаточно акций на бирже")
    public void notEnoughStocks() throws Exception {
        createUser();
        createCompany();
        createStock(2);
        changeUserBalance();

        mockMvc.perform(post("/user/1/stocks/buy")
            .queryParam("symbol", "COMP")
            .queryParam("amount", "4")
        )
            .andExpect(status().isBadRequest());
    }

    private void createUser() throws Exception {
        mockMvc.perform(post("/user")
                .queryParam("userName", "John")
        )
            .andExpect(status().isOk())
            .andExpect(content().json(loadFile("response/user_0.json")));
    }

    private void changeUserBalance() throws Exception {
        mockMvc.perform(put("/user/1/balance")
            .queryParam("balanceUsdDelta", "1000.00")
        )
            .andExpect(status().isOk());
    }

    private void createCompany() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(buildStocksServerUri(builder -> builder
                .path("/company")
                .queryParam("companyName", "Company")
            ))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void createStock(long amount) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(buildStocksServerUri(builder -> builder
                .path("/stock")
                .queryParam("companyId", 1)
                .queryParam("symbol", "COMP")
                .queryParam("amount", amount)
                .queryParam("priceUsd", "250.00")
            ))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void changeStockPrice() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(buildStocksServerUri(builder -> builder
                .path("/stock/price")
                .queryParam("companyId", 1)
                .queryParam("symbol", "COMP")
                .queryParam("priceUsd", 1000.0)
            ))
            .PUT(HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<?> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(response.statusCode(), HttpStatus.OK.value());
    }

    private URI buildStocksServerUri(Function<UriComponentsBuilder, UriComponentsBuilder> uriModifier) {
        return uriModifier.apply(UriComponentsBuilder.newInstance())
            .scheme("http")
            .host(stocksServerContainer.getHost())
            .port(stocksServerContainer.getMappedPort(STOCKS_SERVER_PORT))
            .build()
            .toUri();
    }

    private String loadFile(String path) {
        Resource resource = new DefaultResourceLoader().getResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void silentClose(AutoCloseable autoCloseable) {
        try {
            if (autoCloseable != null) {
                autoCloseable.close();
            }
        } catch (Exception ignored) {
        }
    }
}
