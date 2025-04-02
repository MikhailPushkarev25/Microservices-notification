package ru.pushkarev.notification.service.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pushkarev.notification.dto.MessageDto;
import ru.pushkarev.notification.dto.MessageTransfer;
import ru.pushkarev.notification.dto.UserDto;
import ru.pushkarev.notification.entity.*;
import ru.pushkarev.notification.repository.ChatParticipantRepository;
import ru.pushkarev.notification.repository.ChatRepository;
import ru.pushkarev.notification.repository.MessageRepository;
import ru.pushkarev.notification.repository.MessageStatusRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final ChatRepository chatRepository;
    private final MessageCacheService redisUserService;

    public MessageService(MessageRepository messageRepository,
                          ChatParticipantRepository chatParticipantRepository,
                          MessageStatusRepository messageStatusRepository,
                          ChatRepository chatRepository,
                          MessageCacheService redisUserService) {
        this.messageRepository = messageRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.messageStatusRepository = messageStatusRepository;
        this.chatRepository = chatRepository;
        this.redisUserService = redisUserService;
    }

    /**
     * 3. Отправить сообщение
     */
    @Transactional
    public Message sendMessage(MessageDto messageDto) {
        Chat chat = chatRepository.findById(messageDto.getChatId())
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(messageDto.getSenderId());
        message.setContent(messageDto.getContent());
        message.setSentAt(LocalDateTime.now());

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

    /**
     * 4. Получить сообщения чата (с кешем в Redis)
     */
    public List<Message> getChatMessages(Long chatId) {
        List<MessageTransfer> messages = redisUserService.getCachedMessages(chatId);

        if (messages != null && !messages.isEmpty()) {
            List<Long> ids = messages.stream().map(MessageTransfer::getId).collect(Collectors.toList());
            return messageRepository.findAllById(ids);
        }

        return Collections.emptyList();
    }

    /**
     * 5. Отметить сообщение как "прочитанное"
     */
    @Transactional
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

    @Transactional
    public Chat createChat(String type, List<Long> userIds) {
        Chat chat = new Chat();
        chat.setType(type);
        chat.setCreatedAt(LocalDateTime.now());
        chat = chatRepository.save(chat);

        for (Long userId : userIds) {
            ChatParticipant participant = new ChatParticipant();
            participant.setChat(chat);
            participant.setUserId(userId);
            participant.setJoinedAt(LocalDateTime.now());
            chatParticipantRepository.save(participant);
        }

        return chat;
    }

    /**
     * 2. Добавить участника в чат
     */
    @Transactional
    public void addUserToChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        ChatParticipant participant = new ChatParticipant();
        participant.setChat(chat);
        participant.setUserId(userId);
        participant.setJoinedAt(LocalDateTime.now());

        chatParticipantRepository.save(participant);
    }

}
