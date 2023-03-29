create TABLE IF NOT EXISTS mpa
(
    id   BIGINT generated by default as identity primary key,
    name varchar(200) NOT NULL
    );

create TABLE IF NOT EXISTS films
(
    id           BIGINT generated by default as identity primary key,
    name         varchar(200) NOT NULL,
    description  varchar(200),
    duration     int          NOT NULL,
    release_date date         NOT NULL,
    mpa_id      BIGINT REFERENCES mpa(id)        NOT NULL
    );

create TABLE IF NOT EXISTS genres
(
    id   BIGINT generated by default as identity primary key,
    name varchar(200) NOT NULL
    );

create TABLE IF NOT EXISTS users
(
    id  BIGINT generated by default as identity primary key,
    login    varchar(200) NOT NULL,
    email    varchar(200) NOT NULL,
    name     varchar(200),
    birthday date NOT NULL
    );

create TABLE IF NOT EXISTS film_genres
(
    film_id  BIGINT REFERENCES films (id) ON delete CASCADE,
    genre_id int REFERENCES genres (id) ON delete CASCADE,
    PRIMARY KEY (film_id, genre_id)
    );

create TABLE IF NOT EXISTS likes
(
    film_id  BIGINT REFERENCES films (id) ON delete CASCADE,
    user_id BIGINT REFERENCES users (id) ON delete CASCADE,
    PRIMARY KEY (film_id, user_id)
    );

create TABLE IF NOT EXISTS friends
(
    user_id BIGINT REFERENCES users (id) ON delete CASCADE,
    friend_id BIGINT REFERENCES users (id) ON delete CASCADE
    );

 create TABLE IF NOT EXISTS events
 (
    id BIGINT generated by default as identity primary key,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    event_type varchar(50) NOT NULL,
    operation varchar(50) NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    entity_id BIGINT NOT NULL
 );
