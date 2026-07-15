package com.code.prodapp.trackingservice.websocket;

import com.code.prodapp.trackingservice.DTOs.TrackingSessionResponseDTO;
import com.code.prodapp.trackingservice.services.LiveTrackingStateService;
import com.code.prodapp.trackingservice.services.TrackingWebSocketPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingWebSocketHandler extends TextWebSocketHandler {

    private static final UriTemplate TRACKING_URI_TEMPLATE = new UriTemplate("/ws/tracking/orders/{orderNumber}");
    private static final String ORDER_NUMBER_ATTRIBUTE = "orderNumber";

    private final LiveTrackingStateService liveTrackingStateService;
    private final TrackingWebSocketPublisher trackingWebSocketPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Optional<Long> orderNumber = resolveOrderNumber(session);
        if (orderNumber.isEmpty()) {
            session.close(CloseStatus.BAD_DATA.withReason("Order number is required"));
            return;
        }

        Long resolvedOrderNumber = orderNumber.get();
        session.getAttributes().put(ORDER_NUMBER_ATTRIBUTE, resolvedOrderNumber);
        trackingWebSocketPublisher.register(resolvedOrderNumber, session);

        TrackingSessionResponseDTO latestState = liveTrackingStateService.getLatestState(resolvedOrderNumber);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(latestState)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        getSessionOrderNumber(session)
                .ifPresent(orderNumber -> trackingWebSocketPublisher.unregister(orderNumber, session));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("Tracking WebSocket transport error sessionId={} reason={}", session.getId(), exception.getMessage());
        getSessionOrderNumber(session)
                .ifPresent(orderNumber -> trackingWebSocketPublisher.unregister(orderNumber, session));
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private Optional<Long> resolveOrderNumber(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return Optional.empty();
        }

        try {
            Map<String, String> variables = TRACKING_URI_TEMPLATE.match(uri.getPath());
            return Optional.of(Long.parseLong(variables.get("orderNumber")));
        } catch (RuntimeException exception) {
            log.warn("Could not resolve tracking WebSocket order number from uri={}", uri.getPath());
            return Optional.empty();
        }
    }

    private Optional<Long> getSessionOrderNumber(WebSocketSession session) {
        Object orderNumber = session.getAttributes().get(ORDER_NUMBER_ATTRIBUTE);
        if (orderNumber instanceof Long resolvedOrderNumber) {
            return Optional.of(resolvedOrderNumber);
        }
        return Optional.empty();
    }
}
