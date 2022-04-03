package ru.itmo.iandolzhanskii.sd.hw12.server.controller;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.iandolzhanskii.sd.hw12.server.entity.dto.StockViewDto;
import ru.itmo.iandolzhanskii.sd.hw12.server.service.StockService;

@RestController
@RequestMapping(path = "stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public StockViewDto createStock(
        @RequestParam String symbol,
        @RequestParam BigDecimal priceUsd,
        @RequestParam Long amount,
        @RequestParam Long companyId
    ) {
        return stockService.createStock(symbol, priceUsd, amount, companyId);
    }

    @GetMapping
    public StockViewDto viewStock(@RequestParam String symbol) {
        return stockService.viewStock(symbol);
    }

    @PutMapping(path = "price")
    public StockViewDto changeStockPrice(@RequestParam String symbol, @RequestParam BigDecimal priceUsd) {
        return stockService.changePrice(symbol, priceUsd);
    }

    @PutMapping(path = "amount")
    public StockViewDto changeStockAmount(@RequestParam String symbol, @RequestParam Long amountDelta) {
        return stockService.changeAmount(symbol, amountDelta);
    }
}
