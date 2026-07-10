package com.code.prodapp.notificationservice.DTOs;

import com.code.prodapp.notificationservice.entities.NotificationChannel;
import com.code.prodapp.notificationservice.entities.NotificationDeliveryStatus;
import com.code.prodapp.notificationservice.entities.NotificationReadStatus;
import com.code.prodapp.notificationservice.entities.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {

    private Long notificationId;
    private Long customerId;
    private Long orderNumber;
    private NotificationType notificationType;
    private NotificationChannel notificationChannel;
    private String recipient;
    private String title;
    private String message;
    private NotificationReadStatus readStatus;
    private NotificationDeliveryStatus deliveryStatus;
    private String failureReason;
    private Instant createdAt;
    private Instant readAt;
    private Instant sentAt;

}
