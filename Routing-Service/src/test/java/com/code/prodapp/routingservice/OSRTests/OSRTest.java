package com.code.prodapp.routingservice.OSRTests;

import com.code.prodapp.routingservice.DTOs.AlternativeRoutes;
import com.code.prodapp.routingservice.DTOs.RouteRequestDTO;
import com.code.prodapp.routingservice.clients.RouteFeignClient;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

@RequiredArgsConstructor
public class OSRTest {

    RouteRequestDTO routeRequestDTO = new RouteRequestDTO();
    AlternativeRoutes alternativeRoutes = new AlternativeRoutes(2,1.6F);
    String apiKey = "";
    String drivingProfile = "";

    @BeforeEach
    void setUp() {
        apiKey = System.getenv("osr.api.key");
        drivingProfile = System.getenv("vehicle.type");
        routeRequestDTO.setCoordinates(List.of(List.of(18.73,14.56),List.of(18.73,14.56)));
        routeRequestDTO.setAlternativeRoutes(alternativeRoutes);

    }




    @Test
    public void test_OSR(){
        System.out.println(getAllRoutes(apiKey,drivingProfile,routeRequestDTO).toString());
    }


}
