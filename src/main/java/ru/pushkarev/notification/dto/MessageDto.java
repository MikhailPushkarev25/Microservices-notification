package ru.pushkarev.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String type;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String content;
    private String timestamp;
    private Long userId;
}
