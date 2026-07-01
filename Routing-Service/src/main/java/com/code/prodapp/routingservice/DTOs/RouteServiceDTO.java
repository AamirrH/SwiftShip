package com.code.prodapp.routingservice.DTOs;

import lombok.*;

import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteServiceDTO {


    private Long routeId;
    private Double totalDistance;
    private Double timeToReach;


}
