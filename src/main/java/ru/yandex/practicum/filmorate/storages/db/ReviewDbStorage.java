package ru.yandex.practicum.filmorate.storages.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storages.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Primary
@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review getReviewById(int reviewId) {
        Review review;
        try {
            String sqlQuery = "SELECT * FROM reviews WHERE review_id = ?";
            review = jdbcTemplate.queryForObject(sqlQuery, new ReviewMapper(), reviewId);

        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    String.format("Ошибка при получении: отзыв с id=%d не найден.", reviewId));
        }
        return review;
    }

    @Override
    public List<Review> getAllReviews() {

        String sql = "SELECT * FROM reviews";

        List<Review> listOfReviews = jdbcTemplate.query(sql, new ReviewMapper());

        return listOfReviews.stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> getFilmReviewsSortedByUsefulness(int filmId, int count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ?";

        List<Review> reviewsByFilmId = jdbcTemplate.query(sql,new ReviewMapper(),filmId);

        return reviewsByFilmId.stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Review addReview(Review review) {
        assertFilmExists(review.getFilmId());
        assertUserExists(review.getUserId());

        Number num;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "INSERT INTO reviews " +
                "(film_id, user_id, is_positive, content) " +
                "VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, review.getFilmId());
            ps.setInt(2, review.getUserId());
            ps.setBoolean(3, review.isPositive());
            ps.setString(4, review.getContent());
            return ps;
        }, keyHolder);

        num = keyHolder.getKey();
        if (num != null) {
            review.setReviewId(num.intValue());
        } else throw new RuntimeException("Ошибка: отзыв не был добавлен.");

        return getReviewById(num.intValue());

    }

    @Override
    public void addLikeOrDislikeToReview(int reviewId, int userId, boolean isLike) {
        assertReviewExists(reviewId);
        assertUserExists(userId);
        try {
            String sql = "INSERT INTO review_like_dislike (review_id, user_id, is_like) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, reviewId, userId, isLike);

        } catch (DuplicateKeyException e) {
            throw new ValidationException("Ошибка при добавлении лайка/дизлайка: лайк/дизлайк уже существует.");
        }
    }

    @Override
    public Review updateReview(Review review) {
        String sql;
        Review existingReview;
        int id = review.getReviewId();
        try {
            sql = "SELECT * FROM reviews WHERE review_id = ?";
            existingReview = jdbcTemplate.queryForObject(sql, new ReviewMapper(), id);

        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Ошибка: отзыв с id=%d не найден.", id));
        }

        String sqlQuery = "UPDATE reviews SET is_positive = ?, content = ? WHERE review_id = ?";
        jdbcTemplate.update(sqlQuery,
                review.isPositive(),
                review.getContent(),
                review.getReviewId()
        );
        if (existingReview == null) {
            throw new RuntimeException("Ошибка при обновлении отзыва.");
        }

        return getReviewById(review.getReviewId());
    }

    @Override
    public void removeReview(int reviewId) {
        String sql;
        Review review;
        try {
            sql = "SELECT * FROM reviews WHERE review_id = ?";
            review = jdbcTemplate.queryForObject(sql, new ReviewMapper(), reviewId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Ошибка: отзыв с id=%d не найден.", reviewId));
        }

        if (review != null) {
            sql = "DELETE FROM reviews WHERE review_id = ?";
            jdbcTemplate.update(sql, reviewId);
        } else {
            throw new RuntimeException("Ошибка при удалении отзыва.");
        }

    }

    @Override
    public void removeLikeOrDislikeFromReview(int reviewId, int userId, boolean isLike) {
        String sql = "DELETE FROM review_like_dislike WHERE review_id = ? AND user_id = ? AND is_like = ?";
        int affected = jdbcTemplate.update(sql, reviewId, userId, isLike);

        if (affected == 0) {
            throw new NotFoundException(String.format("Ошибка: лайк к отзыву с id=%d не найден.", reviewId));
        }
    }


    // Вспомогательные методы
    private void assertFilmExists(int filmId) {
        try {
            String sqlQuery = "SELECT id FROM films WHERE id = ?";
            jdbcTemplate.queryForObject(sqlQuery, Integer.class, filmId);

        } catch (DataAccessException e) {
            throw new NotFoundException("Ошибка при создании отзыва: фильм не найден.");
        }
    }

    private void assertUserExists(int userId) {
        try {
            String sqlQuery = "SELECT id FROM users WHERE id = ?";
            jdbcTemplate.queryForObject(sqlQuery, Integer.class, userId);

        } catch (DataAccessException e) {
            throw new NotFoundException("Ошибка при создании отзыва: пользователь не найден.");
        }
    }

    private void assertReviewExists(int reviewId) {
        try {
            String sqlQuery = "SELECT review_id FROM reviews WHERE review_id = ?";
            jdbcTemplate.queryForObject(sqlQuery, Integer.class, reviewId);

        } catch (DataAccessException e) {
            throw new NotFoundException("Ошибка при создании отзыва: пользователь не найден.");
        }
    }


    //Маппер класса Отзыв
    private class ReviewMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer usefulness;
            Review review = new Review(rs.getInt("review_id"),
                    rs.getString("content"),
                    rs.getBoolean("is_positive"),
                    rs.getInt("film_id"),
                    rs.getInt("user_id"),
                    0);
            int reviewId = review.getReviewId();

            // вычисляем рейтинг путем вычитания дизлайков из лайков.
            String sqlQuery = "SELECT (SELECT COUNT(review_id) FROM review_like_dislike WHERE is_like = true AND review_id = ?)" +
                    " - (SELECT COUNT(review_id) FROM review_like_dislike WHERE is_like = false AND review_id = ?)";
            usefulness = jdbcTemplate.queryForObject(sqlQuery, Integer.class, reviewId, reviewId);

            if (usefulness != null) {
                review.setUseful(usefulness);

            } else throw new RuntimeException("Ошибка при расчете рейтинга отзыва.");

            return review;
        }
    }

}
