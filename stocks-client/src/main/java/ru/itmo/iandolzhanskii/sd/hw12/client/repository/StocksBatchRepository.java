package ru.itmo.iandolzhanskii.sd.hw12.client.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.itmo.iandolzhanskii.sd.hw12.client.entity.model.StocksBatch;

public interface StocksBatchRepository extends JpaRepository<StocksBatch, Long> {

    Optional<StocksBatch> findBySymbolAndUserId(String symbol, Long userId);

    @Query(value = "UPDATE stocks_batches SET amount = amount + ?2 WHERE user_id = ?1", nativeQuery = true)
    void addStocksBatchAmount(Long userId, Long amount);
}
