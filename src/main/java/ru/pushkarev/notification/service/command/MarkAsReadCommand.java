package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.UserDto;
import ru.pushkarev.notification.entity.Message;
import ru.pushkarev.notification.entity.MessageStatus;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.repository.MessageRepository;
import ru.pushkarev.notification.repository.MessageStatusRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CommandTypeMapping(CommandType.MARK_AS_READ)
@Service
public class MarkAsReadCommand implements Command<UserDto, Void> {

    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;

    public MarkAsReadCommand(MessageRepository messageRepository, MessageStatusRepository messageStatusRepository) {
        this.messageRepository = messageRepository;
        this.messageStatusRepository = messageStatusRepository;
    }

    @Override
    @Transactional
    public Void execute(UserDto dto) {
        markAsRead(dto);
        return null;
    }

    /**
     * 5. Отметить сообщение как "прочитанное"
     */
    public void markAsRead(UserDto userDto) {
        List<Message> messages = messageRepository.findByChatId(userDto.getChatId());

        for (Message message : messages) {
            Optional<MessageStatus> status = messageStatusRepository.findByMessageIdAndUserId(message.getId(), userDto.getUserId());
            if (status.isPresent()) {
                MessageStatus messageStatus = status.get();
                messageStatus.setStatus("read");
                messageStatus.setUpdateAt(LocalDateTime.now());
                messageStatusRepository.save(messageStatus);
            } else {
                MessageStatus newStatus = new MessageStatus();
                newStatus.setMessage(message);
                newStatus.setUserId(userDto.getUserId());
                newStatus.setStatus("read");
                newStatus.setUpdateAt(LocalDateTime.now());
                messageStatusRepository.save(newStatus);
            }
        }
    }
}
