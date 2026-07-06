package com.code.prodapp.warehouseservice.events;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WarehouseAssignedEvent {

    private String eventType;

    // Order-part
    private Long orderNumber;

    // Customer-part
    private Long customerId;
    private double customerLatitude;
    private double customerLongitude;
    private String customerAddress;

    // Warehouse-part
    private UUID warehouseId;
    private String warehouseName;
    private String city;
    private double warehouseLatitude;
    private double warehouseLongitude;


}
