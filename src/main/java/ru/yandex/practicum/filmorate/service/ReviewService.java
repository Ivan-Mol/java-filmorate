package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storages.ReviewStorage;
import ru.yandex.practicum.filmorate.storages.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.model.EventType.REVIEW;
import static ru.yandex.practicum.filmorate.model.OperationType.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final FilmService filmService;
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;

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
        filmService.getFilm(review.getFilmId());
        filmService.checkUserExists(review.getUserId());
        Review returnedReview = reviewStorage.addReview(review);
        userStorage.addEvent(REVIEW, ADD, review.getUserId(), returnedReview.getReviewId());
        return returnedReview;
    }

    public void addLikeOrDislikeToReview(int reviewId, int userId, boolean isLike) {
        filmService.checkUserExists(userId);
        reviewStorage.assertReviewExists(reviewId);
        reviewStorage.addLikeOrDislikeToReview(reviewId, userId, isLike);
    }

    public Review updateReview(Review review) {
        Review returnedReview = reviewStorage.updateReview(review);
        userStorage.addEvent(REVIEW, UPDATE, returnedReview.getUserId(), returnedReview.getReviewId());
        return returnedReview;
    }

    public void removeReview(int reviewId) {
        Review returnedReview = getReviewById(reviewId);
        userStorage.addEvent(REVIEW, REMOVE, returnedReview.getUserId(), reviewId);
        reviewStorage.removeReview(reviewId);
    }

    public void removeLikeOrDislikeFromReview(int reviewId, int userId, boolean isLike) {
        reviewStorage.removeLikeOrDislikeFromReview(reviewId, userId, isLike);
    }
}