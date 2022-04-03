package ru.itmo.iandolzhanskii.sd.hw12.server.entity.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StockViewDto {

    String symbol;
    String companyName;
    BigDecimal priceUsd;
    Long amount;
}
