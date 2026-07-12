package com.code.prodapp.inventoryservice.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductRequestDTO {

    private String productName;

    private Double productPrice;

    private String image;

    private Integer stock;
}
