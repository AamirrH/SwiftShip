package com.code.prodapp.warehouseservice.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class WarehouseResponseDTO {

    private UUID id;
    private String warehouseName;
    private String city;
    private double lat;
    private double lng;
    private Integer capacity;
    private boolean active;
}
