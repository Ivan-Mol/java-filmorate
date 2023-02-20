package ru.yandex.practicum.filmorate.storages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private static long idCounter = 0;
    private final Map<Long, Film> films = new TreeMap<>();

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film create(Film film) {
        film.setId(++idCounter);
        films.put(film.getId(), film);
        log.debug("Film created {}", film);
        return film;
    }

    @Override
    public Film get(Long id) {
        if (!films.containsKey(id)) {
            log.error("Film with such id(" + id + ") is not found");
            throw new NotFoundException("Film with such id(" + id + ") is not found");
        }
        return films.get(id);
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Film with such id is not found");
            throw new NotFoundException("Film with such id is not found");
        }
        films.put(film.getId(), film);
        log.debug("Film updated: {}", film);
        return film;
    }
}
