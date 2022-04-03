package ru.itmo.iandolzhanskii.sd.hw12.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.iandolzhanskii.sd.hw12.server.entity.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
