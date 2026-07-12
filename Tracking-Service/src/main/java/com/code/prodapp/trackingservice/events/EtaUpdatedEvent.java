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
// Estimated Time of Arrival Fields - constantly being updated to let the customer know the progress
public class EtaUpdatedEvent {

    private String eventType;
    private Long orderNumber;
    private Long customerId;
    private String customerEmail;
    private Long driverId;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double remainingDistanceKm;
    private Double etaMinutes;
    private String trackingStatus;
    private Instant updatedAt;

}
