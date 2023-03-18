package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> findAll();

    Film create(Film film);

    Film get(Long id);

    Film update(Film film);

    List<Film> getPopular(int count);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    void removeGenre(Film film);
}
