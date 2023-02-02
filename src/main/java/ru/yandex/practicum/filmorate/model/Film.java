package ru.yandex.practicum.filmorate.model;


import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@Builder
public class Film {
    int id;
    @NotBlank(message = "Name is empty")
    String name;
    @Length(max = 200, message = "Description length is more than 200")
    @NotBlank(message = "Description is empty")
    String description;
    LocalDate releaseDate;
    @Min(value = 1, message = "Duration is incorrect")
    int duration;
}
