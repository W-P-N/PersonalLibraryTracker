package com.wpn.personallibrarytracker.utility;

import com.wpn.personallibrarytracker.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler({
            UserAlreadyExistsException.class,
            UserNotFoundException.class,
            MethodArgumentNotValidException.class,
            BookNotFoundForUserException.class,
            NoteNotFoundException.class,
            ReviewAlreadyExistsException.class,
            ReviewNotFoundForTheBookException.class
    })
    public ResponseEntity<ErrorResponse> handleUserExceptions(Exception exception) {
        ErrorResponse errorResponse = null;
        if(
                exception instanceof UserNotFoundException ||
                        exception instanceof BookNotFoundForUserException ||
                        exception instanceof NoteNotFoundException ||
                        exception instanceof ReviewNotFoundForTheBookException
        ) {
            errorResponse = new ErrorResponse(
                    exception.getMessage(),
                    HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
        if(exception instanceof MethodArgumentNotValidException) {
            String message = Objects.requireNonNull(((MethodArgumentNotValidException) exception).getBindingResult()
                            .getFieldError())
                    .getField() + " " +
                    ((MethodArgumentNotValidException) exception).getBindingResult()
                            .getFieldError()
                            .getDefaultMessage();
            errorResponse = new ErrorResponse(
                    message,
                    HttpStatus.UNPROCESSABLE_CONTENT.value()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_CONTENT);
        }
        errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
