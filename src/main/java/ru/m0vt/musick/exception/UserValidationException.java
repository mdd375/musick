package ru.m0vt.musick.exception;

import org.springframework.http.HttpStatus;

public class UserValidationException extends BaseException {
    public UserValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "USER_VALIDATION_ERROR");
    }
}