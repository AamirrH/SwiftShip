package com.code.prodapp.orderservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockCheckDTO {

    private Long productId;
    private Integer quantity;

}