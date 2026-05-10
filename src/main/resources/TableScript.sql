DROP DATABASE IF EXISTS libarary_tracker;
CREATE DATABASE library_tracker;
USE library_tracker;

CREATE TABLE User(
     user_id INT AUTO_INCREMENT PRIMARY KEY,
     user_name VARCHAR(50),
     email VARCHAR(40) NOT NULL,
     password VARCHAR (30) NOT NULL
);

CREATE TABLE Book(
    book_id INT PRIMARY KEY auto_increment,
    title VARCHAR(52) NOT NULL,
    author VARCHAR(50),
    total_pages INT NOT NULL,
    user_id INT,
    FOREIGN KEY (user_id)
        REFERENCES User(user_id)
        ON DELETE CASCADE
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

INSERT INTO User(user_name, email, password)
VALUES
    ("test1", "test1@mail.com", "test1password"),
    ("test2", "test2@mail.com", "test2password");

INSERT INTO Book(title, author, total_pages, user_id)
VALUES
    ("The Kite Runner", "M Hosselini", 300, 1),
    ("The Wings Of Freedom", "Abdul Kalam", 209, 1),
    ("The 5 AM club", "Robin Sharma", 452, 2);