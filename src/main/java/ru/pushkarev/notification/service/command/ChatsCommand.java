package ru.pushkarev.notification.service.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.ChatsDto;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.repository.ChatRepository;

import java.util.List;
import java.util.stream.Collectors;

@CommandTypeMapping(CommandType.CHATS)
@Slf4j
@Service
public class ChatsCommand implements Command<Void, List<ChatsDto>> {

    private final ChatRepository chatRepository;

    public ChatsCommand(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatsDto> execute(Void dto) {
        log.info("jobs command ChatsCommand");
        return getChats();
    }

    public List<ChatsDto> getChats() {
        return chatRepository.findAll().stream()
                .map(chat -> new ChatsDto(chat.getId(), chat.getType()))
                .collect(Collectors.toList());
    }
}
