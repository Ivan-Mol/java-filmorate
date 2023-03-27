package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> getAll();

    Film get(Long id);

    Film create(Film film);

    Film update(Film film);

    List<Film> getTopByLikes(int count);
}
