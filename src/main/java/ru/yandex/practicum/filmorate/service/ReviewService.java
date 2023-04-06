package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storages.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final FilmService filmService;

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
        validate(review);
        filmService.getFilm(review.getFilmId());
        filmService.checkUserExists(review.getUserId());
        return reviewStorage.addReview(review);
    }

    public void addLikeOrDislikeToReview(int reviewId, int userId, boolean isLike) {
        filmService.checkUserExists(userId);
        reviewStorage.assertReviewExists(reviewId);
        reviewStorage.addLikeOrDislikeToReview(reviewId, userId, isLike);
    }

    public Review updateReview(Review review) {
        validate(review);
        return reviewStorage.updateReview(review);
    }

    public void removeReview(int reviewId) {
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
