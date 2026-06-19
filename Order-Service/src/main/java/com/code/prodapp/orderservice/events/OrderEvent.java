package com.code.prodapp.orderservice.events;

import com.code.prodapp.orderservice.entities.Item;
import com.code.prodapp.orderservice.entities.Orders;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;

@Getter
@Setter
public class OrderEvent {

    private Long orderNumber;
    private List<ItemHelper> orderedItems;

}
