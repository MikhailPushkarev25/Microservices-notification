package ru.pushkarev.notification.service.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.UserDto;
import ru.pushkarev.notification.entity.Chat;
import ru.pushkarev.notification.entity.ChatParticipant;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.repository.ChatParticipantRepository;
import ru.pushkarev.notification.repository.ChatRepository;

import java.time.LocalDateTime;

@CommandTypeMapping(CommandType.ADD_USER_TO_CHAT)
@Slf4j
@Service
public class AddUserToChatCommand implements Command<UserDto, Void> {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRepository chatRepository;

    public AddUserToChatCommand(ChatParticipantRepository chatParticipantRepository, ChatRepository chatRepository) {
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatRepository = chatRepository;
    }

    @Override
    @Transactional
    public Void execute(UserDto dto) {
        log.info("jobs command addUserToChatCommand");
        addUserToChat(dto.getChatId(), dto.getUserId());
        return null;
    }

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
