package com.code.prodapp.notificationservice.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderConfirmedEvent {

    private String eventType;
    private Long orderNumber;
    private Long customerId;
    private String customerEmail;
    private String orderStatus;
    private String deliveryAddress;
    private Double deliveryLat;
    private Double deliveryLng;

}
