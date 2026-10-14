package com.rishitbal.dispatch_load_balancer.dto;

import com.rishitbal.dispatch_load_balancer.domain.Priority;

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
