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
        log.info("Executing SendMessageCommand");

        // Получаем Chat и User
        Chat chat = getChat(messageDto.getChatId());
        Users user = getUser(messageDto.getSenderId());

        // Проверяем и добавляем участника, если нужно
        addParticipantIfNeeded(chat, user, messageDto.getSenderId());

        // Отправляем сообщение
        Message message = sendMessage(messageDto);
        messageDto.setSenderName(message.getSenderName());

        // Рассылаем сообщение всем участникам
        sendMessageToParticipants(messageDto);

        return null;
    }

    private Chat getChat(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
    }

    private Users getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
    }

    private void addParticipantIfNeeded(Chat chat, Users user, Long senderId) {
        boolean exists = chatParticipantRepository.existsByChatIdAndUserId(chat.getId(), senderId);
        if (!exists) {
            ChatParticipant participant = new ChatParticipant();
            participant.setChat(chat);  // Используем объект Chat
            participant.setUserId(senderId);  // Используем userId
            participant.setJoinedAt(LocalDateTime.now()); // Определяем момент добавления
            chatParticipantRepository.save(participant);

            log.info("Automatically added participant to chat: userId={}, chatId={}", senderId, chat.getId());
        }
    }

    private Message sendMessage(MessageDto messageDto) {
        LocalDateTime sentAt = parseTimestamp(messageDto.getTimestamp());

        // Получаем Chat и User
        Chat chat = getChat(messageDto.getChatId());
        Users sender = getUser(messageDto.getSenderId());

        // Создаём и сохраняем сообщение
        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(messageDto.getSenderId());
        message.setContent(messageDto.getContent());
        message.setSentAt(sentAt);
        message.setSenderName(sender.getUsername());

        message = messageRepository.save(message);

        // Кэшируем сообщение
        redisUserService.cacheMessage(messageDto.getChatId(), message);

        // Обновляем статусы сообщений
        updateMessageStatus(message, messageDto.getSenderId());

        return message;
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        Instant instant = Instant.parse(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private void updateMessageStatus(Message message, Long senderId) {
        List<ChatParticipant> participants = chatParticipantRepository.findByChatId(message.getChat().getId());
        for (ChatParticipant participant : participants) {
            if (!participant.getUserId().equals(senderId)) {
                MessageStatus status = new MessageStatus();
                status.setMessage(message);
                status.setUserId(participant.getUserId());
                status.setStatus("sent");
                status.setUpdateAt(LocalDateTime.now());
                messageStatusRepository.save(status);
            }
        }
    }

    private void sendMessageToParticipants(MessageDto messageDto) {
        List<ChatParticipant> participants = chatParticipantRepository.findByChatId(messageDto.getChatId());
        for (ChatParticipant participant : participants) {
            MessageDto copy = createMessageDtoForParticipant(participant, messageDto);
            publisher.publish(copy);
        }
    }

    private MessageDto createMessageDtoForParticipant(ChatParticipant participant, MessageDto messageDto) {
        return MessageDto.builder()
                .userId(participant.getUserId())
                .chatId(messageDto.getChatId())
                .content(messageDto.getContent())
                .senderId(messageDto.getSenderId())
                .senderName(messageDto.getSenderName())
                .timestamp(messageDto.getTimestamp())
                .type("message")
                .build();
    }
}

