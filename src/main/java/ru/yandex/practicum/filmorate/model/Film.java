package ru.yandex.practicum.filmorate.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Film {
    private final Set<Long> likes = new HashSet<>();
    private Long id;
    @NotBlank(message = "Name is empty")
    private String name;
    @Length(max = 200, message = "Description length is more than 200")
    @NotBlank(message = "Description is empty")
    private String description;
    private LocalDate releaseDate;
    @Min(value = 1, message = "Duration is incorrect")
    private int duration;
    @NotBlank(message = "Rating is empty")
    private Mpa mpa;
    private Set<Genre> genres = new HashSet<>();


    public void addLike(Long userId) {
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        likes.remove(userId);
    }
}
