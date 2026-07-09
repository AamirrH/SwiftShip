package com.code.prodapp.trackingservice.services;

import com.code.prodapp.trackingservice.DTOs.TrackingSessionResponseDTO;
import com.code.prodapp.trackingservice.entities.TrackingSession;
import com.code.prodapp.trackingservice.entities.TrackingStatus;
import com.code.prodapp.trackingservice.exceptions.TrackingSessionNotFoundException;
import com.code.prodapp.trackingservice.repositories.TrackingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingSimulationService {

    private final TrackingSessionRepository trackingSessionRepository;
    private final Double DELIVERY_SPEED = 30.0; // 30 km/hr or 0.00834 km/s

    // Driver does this, starts the delivery and sets status to TRANSIT
    public void startDelivery(Long orderNumber) {
        TrackingSession trackingSession = trackingSessionRepository.findByOrderNumber(orderNumber).orElseThrow(
                () -> new TrackingSessionNotFoundException("Tracking Session for Order number " + orderNumber+" not found"));
        // If found, change status to TRANSIT
        trackingSession.setTrackingStatus(TrackingStatus.IN_TRANSIT);
        trackingSessionRepository.save(trackingSession);

    }

    public void pauseDelivery(Long orderNumber) {
        TrackingSession trackingSession = trackingSessionRepository.findByOrderNumber(orderNumber).orElseThrow(
                () -> new TrackingSessionNotFoundException("Tracking Session for Order number " + orderNumber+" not found"));
        // If found, change status to TRANSIT
        trackingSession.setTrackingStatus(TrackingStatus.PAUSED);
        return ;

    }


    // @Schedule runs every 5 seconds when the application starts BUT the main application must have @EnableScheduling
    // Important Point -> The delivery should be simulated whether it is tracked.
    @Scheduled(fixedRate = 5000)
    public void simulateActiveDeliveries() {
        Double distanceCoveredIn5Seconds = (DELIVERY_SPEED/3600.0)*5;
        List<TrackingSession> trackingSessionList = trackingSessionRepository
                .findAllByTrackingStatus(TrackingStatus.IN_TRANSIT);
        // If deliveries have not started, return.
        if(trackingSessionList.isEmpty()){
            return;
        }
        for (TrackingSession trackingSession : trackingSessionList) {
            trackingSession.setRemainingDistanceKm(
                    trackingSession.getRemainingDistanceKm()-distanceCoveredIn5Seconds);
            trackingSession.setCurrentEtaMinutes(trackingSession.getCurrentEtaMinutes()-(5.0/60.0));
            // progress -> distanceCovered/totalDistance
            double distanceCovered = trackingSession.getTotalDistanceKm()-trackingSession.getRemainingDistanceKm();
            double progress = distanceCovered/trackingSession.getTotalDistanceKm();
            trackingSession.setCurrentLatitude(calculateCurrentLatitude(trackingSession, progress));
            trackingSession.setCurrentLongitude(calculateCurrentLongitude(trackingSession, progress));
            trackingSession.setUpdatedAt(Instant.now());
            trackingSessionRepository.save(trackingSession);
        }



    }

    private Double calculateCurrentLatitude(TrackingSession trackingSession, double progress) {
        return trackingSession.getWarehouseLatitude()
                + (trackingSession.getCustomerLatitude() - trackingSession.getWarehouseLatitude()) * progress;
    }

    private Double calculateCurrentLongitude(TrackingSession trackingSession, double progress) {
        return trackingSession.getWarehouseLongitude()
                + (trackingSession.getCustomerLongitude() - trackingSession.getWarehouseLongitude()) * progress;
    }










}
