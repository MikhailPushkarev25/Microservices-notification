package ru.pushkarev.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pushkarev.notification.entity.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
