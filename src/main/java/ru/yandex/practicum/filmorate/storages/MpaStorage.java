package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {

    Mpa getMpa(long id);

    List<Mpa> getAllMpa();
}
