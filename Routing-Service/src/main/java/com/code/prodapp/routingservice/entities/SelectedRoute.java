package com.code.prodapp.routingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "response_routes")
public class SelectedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "response_uuid")
    private UUID serialId;

    @Column(name = "selected_route_id", nullable = false)
    private Long selectedRouteId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "customer_lng", nullable = false)
    private Double customerLng;

    @Column(name = "customer_lat", nullable = false)
    private Double customerLat;

    private String customerAddress;

    @Column(name = "warehouse_lng", nullable = false)
    private Double warehouseLng;

    @Column(name = "warehouse_lat", nullable = false)
    private Double warehouseLat;

    @Column(name = "kilometers_to_cover", nullable = false)
    private double totalDistance;

    @Column(name = "minutes_to_reach", nullable = false)
    private double timeToReach;

    @Column(name = "model_reasoning", length = 1000)
    private String reasoning;

}
