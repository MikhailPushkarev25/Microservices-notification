package ru.pushkarev.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class Users extends BaseEntity<Long> {

    private String username;
    private String email;
    @Column(name = "create_at")
    private LocalDateTime createAt;
}
