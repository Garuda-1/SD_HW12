package ru.itmo.iandolzhanskii.sd.hw12.client.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.iandolzhanskii.sd.hw12.client.entity.dto.StockBatchViewDto;
import ru.itmo.iandolzhanskii.sd.hw12.client.entity.dto.StockViewDto;
import ru.itmo.iandolzhanskii.sd.hw12.client.entity.model.StocksBatch;
import ru.itmo.iandolzhanskii.sd.hw12.client.http.StocksServerHttpClient;
import ru.itmo.iandolzhanskii.sd.hw12.client.entity.dto.UserViewDto;
import ru.itmo.iandolzhanskii.sd.hw12.client.entity.model.User;
import ru.itmo.iandolzhanskii.sd.hw12.client.repository.StocksBatchRepository;
import ru.itmo.iandolzhanskii.sd.hw12.client.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StocksBatchRepository stocksBatchRepository;
    private final StocksServerHttpClient stocksServerHttpClient;

    @Transactional
    public UserViewDto createUser(String userName) {
        User user = new User();
        user.setName(userName);
        user.setBalanceUsd(BigDecimal.ZERO);
        return userToDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserViewDto getUser(Long userId) {
        return userToDto(findUserById(userId));
    }

    @Transactional
    public UserViewDto updateBalance(Long userId, BigDecimal balanceUsdDelta) {
        User user = findUserById(userId);
        BigDecimal newBalance = user.getBalanceUsd().add(balanceUsdDelta);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("New balance cannot be negative");
        }
        user.setBalanceUsd(user.getBalanceUsd().add(balanceUsdDelta));
        return userToDto(userRepository.save(user));
    }

    @Transactional
    public UserViewDto buyStocks(Long userId, String symbol, Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount of stocks to purchase must be positive");
        }
        User user = findUserById(userId);

        StockViewDto stockViewDto = stocksServerHttpClient.viewStock(symbol);
        BigDecimal purchasePriceUsd = getStocksBatchPrice(stockViewDto.getPriceUsd(), amount);

        if (user.getBalanceUsd().compareTo(purchasePriceUsd) < 0) {
            throw new IllegalArgumentException(
                "Purchase price is $" + purchasePriceUsd + ", balance is $" + user.getBalanceUsd()
            );
        }

        StocksBatch stocksBatch;
        Optional<StocksBatch> stocksBatchOptional = stocksBatchRepository.findBySymbolAndUserId(symbol, userId);
        if (stocksBatchOptional.isPresent()) {
            stocksBatch = stocksBatchOptional.get();
            stocksBatch.setAmount(stocksBatch.getAmount() + amount);
        } else {
            stocksBatch = new StocksBatch();
            stocksBatch.setSymbol(symbol);
            stocksBatch.setUser(user);
            stocksBatch.setAmount(amount);
            user.getStocksBatches().add(stocksBatch);
        }
        stocksBatchRepository.save(stocksBatch);
        stocksServerHttpClient.changeStockAmount(symbol, -amount);

        user.setBalanceUsd(user.getBalanceUsd().subtract(purchasePriceUsd));
        return userToDto(userRepository.save(user));
    }

    @Transactional
    public UserViewDto sellStocks(Long userId, String symbol, Long amount) {
        User user = findUserById(userId);
        StocksBatch stocksBatch = stocksBatchRepository.findBySymbolAndUserId(symbol, userId).orElseThrow(() ->
            new IllegalArgumentException("User does not own any of " + symbol + " stocks")
        );
        if (stocksBatch.getAmount() < amount) {
            throw new IllegalArgumentException(
                "User owns " + stocksBatch.getAmount() + " " + stocksBatch.getSymbol() + " stocks"
            );
        }

        StockViewDto stockViewDto = stocksServerHttpClient.viewStock(symbol);
        BigDecimal sellPriceUsd = getStocksBatchPrice(stockViewDto.getPriceUsd(), amount);

        if (stocksBatch.getAmount().equals(amount)) {
            user.getStocksBatches().remove(stocksBatch);
            stocksBatchRepository.delete(stocksBatch);
        } else {
            stocksBatch.setAmount(stocksBatch.getAmount() - amount);
            stocksBatchRepository.save(stocksBatch);
        }
        stocksServerHttpClient.changeStockAmount(symbol, amount);

        user.setBalanceUsd(user.getBalanceUsd().add(sellPriceUsd));
        return userToDto(userRepository.save(user));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
            new EntityNotFoundException("Cannot find user with id " + userId)
        );
    }

    private UserViewDto userToDto(User user) {
        return UserViewDto.builder()
            .id(user.getId())
            .name(user.getName())
            .balance(user.getBalanceUsd())
            .totalBalance(getUserTotalBalance(user))
            .ownedStocks(Optional.ofNullable(user.getStocksBatches()).map(stocksBatches -> stocksBatches.stream()
                    .map(stock -> StockBatchViewDto.builder()
                        .symbol(stock.getSymbol())
                        .priceUsd(stocksServerHttpClient.viewStock(stock.getSymbol()).getPriceUsd())
                        .amount(stock.getAmount())
                        .build()
                    )
                    .collect(Collectors.toList())
                ).orElse(List.of())
            )
            .build();
    }

    public BigDecimal getUserTotalBalance(User user) {
        return Optional.ofNullable(user.getStocksBatches()).map(stocks -> stocks.stream()
            .map(stocksBatch -> getStocksBatchPrice(
                stocksServerHttpClient.viewStock(stocksBatch.getSymbol()).getPriceUsd(),
                stocksBatch.getAmount())
            )
            .reduce(user.getBalanceUsd(), BigDecimal::add)
        )
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getStocksBatchPrice(BigDecimal stockPriceUsd, long amount) {
        return stockPriceUsd
            .multiply(BigDecimal.valueOf(amount))
            .setScale(2, RoundingMode.DOWN);
    }
}
