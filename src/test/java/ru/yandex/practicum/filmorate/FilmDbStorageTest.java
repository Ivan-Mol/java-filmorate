package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storages.db.UserDbStorage;

import javax.validation.constraints.AssertFalse;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void get() {
        Film expectedFilm = createTestFilm1();
        filmStorage.create(expectedFilm);
        Film actualFilm = filmStorage.get(expectedFilm.getId());
        assertEquals(expectedFilm, actualFilm);
    }

    @Test
    void getAll() {
        Film expectedFilm1 = createTestFilm1();
        filmStorage.create(expectedFilm1);
        Film expectedFilm2 = createTestFilm2();
        filmStorage.create(expectedFilm2);
        List<Film> expectedFilms = List.of(expectedFilm1, expectedFilm2);

        List<Film> actFilms = filmStorage.getAll();
        assertEquals(expectedFilms, actFilms);
    }

    @Test
    void create() {
        Film expectedFilm = createTestFilm1();
        filmStorage.create(expectedFilm);
        Film actualFilm = filmStorage.get(expectedFilm.getId());

        assertEquals(expectedFilm.getId(), actualFilm.getId());
        assertEquals(expectedFilm.getName(), actualFilm.getName());
        assertEquals(expectedFilm.getDescription(), actualFilm.getDescription());
        assertEquals(expectedFilm.getReleaseDate(), actualFilm.getReleaseDate());
        assertEquals(expectedFilm.getDuration(), actualFilm.getDuration());
        assertEquals(expectedFilm.getMpa(), actualFilm.getMpa());
        assertEquals(expectedFilm.getGenres(), actualFilm.getGenres());
    }

    @Test
    void update() {
        Film expectedFilm = createTestFilm1();
        filmStorage.create(expectedFilm);
        expectedFilm.setName("FilmStrange");

        filmStorage.update(expectedFilm);
        Film actualFilm = filmStorage.get(expectedFilm.getId());

        assertEquals(expectedFilm.getId(), actualFilm.getId());
        assertEquals(expectedFilm.getName(), actualFilm.getName());
    }

//    @Test
//    void removeFilmByID(){
//        assertTrue(filmStorage.getAll().isEmpty());
//        Film testFilm1 = createTestFilm1();
//        filmStorage.create(testFilm1);
//        assertFalse(filmStorage.getAll().isEmpty());
//        filmStorage.deleteById(testFilm1.getId());
//        assertTrue(filmStorage.getAll().isEmpty());
//        User testUser = createTestUser1();
//        Long userId = userStorage.create(testUser).getId();
//        userStorage.addLike(testFilm1.getId(),userId);
//
//    }

    private Film createTestFilm1() {
        Film film = new Film();
        film.setId(1L);
        film.setName("film1");
        film.setDescription("film1 description");
        film.setReleaseDate(LocalDate.of(1999, 6, 6));
        film.setDuration(190);

        Mpa mpa = new Mpa(1L, "G");
        mpa.setId(1L);
        film.setMpa(mpa);

        Genre genre1 = new Genre(1L, "Комедия");
        Genre genre2 = new Genre(2L, "Драма");

        film.setGenres(List.of(genre1, genre2));
        return film;
    }

    private Film createTestFilm2() {
        Film film = new Film();
        film.setId(2L);
        film.setName("film1");
        film.setDescription("film2 description");
        film.setReleaseDate(LocalDate.of(1994, 11, 14));
        film.setDuration(90);
        Mpa rating = new Mpa(2L, "PG");
        film.setMpa(rating);
        return film;
    }

    private User createTestUser1() {
        User user = new User();
        user.setEmail("user1@gmail.com");
        user.setLogin("user1");
        user.setName("user1");
        user.setBirthday(LocalDate.of(1990, 11, 12));
        return user;
    }
}