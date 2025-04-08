package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.repository.ChatRepository;

@CommandTypeMapping(CommandType.DELETE_CHAT)
@Service
public class DeleteChatCommand implements Command<Long, Void> {

    private final ChatRepository chatRepository;

    public DeleteChatCommand(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    @Transactional
    public Void execute(Long dto) {
        chatRepository.deleteById(dto);
        return null;
    }
}
