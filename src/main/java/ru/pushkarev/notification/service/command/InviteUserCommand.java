package ru.pushkarev.notification.service.command;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.UserDto;
import ru.pushkarev.notification.entity.Chat;
import ru.pushkarev.notification.entity.ChatParticipant;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.repository.ChatRepository;

@CommandTypeMapping(CommandType.INVITE_USER)
@Service
public class InviteUserCommand implements Command<UserDto, Void> {

    private final ChatRepository chatRepository;

    public InviteUserCommand(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public Void execute(UserDto dto) {
        Chat chat = chatRepository.findById(dto.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
        // Пример добавления участника (предполагается, что есть класс ChatParticipant)
        ChatParticipant participant = new ChatParticipant();
        participant.setChat(chat);
        participant.setUserId(dto.getUserId());
        chat.getParticipants().add(participant);
        chatRepository.save(chat);
        return null;
    }
}
