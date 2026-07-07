package com.code.prodapp.trackingservice.exceptions;

public class TrackingSessionNotFoundException extends RuntimeException {

    public TrackingSessionNotFoundException(String message) {
        super(message);
    }
}
