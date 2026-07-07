package com.code.prodapp.trackingservice.exceptions;

public class DriverNotFoundException extends RuntimeException {

    public DriverNotFoundException(String message) {
        super(message);
    }
}
