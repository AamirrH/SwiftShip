package com.code.prodapp.routingservice.controllers;

import com.code.prodapp.routingservice.DTOs.RouteRequestDTO;
import com.code.prodapp.routingservice.DTOs.RouteResponseDTO;
import com.code.prodapp.routingservice.DTOs.RouteServiceDTO;
import com.code.prodapp.routingservice.services.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteControllers {

    private final RoutingService routingService;

    @PostMapping
    public ResponseEntity<List<RouteServiceDTO>> getAllRoutes(@RequestBody RouteRequestDTO routeRequestDTO) {
        return ResponseEntity.ok(routingService.getAllRoutes(routeRequestDTO));
    }


}
