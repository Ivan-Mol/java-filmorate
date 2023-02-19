package ru.yandex.practicum.filmorate.storages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private static long idCounter = 0;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> findAll() {
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
}
