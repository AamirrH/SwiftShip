package com.code.prodapp.trackingservice.services;

import com.code.prodapp.trackingservice.DTOs.TrackingSessionResponseDTO;
import com.code.prodapp.trackingservice.entities.TrackingSession;
import com.code.prodapp.trackingservice.entities.TrackingStatus;
import com.code.prodapp.trackingservice.events.EtaUpdatedEvent;
import com.code.prodapp.trackingservice.events.OrderDeliveredEvent;
import com.code.prodapp.trackingservice.exceptions.TrackingSessionNotFoundException;
import com.code.prodapp.trackingservice.repositories.TrackingSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingSimulationService {

    private static final String TRACKING_EVENTS_TOPIC = "tracking-events";
    private static final String ETA_UPDATED_EVENT = "ETA_UPDATED";
    private static final String ORDER_DELIVERED_EVENT = "ORDER_DELIVERED";

    private final LiveTrackingStateService liveTrackingStateService;
    private final TrackingSessionRepository trackingSessionRepository;
    private final DriverService driverService;
    private final KafkaTemplate<String, Object> trackingKafkaTemplate;
    private final Double DELIVERY_SPEED = 30.0; // 30 km/hr or 0.00834 km/s

    // Driver does this, starts the delivery and sets status to TRANSIT
    @Transactional
    public void startDelivery(Long orderNumber) {
        TrackingSession trackingSession = trackingSessionRepository.findByOrderNumber(orderNumber).orElseThrow(
                () -> new TrackingSessionNotFoundException("Tracking Session for Order number " + orderNumber+" not found"));
        // If found, change status to TRANSIT
        trackingSession.setTrackingStatus(TrackingStatus.IN_TRANSIT);
        trackingSession.setUpdatedAt(Instant.now());
        trackingSessionRepository.save(trackingSession);

    }

    @Transactional
    public void pauseDelivery(Long orderNumber) {
        TrackingSession trackingSession = trackingSessionRepository.findByOrderNumber(orderNumber).orElseThrow(
                () -> new TrackingSessionNotFoundException("Tracking Session for Order number " + orderNumber+" not found"));
        // If found, change status to PAUSED
        trackingSession.setTrackingStatus(TrackingStatus.PAUSED);
        trackingSession.setUpdatedAt(Instant.now());
        trackingSessionRepository.save(trackingSession);

    }


    // @Schedule runs every 5 seconds when the application starts BUT the main application must have @EnableScheduling
    // Important Point -> The delivery should be simulated whether it is tracked.
    @Transactional
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
            // What if customer lives almost near to the warehouse? then remainingDistance and currentEtaMinutes might become negative so we take 0
            double remainingDistanceKm = Math.max(0.0,
                    trackingSession.getRemainingDistanceKm() - distanceCoveredIn5Seconds);
            double currentEtaMinutes = Math.max(0.0,
                    trackingSession.getCurrentEtaMinutes() - (5.0 / 60.0));

            trackingSession.setRemainingDistanceKm(remainingDistanceKm);
            trackingSession.setCurrentEtaMinutes(currentEtaMinutes);

            // progress -> distanceCovered/totalDistance
            double distanceCovered = trackingSession.getTotalDistanceKm()-trackingSession.getRemainingDistanceKm();
            double progress = Math.min(1.0, distanceCovered/trackingSession.getTotalDistanceKm());

            trackingSession.setCurrentLatitude(calculateCurrentLatitude(trackingSession, progress));
            trackingSession.setCurrentLongitude(calculateCurrentLongitude(trackingSession, progress));
            if (remainingDistanceKm == 0.0) {
                markDeliveryComplete(trackingSession);
            }
            trackingSession.setUpdatedAt(Instant.now());
            // Saves to Database
            trackingSessionRepository.save(trackingSession);
            // Saves to Redis
            liveTrackingStateService.saveLatestState(trackingSession);

            publishEtaUpdatedEvent(trackingSession);
            if (TrackingStatus.DELIVERED.equals(trackingSession.getTrackingStatus())) {
                publishOrderDeliveredEvent(trackingSession);
            }
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

    private void markDeliveryComplete(TrackingSession trackingSession) {
        trackingSession.setCurrentLatitude(trackingSession.getCustomerLatitude());
        trackingSession.setCurrentLongitude(trackingSession.getCustomerLongitude());
        trackingSession.setRemainingDistanceKm(0.0);
        trackingSession.setCurrentEtaMinutes(0.0);
        trackingSession.setTrackingStatus(TrackingStatus.DELIVERED);
        driverService.releaseDriver(trackingSession.getDriver());
    }

    private void publishEtaUpdatedEvent(TrackingSession trackingSession) {
        EtaUpdatedEvent etaUpdatedEvent = new EtaUpdatedEvent();
        etaUpdatedEvent.setEventType(ETA_UPDATED_EVENT);
        etaUpdatedEvent.setOrderNumber(trackingSession.getOrderNumber());
        etaUpdatedEvent.setCustomerId(trackingSession.getCustomerId());
        etaUpdatedEvent.setCustomerEmail(trackingSession.getCustomerEmail());
        etaUpdatedEvent.setDriverId(trackingSession.getDriver().getDriverId());
        etaUpdatedEvent.setCurrentLatitude(trackingSession.getCurrentLatitude());
        etaUpdatedEvent.setCurrentLongitude(trackingSession.getCurrentLongitude());
        etaUpdatedEvent.setRemainingDistanceKm(trackingSession.getRemainingDistanceKm());
        etaUpdatedEvent.setEtaMinutes(trackingSession.getCurrentEtaMinutes());
        etaUpdatedEvent.setTrackingStatus(trackingSession.getTrackingStatus().name());
        etaUpdatedEvent.setUpdatedAt(trackingSession.getUpdatedAt());

        log.info("Kafka send topic={} eventType={} orderNumber={} etaMinutes={} remainingDistanceKm={}",
                TRACKING_EVENTS_TOPIC,
                etaUpdatedEvent.getEventType(),
                etaUpdatedEvent.getOrderNumber(),
                etaUpdatedEvent.getEtaMinutes(),
                etaUpdatedEvent.getRemainingDistanceKm());
        trackingKafkaTemplate.send(
                TRACKING_EVENTS_TOPIC,
                trackingSession.getOrderNumber().toString(),
                etaUpdatedEvent
        );
    }

    private void publishOrderDeliveredEvent(TrackingSession trackingSession) {
        OrderDeliveredEvent orderDeliveredEvent = new OrderDeliveredEvent();
        orderDeliveredEvent.setEventType(ORDER_DELIVERED_EVENT);
        orderDeliveredEvent.setOrderNumber(trackingSession.getOrderNumber());
        orderDeliveredEvent.setCustomerId(trackingSession.getCustomerId());
        orderDeliveredEvent.setCustomerEmail(trackingSession.getCustomerEmail());
        orderDeliveredEvent.setDriverId(trackingSession.getDriver().getDriverId());
        orderDeliveredEvent.setDeliveredLatitude(trackingSession.getCurrentLatitude());
        orderDeliveredEvent.setDeliveredLongitude(trackingSession.getCurrentLongitude());
        orderDeliveredEvent.setTrackingStatus(trackingSession.getTrackingStatus().name());
        orderDeliveredEvent.setDeliveredAt(trackingSession.getUpdatedAt());

        log.info("Kafka send topic={} eventType={} orderNumber={} driverId={}",
                TRACKING_EVENTS_TOPIC,
                orderDeliveredEvent.getEventType(),
                orderDeliveredEvent.getOrderNumber(),
                orderDeliveredEvent.getDriverId());
        trackingKafkaTemplate.send(
                TRACKING_EVENTS_TOPIC,
                trackingSession.getOrderNumber().toString(),
                orderDeliveredEvent
        );
    }










}
