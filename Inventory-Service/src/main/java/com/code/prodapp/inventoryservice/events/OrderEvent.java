package com.code.prodapp.inventoryservice.events;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderEvent {

    private String eventType;
    private Long orderNumber;
    private Long customerId;
    private String customerEmail;
    private String deliveryAddress;
    private Double deliveryLat;
    private Double deliveryLng;
    private List<ItemHelper> orderedItems;

}
