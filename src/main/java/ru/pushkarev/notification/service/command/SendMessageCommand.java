package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.MessageDto;
import ru.pushkarev.notification.entity.Message;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.message.MessageService;
import ru.pushkarev.notification.service.websocket.WebSocketEventPublisher;

@CommandTypeMapping(CommandType.SEND_MESSAGE)
@Service
public class SendMessageCommand implements Command<MessageDto, Void> {
    private final MessageService messageService;

    private final WebSocketEventPublisher publisher;

    public SendMessageCommand(MessageService messageService, WebSocketEventPublisher publisher) {
        this.messageService = messageService;
        this.publisher = publisher;
    }

    @Override
    public Void execute(MessageDto messageDto) {
        Message message = messageService.sendMessage(messageDto);
        messageDto.setSenderName(message.getSenderName());
        publisher.publish(messageDto);
        return null;
    }
}
