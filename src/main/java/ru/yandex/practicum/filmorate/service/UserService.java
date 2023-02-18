package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User addFriend(long userID, long friendID) {
        userStorage.get(userID).addFriendID(userStorage.get(friendID).getId());
        userStorage.get(friendID).addFriendID(userID);
        return userStorage.get(userID);
    }

    public User removeFriend(long userID, long friendID) {
        userStorage.get(userID).removeFriendID(userStorage.get(friendID).getId());
        userStorage.get(friendID).removeFriendID(userID);
        return userStorage.get(userID);
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

    public List<User> findMutualFriends(long id1, long id2) {
        Set<Long> id2friends = new HashSet<>(userStorage.get(id2).getFriends());
        return userStorage.get(id1).getFriends().stream()
                .filter(id2friends::contains)
                .map(userStorage::get)
                .collect(Collectors.toList());
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User getUser(Long id) {
        return userStorage.get(id);
    }

}
