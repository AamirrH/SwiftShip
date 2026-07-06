package com.code.prodapp.trackingservice.controllers;

import com.code.prodapp.trackingservice.DTOs.CreateDriverRequestDTO;
import com.code.prodapp.trackingservice.DTOs.DriverResponseDTO;
import com.code.prodapp.trackingservice.DTOs.UpdateDriverRequestDTO;
import com.code.prodapp.trackingservice.entities.DriverStatus;
import com.code.prodapp.trackingservice.services.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public ResponseEntity<List<DriverResponseDTO>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponseDTO> getDriverById(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getDriverById(driverId));
    }

    @GetMapping("/status/{driverStatus}")
    public ResponseEntity<List<DriverResponseDTO>> getDriversByStatus(@PathVariable DriverStatus driverStatus) {
        return ResponseEntity.ok(driverService.getDriversByStatus(driverStatus));
    }

    @PostMapping
    public ResponseEntity<DriverResponseDTO> createDriver(@Valid @RequestBody CreateDriverRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverService.createDriver(requestDTO));
    }

    @PutMapping("/{driverId}")
    public ResponseEntity<DriverResponseDTO> updateDriver(
            @PathVariable Long driverId,
            @Valid @RequestBody CreateDriverRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(driverService.updateDriver(driverId, requestDTO));
    }

    @PatchMapping("/{driverId}")
    public ResponseEntity<DriverResponseDTO> patchDriver(
            @PathVariable Long driverId,
            @RequestBody UpdateDriverRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(driverService.patchDriver(driverId, requestDTO));
    }

    @DeleteMapping("/{driverId}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long driverId) {
        driverService.deleteDriver(driverId);
        return ResponseEntity.noContent().build();
    }

}
