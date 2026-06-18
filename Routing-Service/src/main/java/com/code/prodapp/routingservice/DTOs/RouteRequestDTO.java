package com.code.prodapp.routingservice.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class RouteRequestDTO {

    private List<List<Double>> coordinates;
    @JsonProperty("alternative_routes")
    private AlternativeRoutes alternativeRoutes;


}
