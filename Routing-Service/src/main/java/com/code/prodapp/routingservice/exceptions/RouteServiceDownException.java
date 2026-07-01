package com.code.prodapp.routingservice.exceptions;

public class RouteServiceDownException extends RuntimeException {
    public RouteServiceDownException(String message) {
        super(message);
    }
}
