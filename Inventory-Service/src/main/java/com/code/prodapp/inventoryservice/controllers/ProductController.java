package com.code.prodapp.inventoryservice.controllers;


import com.code.prodapp.inventoryservice.DTOs.ProductDTO;
import com.code.prodapp.inventoryservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(){
        return ResponseEntity.ok(productService.getAllInventory());
    }

    @PostMapping("/{ID}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable(name = "ID") Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }





}
