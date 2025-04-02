package ru.pushkarev.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkarev.notification.entity.MessageStatus;

import java.util.Optional;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {

    Optional<MessageStatus>  findByMessageIdAndUserId(Long messageId, Long userId);
}
