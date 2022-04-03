package ru.itmo.iandolzhanskii.sd.hw12.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.iandolzhanskii.sd.hw12.server.entity.dto.CompanyViewDto;
import ru.itmo.iandolzhanskii.sd.hw12.server.entity.model.Company;
import ru.itmo.iandolzhanskii.sd.hw12.server.repository.CompanyRepository;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyViewDto createCompany(String companyName) {
        Company company = new Company();
        company.setName(companyName);
        company = companyRepository.save(company);
        return CompanyViewDto.builder()
            .id(company.getId())
            .name(company.getName())
            .build();
    }
}
