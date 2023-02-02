package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class UserController {
    private static int idCounter = 0;
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping("/users")
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping(value = "/users")
    public User create(@RequestBody @Valid User user) {
        if (user.getLogin().contains(" ")){
            log.error("Login contains whitespace");
            throw new ValidationException("Login contains whitespace");
        }
        if (Strings.isBlank(user.getName())) {
            user.setName(user.getLogin());
            log.debug("Name is null or empty. Login is used as name");
        }
        int newId = ++idCounter;
        user.setId(newId);
        users.put(newId, user);
        log.debug("User created");
        return user;
    }


    @PutMapping(value = "/users")
    public User update(@RequestBody @Valid User user){
        int id = user.getId();
        if (user.getLogin().contains(" ")){
            log.error("Login contains whitespace");
            throw new ValidationException("Login contains whitespace");
        }
        if (!users.containsKey(id)) {
            log.error("User with such id is not found");
            throw new ValidationException("User with such id is not found");
        }
        if (Strings.isBlank(user.getName())) {
            user.setName(user.getLogin());
            log.debug("Name is null or empty. Login is used as name");
        }
        users.put(id, user);
        log.debug("User updated");
        return user;
    }

//    электронная почта не может быть пустой и должна содержать символ @;
//    логин не может быть пустым и содержать пробелы;
//    имя для отображения может быть пустым — в таком случае будет использован логин;
//    дата рождения не может быть в будущем.
}
