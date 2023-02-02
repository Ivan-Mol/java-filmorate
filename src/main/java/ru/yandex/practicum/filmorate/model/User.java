package ru.yandex.practicum.filmorate.model;


import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Data
@Builder
public class User {
    private int id;
    @Email(message = "Email is incorrect")
    @NotBlank(message = "Email is empty")
    private String email;
    @NotBlank(message = "Login is empty")
    private String login;
    private String name;
    @PastOrPresent(message = "Birthday can't be in the future")
    private LocalDate birthday;
}
