package com.code.prodapp.inventoryservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemHelper {

    private Long productId;
    private Integer quantity;


}
