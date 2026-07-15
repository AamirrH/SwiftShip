package com.code.prodapp.trackingservice.controllers;

import com.code.prodapp.trackingservice.DTOs.TrackingSessionResponseDTO;
import com.code.prodapp.trackingservice.services.TrackingSessionService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
@RateLimiter(name = "trackingServiceRateLimiter")
public class TrackingController {

    private final TrackingSessionService trackingSessionService;

    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<TrackingSessionResponseDTO> getTrackingSessionByOrderNumber(@PathVariable Long orderNumber) {
        return ResponseEntity.ok(trackingSessionService.getTrackingSessionByOrderNumber(orderNumber));
    }
}
