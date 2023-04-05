package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getAll();

    User create(User user);

    User update(User user);

    User get(Long id);

    void addFriend(long userID, long friendID);

    void removeFriend(long userId, long friendId);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    List<Event> getUserEvents(long userId);

    void addEvent(EventType eventType, OperationType operation, long userId, long entityId);

    void deleteById(Long id);
}