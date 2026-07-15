package com.code.prodapp.notificationservice.services;

import com.code.prodapp.notificationservice.DTOs.CreateNotificationRequestDTO;
import com.code.prodapp.notificationservice.DTOs.NotificationResponseDTO;
import com.code.prodapp.notificationservice.entities.*;
import com.code.prodapp.notificationservice.exceptions.NotificationNotFoundException;
import com.code.prodapp.notificationservice.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponseDTO> getAllNotifications() {
        log.info("Getting all notifications");
        return notificationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public NotificationResponseDTO getNotificationById(Long notificationId) {
        log.info("Getting notification by id {}", notificationId);
        return mapToDTO(findNotificationEntityById(notificationId));
    }

    public List<NotificationResponseDTO> getNotificationsByCustomerId(Long customerId) {
        log.info("Getting notifications for customer {}", customerId);
        return notificationRepository.findAllByCustomerId(customerId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<NotificationResponseDTO> getUnreadNotificationsByCustomerId(Long customerId) {
        log.info("Getting unread notifications for customer {}", customerId);
        return notificationRepository.findAllByCustomerIdAndReadStatus(customerId, NotificationReadStatus.UNREAD)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<NotificationResponseDTO> getNotificationsByRecipient(String recipient) {
        String normalizedRecipient = normalizeRecipient(recipient);
        if (normalizedRecipient == null) {
            log.info("Skipping notification lookup because authenticated recipient is missing");
            return List.of();
        }

        log.info("Getting notifications for authenticated recipient {}", normalizedRecipient);
        return notificationRepository.findAllByRecipientIgnoreCase(normalizedRecipient)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<NotificationResponseDTO> getUnreadNotificationsByRecipient(String recipient) {
        String normalizedRecipient = normalizeRecipient(recipient);
        if (normalizedRecipient == null) {
            log.info("Skipping unread notification lookup because authenticated recipient is missing");
            return List.of();
        }

        log.info("Getting unread notifications for authenticated recipient {}", normalizedRecipient);
        return notificationRepository.findAllByRecipientIgnoreCaseAndReadStatus(
                        normalizedRecipient,
                        NotificationReadStatus.UNREAD
                )
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public NotificationResponseDTO createNotification(CreateNotificationRequestDTO requestDTO) {
        log.info("Creating notification for customer {}", requestDTO.getCustomerId());

        Notification notification = new Notification();
        notification.setCustomerId(requestDTO.getCustomerId());
        notification.setOrderNumber(requestDTO.getOrderNumber());
        notification.setNotificationType(requestDTO.getNotificationType());
        notification.setNotificationChannel(requestDTO.getNotificationChannel() == null
                ? NotificationChannel.IN_APP
                : requestDTO.getNotificationChannel());
        notification.setRecipient(requestDTO.getRecipient());
        notification.setTitle(requestDTO.getTitle());
        notification.setMessage(requestDTO.getMessage());
        notification.setReadStatus(NotificationReadStatus.UNREAD);
        notification.setDeliveryStatus(NotificationDeliveryStatus.NOT_REQUIRED);
        notification.setCreatedAt(Instant.now());

        return mapToDTO(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponseDTO markNotificationAsRead(Long notificationId) {
        log.info("Marking notification {} as read", notificationId);

        Notification notification = findNotificationEntityById(notificationId);
        notification.setReadStatus(NotificationReadStatus.READ);
        notification.setReadAt(Instant.now());

        return mapToDTO(notificationRepository.save(notification));
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification by id {}", notificationId);
        Notification notification = findNotificationEntityById(notificationId);
        notificationRepository.delete(notification);
    }

    private Notification findNotificationEntityById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id " + notificationId));
    }

    private String normalizeRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            return null;
        }

        return recipient.trim();
    }

    private NotificationResponseDTO mapToDTO(Notification notification) {
        return new NotificationResponseDTO(
                notification.getNotificationId(),
                notification.getCustomerId(),
                notification.getOrderNumber(),
                notification.getNotificationType(),
                notification.getNotificationChannel(),
                notification.getRecipient(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReadStatus(),
                notification.getDeliveryStatus(),
                notification.getFailureReason(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getSentAt()
        );
    }
}
