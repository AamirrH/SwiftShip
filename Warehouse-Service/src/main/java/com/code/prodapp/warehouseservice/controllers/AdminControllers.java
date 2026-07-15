package com.code.prodapp.warehouseservice.controllers;

import com.code.prodapp.warehouseservice.DTOs.CreateWarehouseRequestDTO;
import com.code.prodapp.warehouseservice.DTOs.UpdateWarehouseRequestDTO;
import com.code.prodapp.warehouseservice.DTOs.WarehouseResponseDTO;
import com.code.prodapp.warehouseservice.services.WarehouseService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/warehouses")
@RequiredArgsConstructor
@RateLimiter(name = "warehouseServiceRateLimiter")
public class AdminControllers {

    private final WarehouseService warehouseService;

    @GetMapping
    public ResponseEntity<List<WarehouseResponseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> getWarehouseById(@PathVariable UUID id) {
        return ResponseEntity.ok(warehouseService.getWarehouseByUUID(id));
    }

    @PostMapping
    public ResponseEntity<WarehouseResponseDTO> createWarehouse(@Valid @RequestBody CreateWarehouseRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(warehouseService.createWarehouse(requestDTO));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> updateWarehouse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWarehouseRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable UUID id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearest")
    public ResponseEntity<WarehouseResponseDTO> findNearestWarehouse(@RequestParam(required = true) double lon,
                                                                     @RequestParam(required = true) double lat
                                                                     ){
        // Longitude -> X
        // Latitude ->  Y
        return ResponseEntity.ok(warehouseService.findNearestWarehouse(lon,lat));

    }



}
