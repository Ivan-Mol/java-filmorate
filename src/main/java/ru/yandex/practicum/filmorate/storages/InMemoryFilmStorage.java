package ru.yandex.practicum.filmorate.storages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

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
    public List<Film> getPopular(int count) {
        return null;
    }

    @Override
    public Genre getGenreById(long genreId) {
        if (!genres.containsKey(genreId)) {
            log.error("Genre with such id(" + genreId + ") is not found");
            throw new NotFoundException("Genre with such id(" + genreId + ") is not found");
        }
        return genres.get(genreId);    }

    @Override
    public void addLike(long filmId, long userId) {
    }

    @Override
    public void removeLike(long filmId, long userId) {

    }

    @Override
    public List<Long> getLikes(long filmId) {
        return null;
    }

    @Override
    public List<Genre> getGenres() {
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
        return new ArrayList<>(films.get(filmId).getGenres());
    }
}
