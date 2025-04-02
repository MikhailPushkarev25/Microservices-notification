package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.MessageDto;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.message.MessageService;

@CommandTypeMapping(CommandType.SEND_MESSAGE)
@Service
public class SendMessageCommand implements Command<MessageDto, Void> {
    private final MessageService messageService;

    public SendMessageCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Void execute(MessageDto messageDto) {
        messageService.sendMessage(messageDto);
        return null;
    }
}
