package com.code.prodapp.trackingservice.services;

import com.code.prodapp.trackingservice.DTOs.TrackingSessionResponseDTO;
import com.code.prodapp.trackingservice.entities.TrackingSession;
import com.code.prodapp.trackingservice.exceptions.TrackingSessionNotFoundException;
import com.code.prodapp.trackingservice.repositories.TrackingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveTrackingStateService {

    private static final String TRACKING_ORDER_KEY_PREFIX = "tracking:order:";

    private final RedisTemplate<String, TrackingSessionResponseDTO> trackingRedisTemplate;
    private final TrackingSessionMapper trackingSessionMapper;
    private final TrackingSessionRepository trackingSessionRepository;

    public TrackingSessionResponseDTO saveLatestState(TrackingSession trackingSession) {
        TrackingSessionResponseDTO trackingSessionResponseDTO = trackingSessionMapper.mapToDTO(trackingSession);
        try {
            trackingRedisTemplate.opsForValue()
                    .set(buildTrackingOrderKey(trackingSessionResponseDTO.getOrderNumber()), trackingSessionResponseDTO);
        } catch (RuntimeException exception) {
            log.warn("Could not save live tracking state to Redis for order {}",
                    trackingSessionResponseDTO.getOrderNumber());
        }
        return trackingSessionResponseDTO;
    }

    public TrackingSessionResponseDTO getLatestState(Long orderNumber) {
        try {
            TrackingSessionResponseDTO cachedState = trackingRedisTemplate.opsForValue()
                    .get(buildTrackingOrderKey(orderNumber));
            if (cachedState != null) {
                return cachedState;
            }
        } catch (RedisConnectionFailureException exception) {
            log.warn("Could not fetch live tracking state from Redis for order {}", orderNumber);
        }

        TrackingSession trackingSession = trackingSessionRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new TrackingSessionNotFoundException(
                        "Tracking Session with order number #" + orderNumber + " not found"));

        return saveLatestState(trackingSession);
    }

    private String buildTrackingOrderKey(Long orderNumber) {
        return TRACKING_ORDER_KEY_PREFIX + orderNumber;
    }
}
