package ru.yandex.practicum.filmorate.storages.mem;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storages.MpaStorage;

import java.util.List;

@Component
public class InMemoryMpaStorage implements MpaStorage {

    private static final List<Mpa> VALUES = List.of(
            new Mpa(1, "G"),
            new Mpa(2, "PG"),
            new Mpa(3, "PG-13"),
            new Mpa(4, "R"),
            new Mpa(5, "NC-17"));

    @Override
    public Mpa getMpa(long id) {
        return VALUES.stream().filter(mpa -> mpa.getId() == id).findAny().orElse(null);
    }

    @Override
    public List<Mpa> getAllMpa() {
        return VALUES;
    }
}
