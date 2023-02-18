package ru.yandex.practicum.filmorate.storages;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectIsNotFound;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
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

    private static void validate(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Login contains whitespace");
            throw new ValidationException("Login contains whitespace");
        }
        if (Strings.isBlank(user.getName())) {
            user.setName(user.getLogin());
            log.debug("Name is null or empty. Login is used as name");
        }
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        validate(user);
        Long newId = ++idCounter;
        user.setId(newId);
        users.put(newId, user);
        log.debug("User created {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        validate(user);
        if (!users.containsKey(id)) {
            log.error("User with such id(" + id + ") is not found");
            throw new ObjectIsNotFound("User with such id(" + id + ") is not found");
        }
        users.put(id, user);
        log.debug("User updated {}", user);
        return user;
    }

    @Override
    public User get(Long id) {
        if (!users.containsKey(id)) {
            log.error("User with such id(" + id + ") is not found");
            throw new ObjectIsNotFound("User with such id(" + id + ") is not found");
        }
        return users.get(id);
    }
}
