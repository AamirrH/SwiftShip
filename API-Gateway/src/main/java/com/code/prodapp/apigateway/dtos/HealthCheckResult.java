package com.code.prodapp.apigateway.dtos;

public record HealthCheckResult(
        String name,
        String type,
        String target,
        String status,
        Integer statusCode,
        Long latencyMs,
        String message
) {
}
