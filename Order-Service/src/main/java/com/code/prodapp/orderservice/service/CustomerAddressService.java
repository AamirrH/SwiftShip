package com.code.prodapp.orderservice.service;

import com.code.prodapp.orderservice.DTOs.CreateCustomerAddressRequestDTO;
import com.code.prodapp.orderservice.DTOs.CustomerAddressResponseDTO;
import com.code.prodapp.orderservice.DTOs.GeocodingResultDTO;
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
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAddressService {

    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerService customerService;
    private final GeoapifyGeocodingService geoapifyGeocodingService;

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
        return createCustomerAddress(customer, requestDTO);
    }

    @Transactional
    public CustomerAddressResponseDTO createCustomerAddress(Customer customer, CreateCustomerAddressRequestDTO requestDTO) {
        log.info("Creating address for customer email {}", customer.getEmail());

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setLabel(requestDTO.getLabel());
        address.setAddressLine(requestDTO.getAddressLine());
        address.setCity(requestDTO.getCity());
        address.setState(requestDTO.getState());
        address.setPincode(requestDTO.getPincode());
        setCoordinates(address, requestDTO.getLat(), requestDTO.getLng());
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
        setCoordinates(address, requestDTO.getLat(), requestDTO.getLng());
        address.setDefaultAddress(Boolean.TRUE.equals(requestDTO.getDefaultAddress()));

        return mapToResponseDTO(customerAddressRepository.save(address));
    }

    @Transactional
    public CustomerAddressResponseDTO updateCustomerAddress(
            Customer customer,
            Long addressId,
            UpdateCustomerAddressRequestDTO requestDTO
    ) {
        return updateCustomerAddress(customer.getId(), addressId, requestDTO);
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

    public CustomerAddress createCheckoutAddress(
            Customer customer,
            String deliveryAddress,
            Double deliveryLat,
            Double deliveryLng
    ) {
        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setLabel("Delivery");
        address.setAddressLine(deliveryAddress);
        address.setCity("");
        address.setState("");
        address.setPincode("");
        address.setLat(deliveryLat);
        address.setLng(deliveryLng);
        address.setDefaultAddress(false);
        return customerAddressRepository.save(address);
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

    private void setCoordinates(CustomerAddress address, Double lat, Double lng) {
        if (lat != null && lng != null) {
            address.setLat(lat);
            address.setLng(lng);
            return;
        }

        String addressForGeocoding = buildAddressForGeocoding(address);
        try {
            GeocodingResultDTO geocodingResultDTO = geoapifyGeocodingService.geocodeAddress(addressForGeocoding);
            address.setLat(geocodingResultDTO.getLat());
            address.setLng(geocodingResultDTO.getLng());
        } catch (RuntimeException exception) {
            double[] fallbackCoordinates = fallbackCoordinatesFor(address.getCity());
            address.setLat(fallbackCoordinates[0]);
            address.setLng(fallbackCoordinates[1]);
            log.warn("Could not geocode customer address='{}'. Saving with fallback coordinates lat={} lng={} reason={}",
                    addressForGeocoding,
                    address.getLat(),
                    address.getLng(),
                    exception.getMessage());
        }
    }

    private String buildAddressForGeocoding(CustomerAddress address) {
        return Stream.of(
                        address.getAddressLine(),
                        address.getCity(),
                        address.getState(),
                        address.getPincode()
                )
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .reduce((left, right) -> left + ", " + right)
                .orElse("India");
    }

    private double[] fallbackCoordinatesFor(String city) {
        if (city == null) {
            return new double[]{20.5937, 78.9629};
        }

        return switch (city.trim().toLowerCase()) {
            case "pune" -> new double[]{18.5204, 73.8567};
            case "mumbai" -> new double[]{19.0760, 72.8777};
            case "delhi", "new delhi" -> new double[]{28.6139, 77.2090};
            case "bengaluru", "bangalore" -> new double[]{12.9716, 77.5946};
            case "hyderabad" -> new double[]{17.3850, 78.4867};
            case "chennai" -> new double[]{13.0827, 80.2707};
            case "kolkata" -> new double[]{22.5726, 88.3639};
            case "ahmedabad" -> new double[]{23.0225, 72.5714};
            case "jaipur" -> new double[]{26.9124, 75.7873};
            case "lucknow" -> new double[]{26.8467, 80.9462};
            case "nagpur" -> new double[]{21.1458, 79.0882};
            default -> new double[]{20.5937, 78.9629};
        };
    }
}
