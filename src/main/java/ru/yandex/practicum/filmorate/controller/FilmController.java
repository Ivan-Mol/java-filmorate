package ru.yandex.practicum.filmorate.controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

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
    public Film create(@RequestBody Film film) throws ValidationException {
        if (film.getId()==0){
            film.setId(++idCounter);
        }
        if (isValid(film)) {
            films.put(film.getId(), film);
        } else {
            throw new ValidationException();
        }
        return film;
    }

    @PutMapping(value = "/films")
    public Film update(@RequestBody Film film) throws ValidationException {
        if (!films.containsKey(film.getId())){
            throw new ValidationException();
        }
        if (isValid(film)) {
            films.remove(film.getId());
            films.put(film.getId(), film);
        } else {
            throw new ValidationException();
        }
        return film;
    }

    private static boolean isValid(Film film) throws ValidationException {
        return film.getName() != null &&
                !film.getName().isEmpty() &&
                film.getDescription().length() <= 200 &&
                film.getReleaseDate().isAfter(LocalDate.of(1895, Month.DECEMBER, 28)) &&
                film.getDuration()>0;
    }
//    название не может быть пустым;
//    максимальная длина описания — 200 символов;
//    дата релиза — не раньше 28 декабря 1895 года;
//    продолжительность фильма должна быть положительной.
}
