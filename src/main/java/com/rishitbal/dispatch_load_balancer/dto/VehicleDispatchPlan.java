package com.rishitbal.dispatch_load_balancer.dto;

import java.util.List;

public record VehicleDispatchPlan(
        String vehicleId,
        long totalLoad,
        String totalDistance,
        List<AssignedOrderResponse> assignedOrders
) {
}
