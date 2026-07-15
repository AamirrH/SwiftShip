package com.code.prodapp.apigateway.configs;

import com.code.prodapp.apigateway.filters.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class WebSocketRouteConfig {

    private final AuthenticationFilter authenticationFilter;

    public WebSocketRouteConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator trackingWebSocketRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route("Tracking-Service-WebSocket", route -> route
                        .path("/ws/tracking/**")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(filters -> filters.filter(authenticationFilter.apply(customerRoleConfig())))
                        .uri("lb://TRACKING-SERVICE"))
                .build();
    }

    private AuthenticationFilter.Config customerRoleConfig() {
        AuthenticationFilter.Config config = new AuthenticationFilter.Config();
        config.setRole("CUSTOMER");
        return config;
    }
}
