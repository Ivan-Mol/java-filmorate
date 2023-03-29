package ru.yandex.practicum.filmorate.storages.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Map.Entry.comparingByValue;

@Primary
@Component
@Slf4j
public class UserDbStorage implements UserStorage {
    public static final double PERCENTAGE_SELECT_SIMILAR_USERS = 0.3;
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


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
        log.debug("Friend added {}", friendId);
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


    public Map<Long, List<Long>> getSimilarUser(long userId) {
        String sql = "SELECT l2.user_id AS other_user_id, l2.film_id AS mutual_film " +
                    "FROM likes AS l " +
                    "JOIN likes AS l2 " +
                        "ON l2.film_id = l.film_id " +
                        "AND l2.user_id = l.user_id " +
                    "WHERE l.user_id = ? " +
                    "GROUP BY l2.user_id, l2.film_id";
        Map<Long, List<Long>> similarUsersWithFilms = jdbcTemplate.query(sql, rs -> {
            Map<Long, List<Long>> similarUserWithFilms = new HashMap<>();
            while(rs.next()) {
                long otherUserID = rs.getLong("other_user_id");
                List<Long> otherUserFilms = similarUserWithFilms.getOrDefault(otherUserID, new ArrayList<>());
                otherUserFilms.add(rs.getLong("mutual_film"));
            }
            return similarUserWithFilms;
        });
        similarUsersWithFilms.entrySet().stream().sorted(comparingByValue(comparing(List::size)));
        long numTotalSimilarUsers = Math.round(similarUsersWithFilms.size() * PERCENTAGE_SELECT_SIMILAR_USERS);
        return similarUsersWithFilms.entrySet().stream()
                .limit(numTotalSimilarUsers).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public List<Film> getFilmsRecommendations(long userId) {

        return getSimilarUser(userId);
    }


}
