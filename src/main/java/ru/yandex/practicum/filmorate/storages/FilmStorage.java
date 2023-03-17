package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface FilmStorage {

    List<Film> findAll();

    Film create(Film film);

    Film get(Long id);

    Film update(Film film);


    List<Film> getPopular(int count);


    Genre getGenreById(long genreId);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    List<Long> getLikes(long filmId);

    List<Genre> getGenres();

     void removeGenre(Film film);
     void addGenre(Film film);

     List<Genre> getFilmGenres(long filmId);




}
