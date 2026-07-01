package com.wpn.personallibrarytracker.exceptions;

public class BookNotFoundForUserException extends RuntimeException {
    public BookNotFoundForUserException(String message) {
        super(message);
    }
}
