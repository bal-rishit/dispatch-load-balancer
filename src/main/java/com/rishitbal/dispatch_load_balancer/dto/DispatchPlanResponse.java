package com.rishitbal.dispatch_load_balancer.dto;

import java.util.List;


// Format for the successful response expected from /api/dispatch/plan
public record DispatchPlanResponse(
        List<VehicleDispatchPlan> dispatchPlan,
        List<UnassignedOrderResponse> unassignedOrders
) {
}
