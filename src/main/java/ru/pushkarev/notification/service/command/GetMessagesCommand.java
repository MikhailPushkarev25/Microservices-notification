package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.entity.Message;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.message.MessageService;

import java.util.List;

@CommandTypeMapping(CommandType.GET_MESSAGES)
@Service
public class GetMessagesCommand implements Command<Long, List<Message>> {

    private final MessageService messageService;

    public GetMessagesCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public List<Message> execute(Long dto) {
        return messageService.getChatMessages(dto);
    }
}
