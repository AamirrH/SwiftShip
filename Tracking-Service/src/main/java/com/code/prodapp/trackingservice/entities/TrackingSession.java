package com.code.prodapp.trackingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tracking_sessions")
public class TrackingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id")
    private Long trackingId;

    @Column(name = "order_number", nullable = false, unique = true)
    private Long orderNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "selected_route_id", nullable = false)
    private Long selectedRouteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "warehouse_latitude", nullable = false)
    private Double warehouseLatitude;

    @Column(name = "warehouse_longitude", nullable = false)
    private Double warehouseLongitude;

    @Column(name = "customer_latitude", nullable = false)
    private Double customerLatitude;

    @Column(name = "customer_longitude", nullable = false)
    private Double customerLongitude;

    @Column(name = "customer_address")
    private String customerAddress;

    @Column(name = "current_latitude", nullable = false)
    private Double currentLatitude;

    @Column(name = "current_longitude", nullable = false)
    private Double currentLongitude;

    @Column(name = "total_distance_km", nullable = false)
    private Double totalDistanceKm;

    @Column(name = "remaining_distance_km", nullable = false)
    private Double remainingDistanceKm;

    @Column(name = "initial_eta_minutes", nullable = false)
    private Double initialEtaMinutes;

    @Column(name = "current_eta_minutes", nullable = false)
    private Double currentEtaMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_status", nullable = false)
    private TrackingStatus trackingStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
