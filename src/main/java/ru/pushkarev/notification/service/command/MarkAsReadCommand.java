package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.UserDto;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.message.MessageService;

@CommandTypeMapping(CommandType.MARK_AS_READ)
@Service
public class MarkAsReadCommand implements Command<UserDto, Void> {

    private final MessageService messageService;

    public MarkAsReadCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Void execute(UserDto dto) {
        messageService.markAsRead(dto);
        return null;
    }
}
