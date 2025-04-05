package ru.pushkarev.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pushkarev.notification.dto.ChatsDto;
import ru.pushkarev.notification.dto.CreateChatRequest;
import ru.pushkarev.notification.dto.UserDto;
import ru.pushkarev.notification.entity.Message;
import ru.pushkarev.notification.enums.CommandType;
import ru.pushkarev.notification.service.command.CommandExecutor;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final CommandExecutor executor;

    public ChatController(CommandExecutor executor) {
        this.executor = executor;
    }

    @GetMapping()
    public ResponseEntity<List<ChatsDto>> chats() {
        return ResponseEntity.ok(executor.executeCommand(CommandType.CHATS, null));
    }

    @PostMapping("/create")
    public ResponseEntity<String> createChat(@RequestBody CreateChatRequest request) {
        executor.executeCommand(CommandType.CREATE_CHAT, request);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/{chatId}/add-user/{userId}")
    public ResponseEntity<String> addUser(@PathVariable Long chatId, @PathVariable Long userId) {
        executor.executeCommand(CommandType.ADD_USER_TO_CHAT, new UserDto(chatId, userId));
        return ResponseEntity.ok("User added");
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long chatId) {
        return ResponseEntity.ok(executor.executeCommand(CommandType.GET_MESSAGES, chatId));
    }

    @PostMapping("/{chatId}/read/{userId}")
    public ResponseEntity<String> markAsRead(@PathVariable Long chatId, @PathVariable Long userId) {
        executor.executeCommand(CommandType.MARK_AS_READ, new UserDto(chatId, userId));
        return ResponseEntity.ok("Messages marked as read");
    }
}

