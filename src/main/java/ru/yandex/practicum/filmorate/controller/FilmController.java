package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.debug("received GET /films/{}", id);
        return filmService.getFilm(id);
    }

    @GetMapping
    public List<Film> findAll() {
        log.debug("received GET /films");
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        log.debug("received POST /films with body {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        log.debug("received PUT /films with body {}", film);
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("received PUT /films/{}/like/{} ", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("received DELETE /films/{}/like/{} ", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(name = "count", defaultValue = "10", required = false) int count) {
        log.debug("received GET /films/popular, count={}", count);
        return filmService.getPopular(count);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable Long filmId) {
        //log.debug("received DELETE /films/{filmId} ", filmId);
        filmService.deleteFilm(filmId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortDirectorFilms(@PathVariable("directorId") Long directorId, @RequestParam("sortBy") String sortBy) {
        return filmService.getSortDirectorFilms(directorId, sortBy);
    }

    @GetMapping("/search")
    //{{baseUrl}}/films/search?query=upDATE&by=director
    public List<Film> search(@RequestParam(required = false) String query,
                             @RequestParam(required = false) String by) {
        return filmService.search(query, by);
    }
}