package com.code.prodapp.trackingservice.repositories;

import com.code.prodapp.trackingservice.entities.Driver;
import com.code.prodapp.trackingservice.entities.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findAllByDriverStatus(DriverStatus driverStatus);

}
