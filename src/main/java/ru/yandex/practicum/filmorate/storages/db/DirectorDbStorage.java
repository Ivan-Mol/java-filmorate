package ru.yandex.practicum.filmorate.storages.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storages.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAll() {
        String sqlQuery = "SELECT * FROM directors";

        log.debug("Get directors list");
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    @Override
    public Director get(Long id) {
        String sqlQuery = "SELECT * FROM directors WHERE director_id = ?";
        List<Director> directors = jdbcTemplate.query(sqlQuery, this::mapRowToDirector, id);
        if (!directors.isEmpty()) {
            log.debug("Get director by id {}", id);
            return directors.get(0);
        } else {
            throw new NotFoundException("Director not found");
        }
    }

    @Override
    public Director create(Director director) {
        String sqlQuery = "INSERT INTO directors(name) VALUES(?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            director.setId((Long) keyHolder.getKey());
        }
        log.debug("Create director {}", director);
        return director;
    }

    @Override
    public Director update(Director director) {
        String sqlQuery = "UPDATE directors SET name = ? WHERE director_id = ?";

        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        log.debug("Update director {}", director);
        return director;
    }

    @Override
    public void delete(Long id) {
        String sqlQuery = "DELETE FROM directors WHERE director_id = ?";

        log.debug("Delete director {}", id);
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public void addFilmDirector(Film film) {
        Long filmId = film.getId();

        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", filmId);

        String sqlQuery = "MERGE INTO film_directors (director_id, film_id) VALUES(?, ?)";
        List<Director> directors = film.getDirectors();
        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, directors.get(i).getId());
                ps.setLong(2, filmId);
            }

            @Override
            public int getBatchSize() {
                return directors.size();
            }
        });
        log.debug("Add director to Bd from Film {}", film);
    }

    @Override
    public Map<Long, List<Director>> getFilmDirectors(List<Long> filmIds) {
        Map<Long, List<Director>> filmDirectors = new HashMap<>();
        SqlParameterSource source = new MapSqlParameterSource("filmIds", filmIds);
        String sqlQuery = "SELECT * FROM film_directors AS fd " +
                "LEFT JOIN directors AS d ON fd.director_id = d.director_id " +
                "WHERE fd.film_id IN (:filmIds)";
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);

        template.query(sqlQuery, source, rs -> {
            Long filmId = rs.getLong("film_id");
            String directorName = rs.getString("name");
            if (directorName != null) {
                if (filmDirectors.containsKey(filmId)) {
                    filmDirectors.get(filmId).add(new Director(
                            rs.getLong("director_id"),
                            rs.getString("name")));
                } else {
                    List<Director> directors = new ArrayList<>();
                    directors.add(new Director(
                            rs.getLong("director_id"),
                            rs.getString("name")));
                    filmDirectors.put(filmId, directors);
                }
            }
        });
        log.debug("Get directors from BD");
        return filmDirectors;
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        log.debug("Row mapper director");
        return new Director(rs.getLong("director_id"), rs.getString("name"));
    }
}