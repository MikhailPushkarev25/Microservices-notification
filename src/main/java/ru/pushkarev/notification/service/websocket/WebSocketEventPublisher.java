package ru.pushkarev.notification.service.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pushkarev.notification.dto.MessageDto;
import ru.pushkarev.notification.entity.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class WebSocketEventPublisher {
    private final List<WebSocketEventListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(WebSocketEventListener listener) {
        listeners.add(listener);
    }

    public void publish(MessageDto message) {
        for (WebSocketEventListener listener : listeners) {
            listener.onMessageReceived(message);
        }
    }
}
