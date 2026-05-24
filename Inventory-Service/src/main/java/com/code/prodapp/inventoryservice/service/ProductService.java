package com.code.prodapp.inventoryservice.service;


import com.code.prodapp.inventoryservice.DTOs.ProductDTO;
import com.code.prodapp.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public List<ProductDTO> getAllInventory(){
        log.info("Getting all products");
        return productRepository.findAll()
                .stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id){
        log.info("Getting product by id {}", id);
        return modelMapper.map(productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory Not Found")),ProductDTO.class);
    }



}
