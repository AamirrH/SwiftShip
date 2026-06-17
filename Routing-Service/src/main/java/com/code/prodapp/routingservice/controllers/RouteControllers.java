package com.code.prodapp.routingservice.controllers;

import com.code.prodapp.routingservice.DTOs.RouteRequestDTO;
import com.code.prodapp.routingservice.DTOs.RouteResponseDTO;
import com.code.prodapp.routingservice.services.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteControllers {

    private final RoutingService routingService;

    @PostMapping
    public ResponseEntity<RouteResponseDTO> getAllRoutes(@RequestBody RouteRequestDTO routeRequestDTO) {
        return ResponseEntity.ok(routingService.getAllRoutes(routeRequestDTO));
    }


}
