package ru.pushkarev.notification.service.command;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.ChatsDto;
import ru.pushkarev.notification.dto.UpdateChatDescDto;
import ru.pushkarev.notification.entity.Chat;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.repository.ChatRepository;

@CommandTypeMapping(CommandType.UPDATE_CHAT_DESCRIPTION)
@Service
public class UpdateChatDescCommand implements Command<UpdateChatDescDto, ChatsDto> {

    private final ChatRepository chatRepository;

    public UpdateChatDescCommand(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    @Transactional
    public ChatsDto execute(UpdateChatDescDto dto) {
        Chat chat = chatRepository.findById(dto.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
        chat.setDescription(dto.getNewDescription());
        chat = chatRepository.save(chat);
        ChatsDto chatsDto = new ChatsDto();
        chatsDto.setId(chat.getId());
        chatsDto.setDescription(chat.getDescription());
        chatsDto.setType(chat.getType());
        return chatsDto;
    }
}
