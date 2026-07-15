package com.code.prodapp.trackingservice.services;

import com.code.prodapp.trackingservice.DTOs.TrackingSessionResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingWebSocketPublisher {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByOrderNumber = new ConcurrentHashMap<>();

    public void register(Long orderNumber, WebSocketSession session) {
        sessionsByOrderNumber.computeIfAbsent(orderNumber, ignored -> ConcurrentHashMap.newKeySet())
                .add(session);
        log.info("Tracking WebSocket connected orderNumber={} sessionId={}", orderNumber, session.getId());
    }

    public void unregister(Long orderNumber, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByOrderNumber.get(orderNumber);
        if (sessions == null) {
            return;
        }

        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByOrderNumber.remove(orderNumber);
        }
        log.info("Tracking WebSocket disconnected orderNumber={} sessionId={}", orderNumber, session.getId());
    }

    public void publish(TrackingSessionResponseDTO trackingState) {
        Set<WebSocketSession> sessions = sessionsByOrderNumber.get(trackingState.getOrderNumber());
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(trackingState);
        } catch (JsonProcessingException exception) {
            log.warn("Could not serialize live tracking update for order {}", trackingState.getOrderNumber());
            return;
        }

        sessions.removeIf(session -> !sendMessage(session, payload, trackingState.getOrderNumber()));
    }

    private boolean sendMessage(WebSocketSession session, String payload, Long orderNumber) {
        if (!session.isOpen()) {
            return false;
        }

        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
            return true;
        } catch (IOException exception) {
            log.warn("Could not send live tracking update orderNumber={} sessionId={}", orderNumber, session.getId());
            return false;
        }
    }
}
