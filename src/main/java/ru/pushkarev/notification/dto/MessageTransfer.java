package ru.pushkarev.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageTransfer {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String content;
    private LocalDateTime sentAt;
}
