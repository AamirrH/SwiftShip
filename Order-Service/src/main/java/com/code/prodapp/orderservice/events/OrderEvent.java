package com.code.prodapp.orderservice.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderEvent {

    private Long orderNumber;
    private Long customerId;
    private String deliveryAddress;
    private Double deliveryLat;
    private Double deliveryLng;
    private List<ItemHelper> orderedItems;

}
