package com.code.prodapp.trackingservice.DTOs;

import com.code.prodapp.trackingservice.entities.TrackingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingSessionResponseDTO {

    private Long trackingId;
    private Long orderNumber;
    private Long customerId;
    private UUID warehouseId;
    private Long selectedRouteId;

    private Long driverId;
    private String driverName;

    private Double warehouseLatitude;
    private Double warehouseLongitude;
    private Double customerLatitude;
    private Double customerLongitude;
    private String customerAddress;

    private Double currentLatitude;
    private Double currentLongitude;
    private Double totalDistanceKm;
    private Double remainingDistanceKm;
    private Double initialEtaMinutes;
    private Double currentEtaMinutes;

    private TrackingStatus trackingStatus;
    private Instant createdAt;
    private Instant updatedAt;

}
