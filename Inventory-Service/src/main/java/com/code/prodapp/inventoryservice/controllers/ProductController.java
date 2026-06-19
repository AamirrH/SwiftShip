package com.code.prodapp.inventoryservice.controllers;


import com.code.prodapp.inventoryservice.DTOs.*;
import com.code.prodapp.inventoryservice.clients.OrdersFeignClient;
import com.code.prodapp.inventoryservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final DiscoveryClient discoveryClient;
    private final OrdersFeignClient ordersFeignClient;

    @GetMapping("/fetchProducts")
    public String fetchProducts() {
        log.info("fetchProducts");
        return ordersFeignClient.testOrders();
    }

    @GetMapping("/discovered-services")
    public List<String> getDiscoveredServices() {
        return discoveryClient.getServices();
    }



    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(){
        return ResponseEntity.ok(productService.getAllInventory());
    }

    @GetMapping("/{ID}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable(name = "ID") Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/admin")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody CreateProductRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(requestDTO));
    }

    @PutMapping("/admin/{ID}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable(name = "ID") Long id,
            @RequestBody CreateProductRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, requestDTO));
    }

    @PatchMapping("/admin/{ID}")
    public ResponseEntity<ProductDTO> patchProduct(
            @PathVariable(name = "ID") Long id,
            @RequestBody UpdateProductRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(productService.patchProduct(id, requestDTO));
    }

    @DeleteMapping("/admin/{ID}")
    public ResponseEntity<Void> deleteProduct(@PathVariable(name = "ID") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reduceStock")
    public void reduceStock(@RequestBody List<ReduceStockRequestDTO> itemsToReduce){
        productService.reduceStock(itemsToReduce);
    }

    @PutMapping("/addStock")
    public void addStock(@RequestBody List<AddStockRequestDTO> itemsToAdd){
        productService.addStock(itemsToAdd);
    }

    @PostMapping("/checkStock")
    public ResponseEntity<Boolean> InStock(@RequestBody List<StockCheckDTO> stockCheckDTO){
        return ResponseEntity.ok(productService.InStock(stockCheckDTO));
    }




}
