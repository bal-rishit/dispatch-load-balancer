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

// DispatchAllocator contains the logic to allocate delivery orders to nearby vehicles
// based on various rules and constraints
@Component
@RequiredArgsConstructor
public class DispatchAllocator {

    private final DistanceCalculator distanceCalculator; // Uses Haversine formula

    // My logic assumes vehicle moves to the position of the order after it is assigned to it,
    // which changes its position(called temporary route here) wrt to other orders
    // Therefore AllocationResult maintains temporary route positions for each vehicle and assigns orders accordingly
    AllocationResult allocate(
            List<DeliveryOrder> orders,
            List<Vehicle> vehicles
    ) {
        List<VehicleRouteState> vehicleStates = vehicles.stream()
                .map(VehicleRouteState::new)
                .toList();

        List<DeliveryOrder> unassignedOrders = new ArrayList<>();

        //Allocating HIGH priority orders first and assigning them to the vehicles
        allocatePriorityGroup(
                Priority.HIGH,
                orders,
                vehicleStates,
                unassignedOrders
        );

        //Allocating MEDIUM priority orders second and assigning them to the vehicles
        allocatePriorityGroup(
                Priority.MEDIUM,
                orders,
                vehicleStates,
                unassignedOrders
        );

        //Allocating LOW priority orders last and assigning them to the vehicles
        allocatePriorityGroup(
                Priority.LOW,
                orders,
                vehicleStates,
                unassignedOrders
        );

        return new AllocationResult(vehicleStates, unassignedOrders);
    }

    // Allocates all orders of one priority level and assigns them to the vehicles
    // If no remaining vehicle can carry an order, that order is marked unassigned.
    private void allocatePriorityGroup(
            Priority priority,
            List<DeliveryOrder> allOrders,
            List<VehicleRouteState> vehicleStates,
            List<DeliveryOrder> unassignedOrders
    ) {
        List<DeliveryOrder> remainingOrders = allOrders.stream()
                .filter(order -> order.getPriority() == priority)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // Keeps assigning orders in loop until either: all orders are assigned to vehicles
        // or remaining orders cant be assigned to valid vehicles
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


    // Creates valid vehicle-order pair and finds the best valid vehicle-order pair based on distance, capacity, packageWeight and vehicleOrder.
    // A pair is valid only when the vehicle has enough remaining capacity.
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

    //Compares vehicle-order pairs based on their distance, vehicle capacity and order weight
    private int compareCandidates(
            AssignmentCandidate first,
            AssignmentCandidate second
    ) {
        double distanceDifference = first.distance() - second.distance();

        // First order of comparison: shortest distance
        // Assume two distances to be same if there difference fall within 0.000001 km to avoid floating.point round issues
        if (Math.abs(distanceDifference) > 0.000001) {
            return Double.compare(first.distance(), second.distance());
        }

        // Second order of comparison: lowest sufficient remaining capacity
        int capacityComparison = Long.compare(
                first.vehicleState().getRemainingCapacity(),
                second.vehicleState().getRemainingCapacity()
        );


        if (capacityComparison != 0) {
            return capacityComparison;
        }

        // Third order of comparison: Heavier package weight
        int weightComparison = Long.compare(
                second.order().getPackageWeight(),
                first.order().getPackageWeight()
        );

        if (weightComparison != 0) {
            return weightComparison;
        }

        // Fourth order of comparison: VehicleId
        int vehicleIdComparison = first.vehicleState().getVehicle()
                .getVehicleId()
                .compareTo(second.vehicleState().getVehicle().getVehicleId());

        if (vehicleIdComparison != 0) {
            return vehicleIdComparison;
        }

        return first.order().getOrderId()
                .compareTo(second.order().getOrderId());
    }

    // Updates the selected vehicle's temporary route, load, remaining capacity,
    // and total distance after assigning an order.
    private void applyCandidate(AssignmentCandidate candidate) {
        candidate.vehicleState().assignOrder(
                candidate.order(),
                candidate.distance()
        );
    }

    // Internal result of allocation, containing updated vehicle position/state
    // and orders that could not be assigned.
    record AllocationResult(
            List<VehicleRouteState> vehicleStates,
            List<DeliveryOrder> unassignedOrders
    ) {
    }

    // Represents one possible vehicle-to-order pair/assignment and its travel distance.
    private record AssignmentCandidate(
            VehicleRouteState vehicleState,
            DeliveryOrder order,
            double distance
    ) {
    }


    // Temporary planning state for one vehicle.
    // This does not modify the vehicle data stored in the database.
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

        // Checks if this vehicle can carry the order
        boolean canCarry(DeliveryOrder order) {
            return remainingCapacity >= order.getPackageWeight();
        }

        // Assigns an order and moves the temporary route/position endpoint to that order's location.
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