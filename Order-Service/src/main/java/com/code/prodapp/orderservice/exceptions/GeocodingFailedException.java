package com.code.prodapp.orderservice.exceptions;

public class GeocodingFailedException extends RuntimeException {

    public GeocodingFailedException(String message) {
        super(message);
    }

    public GeocodingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
