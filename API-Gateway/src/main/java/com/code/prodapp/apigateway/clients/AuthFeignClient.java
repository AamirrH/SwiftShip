package com.code.prodapp.apigateway.clients;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "Auth-service",path = "/auth")
public interface AuthFeignClient {






}
