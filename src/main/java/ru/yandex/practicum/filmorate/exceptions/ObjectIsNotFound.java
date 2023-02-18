package ru.yandex.practicum.filmorate.exceptions;

public class ObjectIsNotFound extends RuntimeException {
    public ObjectIsNotFound(String message) {
        super(message);
    }
}
