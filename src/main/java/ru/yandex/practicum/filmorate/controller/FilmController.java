package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "/films")
public class FilmController {
    private static long idCounter = 0;
    private final Map<Long, Film> films = new HashMap<>();

    private static void validate(Film film) {
        if (!film.getReleaseDate().isAfter(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.error("Wrong ReleaseDate");
            throw new ValidationException("Wrong ReleaseDate");
        }
    }

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<Film>(films.values());
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        validate(film);
        film.setId(++idCounter);
        films.put(film.getId(), film);
        log.debug("Film created {}", film);
        return film;
    }

    @PutMapping()
    public Film update(@RequestBody @Valid Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Film with such id is not found");
            throw new ValidationException("Film with such id is not found");
        }
        validate(film);
        films.put(film.getId(), film);
        log.debug("Film updated: {}", film);
        return film;
    }

//    название не может быть пустым;
//    максимальная длина описания — 200 символов;
//    дата релиза — не раньше 28 декабря 1895 года;
//    продолжительность фильма должна быть положительной.

}
