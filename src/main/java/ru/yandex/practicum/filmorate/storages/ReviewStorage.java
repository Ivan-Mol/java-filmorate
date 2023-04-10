package ru.yandex.practicum.filmorate.storages;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review getReviewById(Long reviewId);

    List<Review> getAllReviews();

    List<Review> getFilmReviewsSortedByUsefulness(Long filmId, int count);

    Review addReview(Review review);

    void addLikeOrDislikeToReview(Long reviewId, Long userId, boolean isLike);

    Review updateReview(Review review);

    void removeReview(Long reviewId);

    void removeLikeOrDislikeFromReview(Long reviewId, Long userId, boolean isLike);

    void assertReviewExists(Long reviewId);
}
