package com.rishitbal.dispatch_load_balancer.dto;

import java.util.List;

public record DispatchPlanResponse(
        List<VehicleDispatchPlan> dispatchPlan,
        List<UnassignedOrderResponse> unassignedOrders
) {
}
