package com.code.prodapp.notificationservice.services;

import com.code.prodapp.notificationservice.DTOs.CreateNotificationRequestDTO;
import com.code.prodapp.notificationservice.entities.NotificationType;
import com.code.prodapp.notificationservice.events.FulfillmentEvent;
import com.code.prodapp.notificationservice.events.OrderConfirmedEvent;
import com.code.prodapp.notificationservice.events.TrackingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        if (ORDER_CONFIRMED_EVENT.equals(event.getEventType())) {
            createNotification(
                    event.getCustomerId(),
                    event.getOrderNumber(),
                    NotificationType.ORDER_CONFIRMED,
                    "Order confirmed",
                    "Your order #" + event.getOrderNumber() + " has been confirmed."
            );
        }
    }

    @KafkaListener(topics = "fulfillment-events")
    public void handleFulfillmentEvent(FulfillmentEvent event) {
        if (WAREHOUSE_ASSIGNED_EVENT.equals(event.getEventType())) {
            createNotification(
                    event.getCustomerId(),
                    event.getOrderNumber(),
                    NotificationType.WAREHOUSE_ASSIGNED,
                    "Warehouse assigned",
                    "A warehouse has been assigned for order #" + event.getOrderNumber() + "."
            );
            return;
        }

        if (ROUTE_CALCULATED_EVENT.equals(event.getEventType())) {
            createNotification(
                    event.getCustomerId(),
                    event.getOrderNumber(),
                    NotificationType.ROUTE_CALCULATED,
                    "Route calculated",
                    "A delivery route has been calculated for order #" + event.getOrderNumber() + "."
            );
        }
    }

    @KafkaListener(topics = "tracking-events")
    public void handleTrackingEvent(TrackingEvent event) {
        if (ETA_UPDATED_EVENT.equals(event.getEventType())) {
            createNotification(
                    event.getCustomerId(),
                    event.getOrderNumber(),
                    NotificationType.ETA_UPDATED,
                    "ETA updated",
                    "ETA updated for order #" + event.getOrderNumber() + "."
            );
            return;
        }

        if (ORDER_DELIVERED_EVENT.equals(event.getEventType())) {
            createNotification(
                    event.getCustomerId(),
                    event.getOrderNumber(),
                    NotificationType.ORDER_DELIVERED,
                    "Order delivered",
                    "Your order #" + event.getOrderNumber() + " has been delivered."
            );
        }
    }

    private void createNotification(
            Long customerId,
            Long orderNumber,
            NotificationType notificationType,
            String title,
            String message
    ) {
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
}
