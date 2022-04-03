package ru.itmo.iandolzhanskii.sd.hw12.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.iandolzhanskii.sd.hw12.server.entity.dto.CompanyViewDto;
import ru.itmo.iandolzhanskii.sd.hw12.server.service.CompanyService;

@RestController
@RequestMapping(path = "company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public CompanyViewDto createCompany(@RequestParam String companyName) {
        return companyService.createCompany(companyName);
    }
}
