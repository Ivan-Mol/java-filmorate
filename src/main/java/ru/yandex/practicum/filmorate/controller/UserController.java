package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) {
        log.debug("received GET /users/{}", id);
        return userService.getUser(id);
    }

    @GetMapping
    public List<User> findAll() {
        log.debug("received GET /users");
        return userService.findAll();
    }

    @PostMapping
    public User create(@RequestBody @Valid User user) {
        log.debug("received POST /users with body {}", user);
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody @Valid User user) {
        log.debug("received PUT /users with body {}", user);
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.debug("received PUT /users/{}/friends/{} ", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        log.debug("received DELETE /users/{}/friends/{} ", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> findAllFriends(@PathVariable long id) {
        log.debug("received GET /users/{}/friends", id);
        return userService.findAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> findMutualFriends(@PathVariable long id, @PathVariable long otherId) {
        log.debug("received GET /users/{}/friends/common/{}", id, otherId);
        return userService.findMutualFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getUserEvents(@PathVariable("id") long userId) {
        log.debug("received GET /users/{id}/feed");
        return userService.getUserEvents(userId);
    }
}
