package com.rishitbal.dispatch_load_balancer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// Delivery order schema
@Entity
@Table(name = "delivery_orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private long packageWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    public DeliveryOrder(
            String orderId,
            double latitude,
            double longitude,
            String address,
            long packageWeight,
            Priority priority
    ) {
        this.orderId = orderId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.packageWeight = packageWeight;
        this.priority = priority;
    }


}
