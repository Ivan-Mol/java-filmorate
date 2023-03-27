package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storages.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;

    public Mpa getMpa(long id) {
        Mpa mpa = mpaStorage.getMpa(id);
        if (mpa == null) {
            throw new NotFoundException("Mpa with such id is not found");
        }
        return mpa;
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getAllMpa();
    }
}
