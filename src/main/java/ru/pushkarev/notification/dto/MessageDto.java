package ru.pushkarev.notification.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private String type;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String content;
    private String timestamp;
    private Long userId;
}
