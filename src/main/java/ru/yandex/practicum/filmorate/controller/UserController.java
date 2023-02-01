package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    private static int idCounter = 0;
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping("/users")
    public List<User> findAll() {
        return new ArrayList<User>(users.values());
    }

    @PostMapping(value = "/users")
    public User create(@RequestBody User user) throws ValidationException {
        if (user.getName()==null){
            user.setName(user.getLogin());
        }
        if (isValid(user)){
            if (user.getId() == 0) {
                user.setId(++idCounter);
            }
            users.put(user.getId(),user);
        }else {
            throw new ValidationException();
        }
        return users.get(user.getId());
    }


    @PutMapping(value = "/users")
    public User update(@RequestBody User user) throws ValidationException {
        if (!users.containsKey(user.getId())){
            throw new ValidationException();
        }
        if (user.getName().isEmpty()){
            user.setName(user.getLogin());
        }
        if (user.getId()==0){
            user.setId(++idCounter);
        }
        if (isValid(user)){
            users.remove(user.getId());
            users.put(user.getId(),user);
        }else {
            throw new ValidationException();
        }
        return user;
    }

    public static boolean isValid(User user){
        return user.getEmail() != null &&
                user.getEmail().contains("@") &&
                user.getLogin() != null &&
                !user.getLogin().contains(" ") &&
                user.getBirthday().isBefore(LocalDate.now());
    }
//    электронная почта не может быть пустой и должна содержать символ @;
//    логин не может быть пустым и содержать пробелы;
//    имя для отображения может быть пустым — в таком случае будет использован логин;
//    дата рождения не может быть в будущем.
}
