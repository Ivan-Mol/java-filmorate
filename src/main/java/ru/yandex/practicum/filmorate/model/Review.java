package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class Review {
    private int reviewId;
    @NotBlank(message = "Содержание отзыва не может быть пустым.")
    @NotNull(message = "Содержание отзыва не может быть равно null.")
    private  String content;
    @NotNull(message = "Оценка отзыва не может быть равна null.")
    private Boolean isPositive;
    @NotNull(message = "Id фильма не может быть равен null.")
    private  Integer filmId;
    @NotNull(message = "Id пользователя не может быть равен null.")
    private Integer userId;
    private int useful;
}