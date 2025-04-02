package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.UserDto;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.message.MessageService;

@CommandTypeMapping(CommandType.ADD_USER_TO_CHAT)
@Service
public class AddUserToChatCommand implements Command<UserDto, Void> {

    private final MessageService messageService;

    public AddUserToChatCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Void execute(UserDto dto) {
        messageService.addUserToChat(dto.getChatId(), dto.getUserId());
        return null;
    }
}
