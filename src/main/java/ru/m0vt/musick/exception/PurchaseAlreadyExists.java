package ru.m0vt.musick.exception;

import org.springframework.http.HttpStatus;

public class PurchaseAlreadyExists extends BaseException {

    public PurchaseAlreadyExists(String message) {
        super(message, HttpStatus.CONFLICT, "PURCHASE_ALREADY_EXISTS");
    }
}
