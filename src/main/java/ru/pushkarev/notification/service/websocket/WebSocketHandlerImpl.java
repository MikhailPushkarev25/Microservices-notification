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
import ru.pushkarev.notification.entity.Message;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.command.CommandExecutor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebSocketHandlerImpl extends TextWebSocketHandler implements WebSocketEventListener {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

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
    public void onMessageReceived(Message message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            for (WebSocketSession s : activeSessions.values()) {
                s.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (IOException e) {
            log.error("Ошибка отправки WebSocket сообщения", e);
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        try {
            MessageDto messageDto = objectMapper.readValue(message.getPayload(), MessageDto.class);

            commandExecutor.executeCommand(CommandType.SEND_MESSAGE, messageDto);
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
        activeSessions.put(session.getId(), session);
        log.info("WebSocket подключен: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        activeSessions.remove(session.getId());
        log.info("WebSocket отключен: {}", session.getId());
    }
}


