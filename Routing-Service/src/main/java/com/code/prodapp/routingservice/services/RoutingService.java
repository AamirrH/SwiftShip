package com.code.prodapp.routingservice.services;

import com.code.prodapp.routingservice.DTOs.*;
import com.code.prodapp.routingservice.clients.RouteFeignClient;
import com.code.prodapp.routingservice.entities.SelectedRoute;
import com.code.prodapp.routingservice.events.RouteCalculatedEvent;
import com.code.prodapp.routingservice.events.WarehouseAssignedEvent;
import com.code.prodapp.routingservice.exceptions.RouteNotFoundException;
import com.code.prodapp.routingservice.exceptions.RouteServiceDownException;
import com.code.prodapp.routingservice.repositories.RouteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class RoutingService {

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

    @Transactional
    @KafkaListener(topics = "warehouse-assigned")
    public void handleWarehouseAssignedEvent(WarehouseAssignedEvent warehouseAssignedEvent) {
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

        RouteCalculatedEvent routeCalculatedEvent = new RouteCalculatedEvent();
        routeCalculatedEvent.setOrderNumber(warehouseAssignedEvent.getOrderNumber());
        routeCalculatedEvent.setCustomerId(warehouseAssignedEvent.getCustomerId());
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

        routingKafkaTemplate.send("route-calculated", routeCalculatedEvent);
    }

    public ModelRouteResponse getShortestRoute(RouteRequestDTO routeRequestDTO) {
        List<RouteServiceDTO> routeServiceDTOS = getAllRoutes(routeRequestDTO);

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
        if (routeServiceDTOS.isEmpty()) {
            throw new RouteNotFoundException("Routes not returned");
        }

        if (routeServiceDTOS.contains(null)) {
            throw new IllegalArgumentException("A Route Service DTO is required");
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
            throw new RouteServiceDownException("Route Service is unavailable at this time. Please try again later.");
        }

        if (routeResponse == null) {
            throw new RouteNotFoundException("Route not found");
        }

        return new ModelRouteResponse(routeResponse.getSelectedRouteId(), routeResponse.getTotalDistance(),
                routeResponse.getTimeToReach(), routeResponse.getReasoning());

    }

}


