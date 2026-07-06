package com.code.prodapp.trackingservice.DTOs;

import com.code.prodapp.trackingservice.entities.DriverStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDriverRequestDTO {

    private String driverName;
    private Integer phoneNumber;
    private DriverStatus driverStatus;

}
