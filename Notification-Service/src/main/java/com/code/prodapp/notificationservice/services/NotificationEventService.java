package com.code.prodapp.notificationservice.services;

import com.code.prodapp.notificationservice.DTOs.CreateNotificationRequestDTO;
import com.code.prodapp.notificationservice.entities.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventService {

    private static final String ORDER_CONFIRMED_EVENT = "ORDER_CONFIRMED";
    private static final String WAREHOUSE_ASSIGNED_EVENT = "WAREHOUSE_ASSIGNED";
    private static final String ROUTE_CALCULATED_EVENT = "ROUTE_CALCULATED";
    private static final String ETA_UPDATED_EVENT = "ETA_UPDATED";
    private static final String ORDER_DELIVERED_EVENT = "ORDER_DELIVERED";

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-events")
    public void handleOrderEvent(Map<String, Object> event) {
        String eventType = readString(event, "eventType");

        if (ORDER_CONFIRMED_EVENT.equals(eventType)) {
            createNotification(
                    event,
                    NotificationType.ORDER_CONFIRMED,
                    "Order confirmed",
                    "Your order #" + readLong(event, "orderNumber") + " has been confirmed."
            );
        }
    }

    @KafkaListener(topics = "fulfillment-events")
    public void handleFulfillmentEvent(Map<String, Object> event) {
        String eventType = readString(event, "eventType");

        if (WAREHOUSE_ASSIGNED_EVENT.equals(eventType)) {
            createNotification(
                    event,
                    NotificationType.WAREHOUSE_ASSIGNED,
                    "Warehouse assigned",
                    "A warehouse has been assigned for order #" + readLong(event, "orderNumber") + "."
            );
            return;
        }

        if (ROUTE_CALCULATED_EVENT.equals(eventType)) {
            createNotification(
                    event,
                    NotificationType.ROUTE_CALCULATED,
                    "Route calculated",
                    "A delivery route has been calculated for order #" + readLong(event, "orderNumber") + "."
            );
        }
    }

    @KafkaListener(topics = "tracking-events")
    public void handleTrackingEvent(Map<String, Object> event) {
        String eventType = readString(event, "eventType");

        if (ETA_UPDATED_EVENT.equals(eventType)) {
            createNotification(
                    event,
                    NotificationType.ETA_UPDATED,
                    "ETA updated",
                    "ETA updated for order #" + readLong(event, "orderNumber") + "."
            );
            return;
        }

        if (ORDER_DELIVERED_EVENT.equals(eventType)) {
            createNotification(
                    event,
                    NotificationType.ORDER_DELIVERED,
                    "Order delivered",
                    "Your order #" + readLong(event, "orderNumber") + " has been delivered."
            );
        }
    }

    private void createNotification(
            Map<String, Object> event,
            NotificationType notificationType,
            String title,
            String message
    ) {
        Long customerId = readLong(event, "customerId");
        Long orderNumber = readLong(event, "orderNumber");

        if (customerId == null) {
            log.info("Ignoring {} event without customerId for order {}", notificationType, orderNumber);
            return;
        }

        CreateNotificationRequestDTO requestDTO = new CreateNotificationRequestDTO();
        requestDTO.setCustomerId(customerId);
        requestDTO.setOrderNumber(orderNumber);
        requestDTO.setNotificationType(notificationType);
        requestDTO.setTitle(title);
        requestDTO.setMessage(message);

        notificationService.createNotification(requestDTO);
    }

    private String readString(Map<String, Object> event, String fieldName) {
        Object value = event.get(fieldName);
        return value == null ? null : value.toString();
    }

    private Long readLong(Map<String, Object> event, String fieldName) {
        Object value = event.get(fieldName);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }
}
