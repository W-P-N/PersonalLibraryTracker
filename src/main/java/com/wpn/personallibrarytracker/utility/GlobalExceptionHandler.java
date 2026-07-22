package com.wpn.personallibrarytracker.utility;

import com.wpn.personallibrarytracker.exceptions.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Environment environment;

    public GlobalExceptionHandler(
            Environment environment
    ) {
        this.environment = environment;
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                environment.getProperty("EXCEPTIONS.SERVER_ERROR_EXCEPTION"),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            ReviewAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponse> handleAlreadyExistsException(Exception exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ErrorResponse errorResponse = new ErrorResponse(
                message,
                HttpStatus.UNPROCESSABLE_CONTENT.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            BookNotFoundForUserException.class,
            NoteNotFoundException.class,
            ReviewNotFoundForTheBookException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(Exception exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handlerDbException(DataIntegrityViolationException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                environment.getProperty("EXCEPTIONS.CONFLICT_DATABASE_EXCEPTION"),
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidHttpRequests(HttpMessageNotReadableException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                environment.getProperty("EXCEPTIONS.MALFORMED_HTTP_REQUEST_EXCEPTION"),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
