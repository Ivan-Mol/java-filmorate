package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.FilmStorage;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ru.yandex.practicum.filmorate.model.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.OperationType.ADD;
import static ru.yandex.practicum.filmorate.model.OperationType.REMOVE;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorService directorService;

    private static void validate(Film film) {
        if (!film.getReleaseDate().isAfter(LocalDate.of(1895, Month.DECEMBER, 28))) {
            throw new ValidationException("Wrong ReleaseDate");
        }
    }

    public List<Film> findAll() {
        List<Film> films = filmStorage.getAll();
        return directorService.getFilmsWithDirectors(films);
    }

    public Film create(Film film) {
        validate(film);
        filmStorage.create(film);
        directorService.replaceFilmDirectors(film);
        return getFilm(film.getId());
    }

    public Film update(Film film) {
        validate(film);
        filmStorage.get(film.getId());
        directorService.replaceFilmDirectors(film);
        filmStorage.update(film);
        return getFilm(film.getId());
    }

    public void addLike(long filmId, long userId) {
        checkUserExists(userId);
        userStorage.addLike(filmId, userId);
        userStorage.addEvent(LIKE, ADD, userId, filmId);
    }

    public void removeLike(long filmId, long userId) {
        checkUserExists(userId);
        userStorage.removeLike(filmId, userId);
        userStorage.addEvent(LIKE, REMOVE, userId, filmId);
    }

    public List<Film> getPopular(int count) {
        if (count < 1) {
            throw new ValidationException("Can not be less 1");
        }
        List<Film> films = filmStorage.getTopByLikes(count);
        return directorService.getFilmsWithDirectors(films);
    }

    public Film getFilm(long id) {
        List<Film> films = new ArrayList<>();
        films.add(filmStorage.get(id));
        List<Film> filmWithDirector = directorService.getFilmsWithDirectors(films);
        return filmWithDirector.get(0);
    }

    public void deleteFilm(Long id) {
        checkFilmExists(id);
        filmStorage.deleteById(id);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        filmStorage.get(userId);
        filmStorage.get(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getSortDirectorFilms(Long directorId, String sort) {
        directorService.get(directorId);
        List<Film> films = filmStorage.getSortedFilmsByDirector(directorId, sort);
        return directorService.getFilmsWithDirectors(films);
    }

    //throws RuntimeException if User doesn't exist
    private User checkUserExists(long userId) {
        return userStorage.get(userId);
    }

    private Film checkFilmExists(long id) {
        return filmStorage.get(id);
    }

    public List<Film> search(String query, Set<String> by) {
        List<Film> films = filmStorage.search(query, by);
        return directorService.getFilmsWithDirectors(films);
    }
}
