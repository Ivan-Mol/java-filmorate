package ru.yandex.practicum.filmorate.storages.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storages.MpaStorage;

import java.util.List;

@Primary
@Component
@Slf4j
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private static final RowMapper<Mpa> MPA_MAPPER = (rs, rowNum) ->
            new Mpa(rs.getLong("id"), rs.getString("name"));

    private final JdbcTemplate jdbcTemplate;
    private List<Mpa> CACHE;

    private void initCache() {
        CACHE = jdbcTemplate.query("SELECT * FROM mpa", MPA_MAPPER);
    }

    @Override
    public Mpa getMpa(long id) {
        if (CACHE == null){
            initCache();
        }
        return CACHE.stream()
                .filter(mpa -> mpa.getId() == id)
                .findAny()
                .orElse(null);
    }

    @Override
    public List<Mpa> getAllMpa() {
        if (CACHE == null){
            initCache();
        }
        return CACHE;
    }
}