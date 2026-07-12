package com.code.prodapp.trackingservice.services;

import com.code.prodapp.trackingservice.DTOs.TrackingSessionResponseDTO;
import com.code.prodapp.trackingservice.entities.TrackingSession;
import com.code.prodapp.trackingservice.exceptions.TrackingSessionNotFoundException;
import com.code.prodapp.trackingservice.repositories.TrackingSessionRepository;
import io.lettuce.core.RedisCommandExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveTrackingStateService {

    private final RedisTemplate<String, TrackingSessionResponseDTO> trackingRedisTemplate;
    private final TrackingSessionService trackingSessionService;
    private final TrackingSessionRepository trackingSessionRepository;
    // Every 5 seconds state is saved to DB, -> save to Redis too and get the latest state from redis


    // Saves to Redis after the Scheduler processes some operations
    public TrackingSessionResponseDTO saveLatestState(TrackingSession trackingSession) {
        TrackingSessionResponseDTO trackingSessionResponseDTO = trackingSessionService.mapToDTO(trackingSession);
        try {
            trackingRedisTemplate.opsForValue().set("tracking:order:"+trackingSessionResponseDTO.getOrderNumber(),trackingSessionResponseDTO);

        } catch (RedisConnectionFailureException e) {
            log.warn("Could not save to Redis right now...");

        }
        return trackingSessionResponseDTO;
    }

    // Get from Redis
     public TrackingSessionResponseDTO getLatestState(TrackingSession trackingSession) {
         TrackingSessionResponseDTO trackingSessionResponseDTO = trackingSessionService.mapToDTO(trackingSession);

         try {
             return trackingRedisTemplate.opsForValue().get("tracking:order:"+trackingSessionResponseDTO.getOrderNumber());
         } catch (RedisCommandExecutionException e) {
             log.warn("Could not get data from Redis right now... Fetching from database...");
             // Fetching from database
             TrackingSession trackingSession1 = trackingSessionRepository.findByOrderNumber(trackingSession.getOrderNumber())
                     .orElseThrow(() -> new TrackingSessionNotFoundException("Tracking Session with order number #"+trackingSession.getOrderNumber()+" not found"));
              // Save to Redis
             return saveLatestState(trackingSession1);



         }
     }









}
