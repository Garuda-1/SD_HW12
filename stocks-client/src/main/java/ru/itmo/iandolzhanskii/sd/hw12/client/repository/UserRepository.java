package ru.itmo.iandolzhanskii.sd.hw12.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.iandolzhanskii.sd.hw12.client.entity.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
