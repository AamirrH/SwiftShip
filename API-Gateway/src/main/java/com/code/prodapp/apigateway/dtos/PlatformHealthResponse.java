package com.code.prodapp.apigateway.dtos;

import java.time.Instant;
import java.util.List;

public record PlatformHealthResponse(
        Instant checkedAt,
        String overallStatus,
        List<HealthCheckResult> checks
) {
}
