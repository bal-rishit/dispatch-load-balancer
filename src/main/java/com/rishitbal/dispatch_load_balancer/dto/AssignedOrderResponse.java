package com.rishitbal.dispatch_load_balancer.dto;

import com.rishitbal.dispatch_load_balancer.domain.Priority;

public record AssignedOrderResponse(
        String orderId,
        double latitude,
        double longitude,
        String address,
        long packageWeight,
        Priority priority
) {
}
