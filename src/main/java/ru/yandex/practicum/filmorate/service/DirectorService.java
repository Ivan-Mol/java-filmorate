package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storages.DirectorStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@AllArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> getAll() {
        return directorStorage.getAll();
    }

    public Director get(Long id) {
        return directorStorage.get(id);
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        get(director.getId());
        return directorStorage.update(director);
    }

    public void delete(Long id) {
        get(id);
        directorStorage.delete(id);
    }

    public void replaceFilmDirectors(Film film) {
        directorStorage.replaceFilmDirectors(film);
    }

    public List<Film> getFilmsWithDirectors(List<Film> films) {
        List<Long> ids = new ArrayList<>();
        if (!films.isEmpty()) {
            for (Film film : films) {
                ids.add(film.getId());
            }
            Map<Long, List<Director>> directors = directorStorage.getDirectorsByFilmIds(ids);
            for (Film film : films) {
                if (directors.containsKey(film.getId())) {
                    film.addDirectors(directors.get(film.getId()));
                }
            }
        }
        return films;
    }
}
