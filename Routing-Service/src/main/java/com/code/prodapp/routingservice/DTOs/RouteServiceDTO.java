package com.code.prodapp.routingservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalTime;


@Data
public class RouteServiceDTO {


    private Long routeId;
    private Double totalDistance;
    private Double timeToReach;


}
