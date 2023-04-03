package ru.yandex.practicum.filmorate.storages.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storages.GenreStorage;

import java.util.List;

@Primary
@Component
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private static final RowMapper<Genre> GENRE_MAPPER = (rs, rowNum) ->
            new Genre(rs.getLong("id"), rs.getString("name"));

    private final JdbcTemplate jdbcTemplate;
    private List<Genre> genreList;

    private void initCache() {
        genreList = jdbcTemplate.query("SELECT * FROM genres", GENRE_MAPPER);
    }

    @Override
    public Genre getGenre(long genreId) {
        if (genreList == null) {
            initCache();
        }
        return genreList.stream()
                .filter(gen -> gen.getId() == genreId)
                .findAny()
                .orElse(null);
    }

    @Override
    public List<Genre> getAllGenres() {
        if (genreList == null) {
            initCache();
        }
        return genreList;
    }
}