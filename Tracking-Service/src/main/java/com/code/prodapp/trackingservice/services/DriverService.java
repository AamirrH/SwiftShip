package com.code.prodapp.trackingservice.services;

import com.code.prodapp.trackingservice.DTOs.CreateDriverRequestDTO;
import com.code.prodapp.trackingservice.DTOs.DriverResponseDTO;
import com.code.prodapp.trackingservice.DTOs.UpdateDriverRequestDTO;
import com.code.prodapp.trackingservice.entities.Driver;
import com.code.prodapp.trackingservice.entities.DriverStatus;
import com.code.prodapp.trackingservice.exceptions.DriverNotFoundException;
import com.code.prodapp.trackingservice.exceptions.NoAvailableDriverException;
import com.code.prodapp.trackingservice.repositories.DriverRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;

    public List<DriverResponseDTO> getAllDrivers() {
        log.info("Getting all drivers");
        return driverRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public DriverResponseDTO getDriverById(Long driverId) {
        log.info("Getting driver by id {}", driverId);
        return mapToDTO(findDriverEntityById(driverId));
    }

    public List<DriverResponseDTO> getDriversByStatus(DriverStatus driverStatus) {
        log.info("Getting drivers by status {}", driverStatus);
        return driverRepository.findAllByDriverStatus(driverStatus)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public DriverResponseDTO createDriver(CreateDriverRequestDTO requestDTO) {
        log.info("Creating driver {}", requestDTO.getDriverName());

        Driver driver = new Driver();
        driver.setDriverName(requestDTO.getDriverName());
        driver.setPhoneNumber(requestDTO.getPhoneNumber());
        driver.setDriverStatus(requestDTO.getDriverStatus() == null ? DriverStatus.AVAILABLE : requestDTO.getDriverStatus());

        return mapToDTO(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponseDTO updateDriver(Long driverId, CreateDriverRequestDTO requestDTO) {
        log.info("Updating driver by id {}", driverId);

        Driver driver = findDriverEntityById(driverId);
        driver.setDriverName(requestDTO.getDriverName());
        driver.setPhoneNumber(requestDTO.getPhoneNumber());
        driver.setDriverStatus(requestDTO.getDriverStatus() == null ? DriverStatus.AVAILABLE : requestDTO.getDriverStatus());

        return mapToDTO(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponseDTO patchDriver(Long driverId, UpdateDriverRequestDTO requestDTO) {
        log.info("Patching driver by id {}", driverId);

        Driver driver = findDriverEntityById(driverId);

        if (requestDTO.getDriverName() != null) {
            driver.setDriverName(requestDTO.getDriverName());
        }
        if (requestDTO.getPhoneNumber() != null) {
            driver.setPhoneNumber(requestDTO.getPhoneNumber());
        }
        if (requestDTO.getDriverStatus() != null) {
            driver.setDriverStatus(requestDTO.getDriverStatus());
        }

        return mapToDTO(driverRepository.save(driver));
    }

    @Transactional
    public void deleteDriver(Long driverId) {
        log.info("Deleting driver by id {}", driverId);
        Driver driver = findDriverEntityById(driverId);
        driverRepository.delete(driver);
    }

    private Driver findDriverEntityById(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with id " + driverId));
    }

    private DriverResponseDTO mapToDTO(Driver driver) {
        return new DriverResponseDTO(
                driver.getDriverId(),
                driver.getDriverName(),
                driver.getPhoneNumber(),
                driver.getDriverStatus()
        );
    }

    private Integer getTotalDriver(){
        return Math.toIntExact(driverRepository.count());
    }

    // Serial Driver Assignment
    @Transactional
    public Driver driverAssignmentStrategy() {
        List<Driver> availableDrivers = driverRepository.findAllByDriverStatus(DriverStatus.AVAILABLE);

        if (availableDrivers.isEmpty()) {
            throw new NoAvailableDriverException("No available drivers found");
        }

        Driver driver = availableDrivers.getFirst();
        driver.setDriverStatus(DriverStatus.ENGAGED);
        return driverRepository.save(driver);
    }

}
