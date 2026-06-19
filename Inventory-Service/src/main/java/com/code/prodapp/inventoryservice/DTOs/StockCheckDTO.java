package com.code.prodapp.inventoryservice.DTOs;

import lombok.Data;

@Data
public class StockCheckDTO {

    private Long productId;
    private Integer quantity;

}
