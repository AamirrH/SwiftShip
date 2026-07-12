package com.code.prodapp.inventoryservice.DTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long id;

    private String productName;

    private Double productPrice;

    private String image;

    private Integer stock;

}
