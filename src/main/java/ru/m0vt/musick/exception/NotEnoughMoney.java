package ru.m0vt.musick.exception;

import org.springframework.http.HttpStatus;

public class NotEnoughMoney extends BaseException {

    public NotEnoughMoney(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED, "NOT_ENOUGH_MONEY");
    }
}
