package ru.yandex.practicum.filmorate.storages.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Primary
@Component
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public List<User> findAll() {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users");
        List<User> users = new ArrayList<>();
        while (userRows.next()) {
            User user = new User();
            user.setId(userRows.getLong("id"));
            user.setEmail(userRows.getString("email"));
            user.setLogin(userRows.getString("login"));
            user.setName(userRows.getString("name"));
            user.setBirthday(Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate());
            users.add(user);
        }
        return users;
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

    @Override
    public User get(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where id = ?", id);

        if (userRows.next()) {
            User user = new User();
            user.setId(userRows.getLong("id"));
            user.setEmail(userRows.getString("email"));
            user.setLogin(userRows.getString("login"));
            user.setName(userRows.getString("name"));
            user.setBirthday(Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate());
            user.setFriendsList(getFriendsIds(id));
            return user;
        } else {
            log.error("User with such id(" + id + ") is not found");
            throw new NotFoundException("User with such id(" + id + ") is not found");
        }
    }

    public void addFriend(long userId, long friendId) {
        jdbcTemplate.update("INSERT INTO friends (user_id, friend_id) values (?,? )",
                userId, friendId);
        log.debug("Friend added {}", friendId);
    }

    private List<Long> getFriendsIds(long userId) {
        return jdbcTemplate.queryForList("select friend_id from friends where user_id =?", Long.class, userId);
    }

    public void removeFriend(long userId, long friendId) {
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? AND friend_id =?", userId, friendId);

    }

    @Override
    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

}