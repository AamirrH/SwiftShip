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
public class GpsPingEvent {

    private String eventType;
    private Long orderNumber;
    private Long driverId;
    private Double latitude;
    private Double longitude;
    private Instant timestamp;

}
