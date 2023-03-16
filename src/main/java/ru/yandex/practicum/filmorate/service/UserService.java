package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

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

    public void addFriend(long userID, long friendID) {
        checkUserExists(userID);
        checkUserExists(friendID);
        userStorage.addFriend(userID,friendID);
    }

    public void removeFriend(long userID, long friendID) {
        checkUserExists(friendID);
        userStorage.get(userID).removeFriendID(friendID);
        userStorage.get(friendID).removeFriendID(userID);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public List<User> findAllFriends(long id) {
        return userStorage.get(id)
                .getFriends().stream()
                .map(userStorage::get)
                .collect(Collectors.toList());
    }

    public List<User> findMutualFriends(long id, long otherId) {
        Set<Long> id2friends = new HashSet<>(userStorage.get(otherId).getFriends());
        return userStorage.get(id).getFriends().stream()
                .filter(id2friends::contains)
                .map(userStorage::get)
                .collect(Collectors.toList());
    }

    public User create(User user) {
        validate(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validate(user);
        if (userStorage.get(user.getId()) != null) {
            return userStorage.update(user);
        }
        return user;
    }

    public User getUser(Long id) {
        return userStorage.get(id);
    }

    //throws RuntimeException if User doesn't exist
    private void checkUserExists(long userId) {
        userStorage.get(userId);
    }


}
