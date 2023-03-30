package ru.yandex.practicum.filmorate.storages.mem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.util.*;

@Component
@Slf4j

public class InMemoryUserStorage implements UserStorage {
    private static long idCounter = 0;
    private final Map<Long, User> users = new HashMap<>();

    private final Map<Long, Long> likes = new TreeMap<>();


    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        Long newId = ++idCounter;
        user.setId(newId);
        users.put(newId, user);
        log.debug("User created {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        if (!users.containsKey(id)) {
            log.error("User with such id(" + id + ") is not found");
            throw new NotFoundException("User with such id(" + id + ") is not found");
        }
        users.put(id, user);
        log.debug("User updated {}", user);
        return user;
    }

    @Override
    public User get(Long id) {
        if (!users.containsKey(id)) {
            log.error("User with such id(" + id + ") is not found");
            throw new NotFoundException("User with such id(" + id + ") is not found");
        }
        return users.get(id);
    }

    @Override
    public void addFriend(long userID, long friendID) {
        get(userID).addFriendID(friendID);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        get(userId).removeFriendID(friendId);
    }

    @Override
    public void addLike(long filmId, long userId) {
        likes.put(filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        likes.remove(filmId, userId);
    }

    @Override
    public List<Event> getUserEvents(long userId) {
        return Collections.emptyList();
    }

    @Override
    public void deleteById(Long id) {
    }

    @Override
    public void addEvent(EventType eventType, OperationType operation, long userId, long entityId) {};

    @Override
    public void removeUserEvents(long userId) {};
}