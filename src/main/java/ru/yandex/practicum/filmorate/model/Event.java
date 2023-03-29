package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Event {
    private Long eventId;
    private Timestamp timestamp;
    private EventType eventType;
    private OperationType operation;
    private Long userId;
    private Long entityId;
}