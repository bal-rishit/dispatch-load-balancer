package com.rishitbal.dispatch_load_balancer.dto;

import java.util.List;


// Response format for all the assigned orders to a specific vehicle (Part of the DispatchPlanResponse dto)
public record VehicleDispatchPlan(
        String vehicleId,
        long totalLoad,
        String totalDistance,
        List<AssignedOrderResponse> assignedOrders
) {
}
