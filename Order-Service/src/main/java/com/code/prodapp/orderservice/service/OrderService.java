package com.code.prodapp.orderservice.service;

import com.code.prodapp.orderservice.DTOs.*;
import com.code.prodapp.orderservice.clients.InventoryClient;
import com.code.prodapp.orderservice.entities.Customer;
import com.code.prodapp.orderservice.entities.CustomerAddress;
import com.code.prodapp.orderservice.entities.Item;
import com.code.prodapp.orderservice.entities.Orders;
import com.code.prodapp.orderservice.entities.enums.OrderStatus;
import com.code.prodapp.orderservice.events.ItemHelper;
import com.code.prodapp.orderservice.events.OrderConfirmedEvent;
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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String ORDER_PLACED_EVENT = "ORDER_PLACED";
    private static final String ORDER_CONFIRMED_EVENT = "ORDER_CONFIRMED";

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final InventoryClient inventoryClient;
    private final ItemRepository itemRepository;
    private final KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate;
    private final CustomerAddressService customerAddressService;
    private final CustomerService customerService;


    public List<OrderResponseDTO> getAllOrders(){
        log.info("Getting all orders");
        return orderRepository.findAllByCustomerIsNotNullAndCustomerAddressIsNotNull()
                .stream()
                .map(this::mapOrderToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getOrdersForCustomerEmail(String userEmail) {
        log.info("Getting orders for customer email {}", userEmail);
        if (userEmail == null || userEmail.isBlank()) {
            return List.of();
        }
        return orderRepository.findAllByCustomerEmailAndCustomerAddressIsNotNull(userEmail.trim())
                .stream()
                .map(this::mapOrderToResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Long id){
        log.info("Getting order by id {}", id);
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order Not Found"));
        return mapOrderToResponseDTO(order);
    }


    @Retry(name = "orderServiceRetry",fallbackMethod = "createOrderFallbackMethod")
    @CircuitBreaker(name = "orderCircuitBreaker",fallbackMethod = "createOrderFallbackMethod")
    @RateLimiter(name = "orderServiceRateLimiter",fallbackMethod = "createOrderFallbackMethod")
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO, String userEmail) {

        log.info("Creating Order {}", orderRequestDTO);

        // Get All the Items we need to check Stock of
        List<StockCheckDTO> stockCheckDTOList = new ArrayList<>();
        for(ItemRequestDTO item : orderRequestDTO.getItems()){
            stockCheckDTOList.add(modelMapper.map(item,StockCheckDTO.class));
        }
        List<ReturnedItemsDTO> returnedItemsDTOList = inventoryClient.InStockAndReturnPrices(stockCheckDTOList);
        // Now start creating the order.
        CustomerAddress customerAddress = resolveCheckoutAddress(orderRequestDTO, userEmail);
        // Calculate the total price, manually
        double totalPrice = 0.0;
        for(ReturnedItemsDTO returnedItemsDTO : returnedItemsDTOList){
            totalPrice = totalPrice + (returnedItemsDTO.getQuantity()*returnedItemsDTO.getProductPrice());
        }

        Orders order = new Orders();
        order.setPrice(totalPrice);
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
        orderEvent.setEventType(ORDER_PLACED_EVENT);
        orderEvent.setOrderNumber(savedOrder.getId());
        orderEvent.setCustomerId(savedOrder.getCustomer().getId());
        orderEvent.setCustomerEmail(savedOrder.getCustomer().getEmail());
        orderEvent.setDeliveryAddress(savedOrder.getDeliveryAddressSnapshot());
        orderEvent.setDeliveryLat(savedOrder.getDeliveryLat());
        orderEvent.setDeliveryLng(savedOrder.getDeliveryLng());
        orderEvent.setOrderedItems(savedOrder.getItems()
                .stream()
                .map(item -> new ItemHelper(item.getProductId(),item.getQuantity()))
                .collect(Collectors.toList())
        );

        log.info("Kafka send topic={} eventType={} orderNumber={} customerId={}",
                ORDER_EVENTS_TOPIC,
                orderEvent.getEventType(),
                orderEvent.getOrderNumber(),
                orderEvent.getCustomerId());
        orderEventKafkaTemplate.send(ORDER_EVENTS_TOPIC,orderEvent);

        List<ItemResponseDTO> savedItems = savedOrder.getItems()
                .stream()
                .map(item -> new ItemResponseDTO(item.getId(), item.getProductId(), item.getQuantity()))
                .toList();
        return new OrderResponseDTO(
                savedOrder.getId(),
                savedOrder.getCustomer().getId(),
                savedOrder.getCustomerAddress().getId(),
                savedItems,
                BigDecimal.valueOf(savedOrder.getPrice()),
                savedOrder.getOrderStatus().name(),
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


    @Transactional
    @KafkaListener(topics = ORDER_EVENTS_TOPIC)
    public void handleOrderConfirmedEvent(OrderConfirmedEvent orderConfirmedEvent) {
        log.info("Kafka receive topic={} eventType={} orderNumber={} customerId={}",
                ORDER_EVENTS_TOPIC,
                orderConfirmedEvent.getEventType(),
                orderConfirmedEvent.getOrderNumber(),
                orderConfirmedEvent.getCustomerId());
        if (!ORDER_CONFIRMED_EVENT.equals(orderConfirmedEvent.getEventType())) {
            return;
        }

        Orders order = orderRepository.findById(orderConfirmedEvent.getOrderNumber())
                .orElseThrow(() -> new OrderNotFoundException("Order Not Found"));

        if (order.getOrderStatus().equals(OrderStatus.CANCELLED)) {
            log.info("Ignoring confirmation for cancelled order {}", orderConfirmedEvent.getOrderNumber());
            return;
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }
    private String buildAddressSnapshot(CustomerAddress address) {
        return Stream.of(
                address.getAddressLine(),
                address.getCity(),
                address.getState(),
                address.getPincode()
        )
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(", "));
    }

    private CustomerAddress resolveCheckoutAddress(OrderRequestDTO orderRequestDTO, String userEmail) {
        try {
            if (orderRequestDTO.getCustomerId() != null && orderRequestDTO.getCustomerAddressId() != null) {
                CustomerAddress customerAddress = customerAddressService.findAddressForCustomer(
                        orderRequestDTO.getCustomerId(),
                        orderRequestDTO.getCustomerAddressId()
                );
                if (userEmail != null && !userEmail.isBlank()) {
                    customerAddress.getCustomer().setEmail(userEmail);
                }
                return customerAddress;
            }
        } catch (RuntimeException exception) {
            log.warn("Could not use requested checkout address customerId={} addressId={} reason={}",
                    orderRequestDTO.getCustomerId(),
                    orderRequestDTO.getCustomerAddressId(),
                    exception.getMessage());
        }

        Customer customer = customerService.findOrCreateCustomerByEmail(userEmail);
        return customerAddressService.createCheckoutAddress(
                customer,
                orderRequestDTO.getDeliveryAddress(),
                orderRequestDTO.getDeliveryLat(),
                orderRequestDTO.getDeliveryLng()
        );
    }

    private OrderResponseDTO mapOrderToResponseDTO(Orders order) {
        List<ItemResponseDTO> items = order.getItems() == null
                ? List.of()
                : order.getItems()
                .stream()
                .map(item -> new ItemResponseDTO(item.getId(), item.getProductId(), item.getQuantity()))
                .toList();
        Long customerId = order.getCustomer() != null
                ? order.getCustomer().getId()
                : null;
        Long customerAddressId = order.getCustomerAddress() != null
                ? order.getCustomerAddress().getId()
                : null;
        BigDecimal totalPrice = order.getPrice() != null
                ? BigDecimal.valueOf(order.getPrice())
                : BigDecimal.ZERO;
        String orderStatus = order.getOrderStatus() != null
                ? order.getOrderStatus().name()
                : "UNKNOWN";
        return new OrderResponseDTO(
                order.getId(),
                customerId,
                customerAddressId,
                items,
                totalPrice,
                orderStatus,
                order.getDeliveryAddressSnapshot(),
                order.getDeliveryLat(),
                order.getDeliveryLng()
        );
    }



}


