package ru.itmo.iandolzhanskii.sd.hw12.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.iandolzhanskii.sd.hw12.server.entity.model.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);
}
