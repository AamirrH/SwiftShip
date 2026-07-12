package com.code.prodapp.orderservice.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeoapifyGeocodingResponseDTO {

    private List<GeoapifyGeocodingResultDTO> results;

}
