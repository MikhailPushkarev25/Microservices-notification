package ru.pushkarev.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pushkarev.notification.service.websocket.WebSocketSessionManager;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class DebugController {

    private final WebSocketSessionManager sessionManager;

    @GetMapping("/debug/websockets")
    public Set<Long> getActiveUsers() {
        return sessionManager.getAllSessions().keySet();
    }
}
