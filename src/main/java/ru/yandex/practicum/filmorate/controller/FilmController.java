package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @GetMapping("/films/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.debug("received GET /films/{}", id);
        return filmService.getFilm(id);
    }

    @GetMapping("/films")
    public List<Film> findAll() {
        log.debug("received GET /films");
        return filmService.findAll();
    }

    @PostMapping("/films")
    public Film create(@RequestBody @Valid Film film) {
        log.debug("received POST /films with body {}", film);
        return filmService.create(film);
    }

    @PutMapping("/films")
    public Film update(@RequestBody @Valid Film film) {
        log.debug("received PUT /films with body {}", film);
        return filmService.update(film);
    }

    @GetMapping("/mpa/{id}")
    public Mpa getMpa(@PathVariable long id) {
        log.debug("received GET /mpa/{}", id);
        return filmService.getMpa(id);
    }

    @GetMapping("/mpa")
    public List<Mpa> getAllMpa() {
        log.info("received GET /mpa/ Get All Mpa");
        return filmService.getAllMpa();
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("received PUT /films/{}/like/{} ", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("received DELETE /films/{}/like/{} ", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/films/popular")
    public List<Film> getPopular(@RequestParam(name = "count", defaultValue = "10", required = false) int count) {
        log.debug("received GET /films/popular, count={}", count);
        return filmService.getPopular(count);
    }

    @GetMapping("/genres")
    public List<Genre> getGenres() {
        log.info("received GET: '/genres'");
        return filmService.getGenres();
    }

    @GetMapping("/genres/{id}")
    public Genre getGenre(@PathVariable long id) {
        log.info("received GET'/genres' with Id={}", id);
        return filmService.getGenreById(id);
    }



}