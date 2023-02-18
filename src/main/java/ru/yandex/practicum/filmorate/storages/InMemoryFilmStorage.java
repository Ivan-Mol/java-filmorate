package ru.yandex.practicum.filmorate.storages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectIsNotFound;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private static long idCounter = 0;
    private final Map<Long, Film> films = new TreeMap<>();

    private static void validate(Film film) {
        if (!film.getReleaseDate().isAfter(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.error("Wrong ReleaseDate");
            throw new ValidationException("Wrong ReleaseDate");
        }
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film create(Film film) {
        validate(film);
        film.setId(++idCounter);
        films.put(film.getId(), film);
        log.debug("Film created {}", film);
        return film;
    }

    @Override
    public Film get(Long id) {
        if (!films.containsKey(id)) {
            log.error("Film with such id(" + id + ") is not found");
            throw new ObjectIsNotFound("Film with such id(" + id + ") is not found");
        }
        return films.get(id);
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Film with such id is not found");
            throw new ObjectIsNotFound("Film with such id is not found");
        }
        validate(film);
        films.put(film.getId(), film);
        log.debug("Film updated: {}", film);
        return film;
    }

    @Override
    public List<Film> bestByLikes(int count) {
        Comparator<Film> byLikes = Comparator.comparing(film -> film.getLikes().size());
        return films.values().stream()
                .sorted(byLikes.reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
