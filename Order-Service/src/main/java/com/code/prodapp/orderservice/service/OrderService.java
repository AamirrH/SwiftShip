package com.code.prodapp.orderservice.service;

import com.code.prodapp.orderservice.DTOs.AddStockRequestDTO;
import com.code.prodapp.orderservice.DTOs.ItemRequestDTO;
import com.code.prodapp.orderservice.DTOs.OrderRequestDTO;
import com.code.prodapp.orderservice.DTOs.ReduceStockRequestDTO;
import com.code.prodapp.orderservice.clients.InventoryClient;
import com.code.prodapp.orderservice.entities.Item;
import com.code.prodapp.orderservice.entities.Orders;
import com.code.prodapp.orderservice.entities.enums.OrderStatus;
import com.code.prodapp.orderservice.exceptions.OrderAlreadyCancelledException;
import com.code.prodapp.orderservice.exceptions.OrderNotFoundException;
import com.code.prodapp.orderservice.repository.ItemRepository;
import com.code.prodapp.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final InventoryClient inventoryClient;
    private final ItemRepository itemRepository;

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


    @Transactional
    public OrderRequestDTO createOrder(OrderRequestDTO orderRequestDTO) {
        log.info("Creating Order {}", orderRequestDTO);
        Orders order = new Orders();
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPrice(orderRequestDTO.getTotalPrice().doubleValue());
        order.setItems(
                orderRequestDTO.getItems()
                        .stream()
                        .map(item -> {
                            Item item1 = new Item();
                            item1.setProductId(item.getProductId());
                            item1.setQuantity(item.getQuantity());
                            // Bi-Directional Relationship
                            item1.setOrders(order);
                            return item1;
                        })
                        .collect(Collectors.toList())
        );
        List<ReduceStockRequestDTO> reduceStockRequestDTOS = order
                .getItems()
                .stream()
                .map(item -> new ReduceStockRequestDTO(item.getProductId(),item.getQuantity()))
                .toList();
        inventoryClient.reduceStock(reduceStockRequestDTOS);
        // Save the Order in DB
        order.setOrderStatus(OrderStatus.CONFIRMED);
        Orders savedOrder = orderRepository.save(order);
        List<ItemRequestDTO> savedItems = savedOrder.getItems()
                .stream()
                .map(item -> new ItemRequestDTO(item.getId(), item.getProductId(), item.getQuantity()))
                .toList();
        return new OrderRequestDTO(savedOrder.getId(), savedItems, BigDecimal.valueOf(savedOrder.getPrice()));
    }

    public void cancelOrder(Long orderId){
        log.info("Canceling Order {}", orderId);
        // Find All Items corresponding to that particular orderId
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order Not Found"));
        if(order.getOrderStatus().equals(OrderStatus.CANCELLED)){
            throw new OrderAlreadyCancelledException("Order with id " + orderId + " is already cancelled");
        }
        List<Item> orderedItems = itemRepository.findAllByOrdersId(orderId);
        List<AddStockRequestDTO> itemsToAdd = orderedItems
                .stream()
                .map(item -> new AddStockRequestDTO(item.getProductId(),item.getQuantity()))
                .toList();
        // Add Stock using Inventory Client
        inventoryClient.addStock(itemsToAdd);
        // Mark the Order as CANCELLED, saving it keeps a record
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return;
    }



}
