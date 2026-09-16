package com.wpn.personallibrarytracker.utility;

import com.wpn.personallibrarytracker.exceptions.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    public GlobalExceptionHandler(
            MessageSource messageSource
    ) {
        this.messageSource = messageSource;
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                messageSource.getMessage("EXCEPTIONS.SERVER_ERROR_EXCEPTION", null, LocaleContextHolder.getLocale()),
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

    @ExceptionHandler(ResourceNotFoundException.class)
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
                messageSource.getMessage("EXCEPTIONS.CONFLICT_DATABASE_EXCEPTION", null, LocaleContextHolder.getLocale()),
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidHttpRequests(HttpMessageNotReadableException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                messageSource.getMessage("EXCEPTIONS.MALFORMED_HTTP_REQUEST_EXCEPTION", null, LocaleContextHolder.getLocale()),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException exception
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                messageSource.getMessage("EXCEPTIONS.INVALID_CREDENTIALS", null, LocaleContextHolder.getLocale()),
                HttpStatus.UNAUTHORIZED.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(
            InvalidRefreshTokenException exception
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                messageSource.getMessage("EXCEPTIONS.INVALID_REFRESH_TOKEN", null, LocaleContextHolder.getLocale()),
                HttpStatus.UNAUTHORIZED.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}
