package com.code.prodapp.notificationservice.services;

import com.code.prodapp.notificationservice.DTOs.CreateNotificationRequestDTO;
import com.code.prodapp.notificationservice.entities.NotificationType;
import com.code.prodapp.notificationservice.events.FulfillmentEvent;
import com.code.prodapp.notificationservice.events.OrderConfirmedEvent;
import com.code.prodapp.notificationservice.events.TrackingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventService notificationEventService;

    @Test
    void handleOrderConfirmedEventCreatesInAppNotification() {
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setEventType("ORDER_CONFIRMED");
        event.setOrderNumber(63L);
        event.setCustomerId(7L);

        notificationEventService.handleOrderConfirmedEvent(event);

        ArgumentCaptor<CreateNotificationRequestDTO> captor =
                ArgumentCaptor.forClass(CreateNotificationRequestDTO.class);
        verify(notificationService).createNotification(captor.capture());

        CreateNotificationRequestDTO requestDTO = captor.getValue();
        assertThat(requestDTO.getCustomerId()).isEqualTo(7L);
        assertThat(requestDTO.getOrderNumber()).isEqualTo(63L);
        assertThat(requestDTO.getNotificationType()).isEqualTo(NotificationType.ORDER_CONFIRMED);
        assertThat(requestDTO.getTitle()).isEqualTo("Order confirmed");
        assertThat(requestDTO.getMessage()).contains("#63");
    }

    @Test
    void handleFulfillmentEventCreatesRouteCalculatedNotification() {
        FulfillmentEvent event = new FulfillmentEvent();
        event.setEventType("ROUTE_CALCULATED");
        event.setOrderNumber(63L);
        event.setCustomerId(7L);

        notificationEventService.handleFulfillmentEvent(event);

        ArgumentCaptor<CreateNotificationRequestDTO> captor =
                ArgumentCaptor.forClass(CreateNotificationRequestDTO.class);
        verify(notificationService).createNotification(captor.capture());

        CreateNotificationRequestDTO requestDTO = captor.getValue();
        assertThat(requestDTO.getNotificationType()).isEqualTo(NotificationType.ROUTE_CALCULATED);
        assertThat(requestDTO.getCustomerId()).isEqualTo(7L);
        assertThat(requestDTO.getOrderNumber()).isEqualTo(63L);
    }

    @Test
    void handleTrackingEventCreatesDeliveredNotification() {
        TrackingEvent event = new TrackingEvent();
        event.setEventType("ORDER_DELIVERED");
        event.setOrderNumber(63L);
        event.setCustomerId(7L);

        notificationEventService.handleTrackingEvent(event);

        ArgumentCaptor<CreateNotificationRequestDTO> captor =
                ArgumentCaptor.forClass(CreateNotificationRequestDTO.class);
        verify(notificationService).createNotification(captor.capture());

        CreateNotificationRequestDTO requestDTO = captor.getValue();
        assertThat(requestDTO.getNotificationType()).isEqualTo(NotificationType.ORDER_DELIVERED);
        assertThat(requestDTO.getMessage()).contains("delivered");
    }

    @Test
    void ignoresNotificationWhenCustomerIdIsMissing() {
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setEventType("ORDER_CONFIRMED");
        event.setOrderNumber(63L);

        notificationEventService.handleOrderConfirmedEvent(event);

        verify(notificationService, never()).createNotification(org.mockito.ArgumentMatchers.any());
    }
}
