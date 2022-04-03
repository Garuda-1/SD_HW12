package ru.itmo.iandolzhanskii.sd.hw12.client.http;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.itmo.iandolzhanskii.sd.hw12.client.entity.dto.StockViewDto;

@Component
public class StocksServerHttpClient {

    @Value("${stocks-server.host}")
    String serverHost;

    @Value("${stocks-server.port}")
    Integer serverPort;

    public StockViewDto viewStock(String symbol) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(UriComponentsBuilder.newInstance()
                    .scheme("http")
                    .host(serverHost)
                    .port(serverPort)
                    .path("stock")
                    .queryParam("symbol", symbol)
                    .build()
                    .toUri()
                )
                .GET()
                .build();

            ObjectMapper objectMapper = new ObjectMapper();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to connect to server", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public StockViewDto changeStockAmount(String symbol, Long amountDelta) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(UriComponentsBuilder.newInstance()
                    .scheme("http")
                    .host(serverHost)
                    .port(serverPort)
                    .path("/stock/amount")
                    .queryParam("symbol", symbol)
                    .queryParam("amountDelta", amountDelta)
                    .build()
                    .toUri()
                )
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

            ObjectMapper objectMapper = new ObjectMapper();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new IllegalArgumentException("Stocks server has declined the operation");
            }
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to connect to server", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
