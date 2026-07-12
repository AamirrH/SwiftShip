package com.code.prodapp.trackingservice.services;

import com.code.prodapp.trackingservice.DTOs.TrackingSessionResponseDTO;
import com.code.prodapp.trackingservice.entities.Driver;
import com.code.prodapp.trackingservice.entities.TrackingSession;
import org.springframework.stereotype.Service;

@Service
public class TrackingSessionMapper {

    public TrackingSessionResponseDTO mapToDTO(TrackingSession trackingSession) {
        Driver driver = trackingSession.getDriver();

        return new TrackingSessionResponseDTO(
                trackingSession.getTrackingId(),
                trackingSession.getOrderNumber(),
                trackingSession.getCustomerId(),
                trackingSession.getWarehouseId(),
                trackingSession.getSelectedRouteId(),
                driver.getDriverId(),
                driver.getDriverName(),
                trackingSession.getWarehouseLatitude(),
                trackingSession.getWarehouseLongitude(),
                trackingSession.getCustomerLatitude(),
                trackingSession.getCustomerLongitude(),
                trackingSession.getCustomerAddress(),
                trackingSession.getCurrentLatitude(),
                trackingSession.getCurrentLongitude(),
                trackingSession.getTotalDistanceKm(),
                trackingSession.getRemainingDistanceKm(),
                trackingSession.getInitialEtaMinutes(),
                trackingSession.getCurrentEtaMinutes(),
                trackingSession.getTrackingStatus(),
                trackingSession.getCreatedAt(),
                trackingSession.getUpdatedAt()
        );
    }
}
