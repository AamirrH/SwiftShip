package com.code.prodapp.orderservice.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoapifyGeocodingResultDTO {

    private Double lat;
    private Double lon;
    private String formatted;

}
