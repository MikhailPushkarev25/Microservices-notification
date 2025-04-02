package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.CreateChatRequest;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.message.MessageService;

@CommandTypeMapping(CommandType.CREATE_CHAT)
@Service
public class CreateChatCommand implements Command<CreateChatRequest, Void> {

    private final MessageService messageService;

    public CreateChatCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Void execute(CreateChatRequest dto) {
        messageService.createChat(dto.getType(), dto.getUserIds());
        return null;
    }
}
