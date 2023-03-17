package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAll();

    User create(User user);

    User update(User user);

    User get(Long id);

    void addFriend(long userID, long friendID);

    void removeFriend(long userId, long friendId);
}
