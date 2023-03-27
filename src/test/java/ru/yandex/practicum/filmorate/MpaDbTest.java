package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storages.db.MpaDbStorage;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MpaDbTest {
    private final MpaDbStorage mpaDbStorage;

    @Test
    void getMpa() {
        Mpa expectedMpa1 = createMpa1();
        Mpa expectedMpa2 = createMpa2();
        List<Mpa> expectedGenres = new ArrayList<>(List.of(expectedMpa1, expectedMpa2));
        List<Mpa> actualGenres = new ArrayList<>(List.of(mpaDbStorage.getMpa(expectedMpa1.getId()), mpaDbStorage.getMpa(expectedMpa2.getId())));

        Assertions.assertEquals(expectedGenres, actualGenres);
    }

    private Mpa createMpa1() {
        return new Mpa(1L, "G");
    }

    private Mpa createMpa2() {
        return new Mpa(2L, "PG");

    }
}
