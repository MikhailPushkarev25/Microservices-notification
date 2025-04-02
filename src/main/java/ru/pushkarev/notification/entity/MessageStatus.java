package ru.pushkarev.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode()
@Table(name = "message_statuses")
@IdClass(MessageStatusesId.class)
@Entity
public class MessageStatus {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Id
    @Column(name = "user_id")
    private Long userId;

    private String status;

    @Column(name = "updated_at")
    private LocalDateTime updateAt;
}
