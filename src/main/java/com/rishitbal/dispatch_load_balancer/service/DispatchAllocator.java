package com.rishitbal.dispatch_load_balancer.service;

import com.rishitbal.dispatch_load_balancer.domain.DeliveryOrder;
import com.rishitbal.dispatch_load_balancer.domain.Priority;
import com.rishitbal.dispatch_load_balancer.domain.Vehicle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DispatchAllocator {

    private static final double DISTANCE_TOLERANCE = 0.000001;
    private final DistanceCalculator distanceCalculator;

    AllocationResult allocate(
            List<DeliveryOrder> orders,
            List<Vehicle> vehicles
    ) {
        List<VehicleRouteState> vehicleStates = vehicles.stream()
                .map(VehicleRouteState::new)
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

        return new AllocationResult(vehicleStates, unassignedOrders);
    }

    private void allocatePriorityGroup(
            Priority priority,
            List<DeliveryOrder> allOrders,
            List<VehicleRouteState> vehicleStates,
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

    private AssignmentCandidate findBestCandidate(
            List<VehicleRouteState> vehicleStates,
            List<DeliveryOrder> remainingOrders
    ) {
        AssignmentCandidate bestCandidate = null;

        for (VehicleRouteState vehicleState : vehicleStates) {
            for (DeliveryOrder order : remainingOrders) {
                if (!vehicleState.canCarry(order)) {
                    continue;
                }

                double distance = distanceCalculator.calculateDistanceInKilometers(
                        vehicleState.getRouteLatitude(),
                        vehicleState.getRouteLongitude(),
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
                first.vehicleState().getRemainingCapacity(),
                second.vehicleState().getRemainingCapacity()
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

        int vehicleIdComparison = first.vehicleState().getVehicle()
                .getVehicleId()
                .compareTo(second.vehicleState().getVehicle().getVehicleId());

        if (vehicleIdComparison != 0) {
            return vehicleIdComparison;
        }

        return first.order().getOrderId()
                .compareTo(second.order().getOrderId());
    }

    private void applyCandidate(AssignmentCandidate candidate) {
        candidate.vehicleState().assignOrder(
                candidate.order(),
                candidate.distance()
        );
    }

    record AllocationResult(
            List<VehicleRouteState> vehicleStates,
            List<DeliveryOrder> unassignedOrders
    ) {
    }

    private record AssignmentCandidate(
            VehicleRouteState vehicleState,
            DeliveryOrder order,
            double distance
    ) {
    }

    @Getter(AccessLevel.PACKAGE)
    static class VehicleRouteState {

        private final Vehicle vehicle;
        private double routeLatitude;
        private double routeLongitude;
        private long remainingCapacity;
        private long totalLoad;
        private double totalDistance;
        private final List<DeliveryOrder> assignedOrders = new ArrayList<>();

        VehicleRouteState(Vehicle vehicle) {
            this.vehicle = vehicle;
            this.routeLatitude = vehicle.getCurrentLatitude();
            this.routeLongitude = vehicle.getCurrentLongitude();
            this.remainingCapacity = vehicle.getCapacity();
        }

        boolean canCarry(DeliveryOrder order) {
            return remainingCapacity >= order.getPackageWeight();
        }

        void assignOrder(DeliveryOrder order, double distance) {
            assignedOrders.add(order);
            remainingCapacity -= order.getPackageWeight();
            totalLoad += order.getPackageWeight();
            totalDistance += distance;
            routeLatitude = order.getLatitude();
            routeLongitude = order.getLongitude();
        }
    }
}