package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storages.db.GenreDbStorage;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GenreDbStorageTest {
    private final GenreDbStorage genreDbStorage;

    @Test
    void getGenre() {
        Genre expectedGenre1 = createGenre1();
        Genre expectedGenre2 = createGenre2();
        List<Genre> expectedGenres
                = new ArrayList<>(List.of(expectedGenre1, expectedGenre2));
        List<Genre> actualGenres
                = new ArrayList<>(List.of(genreDbStorage.getGenre(expectedGenre1.getId()), genreDbStorage.getGenre(expectedGenre2.getId())));

        Assertions.assertEquals(expectedGenres, actualGenres);
    }

    private Genre createGenre1() {
        return new Genre(1L, "Комедия");
    }

    private Genre createGenre2() {
        return new Genre(2L, "Драма");

    }
}
