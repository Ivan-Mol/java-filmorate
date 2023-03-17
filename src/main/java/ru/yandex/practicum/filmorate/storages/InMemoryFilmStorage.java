package ru.yandex.practicum.filmorate.storages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private static long idCounter = 0;
    private final Map<Long, Film> films = new TreeMap<>();
    private final Map<Long, Long> likes = new TreeMap<>();

    private final Map<Long, Genre> genres = new TreeMap<>();
    private static final List<Mpa> mpaRatings
            = List.of(new Mpa(1, "G"), new Mpa(2, "PG"), new Mpa(3, "PG-13"),
            new Mpa(4, "R"), new Mpa(5, "NC-17"));


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

    @Override
    public Mpa getMpa(long id) {
        for (Mpa mpa : mpaRatings) {
            if (mpa.getId() == id) {
                return mpa;
            }
        }
        throw new NotFoundException("Mpa with such id is not found");
    }

    @Override
    public List<Mpa> getAllMpa() {
        return mpaRatings;
    }

    @Override
    public List<Film> getPopular(int count) {
        return null;
    }

    @Override
    public Genre getGenre(long genreId) {
        if (!genres.containsKey(genreId)) {
            log.error("Genre with such id(" + genreId + ") is not found");
            throw new NotFoundException("Genre with such id(" + genreId + ") is not found");
        }
        return genres.get(genreId);
    }

    @Override
    public void addLike(long filmId, long userId) {
        likes.put(filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        likes.remove(filmId, userId);

    }

    @Override
    public List<Long> getLikes(long filmId) {
        return new ArrayList<>(likes.values());
    }

    @Override
    public List<Genre> getAllGenres() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public void removeGenre(Film film) {
        genres.remove(film.getId());
    }

    @Override
    public void addGenre(Film film) {

    }

    @Override
    public List<Genre> getFilmGenres(long filmId) {
        // return new ArrayList<>(films.get(filmId).getGenres());
        return null;
    }
}
