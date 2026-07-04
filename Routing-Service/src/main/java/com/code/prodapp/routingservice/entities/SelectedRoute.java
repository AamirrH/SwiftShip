package com.code.prodapp.routingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
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

    @Column(name = "selected_route_id",nullable = false)
    private Long selectedRouteId;

    @Column(name = "customer_id",nullable = false)
    private Long customerId;

    @Column(name = "order_id",nullable = false)
    private Long orderId;

    @ElementCollection
    @CollectionTable(name="coordinates_table",joinColumns = @JoinColumn(referencedColumnName = "response_uuid" ,
    name = "selected_route_uuid"))
    @Column(name = "customer_coordinates")
    private List<Double> customerCoordinates;

    private String customerAddress;

    @ElementCollection
    @CollectionTable(name = "warehouse_coordinates_table",joinColumns = @JoinColumn(referencedColumnName = "response_uuid",
    name = "warehouse_coordinate_table_response_uuid" ))
    @Column(name = "warehouse_coordinates")
    private List<Double> wareHouseCoordinates;

    @Column(name = "kilometers_to_cover",nullable = false)
    private double totalDistance;

    @Column(name = "minutes_to_reach",nullable = false)
    private double timeToReach;


    @Column(name = "model_reasoning",nullable = false,length = 100)
    private String reasoning;

}
