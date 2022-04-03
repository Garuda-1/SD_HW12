package ru.itmo.iandolzhanskii.sd.hw12.client.entity.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserViewDto {

    Long id;
    String name;
    BigDecimal balance;
    BigDecimal totalBalance;
    List<StockBatchViewDto> ownedStocks;
}
