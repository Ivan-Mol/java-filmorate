package ru.yandex.practicum.filmorate.storages.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

import static ru.yandex.practicum.filmorate.model.EventType.FRIEND;
import static ru.yandex.practicum.filmorate.model.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.OperationType.ADD;
import static ru.yandex.practicum.filmorate.model.OperationType.REMOVE;


@Primary
@Component
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> getAll() {
        String getAllUsersQuery = "SELECT u.*, f.FRIEND_ID" +
                " FROM USERS u" +
                " LEFT JOIN FRIENDS f ON u.ID = f.USER_ID";
        return getUsers(getAllUsersQuery);
    }

    @Override
    public User get(Long id) {
        String getUserByIdQuery = "SELECT u.*, f.FRIEND_ID" +
                " FROM USERS u" +
                " LEFT JOIN FRIENDS f ON u.ID = f.USER_ID" +
                " WHERE u.ID = " + id;
        List<User> list = getUsers(getUserByIdQuery);
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            throw new NotFoundException("User with such id(" + id + ") is not found");
        }
    }

    private ArrayList<User> getUsers(String query) {
        Map<Long, User> users = new HashMap<>();
        jdbcTemplate.query(query, rs -> {
            long id = rs.getLong("id");
            if (!users.containsKey(id)) {
                User user = new User();
                user.setId(id);
                user.setEmail(rs.getString("email"));
                user.setLogin(rs.getString("login"));
                user.setName(rs.getString("name"));
                user.setBirthday(Objects.requireNonNull(rs.getDate("birthday")).toLocalDate());
                users.put(id, user);
            }
            Long friendId = rs.getObject("friend_id", Long.class);
            if (friendId != null) {
                users.get(id).addFriendID(friendId);
            }
        });
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement("INSERT INTO users ( login, email, name, birthday) VALUES ( ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKeyAs(Long.class));
        log.debug("User created {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update("UPDATE users SET email = ?, login = ?,name = ?,birthday = ? WHERE id = ?",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        log.debug("User updated {}", user);
        return user;
    }

    public void addFriend(long userId, long friendId) {
        jdbcTemplate.update("INSERT INTO friends (user_id, friend_id) values (?,? )",
                userId, friendId);
        addEvent(FRIEND, ADD, userId, friendId);
        log.debug("Friend added {}", friendId);
    }

    public void removeFriend(long userId, long friendId) {
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? AND friend_id =?", userId, friendId);
        addEvent(FRIEND, REMOVE, userId, friendId);
    }

    @Override
    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        addEvent(LIKE, ADD, userId, filmId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
        addEvent(LIKE, REMOVE, userId, filmId);
    }

    @Override
    public List<Event> getUserEvents(long userId) {
        log.debug("/getUserEvents");
        String sql = "SELECT * FROM events AS e WHERE e.user_id = " + userId + " ORDER BY e.timestamp";
        return jdbcTemplate.query(sql, rs -> {
            List<Event> events = new ArrayList<>();
            while (rs.next()) {
                Event event = new Event();
                event.setEventId(rs.getLong("id"));
                event.setTimestamp(rs.getTimestamp("timestamp"));
                event.setEventType(EventType.valueOf(rs.getString("event_type")));
                event.setOperation(OperationType.valueOf(rs.getString("operation")));
                event.setUserId(rs.getLong("user_id"));
                event.setEntityId(rs.getLong("entity_id"));
            }
            return events;
        });
    }
    @Override
    public void deleteById(Long id) {
        String sqlQuery = "DELETE FROM users where id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public void addEvent(EventType eventType, OperationType operation, long userId, long entityId) {
        String sql = "INSERT INTO events (event_type, operation, user_id, entity_id) " +
                    "VALUES (" + eventType + ", " + operation + ", " + userId + ", " + entityId + ")";
        jdbcTemplate.update(sql);
    }

    @Override //TODO возможно не нужен так как нет функционала
    public void removeUserEvents(long userId) {
        String sql = "DELETE FROM events WHERE user_id = " + userId;
        jdbcTemplate.update(sql);
    }
}