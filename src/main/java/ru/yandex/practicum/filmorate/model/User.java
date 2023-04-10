package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User {
    private final Set<Long> friends = new HashSet<>();
    private Long id;
    @Email(message = "Email is incorrect")
    @NotBlank(message = "Email is empty")
    private String email;
    @NotBlank(message = "Login is empty")
    private String login;
    private String name;
    @PastOrPresent(message = "Birthday can't be in the future")
    private LocalDate birthday;

    public void addFriendID(Long friendID) {
        friends.add(friendID);
    }

    public void removeFriendID(Long friendID) {
        friends.remove(friendID);
    }
}
