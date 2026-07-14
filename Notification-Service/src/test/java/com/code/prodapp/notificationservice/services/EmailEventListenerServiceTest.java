package com.code.prodapp.notificationservice.services;

import com.code.prodapp.notificationservice.DTOs.SendEmailRequestDTO;
import com.code.prodapp.notificationservice.DTOs.SendEmailResponseDTO;
import com.code.prodapp.notificationservice.events.FulfillmentEvent;
import com.code.prodapp.notificationservice.events.OrderConfirmedEvent;
import com.code.prodapp.notificationservice.events.TrackingEvent;
import com.code.prodapp.notificationservice.exceptions.EmailDeliveryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailEventListenerServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailEventListenerService emailEventListenerService;

    @Test
    void handleOrderEventSendsConfirmationEmailThroughEmailService() {
        when(emailService.sendEmail(any())).thenReturn(sentResponse());

        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setEventType("ORDER_CONFIRMED");
        event.setOrderNumber(63L);
        event.setCustomerEmail("aamirr.1704@gmail.com");

        emailEventListenerService.handleOrderEvent(event);

        ArgumentCaptor<SendEmailRequestDTO> captor = ArgumentCaptor.forClass(SendEmailRequestDTO.class);
        verify(emailService).sendEmail(captor.capture());

        SendEmailRequestDTO requestDTO = captor.getValue();
        assertThat(requestDTO.getRecipients()).containsExactly("aamirr.1704@gmail.com");
        assertThat(requestDTO.getSubject()).isEqualTo("SwiftShip order confirmed");
        assertThat(requestDTO.getTextBody()).contains("#63");
        assertThat(requestDTO.getHtmlBody()).contains("<strong>#63</strong>");
    }

    @Test
    void handleFulfillmentEventSendsRouteCalculatedEmail() {
        when(emailService.sendEmail(any())).thenReturn(sentResponse());

        FulfillmentEvent event = new FulfillmentEvent();
        event.setEventType("ROUTE_CALCULATED");
        event.setOrderNumber(63L);
        event.setCustomerEmail("aamirr.1704@gmail.com");

        emailEventListenerService.handleFulfillmentEvent(event);

        ArgumentCaptor<SendEmailRequestDTO> captor = ArgumentCaptor.forClass(SendEmailRequestDTO.class);
        verify(emailService).sendEmail(captor.capture());

        SendEmailRequestDTO requestDTO = captor.getValue();
        assertThat(requestDTO.getSubject()).isEqualTo("SwiftShip route calculated");
        assertThat(requestDTO.getRecipients()).containsExactly("aamirr.1704@gmail.com");
    }

    @Test
    void handleTrackingEventSendsDeliveredEmail() {
        when(emailService.sendEmail(any())).thenReturn(sentResponse());

        TrackingEvent event = new TrackingEvent();
        event.setEventType("ORDER_DELIVERED");
        event.setOrderNumber(63L);
        event.setCustomerEmail("aamirr.1704@gmail.com");

        emailEventListenerService.handleTrackingEvent(event);

        ArgumentCaptor<SendEmailRequestDTO> captor = ArgumentCaptor.forClass(SendEmailRequestDTO.class);
        verify(emailService).sendEmail(captor.capture());

        SendEmailRequestDTO requestDTO = captor.getValue();
        assertThat(requestDTO.getSubject()).isEqualTo("SwiftShip order delivered");
        assertThat(requestDTO.getTextBody()).contains("delivered");
    }

    @Test
    void skipsEmailWhenCustomerEmailIsMissing() {
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setEventType("ORDER_CONFIRMED");
        event.setOrderNumber(63L);

        emailEventListenerService.handleOrderEvent(event);

        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void swallowsEmailDeliveryFailureSoKafkaListenerDoesNotCrash() {
        when(emailService.sendEmail(any()))
                .thenThrow(new EmailDeliveryException("Resend failed", new RuntimeException("boom")));

        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setEventType("ORDER_CONFIRMED");
        event.setOrderNumber(63L);
        event.setCustomerEmail("aamirr.1704@gmail.com");

        emailEventListenerService.handleOrderEvent(event);

        verify(emailService).sendEmail(any());
    }

    private SendEmailResponseDTO sentResponse() {
        return new SendEmailResponseDTO(
                "email_123",
                List.of("aamirr.1704@gmail.com"),
                "subject",
                "SENT",
                Instant.now()
        );
    }
}
