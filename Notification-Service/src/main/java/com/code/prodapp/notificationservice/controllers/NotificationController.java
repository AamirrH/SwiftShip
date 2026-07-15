package com.code.prodapp.notificationservice.controllers;

import com.code.prodapp.notificationservice.DTOs.CreateNotificationRequestDTO;
import com.code.prodapp.notificationservice.DTOs.NotificationResponseDTO;
import com.code.prodapp.notificationservice.services.NotificationService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@RateLimiter(name = "notificationServiceRateLimiter")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        return ResponseEntity.ok(notificationService.getNotificationsByRecipient(userEmail));
    }

    @GetMapping("/me/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getMyUnreadNotifications(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsByRecipient(userEmail));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getNotificationById(notificationId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getNotificationsByCustomerId(customerId));
    }

    @GetMapping("/customer/{customerId}/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotificationsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsByCustomerId(customerId));
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody CreateNotificationRequestDTO requestDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(requestDTO));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDTO> markNotificationAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markNotificationAsRead(notificationId));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
