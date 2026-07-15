package com.code.prodapp.orderservice.controllers;

import com.code.prodapp.orderservice.DTOs.CreateCustomerAddressRequestDTO;
import com.code.prodapp.orderservice.DTOs.CreateCustomerRequestDTO;
import com.code.prodapp.orderservice.DTOs.CustomerAddressResponseDTO;
import com.code.prodapp.orderservice.DTOs.CustomerResponseDTO;
import com.code.prodapp.orderservice.DTOs.UpdateCustomerAddressRequestDTO;
import com.code.prodapp.orderservice.DTOs.UpdateCustomerRequestDTO;
import com.code.prodapp.orderservice.service.CustomerAddressService;
import com.code.prodapp.orderservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerAddressService customerAddressService;

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<List<CustomerAddressResponseDTO>> getMyCustomerAddresses(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        return ResponseEntity.ok(customerAddressService.getCustomerAddresses(
                customerService.findOrCreateCustomerByEmail(userEmail).getId()
        ));
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@RequestBody CreateCustomerRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.createCustomer(requestDTO));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<CustomerAddressResponseDTO> createMyCustomerAddress(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestBody CreateCustomerAddressRequestDTO requestDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerAddressService.createCustomerAddress(
                        customerService.findOrCreateCustomerByEmail(userEmail),
                        requestDTO
                ));
    }

    @PatchMapping("/me/addresses/{addressId}")
    public ResponseEntity<CustomerAddressResponseDTO> updateMyCustomerAddress(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @PathVariable Long addressId,
            @RequestBody UpdateCustomerAddressRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(customerAddressService.updateCustomerAddress(
                customerService.findOrCreateCustomerByEmail(userEmail),
                addressId,
                requestDTO
        ));
    }

    @PatchMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long customerId,
            @RequestBody UpdateCustomerRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, requestDTO));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<CustomerAddressResponseDTO>> getCustomerAddresses(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerAddressService.getCustomerAddresses(customerId));
    }

    @GetMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<CustomerAddressResponseDTO> getCustomerAddressById(
            @PathVariable Long customerId,
            @PathVariable Long addressId
    ) {
        return ResponseEntity.ok(customerAddressService.getCustomerAddressById(customerId, addressId));
    }

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<CustomerAddressResponseDTO> createCustomerAddress(
            @PathVariable Long customerId,
            @RequestBody CreateCustomerAddressRequestDTO requestDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerAddressService.createCustomerAddress(customerId, requestDTO));
    }

    @PatchMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<CustomerAddressResponseDTO> updateCustomerAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @RequestBody UpdateCustomerAddressRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(customerAddressService.updateCustomerAddress(customerId, addressId, requestDTO));
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteCustomerAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId
    ) {
        customerAddressService.deleteCustomerAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }
}
