package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.ChatsDto;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.message.MessageService;

import java.util.List;

@CommandTypeMapping(CommandType.CHATS)
@Service
public class ChatsCommand implements Command<Void, List<ChatsDto>> {

    private final MessageService messageService;

    public ChatsCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public List<ChatsDto> execute(Void dto) {
        return messageService.getChats();
    }
}
