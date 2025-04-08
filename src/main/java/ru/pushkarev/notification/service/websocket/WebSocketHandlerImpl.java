package ru.pushkarev.notification.service.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.pushkarev.notification.dto.MessageDto;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.command.CommandExecutor;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebSocketHandlerImpl extends TextWebSocketHandler implements WebSocketEventListener {

    private final ObjectMapper objectMapper;

    @Autowired
    private WebSocketSessionManager sessionManager;

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private WebSocketEventPublisher publisher;

    @Autowired
    public WebSocketHandlerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        publisher.subscribe(this);
    }

    @Override
    public void onMessageReceived(MessageDto message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);

            // Шлём всем userId, для кого предназначено сообщение
            WebSocketSession session = sessionManager.getSession(message.getUserId());
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }

        } catch (IOException e) {
            log.error("Ошибка отправки WebSocket сообщения", e);
        }
    }


    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        try {
            MessageDto messageDto = objectMapper.readValue(message.getPayload(), MessageDto.class);

            if ("typing".equals(messageDto.getType())) {
                publisher.publish(messageDto);
            } else if ("message".equals(messageDto.getType())) {
                commandExecutor.executeCommand(CommandType.SEND_MESSAGE, messageDto);
            }

        } catch (Exception e) {
            log.error("Ошибка обработки WebSocket сообщения", e);
            sendError(session);
        }
    }

    private void sendError(WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of("error", "Ошибка обработки сообщения"))));
        } catch (IOException e) {
            log.error("Ошибка отправки сообщения об ошибке", e);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            // Сохраняем сессию по userId
            sessionManager.registerSession(Long.parseLong(userId), session);
            log.info("WebSocket подключен: userId={}, session={}", userId, session.getId());
        } else {
            log.warn("WebSocket подключен без userId, session={}", session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            // Удаляем сессию по userId
            sessionManager.removeSession(Long.parseLong(userId));
            log.info("WebSocket отключен: userId={}, session={}", userId, session.getId());
        }
    }

    private String getUserIdFromSession(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri.getQuery() != null) {
                String[] params = uri.getQuery().split("&");
                for (String param : params) {
                    if (param.startsWith("userId=")) {
                        return param.split("=")[1];
                    }
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при извлечении userId из URI", e);
        }
        return null;
    }
}
