package ru.yandex.practicum.filmorate.storages.mem;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review getReviewById(int reviewId);

    List<Review> getAllReviews();

    List<Review> getFilmReviewsSortedByUsefulness(int filmId, int count);

    Review addReview(Review review);

    void addLikeOrDislikeToReview(int reviewId, int userId, boolean isLike);

    Review updateReview(Review review);

    void removeReview(int reviewId);

    void removeLikeOrDislikeFromReview(int reviewId, int userId, boolean isLike);
}
