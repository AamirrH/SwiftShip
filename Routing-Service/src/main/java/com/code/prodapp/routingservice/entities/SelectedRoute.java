package com.code.prodapp.routingservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class SelectedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID serialId;

    private Long selectedRouteId;

    private Long customerId;

    private Long orderId;

    private List<Double> customerCoordinates;

    private List<Double> wareHouseCoordinates;

    private double totalDistance;

    private double timeToReach;

    private String reasoning;




}
