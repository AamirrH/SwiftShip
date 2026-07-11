package com.code.prodapp.notificationservice.services;

import com.code.prodapp.notificationservice.DTOs.SendEmailRequestDTO;
import com.code.prodapp.notificationservice.events.FulfillmentEvent;
import com.code.prodapp.notificationservice.events.OrderConfirmedEvent;
import com.code.prodapp.notificationservice.events.TrackingEvent;
import com.code.prodapp.notificationservice.exceptions.EmailDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailEventListenerService {

    private static final String ORDER_CONFIRMED_EVENT = "ORDER_CONFIRMED";
    private static final String WAREHOUSE_ASSIGNED_EVENT = "WAREHOUSE_ASSIGNED";
    private static final String ROUTE_CALCULATED_EVENT = "ROUTE_CALCULATED";
    private static final String ETA_UPDATED_EVENT = "ETA_UPDATED";
    private static final String ORDER_DELIVERED_EVENT = "ORDER_DELIVERED";

    private final EmailService emailService;

    @KafkaListener(topics = "order-events", groupId = "notification-email-service")
    public void handleOrderEvent(OrderConfirmedEvent event) {
        if (ORDER_CONFIRMED_EVENT.equals(event.getEventType())) {
            sendEventEmail(
                    event.getCustomerEmail(),
                    event.getOrderNumber(),
                    "SwiftShip order confirmed",
                    "Your SwiftShip order #" + event.getOrderNumber() + " has been confirmed.",
                    "<p>Your SwiftShip order <strong>#" + event.getOrderNumber() + "</strong> has been confirmed.</p>"
            );
        }
    }

    @KafkaListener(topics = "fulfillment-events", groupId = "notification-email-service")
    public void handleFulfillmentEvent(FulfillmentEvent event) {
        if (WAREHOUSE_ASSIGNED_EVENT.equals(event.getEventType())) {
            sendEventEmail(
                    event.getCustomerEmail(),
                    event.getOrderNumber(),
                    "SwiftShip warehouse assigned",
                    "A warehouse has been assigned for your order #" + event.getOrderNumber() + ".",
                    "<p>A warehouse has been assigned for your order <strong>#" + event.getOrderNumber() + "</strong>.</p>"
            );
            return;
        }

        if (ROUTE_CALCULATED_EVENT.equals(event.getEventType())) {
            sendEventEmail(
                    event.getCustomerEmail(),
                    event.getOrderNumber(),
                    "SwiftShip route calculated",
                    "A delivery route has been calculated for your order #" + event.getOrderNumber() + ".",
                    "<p>A delivery route has been calculated for your order <strong>#" + event.getOrderNumber() + "</strong>.</p>"
            );
        }
    }

    @KafkaListener(topics = "tracking-events", groupId = "notification-email-service")
    public void handleTrackingEvent(TrackingEvent event) {
        if (ETA_UPDATED_EVENT.equals(event.getEventType())) {
            sendEventEmail(
                    event.getCustomerEmail(),
                    event.getOrderNumber(),
                    "SwiftShip ETA updated",
                    "Your order #" + event.getOrderNumber() + " has a new ETA of " + event.getEtaMinutes() + " minutes.",
                    "<p>Your order <strong>#" + event.getOrderNumber() + "</strong> has a new ETA of <strong>" + event.getEtaMinutes() + " minutes</strong>.</p>"
            );
            return;
        }

        if (ORDER_DELIVERED_EVENT.equals(event.getEventType())) {
            sendEventEmail(
                    event.getCustomerEmail(),
                    event.getOrderNumber(),
                    "SwiftShip order delivered",
                    "Your order #" + event.getOrderNumber() + " has been delivered.",
                    "<p>Your order <strong>#" + event.getOrderNumber() + "</strong> has been delivered.</p>"
            );
        }
    }

    private void sendEventEmail(
            String customerEmail,
            Long orderNumber,
            String subject,
            String textBody,
            String htmlBody
    ) {
        if (customerEmail == null || customerEmail.isBlank()) {
            log.info("Skipping email for order {} because customerEmail is missing", orderNumber);
            return;
        }

        SendEmailRequestDTO requestDTO = new SendEmailRequestDTO();
        requestDTO.setRecipients(List.of(customerEmail));
        requestDTO.setSubject(subject);
        requestDTO.setTextBody(textBody);
        requestDTO.setHtmlBody(htmlBody);

        try {
            emailService.sendEmail(requestDTO);
        } catch (EmailDeliveryException exception) {
            log.warn("Failed to send email for order {}", orderNumber, exception);
        }
    }
}
