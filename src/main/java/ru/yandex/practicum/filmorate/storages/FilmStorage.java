package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    List<Film> getAll();

    Film get(Long id);

    Film create(Film film);

    Film update(Film film);

    List<Film> getTopByLikes(int count);

    void deleteById(Long id);

    List<Film> getFilmsRecommendations(long userId);

    List<Film> getSortedFilmsByDirector(Long directorId, String sort);

    List<Film> search(String query, Set<String> by);

    List<Film> getCommonFilms(Long userId, Long friendId);
}