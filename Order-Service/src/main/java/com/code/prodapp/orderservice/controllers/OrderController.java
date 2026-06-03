package com.code.prodapp.orderservice.controllers;

import com.code.prodapp.orderservice.DTOs.OrderRequestDTO;
import com.code.prodapp.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    @Value("${aamir.variable.hussain}")
    private String globalPropertiesSecretVariable;


    @GetMapping("/testOrders")
    public String testOrders() {
        return "testOrders "+globalPropertiesSecretVariable;
    }


    @GetMapping
    public ResponseEntity<List<OrderRequestDTO>> getAllOrders(){
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping("/{ID}")
    public ResponseEntity<OrderRequestDTO> getProductById(@PathVariable(name = "ID") Long id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping("/createOrder")
    public ResponseEntity<OrderRequestDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO){
        return ResponseEntity.ok(orderService.createOrder(orderRequestDTO));
    }

    @PutMapping("/cancelOrder/{id}")
    // TODO : Replace the id with users orders
    public ResponseEntity<String> cancelOrder(@PathVariable(name = "id") Long orderId){
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("Order cancelled");
    }





}
