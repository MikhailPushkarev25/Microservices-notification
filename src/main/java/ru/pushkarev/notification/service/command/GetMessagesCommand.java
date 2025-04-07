package ru.pushkarev.notification.service.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.dto.MessageTransfer;
import ru.pushkarev.notification.entity.Message;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.repository.MessageRepository;
import ru.pushkarev.notification.service.message.MessageCacheService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CommandTypeMapping(CommandType.GET_MESSAGES)
@Slf4j
@Service
public class GetMessagesCommand implements Command<Long, List<Message>> {

    private final MessageCacheService redisUserService;

    private final MessageRepository messageRepository;

    public GetMessagesCommand(MessageCacheService redisUserService, MessageRepository messageRepository) {
        this.redisUserService = redisUserService;
        this.messageRepository = messageRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Message> execute(Long dto) {
        log.info("jobs command GetMessagesCommand");
        return getChatMessages(dto);
    }

    /**
     * 4. Получить сообщения чата (с кешем в Redis)
     */
    public List<Message> getChatMessages(Long chatId) {
        List<MessageTransfer> messages = redisUserService.getCachedMessages(chatId);

        if (messages != null && !messages.isEmpty()) {
            List<Long> ids = messages.stream().map(MessageTransfer::getId).collect(Collectors.toList());
            return messageRepository.findAllById(ids);
        }

        return Collections.emptyList();
    }
}
