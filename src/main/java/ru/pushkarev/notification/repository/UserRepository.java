package ru.pushkarev.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pushkarev.notification.entity.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
}
