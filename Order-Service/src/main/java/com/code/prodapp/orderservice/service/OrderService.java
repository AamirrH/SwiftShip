package com.code.prodapp.orderservice.service;

import com.code.prodapp.orderservice.DTOs.OrderRequestDTO;
import com.code.prodapp.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    public List<OrderRequestDTO> getAllOrders(){
        log.info("Getting all products");
        return orderRepository.findAll()
                .stream()
                .map(order -> modelMapper.map(order,OrderRequestDTO.class))
                .collect(Collectors.toList());
    }

    public OrderRequestDTO getOrderById(Long id){
        log.info("Getting order by id {}", id);
        return modelMapper.map(orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory Not Found")),OrderRequestDTO.class);
    }



}
