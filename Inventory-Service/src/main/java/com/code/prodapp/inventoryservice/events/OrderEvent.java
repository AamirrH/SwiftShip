package com.code.prodapp.inventoryservice.events;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderEvent {

    private Long orderNumber;
    private List<ItemHelper> orderedItems;

}
