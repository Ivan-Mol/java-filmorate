package ru.yandex.practicum.filmorate.storages.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Primary;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import ru.yandex.practicum.filmorate.storages.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Primary
@Component
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getAll() {
        String getAllFilmsQuery = "SELECT f.*, m.NAME AS mpa_name, g.ID AS genre_id, g.NAME AS genre_name " +
                " FROM FILMS f" +
                " LEFT JOIN MPA m ON f.MPA_ID = m.ID" +
                " LEFT JOIN FILM_GENRES fg ON f.ID = fg.FILM_ID" +
                " LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID";
        return getFilms(getAllFilmsQuery);
    }

    @Override
    public Film get(Long id) {
        String getFilmByIdQuery = "SELECT f.*, m.NAME AS mpa_name, g.ID AS genre_id, g.NAME AS genre_name" +
                " FROM FILMS f" +
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
                " LEFT JOIN LIKES l ON f.ID = l.FILM_ID" +
                " GROUP BY f.ID" +
                " ORDER BY COUNT(l.USER_ID) DESC" +
                " LIMIT " + count + ") f" +
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
        Map<Long, Film> films = new LinkedHashMap<>();
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
        String sqlMostSimilarUser = "SELECT l2.user_id " +
                                    "FROM likes AS l " +
                                    "JOIN likes AS l2 ON l.film_id  = l2.film_id  AND l2.user_id != l.user_id " +
                                    "WHERE l.user_id = ? " +
                                    "GROUP BY l2.user_id " +
                                    "ORDER BY COUNT(l2.film_id) DESC " +
                                    "LIMIT 1";
        Integer similarUserId;
        try {
            similarUserId = jdbcTemplate.queryForObject(sqlMostSimilarUser, Integer.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }

        String sqlRecomFilms =  "SELECT f.ID, " +
                                        "f.NAME, " +
                                        "f.DESCRIPTION, " +
                                        "f.DURATION, " +
                                        "f.RELEASE_DATE, " +
                                        "f.MPA_ID, " +
                                        "m.NAME AS mpa_name, " +
                                        "fg.GENRE_ID, " +
                                        "g.NAME AS genre_name " +
                                "FROM LIKES AS l " +
                                "LEFT JOIN LIKES AS l2 ON l.FILM_ID = l2.FILM_ID AND l2.USER_ID = " + userId + " " +
                                "LEFT JOIN FILMS AS f ON l.FILM_ID = f.ID " +
                                "LEFT JOIN MPA AS m ON m.ID = f.MPA_ID " +
                                "LEFT JOIN FILM_GENRES AS fg ON fg.FILM_ID = f.ID " +
                                "LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID " +
                                "WHERE l.USER_ID = " + similarUserId + " AND l2.USER_ID IS NULL";
        return getFilms(sqlRecomFilms);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        log.debug("/getCommonFilms");

        String sqlQuery = "SELECT f.*, m.NAME AS mpa_name, g.ID AS genre_id, g.NAME AS genre_name" +
                " FROM (SELECT f.* FROM FILMS f LEFT JOIN LIKES l ON f.ID = l.FILM_ID" +
                " INNER JOIN LIKES l1 ON f.ID = l1.FILM_ID " +
                "INNER JOIN LIKES l2 ON f.ID = l2.FILM_ID " +
                "WHERE l1.USER_ID = " + userId + " AND l2.USER_ID = " + friendId +
                " GROUP BY f.ID, l.user_id" +
                " ORDER BY COUNT(l.USER_ID) DESC) f" +
                " LEFT JOIN MPA m ON f.MPA_ID = m.ID" +
                " LEFT JOIN FILM_GENRES fg ON f.ID = fg.FILM_ID" +
                " LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID";
        return getFilms(sqlQuery);
    }

    @Override
    public List<Film> getSortedFilmsByDirector(Long directorId, String sort) {
        String sqlSortDirectorFilmsByYear = "SELECT f.*, m.name AS mpa_name, g.id genre_id, g.name AS genre_name " +
                "FROM films AS f LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN film_directors AS fd ON f.id = fd.film_id " +
                "WHERE fd.director_id = " + directorId + " ORDER BY f.release_date";
        String sqlSortDirectorFilmsByLike = "SELECT f.*, m.NAME AS mpa_name, g.ID AS genre_id, g.NAME AS genre_name" +
                " FROM (SELECT f.* FROM FILMS f" +
                "   LEFT JOIN LIKES l ON f.ID = l.FILM_ID" +
                "   GROUP BY f.ID" +
                "   ORDER BY COUNT(l.USER_ID) DESC" +
                "   ) f" +
                " LEFT JOIN MPA m ON f.MPA_ID = m.ID" +
                " LEFT JOIN FILM_GENRES fg ON f.ID = fg.FILM_ID" +
                " LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID " +
                "LEFT JOIN film_directors AS fd ON f.id = fd.film_id " +
                "WHERE fd.director_id =" + directorId;

        switch (sort) {
            case "year":
                log.debug("Film with director {} sort by {}", directorId, sort);
                return getFilms(sqlSortDirectorFilmsByYear);
            case "likes":
                log.debug("Film with director {} sort by {}", directorId, sort);
                return getFilms(sqlSortDirectorFilmsByLike);
            default:
                throw new ValidationException("This sort type is not supported");
        }
    }

    @Override
    public List<Film> search(String query, Set<String> by) {
        String sql = "SELECT f.*, m.NAME AS mpa_name, g.ID AS genre_id, g.NAME AS genre_name" +
                " FROM FILMS f" +
                "    LEFT JOIN (SELECT COUNT(USER_ID) AS likes, FILM_ID" +
                "        FROM LIKES" +
                "        GROUP BY FILM_ID) l on f.ID = l.FILM_ID" +
                "    LEFT JOIN FILM_DIRECTORS FD on f.ID = FD.FILM_ID" +
                "    LEFT JOIN DIRECTORS D on D.DIRECTOR_ID = FD.DIRECTOR_ID" +
                "    LEFT JOIN MPA m ON f.MPA_ID = m.ID" +
                "    LEFT JOIN FILM_GENRES fg ON f.ID = fg.FILM_ID" +
                "    LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID" +
                " WHERE " + getWhereClause(query, by) +
                " ORDER BY l.likes DESC;";
        return getFilms(sql);
    }

    private String getWhereClause(String query, Set<String> by) {
        Set<String> lowercaseBy = by.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Map<String, String> clauseMapper = Map.of(
                "director", "LOWER(d.NAME) LIKE LOWER('%" + query + "%')",
                "title", "LOWER(f.NAME) LIKE LOWER('%" + query + "%')"
        );
        if (!clauseMapper.keySet().containsAll(lowercaseBy)) {
            throw new ValidationException("This search type is not supported: " + lowercaseBy);
        }
        return lowercaseBy.stream()
                .map(clauseMapper::get)
                .collect(Collectors.joining(" OR "));
    }
}