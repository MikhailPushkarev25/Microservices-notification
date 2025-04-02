package ru.pushkarev.notification.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.pushkarev.notification.dto.MessageTransfer;
import ru.pushkarev.notification.entity.Message;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageCacheService {
    private static final int MAX_CACHED_MESSAGES = 100;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MessageCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheMessage(Long chatId, Message message) {
        try {
            String key = "chat:" + chatId + ":messages";
            String messageJson = objectMapper.writeValueAsString(message);

            redisTemplate.opsForList().leftPush(key, messageJson);
            redisTemplate.opsForList().trim(key, 0, MAX_CACHED_MESSAGES - 1);
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Ошибка кеширования сообщения", e);
        }
    }

    public List<MessageTransfer> getCachedMessages(Long chatId) {
        try {
            String key = "chat:" + chatId + ":messages";
            List<String> messagesJson = redisTemplate.opsForList().range(key, 0, -1);

            if (messagesJson.isEmpty()) {
                return Collections.emptyList();
            }

            return messagesJson.stream()
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, MessageTransfer.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Ошибка десериализации", e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка при получении сообщений из Redis", e);
            return Collections.emptyList();
        }
    }

    public void clearChatCache(Long chatId) {
        String key = "chat:" + chatId + ":messages";
        redisTemplate.delete(key);
    }
}