package ru.yandex.practicum.filmorate.storages.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
import java.util.List;
import java.util.Objects;


@Primary
@Component
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_ALL_QUERY = "select films.*, mpa.name AS mpa_name " +
            "from films " +
            "LEFT JOIN mpa " +
            "ON films.mpa_id  = mpa.id";

    private static final String FIND_BY_ID_QUERY = FIND_ALL_QUERY + " where films.id = ?";

    private static final RowMapper<Film> FILM_MAPPER = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));
        film.setReleaseDate(Objects.requireNonNull(rs.getDate("release_date")).toLocalDate());
        Mpa mpa = new Mpa(rs.getLong("mpa_id"), rs.getString("mpa_name"));
        film.setMpa(mpa);
        return film;
    };
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_QUERY, FILM_MAPPER);
        for (Film f : films) {
            f.setGenreList(getFilmGenres(f.getId()));
        }
        return films;
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement("INSERT INTO films ( name, description, duration, release_date, mpa_id) VALUES ( ?, ?, ?, ?, ? )",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getDuration());
            ps.setDate(4, Date.valueOf(film.getReleaseDate()));
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKeyAs(Long.class));

        setGenres(film);
        log.debug("Film created {}", film);
        return film;
    }


    @Override
    public Film get(Long id) {
        List<Film> list = jdbcTemplate.query(FIND_BY_ID_QUERY, FILM_MAPPER, id);
        if (!list.isEmpty()) {
            list.get(0).setGenreList(getFilmGenres(id));
            return list.get(0);
        } else {
            log.error("User with such id(" + id + ") is not found");
            throw new NotFoundException("User with such id(" + id + ") is not found");
        }
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update("UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                        "WHERE id = ?", film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId(), film.getId());
        setGenres(film);
        return film;
    }


//    @Override
//    public Mpa getMpa(long id) {
//        List<Mpa> mpas = jdbcTemplate
//                .query("SELECT * FROM mpa WHERE id = ?",
//                        (rs, rowNum) -> new Mpa(rs.getLong("id"), rs.getString("name")), id);
//        if (mpas.size() == 0) {
//            throw new NotFoundException("Mpa with this Id is not Found");
//        } else {
//            return mpas.get(0);
//        }
//    }

//    @Override
//    public List<Mpa> getAllMpa() {
//        return jdbcTemplate
//                .query("SELECT * FROM mpa",
//                        (rs, rowNum) -> new Mpa(rs.getLong("id"), rs.getString("name")));
//    }


    @Override
    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

//    @Override
//    public List<Long> getLikes(long filmId) {
//        return jdbcTemplate
//                .query("SELECT user_id FROM likes WHERE film_id = ?",
//                        (rs, rowNum) -> rs.getLong("user_id"), filmId);
//    }


//    public List<Genre> getAllGenres() {
//        return jdbcTemplate
//                .query("SELECT * FROM genres",
//                        (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")));
//    }

    public Genre getGenre(long id) {
        List<Genre> genres = jdbcTemplate
                .query("SELECT * FROM genres WHERE id = ?",
                        (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")), id);
        if (genres.size() == 0) {
            throw new NotFoundException("Genre with this Id is not Found");
        } else {
            return genres.get(0);
        }
    }

    public void removeGenre(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
    }

    public void addGenre(Film film) {
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }
    }

    @Override
    public List<Film> getPopular(int count) {
        String getPopQuery = "SELECT films.*, mpa.NAME AS mpa_name" +
                " FROM films" +
                " LEFT JOIN likes ON films.id = likes.film_id" +
                " LEFT JOIN mpa ON FILMS.MPA_ID = MPA.ID" +
                " GROUP BY films.id" +
                " ORDER BY COUNT(likes.user_id) DESC" +
                " LIMIT ?;";
        return jdbcTemplate.query(getPopQuery, FILM_MAPPER, count);
    }

    private void setGenres(Film film) {
        Long filmId = film.getId();
        removeGenre(film);
        List<Genre> genresList = new ArrayList<>(film.getGenres());
        jdbcTemplate.batchUpdate("INSERT INTO film_genres (film_id,genre_id) VALUES ( ?,? )", new BatchPreparedStatementSetter() {
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

    private List<Genre> getFilmGenres(long filmId) {
        return jdbcTemplate
                .query("SELECT g.id, g.name" +
                                " FROM FILM_GENRES fg" +
                                " LEFT JOIN GENRES g ON FG.GENRE_ID = g.ID" +
                                " WHERE fg.FILM_ID = ?",
                        (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")), filmId);
    }
}
