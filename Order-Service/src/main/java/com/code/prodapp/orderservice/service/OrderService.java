package com.code.prodapp.orderservice.service;

import com.code.prodapp.orderservice.DTOs.*;
import com.code.prodapp.orderservice.clients.InventoryClient;
import com.code.prodapp.orderservice.entities.CustomerAddress;
import com.code.prodapp.orderservice.entities.Item;
import com.code.prodapp.orderservice.entities.Orders;
import com.code.prodapp.orderservice.entities.enums.OrderStatus;
import com.code.prodapp.orderservice.events.ItemHelper;
import com.code.prodapp.orderservice.events.OrderEvent;
import com.code.prodapp.orderservice.exceptions.OrderAlreadyCancelledException;
import com.code.prodapp.orderservice.exceptions.OrderNotFoundException;
import com.code.prodapp.orderservice.repository.ItemRepository;
import com.code.prodapp.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate;
    private final CustomerAddressService customerAddressService;


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


    @Retry(name = "orderServiceRetry",fallbackMethod = "createOrderFallbackMethod")
    @CircuitBreaker(name = "orderCircuitBreaker",fallbackMethod = "createOrderFallbackMethod")
    @RateLimiter(name = "orderServiceRateLimiter",fallbackMethod = "createOrderFallbackMethod")
    @Transactional
    public OrderRequestDTO createOrder(OrderRequestDTO orderRequestDTO) {

        log.info("Creating Order {}", orderRequestDTO);

        // Get All the Items we need to check Stock of
        List<StockCheckDTO> stockCheckDTOList = new ArrayList<>();
        for(ItemRequestDTO item : orderRequestDTO.getItems()){
            stockCheckDTOList.add(modelMapper.map(item,StockCheckDTO.class));
        }
        boolean inStock = inventoryClient.InStock(stockCheckDTOList);

        // Now start creating the order.
        CustomerAddress customerAddress = customerAddressService.findAddressForCustomer(
                orderRequestDTO.getCustomerId(),
                orderRequestDTO.getCustomerAddressId()
        );

        Orders order = new Orders();
        order.setPrice(orderRequestDTO.getTotalPrice().doubleValue());
        order.setCustomer(customerAddress.getCustomer());
        order.setCustomerAddress(customerAddress);
        order.setDeliveryAddressSnapshot(buildAddressSnapshot(customerAddress));
        order.setDeliveryLat(customerAddress.getLat());
        order.setDeliveryLng(customerAddress.getLng());
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
        order.setOrderStatus(OrderStatus.PLACED);
        // Save the Order in DB
        Orders savedOrder = orderRepository.save(order);

        // Create an Order Event for Kafka
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderNumber(savedOrder.getId());
        orderEvent.setCustomerId(savedOrder.getCustomer().getId());
        orderEvent.setDeliveryAddress(savedOrder.getDeliveryAddressSnapshot());
        orderEvent.setDeliveryLat(savedOrder.getDeliveryLat());
        orderEvent.setDeliveryLng(savedOrder.getDeliveryLng());
        orderEvent.setOrderedItems(savedOrder.getItems()
                .stream()
                .map(item -> new ItemHelper(item.getProductId(),item.getQuantity()))
                .collect(Collectors.toList())
        );

        orderEventKafkaTemplate.send("order-placed",orderEvent);

        List<ItemRequestDTO> savedItems = savedOrder.getItems()
                .stream()
                .map(item -> new ItemRequestDTO(item.getId(), item.getProductId(), item.getQuantity()))
                .toList();
        return new OrderRequestDTO(
                savedOrder.getId(),
                savedOrder.getCustomer().getId(),
                savedOrder.getCustomerAddress().getId(),
                savedItems,
                BigDecimal.valueOf(savedOrder.getPrice()),
                savedOrder.getDeliveryAddressSnapshot(),
                savedOrder.getDeliveryLat(),
                savedOrder.getDeliveryLng()
        );
    }


    public OrderRequestDTO createOrderFallbackMethod(){
        log.info("Fallback Method Hit");
        return null;
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

    private String buildAddressSnapshot(CustomerAddress address) {
        return String.join(", ",
                address.getAddressLine(),
                address.getCity(),
                address.getState(),
                address.getPincode()
        );
    }



}
