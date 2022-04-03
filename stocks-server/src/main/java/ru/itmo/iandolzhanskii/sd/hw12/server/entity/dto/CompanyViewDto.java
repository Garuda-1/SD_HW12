package ru.itmo.iandolzhanskii.sd.hw12.server.entity.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompanyViewDto {

    Long id;
    String name;
}
