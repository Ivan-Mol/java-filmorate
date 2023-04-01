package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface DirectorStorage {
    List<Director> getAll();

    Director get(Long id);

    Director create(Director director);

    Director update(Director director);

    void delete(Long id);

    void replaceFilmDirectors(Film film);

    Map<Long, List<Director>> getDirectorsByFilmIds(List<Long> filmIds);

}
