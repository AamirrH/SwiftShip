package com.code.prodapp.routingservice.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlternativeRoutes {

    @JsonProperty("target_count")
    private int targetCount;
    @JsonProperty("weight_factor")
    private float weightFactor;

}
