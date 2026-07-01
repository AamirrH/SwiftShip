package com.code.prodapp.routingservice.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelRouteResponse {

    private Long selectedRouteId;
    private Double totalDistance;
    private Double timeToReach;
    private String reasoning;

    @Override
    public String toString() {
        return "ModelRouteResponse{" +
                "selectedRouteId=" + selectedRouteId +
                ", totalDistance=" + totalDistance +
                ", timeToReach=" + timeToReach +
                ", reasoning='" + reasoning + '\'' +
                '}';
    }
}
