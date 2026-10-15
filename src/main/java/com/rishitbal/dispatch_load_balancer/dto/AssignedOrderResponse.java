package com.rishitbal.dispatch_load_balancer.dto;

import com.rishitbal.dispatch_load_balancer.domain.Priority;

// Record of an order that has been assigned to a vehicle
public record AssignedOrderResponse(
        String orderId,
        double latitude,
        double longitude,
        String address,
        long packageWeight,
        Priority priority
) {
}
