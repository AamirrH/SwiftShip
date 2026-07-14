package com.code.prodapp.apigateway.controllers;

import com.code.prodapp.apigateway.dtos.PlatformHealthResponse;
import com.code.prodapp.apigateway.services.JWTCheckerService;
import com.code.prodapp.apigateway.services.PlatformHealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/platform")
public class PlatformHealthController {

    private final JWTCheckerService jwtCheckerService;
    private final PlatformHealthService platformHealthService;

    public PlatformHealthController(
            JWTCheckerService jwtCheckerService,
            PlatformHealthService platformHealthService
    ) {
        this.jwtCheckerService = jwtCheckerService;
        this.platformHealthService = platformHealthService;
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<PlatformHealthResponse>> getPlatformHealth(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        try {
            String token = authorizationHeader.substring("Bearer ".length());
            String role = jwtCheckerService.getRoleFromToken(token);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
            }
        } catch (Exception exception) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return platformHealthService.checkPlatformHealth()
                .map(ResponseEntity::ok);
    }
}
