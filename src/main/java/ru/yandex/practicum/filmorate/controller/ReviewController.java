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
    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) {
        log.debug("/getReviewById");
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getAllReviews(@RequestParam(required = false) Integer filmId,
                                      @RequestParam(defaultValue = "10") int count) {
        log.debug("/getAllReviews");
        if (filmId == null) {
            return reviewService.getAllReviews();
        }
        return reviewService.getFilmReviewsSortedByUsefulness(filmId, count);
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.debug("/addReview");
        log.debug("Добавлен новый отзыв");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.debug("/updateReview");
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable int id) {
        log.debug("/removeReview");
        reviewService.removeReview(id);
        log.debug("Удален отзыв с id={}", id);
    }
    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(@PathVariable int id, @PathVariable int userId) {
        log.debug("/addLikeToReview");
        reviewService.addValueToReview(id, userId, true);
        log.debug("К отзыву с id={} добавлен лайк пользователя с id={}.", id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(@PathVariable int id, @PathVariable int userId) {
        log.debug("/addDislikeToReview");
        reviewService.addValueToReview(id, userId, false);
        log.debug("К отзыву с id={} добавлен дизлайк пользователя с id={}.", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.debug("/removeLike");
        reviewService.removeValueFromReview(id, userId, true);
        log.debug("У отзыва с id={} удален лайк пользователя с id={}.", id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDisLike(@PathVariable int id, @PathVariable int userId) {
        log.debug("/removeDisLike");
        reviewService.removeValueFromReview(id, userId, false);
        log.debug("У отзыва с id={} удален дизлайк пользователя с id={}.", id, userId);
    }
}
