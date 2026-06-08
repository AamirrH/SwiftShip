package com.code.prodapp.apigateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {




    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) ->  {
            ServerHttpRequest request = exchange.getRequest();


        })
    }


    public static class Config {
        private String role;
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
