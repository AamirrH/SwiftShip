package com.code.prodapp.trackingservice.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteCalculatedEvent {

    private String eventType;

    private Long orderNumber;
    private Long customerId;
    private UUID warehouseId;

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
