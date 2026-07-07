package com.code.prodapp.trackingservice.exceptions;

public class NoAvailableDriverException extends RuntimeException {

    public NoAvailableDriverException(String message) {
        super(message);
    }
}
