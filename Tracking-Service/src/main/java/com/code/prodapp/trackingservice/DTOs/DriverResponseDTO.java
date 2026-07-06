package com.code.prodapp.trackingservice.DTOs;

import com.code.prodapp.trackingservice.entities.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriverResponseDTO {

    private Long driverId;
    private String driverName;
    private Integer phoneNumber;
    private DriverStatus driverStatus;

}
