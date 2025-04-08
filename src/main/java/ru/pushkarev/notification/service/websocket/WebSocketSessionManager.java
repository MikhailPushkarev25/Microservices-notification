package ru.pushkarev.notification.service.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionManager {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(Long userId, WebSocketSession session) {
        sessions.put(userId, session);
        log.info("🟢 Подключён WebSocket: userId={}, sessionId={}", userId, session.getId());
    }

    public void removeSession(Long userId) {
        WebSocketSession removed = sessions.remove(userId);
        if (removed != null) {
            log.info("🔴 Отключён WebSocket: userId={}, sessionId={}", userId, removed.getId());
        }
    }

    public WebSocketSession getSession(Long userId) {
        return sessions.get(userId);
    }

    public Map<Long, WebSocketSession> getAllSessions() {
        return sessions;
    }

    public boolean isUserConnected(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }
}

