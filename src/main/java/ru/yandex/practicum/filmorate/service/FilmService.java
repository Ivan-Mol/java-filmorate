package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storages.FilmStorage;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

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
        return directorService.getListDirectors(films);
    }

    public Film create(Film film) {
        validate(film);
        filmStorage.create(film);
        film.setId(film.getId());
        directorService.addDirectorToBd(film);
        return getFilm(film.getId());
    }

    public Film update(Film film) {
        validate(film);
        filmStorage.get(film.getId());
        directorService.addDirectorToBd(film);
        filmStorage.update(film);
        return getFilm(film.getId());
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
        List<Film> films =filmStorage.getTopByLikes(count);
        return directorService.getListDirectors(films);

    }

    public Film getFilm(long id) {
        Film film = filmStorage.get(id);
        return directorService.getDirector(film);
    }

    public void deleteFilm(Long id) {
        checkFilmExists(id);
        filmStorage.deleteById(id);
    }

    public List<Film> getSortDirectorFilms(Long directorId, String sort) {
        directorService.get(directorId);
        List<Film> films = filmStorage.getSortDirectorFilms(directorId, sort);
        return directorService.getListDirectors(films);
    }

    //throws RuntimeException if User doesn't exist
    private void checkUserExists(long userId) {
        userStorage.get(userId);
    }

    private void checkFilmExists(long id) {
        filmStorage.get(id);
    }
}
