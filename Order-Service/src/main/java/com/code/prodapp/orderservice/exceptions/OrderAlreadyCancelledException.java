package com.code.prodapp.orderservice.exceptions;

public class OrderAlreadyCancelledException extends RuntimeException {
    public OrderAlreadyCancelledException(String message) {
        super(message);
    }
}
