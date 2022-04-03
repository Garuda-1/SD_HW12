package ru.itmo.iandolzhanskii.sd.hw12.server.service;

import java.math.BigDecimal;

import javax.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.iandolzhanskii.sd.hw12.server.entity.dto.StockViewDto;
import ru.itmo.iandolzhanskii.sd.hw12.server.entity.model.Company;
import ru.itmo.iandolzhanskii.sd.hw12.server.entity.model.Stock;
import ru.itmo.iandolzhanskii.sd.hw12.server.repository.CompanyRepository;
import ru.itmo.iandolzhanskii.sd.hw12.server.repository.StockRepository;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public StockViewDto createStock(String symbol, BigDecimal priceUsd, Long amount, Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() ->
            new EntityNotFoundException("Could not find company with id " + companyId)
        );

        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setPriceUsd(priceUsd);
        stock.setAmount(amount);
        stock.setCompany(company);
        stock = stockRepository.save(stock);

        return stockToDto(stock);
    }

    @Transactional
    public StockViewDto viewStock(String symbol) {
        return stockToDto(findBySymbol(symbol));
    }

    @Transactional
    public StockViewDto changePrice(String symbol, BigDecimal priceUsd) {
        Stock stock = findBySymbol(symbol);
        stock.setPriceUsd(priceUsd);
        stock = stockRepository.save(stock);
        return stockToDto(stock);
    }

    @Transactional
    public StockViewDto changeAmount(String symbol, Long amountDelta) {
        Stock stock = findBySymbol(symbol);
        long newAmount = stock.getAmount() + amountDelta;
        if (newAmount < 0) {
            throw new IllegalArgumentException(
                "New stock amount cannot be negative, current amount is " + stock.getAmount()
            );
        }
        stock.setAmount(newAmount);
        stock = stockRepository.save(stock);
        return stockToDto(stock);
    }

    private Stock findBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol).orElseThrow(() ->
            new EntityNotFoundException("Could not find stock with symbol " + symbol)
        );
    }

    private StockViewDto stockToDto(Stock stock) {
        return StockViewDto.builder()
            .symbol(stock.getSymbol())
            .companyName(stock.getCompany().getName())
            .priceUsd(stock.getPriceUsd())
            .amount(stock.getAmount())
            .build();
    }
}
