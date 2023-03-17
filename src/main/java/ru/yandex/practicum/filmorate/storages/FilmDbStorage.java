package ru.yandex.practicum.filmorate.storages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
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
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAll() {
        return getFilms(FIND_ALL_QUERY);
    }

    private List<Film> getFilms(String query) {
        return jdbcTemplate.query(query, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setDuration(rs.getInt("duration"));
            film.setReleaseDate(Objects.requireNonNull(rs.getDate("release_date")).toLocalDate());
            Mpa mpa = new Mpa(rs.getLong("mpa_id"),rs.getString("mpa_name"));
            film.setMpa(mpa);
            return film;
        });
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
        log.debug("Film created {}", film);
        return film;
    }

    @Override
    public Film get(Long id) {
        String findByIdQuery = FIND_ALL_QUERY + " where films.id = " + id;
        List<Film> list = getFilms(findByIdQuery);
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            log.error("User with such id(" + id + ") is not found");
            throw new NotFoundException("User with such id(" + id + ") is not found");
        }
    }

    @Override
    public Film update(Film film) {
        String sql =
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                        "WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId(), film.getId());
        return film;
    }

    @Override
    public Mpa getMpa(long id) {
        List<Mpa> mpas = jdbcTemplate
                .query("SELECT * FROM mpa WHERE id = ?",
                        (rs, rowNum) -> new Mpa(rs.getLong("id"),rs.getString("name")),id);
        if (mpas.size()==0){
            throw new NotFoundException("Mpa with this Id is not Found");
        }else {
            return mpas.get(0);
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        return jdbcTemplate
                .query("SELECT * FROM mpa",
                        (rs, rowNum) -> new Mpa(rs.getLong("id"),rs.getString("name")));
    }


    @Override
    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Long> getLikes(long filmId) {
        return jdbcTemplate
                .query("SELECT user_id FROM likes WHERE film_id = ?",
                        (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }


    public List<Genre> getAllGenres() {
        return null;
    }

    public Genre getGenre(long id) {
        List<Genre> genres = jdbcTemplate
                .query("SELECT * FROM genres WHERE id = ?",
                        (rs, rowNum) -> new Genre(rs.getLong("id"),rs.getString("name")),id);
        if (genres.size()==0){
            throw new NotFoundException("Genre with this Id is not Found");
        }else {
            return genres.get(0);
        }
    }

    public void removeGenre(Film film) {

    }

    public void addGenre(Film film) {
    }

    @Override
    public List<Genre> getFilmGenres(long filmId) {
        return null;
    }

    @Override
    public List<Film> getPopular(int count) {
        return null;
    }
}
