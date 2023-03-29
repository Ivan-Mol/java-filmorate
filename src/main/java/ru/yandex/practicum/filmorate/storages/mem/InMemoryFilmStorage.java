package ru.yandex.practicum.filmorate.storages.mem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storages.FilmStorage;

import java.util.*;

@Component
@Slf4j

public class InMemoryFilmStorage implements FilmStorage {
    private static long idCounter = 0;
    private final Map<Long, Film> films = new TreeMap<>();
    private final Map<Long, Genre> genres = new TreeMap<>();

    @Override
    public List<Film> getAll() {
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

    @Override
    public List<Film> getTopByLikes(int count) {
        return null;
    }

    @Override
    public List<Film> getFilmsRecommendations(long userId) {
        return Collections.emptyList();
    }
}