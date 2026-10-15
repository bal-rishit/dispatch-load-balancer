package com.rishitbal.dispatch_load_balancer.service;

import com.rishitbal.dispatch_load_balancer.domain.DeliveryOrder;
import com.rishitbal.dispatch_load_balancer.domain.Priority;
import com.rishitbal.dispatch_load_balancer.domain.Vehicle;
import com.rishitbal.dispatch_load_balancer.repository.DeliveryOrderRepository;
import com.rishitbal.dispatch_load_balancer.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import com.rishitbal.dispatch_load_balancer.dto.AssignedOrderResponse;
import com.rishitbal.dispatch_load_balancer.dto.DispatchPlanResponse;
import com.rishitbal.dispatch_load_balancer.dto.UnassignedOrderResponse;
import com.rishitbal.dispatch_load_balancer.dto.VehicleDispatchPlan;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DispatchPlanningService {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final DistanceCalculator distanceCalculator;
    private static final double DISTANCE_TOLERANCE = 0.000001;

    private static class VehicleState {

        private final Vehicle vehicle;
        private double routeLatitude;
        private double routeLongitude;
        private long remainingCapacity;
        private long totalLoad;
        private double totalDistance;
        private final List<DeliveryOrder> assignedOrders = new ArrayList<>();

        private VehicleState(Vehicle vehicle) {
            this.vehicle = vehicle;
            this.routeLatitude = vehicle.getCurrentLatitude();
            this.routeLongitude = vehicle.getCurrentLongitude();
            this.remainingCapacity = vehicle.getCapacity();
            this.totalLoad = 0;
            this.totalDistance = 0;
        }
    }

    private record AssignmentCandidate(
            VehicleState vehicleState,
            DeliveryOrder order,
            double distance
    ) {
    }

    private AssignmentCandidate findBestCandidate(
            List<VehicleState> vehicleStates,
            List<DeliveryOrder> remainingOrders
    ) {
        AssignmentCandidate bestCandidate = null;

        for (VehicleState vehicleState : vehicleStates) {
            for (DeliveryOrder order : remainingOrders) {
                if (vehicleState.remainingCapacity < order.getPackageWeight()) {
                    continue;
                }

                double distance = distanceCalculator.calculateDistanceInKilometers(
                        vehicleState.routeLatitude,
                        vehicleState.routeLongitude,
                        order.getLatitude(),
                        order.getLongitude()
                );

                AssignmentCandidate candidate = new AssignmentCandidate(
                        vehicleState,
                        order,
                        distance
                );

                if (bestCandidate == null
                        || compareCandidates(candidate, bestCandidate) < 0) {
                    bestCandidate = candidate;
                }
            }
        }

        return bestCandidate;
    }

    private int compareCandidates(
            AssignmentCandidate first,
            AssignmentCandidate second
    ) {
        double distanceDifference = first.distance() - second.distance();

        if (Math.abs(distanceDifference) > DISTANCE_TOLERANCE) {
            return Double.compare(first.distance(), second.distance());
        }

        int capacityComparison = Long.compare(
                first.vehicleState().remainingCapacity,
                second.vehicleState().remainingCapacity
        );

        if (capacityComparison != 0) {
            return capacityComparison;
        }

        int weightComparison = Long.compare(
                second.order().getPackageWeight(),
                first.order().getPackageWeight()
        );

        if (weightComparison != 0) {
            return weightComparison;
        }

        int vehicleIdComparison = first.vehicleState().vehicle.getVehicleId()
                .compareTo(second.vehicleState().vehicle.getVehicleId());

        if (vehicleIdComparison != 0) {
            return vehicleIdComparison;
        }

        return first.order().getOrderId()
                .compareTo(second.order().getOrderId());
    }

    private void applyCandidate(AssignmentCandidate candidate) {
        VehicleState vehicleState = candidate.vehicleState();
        DeliveryOrder order = candidate.order();

        vehicleState.assignedOrders.add(order);
        vehicleState.remainingCapacity -= order.getPackageWeight();
        vehicleState.totalLoad += order.getPackageWeight();
        vehicleState.totalDistance += candidate.distance();
        vehicleState.routeLatitude = order.getLatitude();
        vehicleState.routeLongitude = order.getLongitude();
    }

    private void allocatePriorityGroup(
            Priority priority,
            List<DeliveryOrder> allOrders,
            List<VehicleState> vehicleStates,
            List<DeliveryOrder> unassignedOrders
    ) {
        List<DeliveryOrder> remainingOrders = allOrders.stream()
                .filter(order -> order.getPriority() == priority)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        while (!remainingOrders.isEmpty()) {
            AssignmentCandidate bestCandidate = findBestCandidate(
                    vehicleStates,
                    remainingOrders
            );

            if (bestCandidate == null) {
                unassignedOrders.addAll(remainingOrders);
                return;
            }

            applyCandidate(bestCandidate);
            remainingOrders.remove(bestCandidate.order());
        }
    }

    public DispatchPlanResponse generatePlan() {
        List<DeliveryOrder> orders = deliveryOrderRepository.findAll();

        List<VehicleState> vehicleStates = vehicleRepository.findAll()
                .stream()
                .map(VehicleState::new)
                .toList();

        List<DeliveryOrder> unassignedOrders = new ArrayList<>();

        allocatePriorityGroup(
                Priority.HIGH,
                orders,
                vehicleStates,
                unassignedOrders
        );

        allocatePriorityGroup(
                Priority.MEDIUM,
                orders,
                vehicleStates,
                unassignedOrders
        );

        allocatePriorityGroup(
                Priority.LOW,
                orders,
                vehicleStates,
                unassignedOrders
        );

        return buildPlanResponse(vehicleStates, unassignedOrders);
    }

    private DispatchPlanResponse buildPlanResponse(
            List<VehicleState> vehicleStates,
            List<DeliveryOrder> unassignedOrders
    ) {
        List<VehicleDispatchPlan> dispatchPlan = vehicleStates.stream()
                .map(this::toVehicleDispatchPlan)
                .toList();

        List<UnassignedOrderResponse> unassignedOrderResponses = unassignedOrders.stream()
                .map(this::toUnassignedOrderResponse)
                .toList();

        return new DispatchPlanResponse(
                dispatchPlan,
                unassignedOrderResponses
        );
    }

    private VehicleDispatchPlan toVehicleDispatchPlan(
            VehicleState vehicleState
    ) {
        List<AssignedOrderResponse> assignedOrders = vehicleState.assignedOrders.stream()
                .map(this::toAssignedOrderResponse)
                .toList();

        return new VehicleDispatchPlan(
                vehicleState.vehicle.getVehicleId(),
                vehicleState.totalLoad,
                formatDistance(vehicleState.totalDistance),
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
