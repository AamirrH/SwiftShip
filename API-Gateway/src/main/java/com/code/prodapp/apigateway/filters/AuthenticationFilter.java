package com.code.prodapp.apigateway.filters;

import com.code.prodapp.apigateway.services.JWTCheckerService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JWTCheckerService  jwtCheckerService;

    public AuthenticationFilter(JWTCheckerService jwtCheckerService) {
        super(Config.class);
        this.jwtCheckerService = jwtCheckerService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) ->  {
            String authToken = exchange.getRequest().getHeaders().getFirst("Authorization");
            if(authToken == null || !authToken.startsWith("Bearer ")) {
                // No Token Found, do not accept this request
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

                    if (!hasRequiredRole(config.getRole(), role)) {
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
                    return chain.filter(exchange.mutate().request(request).build());
                } catch (Exception e) {
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
