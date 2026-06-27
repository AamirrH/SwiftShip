package com.code.prodapp.inventoryservice.service;


import com.code.prodapp.inventoryservice.DTOs.*;
import com.code.prodapp.inventoryservice.entities.Product;
import com.code.prodapp.inventoryservice.events.ItemHelper;
import com.code.prodapp.inventoryservice.events.OrderConfirmedEvent;
import com.code.prodapp.inventoryservice.events.OrderEvent;
import com.code.prodapp.inventoryservice.exceptions.NotEnoughStockAvailableException;
import com.code.prodapp.inventoryservice.exceptions.ProductNotFoundException;
import com.code.prodapp.inventoryservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String, OrderConfirmedEvent> orderConfirmedKafkaTemplate;

    public List<ProductDTO> getAllInventory(){
        log.info("Getting all products");
        return productRepository.findAll()
                .stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id){
        log.info("Getting product by id {}", id);
        return modelMapper.map(findProductEntityById(id), ProductDTO.class);
    }

    @Transactional
    public ProductDTO createProduct(CreateProductRequestDTO requestDTO) {
        log.info("Creating product {}", requestDTO.getProductName());

        Product product = new Product();
        product.setProductName(requestDTO.getProductName());
        product.setProductPrice(requestDTO.getProductPrice());
        product.setStock(requestDTO.getStock());

        return modelMapper.map(productRepository.save(product), ProductDTO.class);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, CreateProductRequestDTO requestDTO) {
        log.info("Updating product by id {}", id);

        Product product = findProductEntityById(id);
        product.setProductName(requestDTO.getProductName());
        product.setProductPrice(requestDTO.getProductPrice());
        product.setStock(requestDTO.getStock());

        return modelMapper.map(productRepository.save(product), ProductDTO.class);
    }

    @Transactional
    public ProductDTO patchProduct(Long id, UpdateProductRequestDTO requestDTO) {
        log.info("Patching product by id {}", id);

        Product product = findProductEntityById(id);

        if (requestDTO.getProductName() != null) {
            product.setProductName(requestDTO.getProductName());
        }
        if (requestDTO.getProductPrice() != null) {
            product.setProductPrice(requestDTO.getProductPrice());
        }
        if (requestDTO.getStock() != null) {
            product.setStock(requestDTO.getStock());
        }

        return modelMapper.map(productRepository.save(product), ProductDTO.class);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product by id {}", id);

        Product product = findProductEntityById(id);
        productRepository.delete(product);
    }

    @Transactional
    public void reduceStock(List<ReduceStockRequestDTO> reduceStockRequestDTOS){
        reduceStockRequestDTOS.forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + item.getProductId()));
            if(product.getStock()<item.getQuantity()){
                throw new NotEnoughStockAvailableException("Product Stock Not Enough");
            }
            else{
                product.setStock(product.getStock()-item.getQuantity());
            }
            // Save the product with the new stock
            productRepository.save(product);
        });
        return;
    }

    @Transactional
    public void addStock(List<AddStockRequestDTO> addStockRequestDTOS){
        addStockRequestDTOS.forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + item.getProductId()));
            // Unconditional because when cancelling an order we just have to return the stock
            product.setStock(product.getStock()+item.getQuantity());
            // Save the product
            productRepository.save(product);
        });
        return;
    }

    private Product findProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));
    }

    public boolean InStock(List<StockCheckDTO> stockCheckDTO) {

        Map<Long,Integer> requestedItemsMap = stockCheckDTO
                .stream()
                .collect(Collectors
                        .toMap(StockCheckDTO::getProductId,
                                StockCheckDTO::getQuantity,
                                Integer::sum)
                );
        // Single DB Call.
        List<Product> products = productRepository.findAllById(requestedItemsMap.keySet());

        if (products.size() != requestedItemsMap.size()) {
            throw new ProductNotFoundException("One or more requested products were not found");
        }

        for(Product product : products){
            Integer requestedQuantity = requestedItemsMap.get(product.getId());
            if(product.getStock()<requestedQuantity){
                throw new NotEnoughStockAvailableException("Product with name "+product.getProductName()
                        +" Stock Not Enough");
            }

        }
        return true;
    }


    // Consumer is a Producer itself, consumes OrderEvent and Produces OrderConfirmedEvent
    @Transactional
    @KafkaListener(topics = "order-placed")
    public void handleOrderPlacedEvent(OrderEvent orderEvent) {
        // Reduce Stock Asynchronously
        Map<Long,Integer> requestedItemsMap = orderEvent.getOrderedItems()
                .stream()
                .collect(Collectors
                        .toMap(ItemHelper::getProductId,
                                ItemHelper::getQuantity,
                                Integer::sum)
                );
        List<Product> products = productRepository.findAllById(requestedItemsMap.keySet());

        if (products.size() != requestedItemsMap.size()) {
            throw new ProductNotFoundException("One or more ordered products were not found");
        }

        for(Product product : products){
            Integer requestedQuantity = requestedItemsMap.get(product.getId());
            if(product.getStock()<requestedQuantity){
                throw new NotEnoughStockAvailableException("Product with name "+product.getProductName()
                        +" Stock Not Enough");
            }
            else {
                product.setStock(product.getStock() - requestedQuantity);
            }

        }
        // Save the updated stock products.
        productRepository.saveAll(products);

        // Cancel-window (Orders can only be canceled until they are not confirmed)





        // Build the OrderConfirmedEvent
        OrderConfirmedEvent orderConfirmedEvent = new OrderConfirmedEvent();
        orderConfirmedEvent.setOrderNumber(orderEvent.getOrderNumber());
        orderConfirmedEvent.setCustomerId(orderEvent.getCustomerId());
        orderConfirmedEvent.setOrderStatus("CONFIRMED");
        // X-Coordinate
        orderConfirmedEvent.setDeliveryLng(orderEvent.getDeliveryLng());
        // Y-Coordinate
        orderConfirmedEvent.setDeliveryLat(orderEvent.getDeliveryLat());
        orderConfirmedKafkaTemplate.send("order-confirmed", orderConfirmedEvent);

    }



}
