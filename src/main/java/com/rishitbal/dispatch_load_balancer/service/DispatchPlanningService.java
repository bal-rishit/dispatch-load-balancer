package com.rishitbal.dispatch_load_balancer.service;

import com.rishitbal.dispatch_load_balancer.domain.DeliveryOrder;
import com.rishitbal.dispatch_load_balancer.domain.Vehicle;
import com.rishitbal.dispatch_load_balancer.dto.DispatchPlanResponse;
import com.rishitbal.dispatch_load_balancer.exception.DispatchDataUnavailableException;
import com.rishitbal.dispatch_load_balancer.repository.DeliveryOrderRepository;
import com.rishitbal.dispatch_load_balancer.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchPlanningService {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchAllocator dispatchAllocator;
    private final DispatchPlanMapper dispatchPlanMapper;

    public DispatchPlanResponse generatePlan() {
        List<DeliveryOrder> orders = deliveryOrderRepository.findAll();
        List<Vehicle> vehicles = vehicleRepository.findAll();

        validateDispatchData(orders, vehicles);

        // Runs actual logic to allocate orders and vehicles
        DispatchAllocator.AllocationResult allocationResult =
                dispatchAllocator.allocate(orders, vehicles);

        // Converts results into API Response DTO
        return dispatchPlanMapper.toResponse(allocationResult);
    }

    // Ensures both vehicles and orders data are present before attempting to allocate
    private void validateDispatchData(
            List<DeliveryOrder> orders,
            List<Vehicle> vehicles
    ) {
        if (vehicles.isEmpty() && orders.isEmpty()) {
            throw new DispatchDataUnavailableException(
                    "No vehicles or delivery orders are available to generate a dispatch plan."
            );
        }

        if (vehicles.isEmpty()) {
            throw new DispatchDataUnavailableException(
                    "No vehicles are available to generate a dispatch plan."
            );
        }

        if (orders.isEmpty()) {
            throw new DispatchDataUnavailableException(
                    "No delivery orders are available to generate a dispatch plan."
            );
        }
    }
}