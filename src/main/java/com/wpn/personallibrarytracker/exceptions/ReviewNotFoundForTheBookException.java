package com.wpn.personallibrarytracker.exceptions;

public class ReviewNotFoundForTheBookException extends RuntimeException {
    public ReviewNotFoundForTheBookException(String message) {
        super(message);
    }
}
