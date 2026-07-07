package com.code.prodapp.trackingservice.services;

import com.code.prodapp.trackingservice.entities.Driver;
import com.code.prodapp.trackingservice.entities.TrackingSession;
import com.code.prodapp.trackingservice.entities.TrackingStatus;
import com.code.prodapp.trackingservice.events.RouteCalculatedEvent;
import com.code.prodapp.trackingservice.repositories.TrackingSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TrackingSessionService {

    private final TrackingSessionRepository trackingSessionRepository;
    private final DriverService driverService;

    @Transactional
    @KafkaListener(topics = "fullfillment-events")
    public void handleRouteCalculatedEvent(RouteCalculatedEvent routeCalculatedEvent){
        if (!"ROUTE_CALCULATED".equals(routeCalculatedEvent.getEventType())) {
            return;
        }
        // Assign a driver
        Driver assignedDriver = driverService.driverAssignmentStrategy();
        TrackingSession trackingSession = createTrackingSession(routeCalculatedEvent,assignedDriver);
        // Save the session.
        trackingSessionRepository.save(trackingSession);


    }

    private TrackingSession createTrackingSession(RouteCalculatedEvent routeCalculatedEvent, Driver assignedDriver){
        Instant now = Instant.now();
        TrackingSession trackingSession = new TrackingSession();
        trackingSession.setOrderNumber(routeCalculatedEvent.getOrderNumber());
        trackingSession.setCustomerId(routeCalculatedEvent.getCustomerId());
        trackingSession.setWarehouseId(routeCalculatedEvent.getWarehouseId());
        trackingSession.setSelectedRouteId(routeCalculatedEvent.getSelectedRouteId());
        trackingSession.setDriver(assignedDriver);
        trackingSession.setWarehouseLatitude(routeCalculatedEvent.getWarehouseLatitude());
        trackingSession.setWarehouseLongitude(routeCalculatedEvent.getWarehouseLongitude());
        trackingSession.setCustomerLatitude(routeCalculatedEvent.getCustomerLatitude());
        trackingSession.setCustomerLongitude(routeCalculatedEvent.getCustomerLongitude());
        trackingSession.setCustomerAddress(routeCalculatedEvent.getCustomerAddress());

        // Driver starts from warehouse location
        trackingSession.setCurrentLatitude(routeCalculatedEvent.getWarehouseLatitude());
        trackingSession.setCurrentLongitude(routeCalculatedEvent.getWarehouseLongitude());

        trackingSession.setTotalDistanceKm(routeCalculatedEvent.getTotalDistance());
        trackingSession.setRemainingDistanceKm(routeCalculatedEvent.getTotalDistance());

        trackingSession.setInitialEtaMinutes(routeCalculatedEvent.getTimeToReach());
        trackingSession.setCurrentEtaMinutes(routeCalculatedEvent.getTimeToReach());

        trackingSession.setTrackingStatus(TrackingStatus.DRIVER_ASSIGNED);

        trackingSession.setCreatedAt(now);
        trackingSession.setUpdatedAt(now);

        return trackingSession;
    }




}
