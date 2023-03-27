package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.db.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    void create() {
        User expectedUser = createTestUser1();
        userStorage.create(expectedUser);
        User actualUser = userStorage.get(expectedUser.getId());
        assertEquals(expectedUser.getId(), actualUser.getId());
        assertEquals(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getLogin(), actualUser.getLogin());
        assertEquals(expectedUser.getBirthday(), actualUser.getBirthday());
    }

    @Test
    void get() {
        User expUser = createTestUser1();
        userStorage.create(expUser);
        User actUser = userStorage.get(expUser.getId());
        assertEquals(expUser.getId(), actUser.getId());
        assertEquals(expUser.getName(), actUser.getName());
    }

    @Test
    void getAll() {
        User expUser1 = createTestUser1();
        userStorage.create(expUser1);

        User expUser2 = createTestUser2();
        userStorage.create(expUser2);

        List<User> espectedList = List.of(expUser1, expUser2);
        List<User> actualList = userStorage.getAll();

        assertEquals(espectedList, actualList);
        assertEquals(2, actualList.size());
    }

    @Test
    void update() {
        User expectedUser = createTestUser1();
        userStorage.create(expectedUser);

        expectedUser.setName("UserStrange");
        userStorage.update(expectedUser);

        User actualUser = userStorage.get(expectedUser.getId());
        assertEquals(expectedUser.getId(), actualUser.getId());
        assertEquals(expectedUser.getName(), actualUser.getName());
    }

    @Test
    void addFriend() {
        User expectedUser1 = createTestUser1();
        userStorage.create(expectedUser1);
        User expectedUser2 = createTestUser2();
        userStorage.create(expectedUser2);

        List<Long> actualFriends1 = new ArrayList<>(expectedUser1.getFriends());
        assertTrue(actualFriends1.isEmpty());
        expectedUser1.addFriendID(expectedUser2.getId());
        actualFriends1 = new ArrayList<>(expectedUser1.getFriends());
        assertEquals(expectedUser2.getId(), actualFriends1.get(0));

        List<Long> actualFriends2 = new ArrayList<>(expectedUser2.getFriends());
        assertTrue(actualFriends2.isEmpty());
        expectedUser2.addFriendID(expectedUser1.getId());
        actualFriends2 = new ArrayList<>(expectedUser2.getFriends());
        assertEquals(expectedUser1.getId(), actualFriends2.get(0));
    }

    @Test
    void removeFriend() {
        User expUser1 = createTestUser1();
        userStorage.create(expUser1);
        User expUser2 = createTestUser2();
        userStorage.create(expUser2);

        expUser1.addFriendID(expUser2.getId());
        expUser2.addFriendID(expUser1.getId());
        assertFalse(expUser1.getFriends().isEmpty());
        assertFalse(expUser2.getFriends().isEmpty());

        expUser1.removeFriendID(expUser2.getId());
        assertTrue(expUser1.getFriends().isEmpty());
        assertFalse(expUser2.getFriends().isEmpty());

        expUser2.removeFriendID(expUser1.getId());
        assertTrue(expUser1.getFriends().isEmpty());
        assertTrue(expUser2.getFriends().isEmpty());
    }

    private User createTestUser1() {
        User user = new User();
        user.setEmail("user1@gmail.com");
        user.setLogin("user1");
        user.setName("user1");
        user.setBirthday(LocalDate.of(1990, 11, 12));
        return user;
    }

    private User createTestUser2() {
        User user = new User();
        user.setEmail("user2@gmail.com");
        user.setLogin("user2");
        user.setName("user2");
        user.setBirthday(LocalDate.of(1987, 2, 7));
        return user;
    }
}