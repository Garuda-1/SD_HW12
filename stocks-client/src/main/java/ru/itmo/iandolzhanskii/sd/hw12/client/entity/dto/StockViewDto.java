package ru.itmo.iandolzhanskii.sd.hw12.client.entity.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class StockViewDto {

    String symbol;
    String companyName;
    BigDecimal priceUsd;
    Long amount;
}
