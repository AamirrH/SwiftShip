package com.code.prodapp.orderservice.service;

import com.code.prodapp.orderservice.DTOs.CreateCustomerAddressRequestDTO;
import com.code.prodapp.orderservice.DTOs.CustomerAddressResponseDTO;
import com.code.prodapp.orderservice.DTOs.UpdateCustomerAddressRequestDTO;
import com.code.prodapp.orderservice.entities.Customer;
import com.code.prodapp.orderservice.entities.CustomerAddress;
import com.code.prodapp.orderservice.exceptions.CustomerAddressNotFoundException;
import com.code.prodapp.orderservice.repository.CustomerAddressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAddressService {

    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerService customerService;

    public List<CustomerAddressResponseDTO> getCustomerAddresses(Long customerId) {
        log.info("Getting addresses for customer {}", customerId);
        customerService.findCustomerEntityById(customerId);

        return customerAddressRepository.findAllByCustomerId(customerId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public CustomerAddressResponseDTO getCustomerAddressById(Long customerId, Long addressId) {
        log.info("Getting address {} for customer {}", addressId, customerId);
        return mapToResponseDTO(findAddressForCustomer(customerId, addressId));
    }

    @Transactional
    public CustomerAddressResponseDTO createCustomerAddress(Long customerId, CreateCustomerAddressRequestDTO requestDTO) {
        log.info("Creating address for customer {}", customerId);

        Customer customer = customerService.findCustomerEntityById(customerId);

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setLabel(requestDTO.getLabel());
        address.setAddressLine(requestDTO.getAddressLine());
        address.setCity(requestDTO.getCity());
        address.setState(requestDTO.getState());
        address.setPincode(requestDTO.getPincode());
        address.setLat(requestDTO.getLat());
        address.setLng(requestDTO.getLng());
        address.setDefaultAddress(Boolean.TRUE.equals(requestDTO.getDefaultAddress()));

        return mapToResponseDTO(customerAddressRepository.save(address));
    }

    @Transactional
    public CustomerAddressResponseDTO updateCustomerAddress(
            Long customerId,
            Long addressId,
            UpdateCustomerAddressRequestDTO requestDTO
    ) {
        log.info("Updating address {} for customer {}", addressId, customerId);

        CustomerAddress address = findAddressForCustomer(customerId, addressId);
        address.setLabel(requestDTO.getLabel());
        address.setAddressLine(requestDTO.getAddressLine());
        address.setCity(requestDTO.getCity());
        address.setState(requestDTO.getState());
        address.setPincode(requestDTO.getPincode());
        address.setLat(requestDTO.getLat());
        address.setLng(requestDTO.getLng());
        address.setDefaultAddress(Boolean.TRUE.equals(requestDTO.getDefaultAddress()));

        return mapToResponseDTO(customerAddressRepository.save(address));
    }

    @Transactional
    public void deleteCustomerAddress(Long customerId, Long addressId) {
        log.info("Deleting address {} for customer {}", addressId, customerId);
        customerAddressRepository.delete(findAddressForCustomer(customerId, addressId));
    }

    public CustomerAddress findAddressForCustomer(Long customerId, Long addressId) {
        CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new CustomerAddressNotFoundException("Customer address not found with id " + addressId));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw new CustomerAddressNotFoundException(
                    "Address " + addressId + " does not belong to customer " + customerId
            );
        }

        return address;
    }

    private CustomerAddressResponseDTO mapToResponseDTO(CustomerAddress address) {
        return new CustomerAddressResponseDTO(
                address.getId(),
                address.getCustomer().getId(),
                address.getLabel(),
                address.getAddressLine(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getLat(),
                address.getLng(),
                address.getDefaultAddress()
        );
    }
}
