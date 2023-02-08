package ru.yandex.practicum.filmorate.model;


import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    @Email(message = "Email is incorrect")
    @NotBlank(message = "Email is empty")
    private String email;
    @NotBlank(message = "Login is empty")
    private String login;
    private String name;
    @PastOrPresent(message = "Birthday can't be in the future")
    private LocalDate birthday;
}
