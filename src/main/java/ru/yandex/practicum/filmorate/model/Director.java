package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Director {
    private Long id;
    @NotBlank
    private String name;
}
