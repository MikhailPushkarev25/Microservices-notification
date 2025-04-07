package ru.pushkarev.notification.service.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.CreateChatRequest;
import ru.pushkarev.notification.entity.Chat;
import ru.pushkarev.notification.entity.ChatParticipant;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.repository.ChatParticipantRepository;
import ru.pushkarev.notification.repository.ChatRepository;

import java.time.LocalDateTime;
import java.util.List;

@CommandTypeMapping(CommandType.CREATE_CHAT)
@Slf4j
@Service
public class CreateChatCommand implements Command<CreateChatRequest, Void> {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRepository chatRepository;

    public CreateChatCommand(ChatParticipantRepository chatParticipantRepository, ChatRepository chatRepository) {
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatRepository = chatRepository;
    }

    @Override
    @Transactional
    public Void execute(CreateChatRequest dto) {
        log.info("jobs command CreateChatCommand");
        createChat(dto.getType(), dto.getUserIds());
        return null;
    }

    public void createChat(String type, List<Long> userIds) {
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

    }
}
