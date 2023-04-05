package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
        log.debug("Review was added");
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
        log.debug("Review with id={} was deleted", id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(@PathVariable int id, @PathVariable int userId) {
        log.debug("/addLikeToReview");
        reviewService.addLikeOrDislikeToReview(id, userId, true);
        log.debug("Review with id={}: like was adde from user with id={}.", id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(@PathVariable int id, @PathVariable int userId) {
        log.debug("/addDislikeToReview");
        reviewService.addLikeOrDislikeToReview(id, userId, false);
        log.debug("Review with id={}: dislike was adde from user with id={}.", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.debug("/removeLike");
        reviewService.removeLikeOrDislikeFromReview(id, userId, true);
        log.debug("Review with id={}: like was deleted from user with id={}.", id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDisLike(@PathVariable int id, @PathVariable int userId) {
        log.debug("/removeDisLike");
        reviewService.removeLikeOrDislikeFromReview(id, userId, false);
        log.debug("Review with id={}: dislike was deleted from user with id={}.", id, userId);
    }
}
