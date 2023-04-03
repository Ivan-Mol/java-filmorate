package ru.yandex.practicum.filmorate.storages.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storages.MpaStorage;

import java.util.ArrayList;
import java.util.List;

@Primary
@Component
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private static final RowMapper<Mpa> MPA_MAPPER = (rs, rowNum) ->
            new Mpa(rs.getLong("id"), rs.getString("name"));

    private static final List<Mpa> CACHE = new ArrayList<>();

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        CACHE.addAll(jdbcTemplate.query("SELECT * FROM mpa", MPA_MAPPER));
    }

    @Override
    public Mpa getMpa(long id) {
        return CACHE.stream().filter(mpa -> mpa.getId() == id).findAny().orElse(null);
    }

    @Override
    public List<Mpa> getAllMpa() {
        return CACHE;
    }
}
