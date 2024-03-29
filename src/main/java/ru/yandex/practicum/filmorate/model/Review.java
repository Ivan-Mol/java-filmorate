package ru.yandex.practicum.filmorate.model;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@EqualsAndHashCode
@Getter
@Setter
@ToString
@AllArgsConstructor
public class Review {
    @NotNull
    private long reviewId;
    @NotBlank(message = "Content can't be empty.")
    @NotNull(message = "Content can't be null.")
    private String content;
    @NotNull(message = "Estimate can't be null.")
    private Boolean isPositive;
    @NotNull(message = "Film Id can't be null.")
    private Long filmId;
    @NotNull(message = "User Id can't be null.")
    private Long userId;
    private int useful;
}
