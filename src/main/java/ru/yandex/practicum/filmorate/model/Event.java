package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Event {
    private Long eventId;
    private Long timestamp;
    private EventType eventType;
    private OperationType operation;
    private Long userId;
    private Long entityId;
}