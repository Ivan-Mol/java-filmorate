package ru.yandex.practicum.filmorate.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
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
    @NotNull
    private Mpa mpa;
    private List<Genre> genres = new ArrayList<>();
    @NotNull
    private Director director;

    public void addGenre(Genre genre) {
        genres.add(genre);
    }
}
