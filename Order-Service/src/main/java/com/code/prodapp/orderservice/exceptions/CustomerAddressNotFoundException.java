package com.code.prodapp.orderservice.exceptions;

public class CustomerAddressNotFoundException extends RuntimeException {

    public CustomerAddressNotFoundException(String message) {
        super(message);
    }
}
