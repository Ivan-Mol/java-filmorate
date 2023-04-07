package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor
public class Event {
    private Long eventId;
    private Long timestamp;
    @NonNull
    private EventType eventType;
    @NonNull
    private OperationType operation;
    @NonNull
    private Long userId;
    @NonNull
    private Long entityId;
}