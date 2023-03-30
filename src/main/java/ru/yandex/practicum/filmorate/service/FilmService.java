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
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private static void validate(Film film) {
        if (!film.getReleaseDate().isAfter(LocalDate.of(1895, Month.DECEMBER, 28))) {
            throw new ValidationException("Wrong ReleaseDate");
        }
    }

    public List<Film> findAll() {
        return filmStorage.getAll();
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
        userStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        checkUserExists(userId);
        userStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count) {
        if (count < 1) {
            throw new ValidationException("Can not be less 1");
        }
        return filmStorage.getTopByLikes(count);

    }

    public Film getFilm(long id) {
        return filmStorage.get(id);
    }

    public void deleteFilm(Long id) {
        checkFilmExists(id);
        filmStorage.deleteById(id);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        User user = checkUserExists(userId);
        User friend = checkUserExists(friendId);
        if (Objects.nonNull(user) && Objects.nonNull(friend)) {
            return filmStorage.getCommonFilms(userId, friendId);
        }
        return null;
    }

    //throws RuntimeException if User doesn't exist
    private User checkUserExists(long userId) {
        return userStorage.get(userId);
    }

    private Film checkFilmExists(long id) {
        return filmStorage.get(id);
    }
}
