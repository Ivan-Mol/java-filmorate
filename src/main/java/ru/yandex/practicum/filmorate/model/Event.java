package ru.yandex.practicum.filmorate.model;

import lombok.*;

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