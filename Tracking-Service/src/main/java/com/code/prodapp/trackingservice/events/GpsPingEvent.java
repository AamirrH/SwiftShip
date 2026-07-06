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
// This Ping is sent as a GPS notification signaling drivers current location and timestamp
// GPSPing is sent into tracking service by the driver-app and ETAUpdated is calculated by that information
public class GpsPingEvent {

    private String eventType;
    private Long orderNumber;
    private Long driverId;
    private Double currentLatitude;
    private Double currentLongitude;
    private Instant timestamp;

}
