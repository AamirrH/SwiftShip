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
public class EtaUpdatedEvent {

    private String eventType;
    private Long orderNumber;
    private Long customerId;
    private Long driverId;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double remainingDistanceKm;
    private Double etaMinutes;
    private String trackingStatus;
    private Instant updatedAt;

}
