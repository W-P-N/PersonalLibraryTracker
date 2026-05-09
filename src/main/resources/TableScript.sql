DROP DATABASE IF EXISTS libarary_tracker;
CREATE DATABASE library_tracker;
USE library_tracker;

CREATE TABLE Book(
     book_id INT PRIMARY KEY auto_increment,
     title VARCHAR(52) NOT NULL,
     author VARCHAR(50),
     total_pages INT NOT NULL
);


CREATE TABLE Reading_Session(
    reading_session_id INT AUTO_INCREMENT PRIMARY KEY,
    session_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_session_page_number INT,
    pages_read_in_session INT,
    book_id INT,
    FOREIGN KEY (book_id)
        REFERENCES Book(book_id)
        ON DELETE CASCADE
);

CREATE TABLE Review(
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(300),
    rating INT
        NOT NULL
        DEFAULT 3
        CHECK (rating > 0 AND rating <= 5),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    book_id INT,
    FOREIGN KEY (book_id)
        REFERENCES Book(book_id)
        ON DELETE CASCADE
);

CREATE TABLE Note(
    note_id INT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    book_id INT,
    FOREIGN KEY (book_id)
        REFERENCES Book(book_id)
        ON DELETE CASCADE
);

INSERT INTO Book(title, author, total_pages)
VALUES
    ("The Kite Runner", "M Hosselini", 300),
    ("The Wings Of Freedom", "Abdul Kalam", 209);