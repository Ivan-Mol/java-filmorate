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
public class FilmController {
    private static int idCounter = 0;
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping("/films")
    public List<Film> findAll() {
        return new ArrayList<Film>(films.values());
    }

    @PostMapping(value = "/films")
    public Film create(@RequestBody @Valid Film film) {
        film.setId(++idCounter);
        if (film.getReleaseDate().isAfter(LocalDate.of(1895, Month.DECEMBER, 28))) {
            films.put(film.getId(), film);
            log.debug("Film created");
        } else {
            log.error("Wrong ReleaseDate");
            throw new ValidationException("Wrong ReleaseDate");
        }
        return film;
    }

    @PutMapping(value = "/films")
    public Film update(@RequestBody @Valid Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Film with such id is not found");
            throw new ValidationException("Film with such id is not found");
        }
        if (film.getReleaseDate().isAfter(LocalDate.of(1895, Month.DECEMBER, 28))) {
            films.put(film.getId(), film);
            log.debug("Film created");
        } else {
            log.error("Wrong ReleaseDate");
            throw new ValidationException("Wrong ReleaseDate");
        }
        return film;
    }

//    название не может быть пустым;
//    максимальная длина описания — 200 символов;
//    дата релиза — не раньше 28 декабря 1895 года;
//    продолжительность фильма должна быть положительной.

}
