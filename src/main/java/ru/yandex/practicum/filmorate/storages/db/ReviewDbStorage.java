package ru.yandex.practicum.filmorate.storages.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storages.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review getReviewById(Long reviewId) {
        log.debug("/getReviewById");
        Review review;
        try {
            String sql = "SELECT * FROM reviews WHERE review_id = ?";
            review = jdbcTemplate.queryForObject(sql, this::mapRowToReview, reviewId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(e.getMessage() +
                    String.format("GetError: review with id=%d not found.", reviewId));
        }
        return review;
    }

    @Override
    public List<Review> getAllReviews() {
        log.debug("/getAllReviews");
        String sql = "SELECT r.REVIEW_ID, r.FILM_ID, R.USER_ID, r.IS_POSITIVE, r.CONTENT, COALESCE(SUM(rld.IS_LIKE),0) AS useful " +
                "FROM REVIEWS r " +
                "LEFT JOIN review_like_dislike rld ON r.review_id = rld.review_id " +
                "GROUP BY r.REVIEW_ID, r.FILM_ID, R.USER_ID, r.IS_POSITIVE, r.CONTENT " +
                "ORDER BY useful DESC";
        List<Review> listOfReviews = jdbcTemplate.query(sql, this::mapRowToReview);
        return listOfReviews;
    }

    @Override
    public List<Review> getFilmReviewsSortedByUsefulness(Long filmId, int count) {
        log.debug("/getFilmReviewsSortedByUsefulness");
        String sql = "SELECT r.REVIEW_ID, r.FILM_ID, R.USER_ID, r.IS_POSITIVE, r.CONTENT, COALESCE(SUM(rld.IS_LIKE),0) AS useful " +
                "FROM REVIEWS r " +
                "LEFT JOIN review_like_dislike rld ON r.review_id = rld.review_id " +
                "WHERE r.film_id = ?" +
                "GROUP BY r.REVIEW_ID, r.FILM_ID, R.USER_ID, r.IS_POSITIVE, r.CONTENT " +
                "ORDER BY useful DESC " +
                "LIMIT ?";
        List<Review> reviewsByFilmId = jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
        return reviewsByFilmId;
    }

    @Override
    public Review addReview(Review review) {
        log.debug("/addReview");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "INSERT INTO reviews " +
                "(film_id, user_id, is_positive, content) " +
                "VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, review.getFilmId());
            ps.setLong(2, review.getUserId());
            ps.setBoolean(3, review.getIsPositive());
            ps.setString(4, review.getContent());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        log.debug("generated id: {}", id);
        if (id != null) {
            review.setReviewId(id);
        } else throw new RuntimeException("Error: review was not added.");

        return getReviewById(review.getReviewId());
    }

    @Override
    public void addLikeOrDislikeToReview(Long reviewId, Long userId, boolean isLike) {
        log.debug("/addLikeOrDislikeToReview");
        int rate = 1;
        if (!isLike) rate = -1;

        try {
            String sql = "INSERT INTO review_like_dislike (review_id, user_id, is_like) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, reviewId, userId, rate);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("AddError like/dislike: like/dislike already exist." +
                    e.getMessage());
        }
    }

    @Override
    public Review updateReview(Review review) {
        log.debug("/updateReview");
        String sql;
        Review existingReview;
        Long id = review.getReviewId();
        try {
            sql = "SELECT * FROM reviews WHERE review_id = ?";
            existingReview = jdbcTemplate.queryForObject(sql, this::mapRowToReview, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(e.getMessage() + String.format("Error: review with id=%d not found.", id));
        }

        String sqlQuery = "UPDATE reviews SET is_positive = ?, content = ? WHERE review_id = ?";
        jdbcTemplate.update(sqlQuery,
                review.getIsPositive(),
                review.getContent(),
                review.getReviewId());

        if (existingReview == null) {
            throw new RuntimeException("UpdatingError review.");
        }
        return getReviewById(review.getReviewId());
    }

    @Override
    public void removeReview(Long reviewId) {
        log.debug("/removeReview");
        Review review = getReviewById(reviewId);

        if (review != null) {
            String sql = "DELETE FROM reviews WHERE review_id = ?";
            jdbcTemplate.update(sql, reviewId);
        } else {
            throw new RuntimeException("DeleteError review.");
        }
    }

    @Override
    public void removeLikeOrDislikeFromReview(Long reviewId, Long userId, boolean isLike) {
        log.debug("/removeLikeOrDislikeFromReview");
        String sql = "DELETE FROM review_like_dislike WHERE review_id = ? AND user_id = ? AND is_like = ?";
        int affected = jdbcTemplate.update(sql, reviewId, userId, isLike);
        if (affected == 0) {
            throw new NotFoundException(String.format("Error: like to review с id=%d not found.", reviewId));
        }
    }

    @Override
    public void assertReviewExists(Long reviewId) {
        log.debug("/assertReviewExists");
        try {
            String sqlQuery = "SELECT review_id FROM reviews WHERE review_id = ?";
            jdbcTemplate.queryForObject(sqlQuery, Long.class, reviewId);
        } catch (DataAccessException e) {
            throw new NotFoundException("ReviewGetError: review not found." + e.getMessage());
        }
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        Review review = new Review(rs.getInt("review_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getLong("film_id"),
                rs.getLong("user_id"),
                0);

        int numCol = meta.getColumnCount();
        for (int i = 1; i <= numCol; i++) {
            if (meta.getColumnName(i).equalsIgnoreCase("useful")) {
                review.setUseful(rs.getInt("useful"));
            }
        }
        return review;
    }
}