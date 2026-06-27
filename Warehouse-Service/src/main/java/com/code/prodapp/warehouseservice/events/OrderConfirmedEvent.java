package com.code.prodapp.warehouseservice.events;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderConfirmedEvent {

    private Long orderNumber;
    private Long customerId;
    private String orderStatus;
    private Double deliveryLat;
    private Double deliveryLng;

}
