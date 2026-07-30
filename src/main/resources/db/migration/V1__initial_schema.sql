-- V1__initial_schema.sql
-- Baseline schema for Personal Library Tracker
-- Tables created in dependency order: parents before children

-- ============================================================
-- 1. users — no dependencies
-- ============================================================
CREATE TABLE users (
                       user_id    INT AUTO_INCREMENT PRIMARY KEY,
                       user_name  VARCHAR(255) NOT NULL,
                       email      VARCHAR(255) NOT NULL UNIQUE,
                       password   VARCHAR(255) NOT NULL
);

-- ============================================================
-- 2. books — depends on users
-- ============================================================
CREATE TABLE books (
                       book_id     INT AUTO_INCREMENT PRIMARY KEY,
                       title       VARCHAR(255) NOT NULL,
                       author      VARCHAR(255) NOT NULL,
                       total_pages INT NOT NULL,
                       isbn        VARCHAR(255) UNIQUE,
                       cover_url   VARCHAR(255),
                       user_id     INT NOT NULL,
                       CONSTRAINT fk_book_user
                           FOREIGN KEY (user_id) REFERENCES users(user_id)
                               ON DELETE CASCADE
);

-- ============================================================
-- 3. reviews — depends on books
-- One review per book, enforced via UNIQUE on book_id
-- ============================================================
CREATE TABLE reviews (
                         review_id  INT AUTO_INCREMENT PRIMARY KEY,
                         content    VARCHAR(255) NOT NULL,
                         rating     INT NOT NULL,
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         book_id    INT NOT NULL UNIQUE,
                         CONSTRAINT fk_review_book
                             FOREIGN KEY (book_id) REFERENCES books(book_id)
                                 ON DELETE CASCADE
);

-- ============================================================
-- 4. reading_sessions — depends on books
-- pages_read_in_session intentionally left nullable/unconstrained —
-- scheduled for removal in favor of a LAG() window function query
-- (see backlog: ReadingSession architecture refactor)
-- ============================================================
CREATE TABLE reading_sessions (
                                  reading_session_id      INT AUTO_INCREMENT PRIMARY KEY,
                                  session_date_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  end_session_page_number INT NOT NULL,
                                  pages_read_in_session   INT,
                                  book_id                 INT NOT NULL,
                                  CONSTRAINT fk_reading_session_book
                                      FOREIGN KEY (book_id) REFERENCES books(book_id)
                                          ON DELETE CASCADE
);

-- ============================================================
-- 5. notes — depends on books
-- page_number intentionally nullable — notes may be general
-- thoughts not tied to any specific page
-- ============================================================
CREATE TABLE notes (
                       note_id     INT AUTO_INCREMENT PRIMARY KEY,
                       content     VARCHAR(255) NOT NULL,
                       created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       book_id     INT NOT NULL,
                       page_number INT,
                       CONSTRAINT fk_note_book
                           FOREIGN KEY (book_id) REFERENCES books(book_id)
                               ON DELETE CASCADE
);

-- ============================================================
-- 6. refresh_tokens — depends on users
-- ============================================================
CREATE TABLE refresh_tokens (
                                refresh_token_id INT AUTO_INCREMENT PRIMARY KEY,
                                token             VARCHAR(255) NOT NULL UNIQUE,
                                user_id           INT NOT NULL,
                                expiry_date       DATETIME NOT NULL,
                                CONSTRAINT fk_refresh_token_user
                                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                                        ON DELETE CASCADE
);