package com.code.prodapp.orderservice.service;

import com.code.prodapp.orderservice.DTOs.CreateCustomerRequestDTO;
import com.code.prodapp.orderservice.DTOs.CustomerResponseDTO;
import com.code.prodapp.orderservice.DTOs.UpdateCustomerRequestDTO;
import com.code.prodapp.orderservice.entities.Customer;
import com.code.prodapp.orderservice.exceptions.CustomerNotFoundException;
import com.code.prodapp.orderservice.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    public List<CustomerResponseDTO> getAllCustomers() {
        log.info("Getting all customers");
        return customerRepository.findAll()
                .stream()
                .map(customer -> modelMapper.map(customer, CustomerResponseDTO.class))
                .toList();
    }

    public CustomerResponseDTO getCustomerById(Long id) {
        log.info("Getting customer by id {}", id);
        return modelMapper.map(findCustomerEntityById(id), CustomerResponseDTO.class);
    }

    @Transactional
    public CustomerResponseDTO createCustomer(CreateCustomerRequestDTO requestDTO) {
        log.info("Creating customer {}", requestDTO.getEmail());

        Customer customer = new Customer();
        customer.setName(requestDTO.getName());
        customer.setEmail(requestDTO.getEmail());
        customer.setPhoneNumber(requestDTO.getPhoneNumber());

        return modelMapper.map(customerRepository.save(customer), CustomerResponseDTO.class);
    }

    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, UpdateCustomerRequestDTO requestDTO) {
        log.info("Updating customer by id {}", id);

        Customer customer = findCustomerEntityById(id);
        customer.setName(requestDTO.getName());
        customer.setEmail(requestDTO.getEmail());
        customer.setPhoneNumber(requestDTO.getPhoneNumber());

        return modelMapper.map(customerRepository.save(customer), CustomerResponseDTO.class);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer by id {}", id);
        customerRepository.delete(findCustomerEntityById(id));
    }

    public Customer findCustomerEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id " + id));
    }
}
