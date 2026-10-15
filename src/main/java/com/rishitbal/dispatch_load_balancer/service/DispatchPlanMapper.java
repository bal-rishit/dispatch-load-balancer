package com.rishitbal.dispatch_load_balancer.service;

import com.rishitbal.dispatch_load_balancer.domain.DeliveryOrder;
import com.rishitbal.dispatch_load_balancer.dto.AssignedOrderResponse;
import com.rishitbal.dispatch_load_balancer.dto.DispatchPlanResponse;
import com.rishitbal.dispatch_load_balancer.dto.UnassignedOrderResponse;
import com.rishitbal.dispatch_load_balancer.dto.VehicleDispatchPlan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class DispatchPlanMapper {

    DispatchPlanResponse toResponse(
            DispatchAllocator.AllocationResult allocationResult
    ) {
        List<VehicleDispatchPlan> dispatchPlan = allocationResult.vehicleStates()
                .stream()
                .map(this::toVehicleDispatchPlan)
                .toList();

        List<UnassignedOrderResponse> unassignedOrders = allocationResult
                .unassignedOrders()
                .stream()
                .map(this::toUnassignedOrderResponse)
                .toList();

        return new DispatchPlanResponse(dispatchPlan, unassignedOrders);
    }

    private VehicleDispatchPlan toVehicleDispatchPlan(
            DispatchAllocator.VehicleRouteState vehicleState
    ) {
        List<AssignedOrderResponse> assignedOrders = vehicleState
                .getAssignedOrders()
                .stream()
                .map(this::toAssignedOrderResponse)
                .toList();

        return new VehicleDispatchPlan(
                vehicleState.getVehicle().getVehicleId(),
                vehicleState.getTotalLoad(),
                formatDistance(vehicleState.getTotalDistance()),
                assignedOrders
        );
    }

    private AssignedOrderResponse toAssignedOrderResponse(
            DeliveryOrder order
    ) {
        return new AssignedOrderResponse(
                order.getOrderId(),
                order.getLatitude(),
                order.getLongitude(),
                order.getAddress(),
                order.getPackageWeight(),
                order.getPriority()
        );
    }

    private UnassignedOrderResponse toUnassignedOrderResponse(
            DeliveryOrder order
    ) {
        return new UnassignedOrderResponse(
                order.getOrderId(),
                order.getLatitude(),
                order.getLongitude(),
                order.getAddress(),
                order.getPackageWeight(),
                order.getPriority(),
                "No vehicle has sufficient remaining capacity."
        );
    }

    private String formatDistance(double distance) {
        return String.format(Locale.ROOT, "%.1f km", distance);
    }
}