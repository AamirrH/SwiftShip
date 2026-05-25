package com.code.prodapp.orderservice.DTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {

    private Long id;
    private List<OrderItemRequestDTO> items;
    private BigDecimal totalPrice;

}
