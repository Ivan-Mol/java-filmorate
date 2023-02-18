package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storages.FilmStorage;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film addLike(long filmId, long userId) {
        filmStorage.get(filmId).addLike(userService.getUser(userId).getId());
        return filmStorage.get(filmId);
    }

    public Film removeLike(long filmId, long userId) {
        filmStorage.get(filmId).removeLike(userService.getUser(userId).getId());
        return filmStorage.get(filmId);
    }

    public List<Film> bestByLikes(int count) {
        return filmStorage.bestByLikes(count);
    }

    public Film getFilm(long id) {
        return filmStorage.get(id);
    }
}
