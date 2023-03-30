package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storages.DirectorStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void addDirectorToBd(Film film) {
//        if (!film.getDirectors().isEmpty() && film.getDirectors() != null) {
        directorStorage.addFilmDirector(film);
//        }
    }

    public List<Film> getListDirectors(List<Film> films) {
        List<Long> ids = new ArrayList<>(); //films.stream().map(Film::getId).collect(Collectors.toList());
        if (!films.isEmpty()) {
            for (Film film : films) {
                ids.add(film.getId());
            }
            Map<Long, List<Director>> directors = directorStorage.getFilmDirectors(ids);
            for (Film film : films) {
                if (directors.containsKey(film.getId())) {
                    film.addDirector(directors.get(film.getId()));
                }
            }
        }
        return films;
    }

    public Film getDirector(Film film) {
        List<Long> ids = new ArrayList<>();
        ids.add(film.getId());
        Map<Long, List<Director>> directors = directorStorage.getFilmDirectors(ids);
        if (directors.containsKey(film.getId())) {
            film.addDirector(directors.get(film.getId()));
        }
        return film;
    }
}
