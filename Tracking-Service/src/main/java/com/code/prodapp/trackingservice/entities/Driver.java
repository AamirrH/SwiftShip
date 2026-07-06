package com.code.prodapp.trackingservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverId;

    @Column(nullable = false,name = "driver_name")
    private String driverName;

    @Column(nullable = false,name = "contact_number")
    private Integer phoneNumber;

    @Enumerated(EnumType.STRING)
    private DriverStatus driverStatus;


}
