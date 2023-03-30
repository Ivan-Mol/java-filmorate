package ru.yandex.practicum.filmorate.storages.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storages.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Primary
@Component
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getAll() {
        String getAllFilmsQuery = "SELECT f.*, m.NAME AS mpa_name, g.ID AS genre_id, g.NAME AS genre_name FROM FILMS f" +
                " LEFT JOIN MPA m ON f.MPA_ID = m.ID" +
                " LEFT JOIN FILM_GENRES fg ON f.ID = fg.FILM_ID" +
                " LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID";
        return getFilms(getAllFilmsQuery);
    }

    @Override
    public Film get(Long id) {
        String getFilmByIdQuery = "SELECT f.*, m.NAME AS mpa_name, g.ID AS genre_id, g.NAME AS genre_name FROM FILMS f" +
                " LEFT JOIN MPA m ON f.MPA_ID = m.ID" +
                " LEFT JOIN FILM_GENRES fg ON f.ID = fg.FILM_ID" +
                " LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID" +
                " WHERE f.id = " + id;
        List<Film> list = getFilms(getFilmByIdQuery);
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            throw new NotFoundException("Film with such id(" + id + ") is not found");
        }
    }

    @Override
    public List<Film> getTopByLikes(int count) {
        String getTopFilmsByLikesQuery = "SELECT f.*, m.NAME AS mpa_name, g.ID AS genre_id, g.NAME AS genre_name" +
                " FROM (SELECT f.* FROM FILMS f" +
                "   LEFT JOIN LIKES l ON f.ID = l.FILM_ID" +
                "   GROUP BY f.ID" +
                "   ORDER BY COUNT(l.USER_ID) DESC" +
                "   LIMIT " + count + ") f" +
                " LEFT JOIN MPA m ON f.MPA_ID = m.ID" +
                " LEFT JOIN FILM_GENRES fg ON f.ID = fg.FILM_ID" +
                " LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID";
        return getFilms(getTopFilmsByLikesQuery);
    }

    @Override
    public void deleteById(Long id) {
        String sqlQuery = "DELETE FROM films WHERE id = ?;";
        jdbcTemplate.update(sqlQuery, id);
    }


    private List<Film> getFilms(String query) {
        Map<Long, Film> films = new HashMap<>();
        jdbcTemplate.query(query, rs -> {
            long id = rs.getLong("id");
            if (!films.containsKey(id)) {
                Film film = new Film();
                film.setId(id);
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                film.setDuration(rs.getInt("duration"));
                film.setReleaseDate(Objects.requireNonNull(rs.getDate("release_date")).toLocalDate());
                film.setMpa(new Mpa(rs.getLong("mpa_id"), rs.getString("mpa_name")));
                films.put(id, film);
            }
            String genreName = rs.getString("genre_name");
            if (genreName != null) {
                films.get(id).addGenre(new Genre(rs.getLong("genre_id"), genreName));
            }
        });
        return new ArrayList<>(films.values());
    }

    @Override
    public Film create(Film film) {
        String createFilmQuery = "INSERT INTO FILMS (name,description,duration,release_date,mpa_id) VALUES (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(createFilmQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getDuration());
            ps.setDate(4, Date.valueOf(film.getReleaseDate()));
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKeyAs(Long.class));
        log.debug("Film created {}", film);
        replaceFilmGenres(film);
        return get(film.getId());
    }

    @Override
    public Film update(Film film) {
        String updateFilmQuery = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, " +
                "mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(updateFilmQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        log.debug("Film updated {}", film);
        replaceFilmGenres(film);
        return get(film.getId());
    }

    private void replaceFilmGenres(Film film) {
        Long filmId = film.getId();
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        List<Genre> genresList = film.getGenres();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String addGenresQuery = "MERGE INTO film_genres (film_id,genre_id) VALUES (?,?)";
            jdbcTemplate.batchUpdate(addGenresQuery, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, filmId);
                    ps.setLong(2, genresList.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return genresList.size();
                }
            });
        }
        log.debug("Genres {} for film {} updated", genresList, filmId);
    }

    @Override
    public List<Film> getFilmsRecommendations(long userId) {
        log.debug("/getFilmsRecommendations");
        /* SELECT... получение списка фильмов у наиболее схожего по пред-ниям с userID др.юзера
           за минусом фильмов, которые лайкнуты и обоих юзеров */
        String sql = "SELECT l3.film_id AS id, " +
                            "f.name, " +
                            "f.description, " +
                            "f.duration, " +
                            "f.release_date, " +
                            "f.mpa_id, " +
                            "m.name AS mpa_name, " +
                            "fg.genre_id, " +
                            "g.name AS genre_name " +
                    "FROM likes AS l3 " +
                    "LEFT JOIN films AS f ON f.id = l3.film_id " +
                    "LEFT JOIN mpa AS m ON m.id = f.mpa_id " +
                    "LEFT JOIN film_genres AS fg ON fg.film_id = l3.film_id " +
                    "LEFT JOIN genres AS g ON g.id = fg.genre_id " +
                    // SELECT... получение наиболее схожего по предпочтениям с userID другого юзера
                    "WHERE l3.user_id  =    (SELECT l2.user_id AS other_user " +
                                            "FROM likes AS l " +
                                            "JOIN likes AS l2 " +
                                            "ON l2.film_id = l.film_id AND l2.user_id != l.user_id " +
                                            "WHERE l.user_id = " + userId + " " +
                                            "GROUP BY other_user " +
                                            "ORDER BY COUNT(l2.film_id) DESC " +
                                            "LIMIT 1) " +
                    // SELECT... получение списка фильмов лайкнутых userID для фильтрации (не включ.)
                    "AND l3.film_id NOT IN (SELECT l4.film_id " +
                                            "FROM likes AS l4 " +
                                            "WHERE l4.user_id = " + userId + ")";
        return getFilms(sql);
    }

    @Override
    public List<Film> search(String query, String by) {
        List<Film> filmList = new ArrayList<>();
        switch (by) {
            case "director":
               // filmList = jdbcTemplate.query();
                break;
            case "title":
               // filmList = jdbcTemplate.query();
                break;
            case "director,title":
                // filmList = jdbcTemplate.query();
                break;
            case "title,director":
               // filmList = jdbcTemplate.query();
                break;
        }
        return filmList;
    }
}