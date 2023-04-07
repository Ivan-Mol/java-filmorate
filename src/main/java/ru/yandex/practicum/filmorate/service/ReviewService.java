package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storages.EventStorage;
import ru.yandex.practicum.filmorate.storages.ReviewStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.model.EventType.REVIEW;
import static ru.yandex.practicum.filmorate.model.OperationType.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final FilmService filmService;
    private final ReviewStorage reviewStorage;
    private final EventStorage eventStorage;

    public Review getReviewById(int reviewId) {
        return reviewStorage.getReviewById(reviewId);
    }

    public List<Review> getAllReviews() {
        return reviewStorage.getAllReviews();
    }

    public List<Review> getFilmReviewsSortedByUsefulness(int filmId, int count) {
        return reviewStorage.getFilmReviewsSortedByUsefulness(filmId, count);
    }

    public Review addReview(Review review) {
        validate(review);
        filmService.getFilm(review.getFilmId());
        filmService.checkUserExists(review.getUserId());
        Review returnedReview = reviewStorage.addReview(review);
        eventStorage.addEvent(
                new Event(REVIEW, ADD, review.getUserId().longValue(), (long)returnedReview.getReviewId()));
        return returnedReview;    }

    public void addLikeOrDislikeToReview(int reviewId, int userId, boolean isLike) {
        filmService.checkUserExists(userId);
        reviewStorage.assertReviewExists(reviewId);
        reviewStorage.addLikeOrDislikeToReview(reviewId, userId, isLike);
    }

    public Review updateReview(Review review) {
        validate(review);
        Review returnedReview = reviewStorage.updateReview(review);
        eventStorage.addEvent(
                new Event(REVIEW, UPDATE, returnedReview.getUserId().longValue(), (long)returnedReview.getReviewId()));
        return returnedReview;
    }

    public void removeReview(int reviewId) {
        Review returnedReview = getReviewById(reviewId);
        eventStorage.addEvent(new Event(REVIEW, REMOVE, returnedReview.getUserId().longValue(), (long)reviewId));
        reviewStorage.removeReview(reviewId);
    }

    public void removeLikeOrDislikeFromReview(int reviewId, int userId, boolean isLike) {
        reviewStorage.removeLikeOrDislikeFromReview(reviewId, userId, isLike);
    }

    private void validate(Review review){
        if (review.getContent() == null || review.getContent().isEmpty() || review.getContent().length() > 1024) {
            throw new ValidationException("Review content invalid");
        }
    }

}