-- V3__remove_pages_read_in_session_field_from_reading_sessions.sql
ALTER TABLE books DROP INDEX isbn;
ALTER TABLE books ADD CONSTRAINT uq_user_isbn UNIQUE (user_id, isbn);