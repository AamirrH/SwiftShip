package com.code.prodapp.routingservice.OSRTests;

import com.code.prodapp.routingservice.DTOs.AlternativeRoutes;
import com.code.prodapp.routingservice.DTOs.RouteRequestDTO;
import com.code.prodapp.routingservice.clients.RouteFeignClient;
import com.code.prodapp.routingservice.services.RoutingService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
public class OSRTest {

    RouteRequestDTO routeRequestDTO = new RouteRequestDTO();
    AlternativeRoutes alternativeRoutes = new AlternativeRoutes(2,1.6F);
    String apiKey = System.getenv("osr.api.key");
    String drivingProfile = System.getenv("vehicle.type");
    @Autowired
    private RoutingService routingService;

    @BeforeEach
    void setUp() {
        routeRequestDTO.setCoordinates(List.of(List.of(18.73,14.56),List.of(18.73,14.56)));
        routeRequestDTO.setAlternativeRoutes(alternativeRoutes);

    }


    @Test
    public void test_OSR(){
        System.out.println(routingService.getAllRoutes(routeRequestDTO).toString());
    }


}
