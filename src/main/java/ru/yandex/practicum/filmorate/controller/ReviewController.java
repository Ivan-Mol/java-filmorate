package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    //Контроллер отзывов

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getAllReviews(@RequestParam(required = false) Integer filmId,
                                      @RequestParam(defaultValue = "10") int count) {
        if (filmId == null) {
            return reviewService.getAllReviews();
        }
        return reviewService.getFilmReviewsSortedByUsefulness(filmId, count);
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.debug("Добавлен новый фильм: {}");
        return reviewService.addReview(review);

    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }


    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable int id) {
        reviewService.removeReview(id);
        log.debug("Удален отзыв с id={}", id);
    }

    // Контроллеры лайков

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.addValueToReview(id, userId, true);
        log.debug("К отзыву с id={} добавлен лайк пользователя с id={}.", id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.addValueToReview(id, userId, false);
        log.debug("К отзыву с id={} добавлен дизлайк пользователя с id={}.", id, userId);
    }


    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeValueFromReview(id, userId, true);
        log.debug("У отзыва с id={} удален лайк пользователя с id={}.", id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDisLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeValueFromReview(id, userId, false);
        log.debug("У отзыва с id={} удален дизлайк пользователя с id={}.", id, userId);
    }
}
