package com.code.prodapp.apigateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

// This is a GlobalFilter which affects all requests coming to the API Gateway
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(GlobalLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = request.getId();
        long startedAt = System.currentTimeMillis();
        // Pre-Filter -> Runs Before routing to the correct microservice and inspects/changes the REQUEST
        logger.info("Gateway incoming requestId={} method={} path={} uri={} remoteAddress={}",
                requestId,
                request.getMethod(),
                request.getPath().value(),
                request.getURI(),
                request.getRemoteAddress());

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    // Post-Global Filter -> Runs after the request has been routed to the correct microservice and response is returning to client
                    Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    logger.info("Gateway completed requestId={} method={} path={} status={} routeId={} routeUri={} durationMs={}",
                            requestId,
                            request.getMethod(),
                            request.getPath().value(),
                            statusCode,
                            route != null ? route.getId() : "NO_ROUTE_MATCHED",
                            route != null ? route.getUri() : "NO_ROUTE_URI",
                            System.currentTimeMillis() - startedAt);
                }));


    }

    @Override
    public int getOrder() {
        // Highest Priority, runs always first
        return Integer.MIN_VALUE;
    }
}
