package com.code.prodapp.trackingservice.repositories;

import com.code.prodapp.trackingservice.entities.TrackingSession;
import com.code.prodapp.trackingservice.entities.TrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingSessionRepository extends JpaRepository<TrackingSession, Long> {

    Optional<TrackingSession> findByOrderNumber(Long orderNumber);

    List<TrackingSession> findAllByTrackingStatus(TrackingStatus trackingStatus);

}
