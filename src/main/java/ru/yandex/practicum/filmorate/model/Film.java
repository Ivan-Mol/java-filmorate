package ru.yandex.practicum.filmorate.model;


import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class Film {
    private Long id;
    @NotBlank(message = "Name is empty")
    private String name;
    @Length(max = 200, message = "Description length is more than 200")
    @NotBlank(message = "Description is empty")
    private String description;
    private LocalDate releaseDate;
    @Min(value = 1, message = "Duration is incorrect")
    private int duration;
}
