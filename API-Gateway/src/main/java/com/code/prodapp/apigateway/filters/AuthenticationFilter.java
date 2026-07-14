package com.code.prodapp.apigateway.filters;

import com.code.prodapp.apigateway.services.JWTCheckerService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final JWTCheckerService  jwtCheckerService;

    public AuthenticationFilter(JWTCheckerService jwtCheckerService) {
        super(Config.class);
        this.jwtCheckerService = jwtCheckerService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) ->  {
            String path = exchange.getRequest().getPath().value();
            String requestId = exchange.getRequest().getId();
            logger.info("Gateway auth filter entered requestId={} method={} path={} requiredRole={}",
                    requestId,
                    exchange.getRequest().getMethod(),
                    path,
                    config.getRole());

            if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
                logger.info("Gateway auth filter allowing OPTIONS requestId={} path={}", requestId, path);
                return chain.filter(exchange);
            }

            String authToken = exchange.getRequest().getHeaders().getFirst("Authorization");
            if(authToken == null || !authToken.startsWith("Bearer ")) {
                // No Token Found, do not accept this request
                logger.warn("Gateway auth filter rejected missing bearer token requestId={} path={} requiredRole={}",
                        requestId,
                        path,
                        config.getRole());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            else{
                // Validate the JWT

                try {
                    String token = authToken.split("Bearer ")[1];
                    Long userId = jwtCheckerService.getUserIdFromToken(token);
                    String email = jwtCheckerService.getEmailFromToken(token);
                    String role = jwtCheckerService.getRoleFromToken(token);
                    logger.info("Gateway auth filter token decoded requestId={} path={} userId={} email={} role={} requiredRole={}",
                            requestId,
                            path,
                            userId,
                            email,
                            role,
                            config.getRole());

                    if (!hasRequiredRole(config.getRole(), role)) {
                        logger.warn("Gateway auth filter rejected role mismatch requestId={} path={} actualRole={} requiredRole={}",
                                requestId,
                                path,
                                role,
                                config.getRole());
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    ServerHttpRequest request = exchange
                            .getRequest()
                            .mutate()
                            .header("X-User-Id", String.valueOf(userId))
                            .header("X-User-Email", email)
                            .header("X-User-Role", role)
                            .build();
                    logger.info("Gateway auth filter forwarding requestId={} path={} userId={} role={}",
                            requestId,
                            path,
                            userId,
                            role);
                    return chain.filter(exchange.mutate().request(request).build());
                } catch (Exception e) {
                    logger.warn("Gateway auth filter rejected invalid token requestId={} path={} reason={}",
                            requestId,
                            path,
                            e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

            }
        });
    }

    private boolean hasRequiredRole(String requiredRole, String actualRole) {
        if (requiredRole == null || requiredRole.isBlank()) {
            return true;
        }
        if (actualRole == null || actualRole.isBlank()) {
            return false;
        }
        if (requiredRole.equalsIgnoreCase(actualRole)) {
            return true;
        }
        return "CUSTOMER".equalsIgnoreCase(requiredRole) && "ADMIN".equalsIgnoreCase(actualRole);
    }


    public static class Config {
        private String role;
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
