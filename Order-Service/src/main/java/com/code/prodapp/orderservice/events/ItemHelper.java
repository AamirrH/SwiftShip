package com.code.prodapp.orderservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemHelper {

    private Long productId;
    private Integer quantity;


}
