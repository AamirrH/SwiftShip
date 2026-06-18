package com.code.prodapp.routingservice.services;

import com.code.prodapp.routingservice.DTOs.RouteRequestDTO;
import com.code.prodapp.routingservice.DTOs.RouteResponseDTO;
import com.code.prodapp.routingservice.DTOs.RouteServiceDTO;
import com.code.prodapp.routingservice.DTOs.Routes;
import com.code.prodapp.routingservice.clients.RouteFeignClient;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.RouteMatcher;

import java.util.List;
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

    public List<RouteServiceDTO> getAllRoutes(RouteRequestDTO routeRequestDTO) {
        AtomicLong counter = new AtomicLong(1L);
        RouteResponseDTO routeResponseDTO = routeFeignClient.getAllRoutes(apiKey,drivingProfile,routeRequestDTO);
        return routeResponseDTO
                .getRoutes()
                .stream()
                .map(r -> {
                    RouteServiceDTO routeServiceDTO = new RouteServiceDTO();
                    routeServiceDTO.setRouteId(counter.getAndIncrement());
                    routeServiceDTO.setTotalDistance(r.getSummary().getDistance()/1000.0);
                    routeServiceDTO.setTimeToReach(r.getSummary().getDuration()/60.0);
                    return routeServiceDTO;
                })
                .collect(Collectors.toList());

    }


}
