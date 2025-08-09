package ru.yandex.practicum.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "actions")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Column(name = "event_id",nullable = false)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type",nullable = false)
    ActionType actionType;


    @Column(nullable = false)
    Instant timestamp;
}
