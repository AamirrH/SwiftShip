package com.code.prodapp.trackingservice.DTOs;

import com.code.prodapp.trackingservice.entities.DriverStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDriverRequestDTO {

    @NotBlank
    private String driverName;

    @NotNull
    private Integer phoneNumber;

    private DriverStatus driverStatus;

}
