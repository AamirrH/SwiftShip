package com.code.prodapp.routingservice.clients;

import com.code.prodapp.routingservice.DTOs.RouteRequestDTO;
import com.code.prodapp.routingservice.DTOs.RouteResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "OpenRouteService",url = "https://api.openrouteservice.org")
public interface RouteFeignClient {

    @PostMapping("/v2/directions/{profile}")
    RouteResponseDTO getAllRoutes(@RequestHeader("Authorization") String apiKey,
                                   @PathVariable String profile,
                                   @RequestBody RouteRequestDTO routeRequestDTO);

}
