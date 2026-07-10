package com.code.prodapp.notificationservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_number")
    private Long orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_channel", nullable = false)
    private NotificationChannel notificationChannel;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "read_status", nullable = false)
    private NotificationReadStatus readStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private NotificationDeliveryStatus deliveryStatus;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "sent_at")
    private Instant sentAt;

}
