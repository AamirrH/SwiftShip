package com.code.prodapp.orderservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReturnedItemsDTO {

    private Long productId;
    private Integer quantity;
    private double productPrice;


}
