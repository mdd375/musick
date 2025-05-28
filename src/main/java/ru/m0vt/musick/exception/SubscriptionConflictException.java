package ru.m0vt.musick.exception;

import org.springframework.http.HttpStatus;

public class SubscriptionConflictException extends BaseException {
    public SubscriptionConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "SUBSCRIPTION_CONFLICT");
    }
}