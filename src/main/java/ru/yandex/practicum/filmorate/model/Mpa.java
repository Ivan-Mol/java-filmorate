package ru.yandex.practicum.filmorate.model;

import lombok.*;
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Mpa {
    private long id;
    private String name;

    public Mpa(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
