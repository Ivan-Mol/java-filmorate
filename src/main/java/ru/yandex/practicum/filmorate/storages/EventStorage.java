package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {
    void addEvent(Event event);

    List<Event> getUserEvents(long userId);
}