package com.code.prodapp.notificationservice.events;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TrackingEvent {

    private String eventType;
    private Long orderNumber;
    private Long customerId;
    private Long driverId;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double remainingDistanceKm;
    private Double etaMinutes;
    private Double deliveredLatitude;
    private Double deliveredLongitude;
    private String trackingStatus;
    private Instant updatedAt;
    private Instant deliveredAt;

}
