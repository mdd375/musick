-- V1__init_schema.sql
-- Initial database schema for Musick application

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Artist profile table (extends user)
CREATE TABLE artist_profile (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    bio TEXT,
    photo_url VARCHAR(255),
    CONSTRAINT fk_artist_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Albums table
CREATE TABLE albums (
    id BIGSERIAL PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    cover_url VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL,
    release_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_album_artist FOREIGN KEY (artist_id) REFERENCES artist_profile (user_id)
);

-- Tracks table
CREATE TABLE tracks (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    duration_sec INTEGER NOT NULL,
    track_number INTEGER NOT NULL,
    audio_url VARCHAR(255) NOT NULL,
    CONSTRAINT fk_track_album FOREIGN KEY (album_id) REFERENCES albums (id) ON DELETE CASCADE
);

-- Tags table
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Album-Tag relationship table (junction table for many-to-many)
CREATE TABLE album_tags (
    album_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (album_id, tag_id),
    CONSTRAINT fk_album_tags_album FOREIGN KEY (album_id) REFERENCES albums (id) ON DELETE CASCADE,
    CONSTRAINT fk_album_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

-- Reviews table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    favorite_tracks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_album FOREIGN KEY (album_id) REFERENCES albums (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Purchases table
CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    purchase_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchase_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_purchase_album FOREIGN KEY (album_id) REFERENCES albums (id)
);

-- Subscriptions table
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_artist FOREIGN KEY (artist_id) REFERENCES artist_profile (user_id) ON DELETE CASCADE,
    CONSTRAINT uk_user_artist UNIQUE (user_id, artist_id)
);

-- Initial data insertion
INSERT INTO tags (name) VALUES 
    ('Rock'), 
    ('Pop'),
    ('Jazz'),
    ('Classical'),
    ('Hip-Hop'),
    ('Electronic'),
    ('Folk'),
    ('Metal'),
    ('Ambient'),
    ('R&B');