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
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storages.ReviewStorage;
import java.sql.*;
import java.util.List;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    ReviewMapper reviewMapper = new ReviewMapper();



    @Override
    public Review getReviewById(int reviewId) {
        log.debug("/getReviewById");
        Review review;
        try {
            String sql = "SELECT * FROM reviews WHERE review_id = ?";
            review = jdbcTemplate.queryForObject(sql, reviewMapper, reviewId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException( e.getMessage() +
                    String.format("GetError: review with id=%d not found.", reviewId));
        }
        return review;
    }

    @Override
    public List<Review> getAllReviews() {
        log.debug("/getAllReviews");
        String sql = "SELECT r.REVIEW_ID, r.FILM_ID, R.USER_ID, r.IS_POSITIVE, r.CONTENT, COALESCE(SUM(rld.IS_LIKE),0) AS useful "+
                "FROM REVIEWS r "+
                "LEFT JOIN review_like_dislike rld ON r.review_id = rld.review_id "+
                "GROUP BY r.REVIEW_ID, r.FILM_ID, R.USER_ID, r.IS_POSITIVE, r.CONTENT "+
                "ORDER BY useful DESC";
        List<Review> listOfReviews = jdbcTemplate.query(sql, reviewMapper);
        return  listOfReviews;
    }

    @Override
    public List<Review> getFilmReviewsSortedByUsefulness(int filmId, int count) {
        log.debug("/getFilmReviewsSortedByUsefulness");
        String sql = "SELECT r.REVIEW_ID, r.FILM_ID, R.USER_ID, r.IS_POSITIVE, r.CONTENT, COALESCE(SUM(rld.IS_LIKE),0) AS useful "+
                "FROM REVIEWS r "+
                "LEFT JOIN review_like_dislike rld ON r.review_id = rld.review_id "+
                "WHERE r.film_id = ?" +
                "GROUP BY r.REVIEW_ID, r.FILM_ID, R.USER_ID, r.IS_POSITIVE, r.CONTENT "+
                "ORDER BY useful DESC "+
                "LIMIT ?";
        List<Review> reviewsByFilmId = jdbcTemplate.query(sql,reviewMapper,filmId, count);
        return reviewsByFilmId;
    }

    @Override
    public Review addReview(Review review) {
        log.debug("/addReview");
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
            ps.setBoolean(3, review.getIsPositive());
            ps.setString(4, review.getContent());
            return ps;
        }, keyHolder);
        num = keyHolder.getKey();
        log.debug(num.toString());
        if (num != null) {
            review.setReviewId(num.intValue());
        } else throw new RuntimeException("Error: review was not added.");

        return getReviewById(review.getReviewId());
    }

    @Override
    public void addLikeOrDislikeToReview(int reviewId, int userId, boolean isLike) {
        log.debug("/addLikeOrDislikeToReview");
        assertReviewExists(reviewId);
        assertUserExists(userId);
        int rate= 1;
        if(!isLike) rate = -1;

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
        int id = review.getReviewId();
        try {
            sql = "SELECT * FROM reviews WHERE review_id = ?";
            existingReview = jdbcTemplate.queryForObject(sql, reviewMapper, id);
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
    public void removeReview(int reviewId) {
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
    public void removeLikeOrDislikeFromReview(int reviewId, int userId, boolean isLike) {
        log.debug("/removeLikeOrDislikeFromReview");
        String sql = "DELETE FROM review_like_dislike WHERE review_id = ? AND user_id = ? AND is_like = ?";
        int affected = jdbcTemplate.update(sql, reviewId, userId, isLike);
        if (affected == 0) {
            throw new NotFoundException(String.format("Error: like to review с id=%d not found.", reviewId));
        }
    }

    // Вспомогательные методы
    private void assertFilmExists(int filmId) {
        log.debug("/assertFilmExists");
        try {
            String sqlQuery = "SELECT id FROM films WHERE id = ?";
            jdbcTemplate.queryForObject(sqlQuery, Integer.class, filmId);
        } catch (DataAccessException e) {
            throw new NotFoundException("FilmGetError: film not found." + e.getMessage());
        }
    }

    private void assertUserExists(int userId) {
        log.debug("/assertUserExists");
        try {
            String sqlQuery = "SELECT id FROM users WHERE id = ?";
            jdbcTemplate.queryForObject(sqlQuery, Integer.class, userId);
        } catch (DataAccessException e) {
            throw new NotFoundException("UserGetError: user not found." + e.getMessage());
        }
    }

    private void assertReviewExists(int reviewId) {
        log.debug("/assertReviewExists");
        try {
            String sqlQuery = "SELECT review_id FROM reviews WHERE review_id = ?";
            jdbcTemplate.queryForObject(sqlQuery, Integer.class, reviewId);
        } catch (DataAccessException e) {
            throw new NotFoundException("ReviewGetError: review not found." + e.getMessage());
        }
    }

    private class ReviewMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            ResultSetMetaData meta = rs.getMetaData();
            Review review = new Review(rs.getInt("review_id"),
                    rs.getString("content"),
                    rs.getBoolean("is_positive"),
                    rs.getInt("film_id"),
                    rs.getInt("user_id"),
                    0);

            int numCol = meta.getColumnCount();
            for (int i = 1; i <= numCol; i++) {
                if(meta.getColumnName(i).equalsIgnoreCase("useful")) {
                    review.setUseful(rs.getInt("useful"));
                }
            }
            return review;
        }
    }
}