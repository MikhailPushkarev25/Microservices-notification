package ru.pushkarev.notification.service.websocket;

import ru.pushkarev.notification.dto.MessageDto;
import ru.pushkarev.notification.entity.Message;

public interface WebSocketEventListener {
    void onMessageReceived(MessageDto message);
}
