package com.rishitbal.dispatch_load_balancer.dto;

import com.rishitbal.dispatch_load_balancer.domain.Priority;

// Record of an order that has not been assigned to a vehicle due to constraints
public record UnassignedOrderResponse(
        String orderId,
        double latitude,
        double longitude,
        String address,
        long packageWeight,
        Priority priority,
        String reason
) {
}
