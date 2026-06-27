package com.code.prodapp.orderservice.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderConfirmedEvent {

    private Long orderNumber;
    private Long customerId;
    private String orderStatus;
    private String deliveryAddress;
    private Double deliveryLat;
    private Double deliveryLng;

}
