package com.code.prodapp.routingservice.services;

import com.code.prodapp.routingservice.DTOs.RouteRequestDTO;
import com.code.prodapp.routingservice.DTOs.RouteResponseDTO;
import com.code.prodapp.routingservice.clients.RouteFeignClient;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class RoutingService {

    @Value("${osr.api.key}")
    private String apiKey;
    private final RouteFeignClient routeFeignClient;
    @Value("${vehicle.type}")
    private String drivingProfile;
    private final ModelMapper modelMapper;

    public RouteResponseDTO getAllRoutes(RouteRequestDTO routeRequestDTO) {
        return modelMapper
                .map(routeFeignClient.getAllRoutes(apiKey,drivingProfile,routeRequestDTO),RouteResponseDTO.class);
    }



}
