package com.code.prodapp.notificationservice.events;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FulfillmentEvent {

    private String eventType;
    private Long orderNumber;
    private Long customerId;
    private UUID warehouseId;
    private String warehouseName;
    private String city;
    private Long selectedRouteId;
    private Double totalDistance;
    private Double timeToReach;
    private String reasoning;
    private Double customerLatitude;
    private Double customerLongitude;
    private String customerAddress;
    private Double warehouseLatitude;
    private Double warehouseLongitude;

}
