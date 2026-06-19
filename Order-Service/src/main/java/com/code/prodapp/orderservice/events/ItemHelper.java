package com.code.prodapp.orderservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemHelper {

    private Long productId;
    private Integer quantity;


}
