package ru.itmo.iandolzhanskii.sd.hw12.client.entity.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StockBatchViewDto {

    String symbol;
    BigDecimal priceUsd;
    Long amount;
}
