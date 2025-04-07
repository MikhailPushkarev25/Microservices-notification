package ru.pushkarev.notification.service.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.MessageDto;
import ru.pushkarev.notification.entity.*;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.repository.*;
import ru.pushkarev.notification.service.message.MessageCacheService;
import ru.pushkarev.notification.service.websocket.WebSocketEventPublisher;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@CommandTypeMapping(CommandType.SEND_MESSAGE)
@Slf4j
@Service
public class SendMessageCommand implements Command<MessageDto, Void> {

    private final WebSocketEventPublisher publisher;
    private final MessageRepository messageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final ChatRepository chatRepository;
    private final MessageCacheService redisUserService;
    private final UserRepository userRepository;

    public SendMessageCommand(WebSocketEventPublisher publisher,
                              MessageRepository messageRepository,
                              ChatParticipantRepository chatParticipantRepository,
                              MessageStatusRepository messageStatusRepository,
                              ChatRepository chatRepository,
                              MessageCacheService redisUserService,
                              UserRepository userRepository) {
        this.publisher = publisher;
        this.messageRepository = messageRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.messageStatusRepository = messageStatusRepository;
        this.chatRepository = chatRepository;
        this.redisUserService = redisUserService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Void execute(MessageDto messageDto) {
        log.info("jobs command SendMessageCommand");
        Message message = sendMessage(messageDto);
        messageDto.setSenderName(message.getSenderName());
        publisher.publish(messageDto);
        return null;
    }

    /**
     * 3. Отправить сообщение
     */
    public Message sendMessage(MessageDto messageDto) {
        Instant instant = Instant.parse(messageDto.getTimestamp());
        LocalDateTime sentAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        Chat chat = chatRepository.findById(messageDto.getChatId())
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        Users sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(messageDto.getSenderId());
        message.setContent(messageDto.getContent());
        message.setSentAt(sentAt);
        message.setSenderName(sender.getUsername());

        message = messageRepository.save(message);

        redisUserService.cacheMessage(messageDto.getChatId(), message);

        // Обновить статусы сообщений (изначально "sent")
        List<ChatParticipant> participants = chatParticipantRepository.findByChatId(messageDto.getChatId());
        for (ChatParticipant participant : participants) {
            if (!participant.getUserId().equals(messageDto.getSenderId())) {
                MessageStatus status = new MessageStatus();
                status.setMessage(message);
                status.setUserId(participant.getUserId());
                status.setStatus("sent");
                status.setUpdateAt(LocalDateTime.now());
                messageStatusRepository.save(status);
            }
        }

        return message;
    }
}
