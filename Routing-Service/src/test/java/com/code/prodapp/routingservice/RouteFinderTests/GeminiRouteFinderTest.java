package com.code.prodapp.routingservice.RouteFinderTests;

import com.code.prodapp.routingservice.DTOs.RouteServiceDTO;
import com.code.prodapp.routingservice.services.RoutingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class GeminiRouteFinderTest {

    @Autowired
    private RoutingService routingService;

    List<RouteServiceDTO> routeServiceDTOList;


    @BeforeEach
    void setup() {
        routeServiceDTOList = new ArrayList<>();
        routeServiceDTOList.add(new RouteServiceDTO(1L,7298.3,579.6));
        routeServiceDTOList.add(new RouteServiceDTO(2L,6151.1,543.9));

    }

    @Test
    void findShortestPath(){
        System.out.println(routingService.getShortestRoute(routeServiceDTOList));
    }



}
