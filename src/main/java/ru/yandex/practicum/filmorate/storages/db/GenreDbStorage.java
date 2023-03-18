package ru.yandex.practicum.filmorate.storages.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storages.GenreStorage;

import java.util.ArrayList;
import java.util.List;

@Primary
@Component
@Slf4j
public class GenreDbStorage implements GenreStorage {

    private static final RowMapper<Genre> GENRE_MAPPER = (rs, rowNum) ->
            new Genre(rs.getLong("id"), rs.getString("name"));

    private static final List<Genre> CACHE = new ArrayList<>();

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        CACHE.addAll(jdbcTemplate.query("SELECT * FROM genres", GENRE_MAPPER));
    }

    @Override
    public List<Genre> getAllGenres() {
        return CACHE;
    }

    @Override
    public Genre getGenre(long genreId) {
        return CACHE.stream().filter(gen -> gen.getId() == genreId).findAny().orElse(null);
    }
}
