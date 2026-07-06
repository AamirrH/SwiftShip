package com.code.prodapp.trackingservice.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDeliveredEvent {

    private String eventType;
    private Long orderNumber;
    private Long customerId;
    private Long driverId;
    private Double deliveredLatitude;
    private Double deliveredLongitude;
    private String trackingStatus;
    private Instant deliveredAt;

}
