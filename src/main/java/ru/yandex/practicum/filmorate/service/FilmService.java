package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storages.FilmStorage;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validate(film);
        filmStorage.get(film.getId());
        return filmStorage.update(film);
    }








    public void addLike(long filmId, long userId) {
        checkUserExists(userId);
        filmStorage.get(filmId).addLike(userId);
    }

    public void removeLike(long filmId, long userId) {
        checkUserExists(userId);
        filmStorage.get(filmId).removeLike(userId);
    }

    public List<Film> getPopular(int count) {
        if (count < 1) {
            new ValidationException("Can not be less 1");
        }
        return filmStorage.getPopular(count);

    }

    public Film getFilm(long id) {
        return filmStorage.get(id);
    }

    private static void validate(Film film) {
        if (!film.getReleaseDate().isAfter(LocalDate.of(1895, Month.DECEMBER, 28))) {
            throw new ValidationException("Wrong ReleaseDate");
        }
    }

    //throws RuntimeException if User doesn't exist
    private void checkUserExists(long userId) {
        userStorage.get(userId);
    }


    public Mpa getMpa(long id) {
        return filmStorage.getMpa(id);
    }

    public List<Mpa> getAllMpa() {
        return filmStorage.getAllMpa();
    }


    public List<Genre> getGenres() {
        return filmStorage.getAllGenres().stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toList());
    }

    public Genre getGenreById(long id) {
        return filmStorage.getGenre(id);
    }

    public void setGenres(Film film) {
        filmStorage.removeGenre(film);
        filmStorage.addGenre(film);
    }

    public List<Genre> getFilmGenres(long filmId) {
        return new ArrayList<>(filmStorage.getFilmGenres(filmId));
    }


}
