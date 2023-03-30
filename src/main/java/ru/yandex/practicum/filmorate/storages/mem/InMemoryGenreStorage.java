package ru.yandex.practicum.filmorate.storages.mem;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storages.GenreStorage;

import java.util.List;

@Component

public class InMemoryGenreStorage implements GenreStorage {

    private static final List<Genre> VALUES = List.of(
            new Genre(1, "Комедия"),
            new Genre(2, "Драма"),
            new Genre(3, "Мультфильм"),
            new Genre(4, "Триллер"),
            new Genre(5, "Документальный"),
            new Genre(6, "Боевик")
    );

    @Override
    public List<Genre> getAllGenres() {
        return VALUES;
    }

    @Override
    public Genre getGenre(long genreId) {
        return VALUES.stream().filter(gen -> gen.getId() == genreId).findAny().orElse(null);
    }
}
