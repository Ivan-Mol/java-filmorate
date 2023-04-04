package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storages.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;

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
        return reviewStorage.addReview(review);
    }

    public void addLikeOrDislikeToReview(int reviewId, int userId, boolean isLike) {
        reviewStorage.addLikeOrDislikeToReview(reviewId, userId, isLike);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void removeReview(int reviewId) {
        reviewStorage.removeReview(reviewId);
    }

    public void removeLikeOrDislikeFromReview(int reviewId, int userId, boolean isLike) {
        reviewStorage.removeLikeOrDislikeFromReview(reviewId, userId, isLike);
    }
}
