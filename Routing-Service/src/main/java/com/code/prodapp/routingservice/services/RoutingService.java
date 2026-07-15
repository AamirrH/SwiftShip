package com.code.prodapp.routingservice.services;

import com.code.prodapp.routingservice.DTOs.*;
import com.code.prodapp.routingservice.clients.RouteFeignClient;
import com.code.prodapp.routingservice.entities.SelectedRoute;
import com.code.prodapp.routingservice.events.RouteCalculatedEvent;
import com.code.prodapp.routingservice.events.WarehouseAssignedEvent;
import com.code.prodapp.routingservice.exceptions.RouteNotFoundException;
import com.code.prodapp.routingservice.exceptions.RouteServiceDownException;
import com.code.prodapp.routingservice.repositories.RouteRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    private static final String FULFILLMENT_EVENTS_TOPIC = "fulfillment-events";
    private static final String WAREHOUSE_ASSIGNED_EVENT = "WAREHOUSE_ASSIGNED";
    private static final String ROUTE_CALCULATED_EVENT = "ROUTE_CALCULATED";
    private static final double FALLBACK_ROAD_DISTANCE_FACTOR = 1.35;
    private static final double FALLBACK_SPEED_KMPH = 45.0;

    @Value("${osr.api.key}")
    private String apiKey;
    private final RouteFeignClient routeFeignClient;
    @Value("${vehicle.type}")
    private String drivingProfile;
    private final ModelMapper modelMapper;
    private final ChatClient chatClient;
    private final Integer NUMBER_OF_FINAL_ROUTES = 3;
    private final Float WEIGHT_FACTOR = 1.6F;
    private final KafkaTemplate<String, RouteCalculatedEvent> routingKafkaTemplate;
    private final RouteRepository routeRepository;

    @Retry(name = "routingServiceRetry", fallbackMethod = "routeCalculationFallback")
    @CircuitBreaker(name = "routingCircuitBreaker", fallbackMethod = "routeCalculationFallback")
    @RateLimiter(name = "routingServiceRateLimiter")
    public List<RouteServiceDTO> getAllRoutes(RouteRequestDTO routeRequestDTO) {
        AtomicLong counter = new AtomicLong(1L);
        RouteResponseDTO routeResponseDTO = routeFeignClient.getAllRoutes(apiKey, drivingProfile, routeRequestDTO);
        return routeResponseDTO
                .getRoutes()
                .stream()
                .map(r -> {
                    RouteServiceDTO routeServiceDTO = new RouteServiceDTO();
                    routeServiceDTO.setRouteId(counter.getAndIncrement());
                    routeServiceDTO.setTotalDistance(r.getSummary().getDistance() / 1000.0);
                    routeServiceDTO.setTimeToReach(r.getSummary().getDuration() / 60.0);
                    return routeServiceDTO;
                })
                .collect(Collectors.toList());

    }

    public List<RouteServiceDTO> routeCalculationFallback(RouteRequestDTO routeRequestDTO, Throwable throwable) {
        log.warn("Route calculation fallback hit reason={}", throwable.getMessage());
        return List.of(buildFallbackRoute(routeRequestDTO));
    }

    @Transactional
    @KafkaListener(topics = FULFILLMENT_EVENTS_TOPIC)
    public void handleWarehouseAssignedEvent(WarehouseAssignedEvent warehouseAssignedEvent) {
        log.info("Kafka receive topic={} eventType={} orderNumber={} warehouseId={}",
                FULFILLMENT_EVENTS_TOPIC,
                warehouseAssignedEvent.getEventType(),
                warehouseAssignedEvent.getOrderNumber(),
                warehouseAssignedEvent.getWarehouseId());
        if (!WAREHOUSE_ASSIGNED_EVENT.equals(warehouseAssignedEvent.getEventType())) {
            return;
        }
        if (!isValidWarehouseAssignedEvent(warehouseAssignedEvent)) {
            log.warn("Skipping malformed warehouse assignment event orderNumber={} customerId={} warehouseId={}",
                    warehouseAssignedEvent.getOrderNumber(),
                    warehouseAssignedEvent.getCustomerId(),
                    warehouseAssignedEvent.getWarehouseId());
            return;
        }

        // Calculate route using the assigned warehouse.
        Double customerLatitude = warehouseAssignedEvent.getCustomerLatitude();
        Double customerLongitude = warehouseAssignedEvent.getCustomerLongitude();
        Double warehouseLatitude = warehouseAssignedEvent.getWarehouseLatitude();
        Double warehouseLongitude = warehouseAssignedEvent.getWarehouseLongitude();

        // Coordinates are [longitude, latitude]. Origin is warehouse, destination is customer.
        RouteRequestDTO routeRequestDTO = new RouteRequestDTO();
        routeRequestDTO.setCoordinates(List.of(
                List.of(warehouseLongitude, warehouseLatitude),
                List.of(customerLongitude, customerLatitude)
        ));
        AlternativeRoutes alternativeRoutes = new AlternativeRoutes(NUMBER_OF_FINAL_ROUTES, WEIGHT_FACTOR);
        routeRequestDTO.setAlternativeRoutes(alternativeRoutes);

        ModelRouteResponse modelRouteResponse = getShortestRoute(routeRequestDTO);

        SelectedRoute selectedRoute = new SelectedRoute();
        selectedRoute.setSelectedRouteId(modelRouteResponse.getSelectedRouteId());
        selectedRoute.setOrderId(warehouseAssignedEvent.getOrderNumber());
        selectedRoute.setCustomerId(warehouseAssignedEvent.getCustomerId());
        selectedRoute.setWarehouseId(warehouseAssignedEvent.getWarehouseId());
        selectedRoute.setCustomerAddress(warehouseAssignedEvent.getCustomerAddress());
        selectedRoute.setCustomerLng(customerLongitude);
        selectedRoute.setCustomerLat(customerLatitude);
        selectedRoute.setWarehouseLng(warehouseLongitude);
        selectedRoute.setWarehouseLat(warehouseLatitude);
        selectedRoute.setTotalDistance(modelRouteResponse.getTotalDistance());
        selectedRoute.setTimeToReach(modelRouteResponse.getTimeToReach());
        selectedRoute.setReasoning(modelRouteResponse.getReasoning());
        routeRepository.save(selectedRoute);
        log.info("Saved selected route for orderNumber={} selectedRouteId={} totalDistance={} timeToReach={}",
                selectedRoute.getOrderId(),
                selectedRoute.getSelectedRouteId(),
                selectedRoute.getTotalDistance(),
                selectedRoute.getTimeToReach());

        RouteCalculatedEvent routeCalculatedEvent = new RouteCalculatedEvent();
        routeCalculatedEvent.setEventType(ROUTE_CALCULATED_EVENT);
        routeCalculatedEvent.setOrderNumber(warehouseAssignedEvent.getOrderNumber());
        routeCalculatedEvent.setCustomerId(warehouseAssignedEvent.getCustomerId());
        routeCalculatedEvent.setCustomerEmail(warehouseAssignedEvent.getCustomerEmail());
        routeCalculatedEvent.setWarehouseId(warehouseAssignedEvent.getWarehouseId());
        routeCalculatedEvent.setSelectedRouteId(modelRouteResponse.getSelectedRouteId());
        routeCalculatedEvent.setTotalDistance(modelRouteResponse.getTotalDistance());
        routeCalculatedEvent.setTimeToReach(modelRouteResponse.getTimeToReach());
        routeCalculatedEvent.setReasoning(modelRouteResponse.getReasoning());
        routeCalculatedEvent.setCustomerLatitude(customerLatitude);
        routeCalculatedEvent.setCustomerLongitude(customerLongitude);
        routeCalculatedEvent.setCustomerAddress(warehouseAssignedEvent.getCustomerAddress());
        routeCalculatedEvent.setWarehouseLatitude(warehouseLatitude);
        routeCalculatedEvent.setWarehouseLongitude(warehouseLongitude);

        log.info("Kafka send topic={} eventType={} orderNumber={} selectedRouteId={}",
                FULFILLMENT_EVENTS_TOPIC,
                routeCalculatedEvent.getEventType(),
                routeCalculatedEvent.getOrderNumber(),
                routeCalculatedEvent.getSelectedRouteId());
        routingKafkaTemplate.send(
                FULFILLMENT_EVENTS_TOPIC,
                warehouseAssignedEvent.getOrderNumber().toString(),
                routeCalculatedEvent
        );
    }

    public ModelRouteResponse getShortestRoute(RouteRequestDTO routeRequestDTO) {
        List<RouteServiceDTO> routeServiceDTOS;
        try {
            routeServiceDTOS = getAllRoutes(routeRequestDTO);
        } catch (RuntimeException exception) {
            log.warn("External route calculation failed. Using local fallback route reason={}", exception.getMessage());
            routeServiceDTOS = List.of(buildFallbackRoute(routeRequestDTO));
        }

        String systemPrompt = """
                You are a Shortest-Route Finder Model, Your sole purpose is to find the shortest route between two 
                coordinates/list of coordinates. The parameters to find the shortest route are based on distance (given 
                in double-datatype "kilometers", time given in double-datatype "minutes", you would be provided a list of
                JSON Route object with the parameters :-
                    Long routeId;
                    Double totalDistance;
                    Double timeToReach;
                based on these parameters you have to find an optimal route, and also mention its routeId.
                There could be several variations where less distance more time
                OR more distance less time could be present. you have to provide an optimal route accordingly, and return
                the answer in the JSON format strictly which is the RouteServiceDTO format.
                Your sole purpose is finding the route upon being queried with the JSON data, nothing else,
                you will not entertain any other query from the client, and you shall not provide any help other than the sole function
                Also, keep in mind you have to provide only ONE answer. if there are tie-breakers, reason accordingly and provide the answer
                For every answer you have to provide a small, short, concise reasoning about why you picked this route,
                and the reasoning should be general not something like "I picked..." etc.
                
                Here are the list of routeServiceDTOS {routeServiceDTOS}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(systemPrompt);

        // Validate before rendering the text
        routeServiceDTOS = routeServiceDTOS.stream()
                .filter(this::isValidRouteServiceDTO)
                .toList();
        if (routeServiceDTOS.isEmpty()) {
            throw new RouteNotFoundException("Routes not returned");
        }

        // Render the system prompt with the parameters
        String renderedText = promptTemplate.render(Map.of("routeServiceDTOS", routeServiceDTOS));

        ModelRouteResponse routeResponse = null;
        try {
            routeResponse = chatClient.prompt()
                    .user(renderedText)
                    .call()
                    .entity(ModelRouteResponse.class);
        } catch (Exception e) {
            log.warn("Route AI selection failed. Using local route selection reason={}", e.getMessage());
            return selectLocalRoute(routeServiceDTOS);
        }

        if (!isValidModelRouteResponse(routeResponse)) {
            log.warn("Route AI returned an incomplete route response. Using local route selection.");
            return selectLocalRoute(routeServiceDTOS);
        }

        return new ModelRouteResponse(routeResponse.getSelectedRouteId(), routeResponse.getTotalDistance(),
                routeResponse.getTimeToReach(), routeResponse.getReasoning());

    }

    private boolean isValidWarehouseAssignedEvent(WarehouseAssignedEvent warehouseAssignedEvent) {
        return warehouseAssignedEvent != null
                && warehouseAssignedEvent.getOrderNumber() != null
                && warehouseAssignedEvent.getCustomerId() != null
                && warehouseAssignedEvent.getWarehouseId() != null
                && isValidLatitude(warehouseAssignedEvent.getCustomerLatitude())
                && isValidLongitude(warehouseAssignedEvent.getCustomerLongitude())
                && isValidLatitude(warehouseAssignedEvent.getWarehouseLatitude())
                && isValidLongitude(warehouseAssignedEvent.getWarehouseLongitude());
    }

    private boolean isValidModelRouteResponse(ModelRouteResponse routeResponse) {
        return routeResponse != null
                && routeResponse.getSelectedRouteId() != null
                && routeResponse.getTotalDistance() != null
                && routeResponse.getTimeToReach() != null
                && Double.isFinite(routeResponse.getTotalDistance())
                && Double.isFinite(routeResponse.getTimeToReach())
                && routeResponse.getTotalDistance() > 0
                && routeResponse.getTimeToReach() > 0;
    }

    private boolean isValidRouteServiceDTO(RouteServiceDTO routeServiceDTO) {
        return routeServiceDTO != null
                && routeServiceDTO.getRouteId() != null
                && routeServiceDTO.getTotalDistance() != null
                && routeServiceDTO.getTimeToReach() != null
                && Double.isFinite(routeServiceDTO.getTotalDistance())
                && Double.isFinite(routeServiceDTO.getTimeToReach())
                && routeServiceDTO.getTotalDistance() > 0
                && routeServiceDTO.getTimeToReach() > 0;
    }

    private boolean isValidLatitude(double latitude) {
        return Double.isFinite(latitude) && latitude >= -90.0 && latitude <= 90.0;
    }

    private boolean isValidLongitude(double longitude) {
        return Double.isFinite(longitude) && longitude >= -180.0 && longitude <= 180.0;
    }

    private ModelRouteResponse selectLocalRoute(List<RouteServiceDTO> routeServiceDTOS) {
        RouteServiceDTO selectedRoute = routeServiceDTOS
                .stream()
                .min((left, right) -> {
                    int timeComparison = Double.compare(left.getTimeToReach(), right.getTimeToReach());
                    if (timeComparison != 0) {
                        return timeComparison;
                    }
                    return Double.compare(left.getTotalDistance(), right.getTotalDistance());
                })
                .orElseThrow(() -> new RouteNotFoundException("Route not found"));

        return new ModelRouteResponse(
                selectedRoute.getRouteId(),
                selectedRoute.getTotalDistance(),
                selectedRoute.getTimeToReach(),
                "Selected locally because external route optimization was unavailable."
        );
    }

    private RouteServiceDTO buildFallbackRoute(RouteRequestDTO routeRequestDTO) {
        double distanceKm = calculateFallbackDistanceKm(routeRequestDTO);
        double etaMinutes = Math.max(1.0, (distanceKm / FALLBACK_SPEED_KMPH) * 60.0);
        return new RouteServiceDTO(
                1L,
                round(distanceKm),
                round(etaMinutes)
        );
    }

    private double calculateFallbackDistanceKm(RouteRequestDTO routeRequestDTO) {
        if (routeRequestDTO == null || routeRequestDTO.getCoordinates() == null || routeRequestDTO.getCoordinates().size() < 2) {
            return 1.0;
        }

        List<Double> origin = routeRequestDTO.getCoordinates().get(0);
        List<Double> destination = routeRequestDTO.getCoordinates().get(1);
        if (origin == null || destination == null || origin.size() < 2 || destination.size() < 2) {
            return 1.0;
        }

        double warehouseLongitude = origin.get(0);
        double warehouseLatitude = origin.get(1);
        double customerLongitude = destination.get(0);
        double customerLatitude = destination.get(1);
        return Math.max(1.0, haversineKm(warehouseLatitude, warehouseLongitude, customerLatitude, customerLongitude)
                * FALLBACK_ROAD_DISTANCE_FACTOR);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double latitudeDistance = Math.toRadians(lat2 - lat1);
        double longitudeDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}
