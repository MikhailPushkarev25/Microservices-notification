package ru.pushkarev.notification.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class MessageStatusesId implements Serializable {

    private Long message;
    private Long userId;
}
