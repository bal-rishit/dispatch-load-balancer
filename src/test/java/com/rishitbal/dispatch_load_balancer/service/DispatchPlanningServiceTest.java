package com.rishitbal.dispatch_load_balancer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.rishitbal.dispatch_load_balancer.domain.DeliveryOrder;
import com.rishitbal.dispatch_load_balancer.domain.Priority;
import com.rishitbal.dispatch_load_balancer.domain.Vehicle;
import com.rishitbal.dispatch_load_balancer.dto.DispatchPlanResponse;
import com.rishitbal.dispatch_load_balancer.repository.DeliveryOrderRepository;
import com.rishitbal.dispatch_load_balancer.repository.VehicleRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DispatchPlanningServiceTest {

    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private DispatchPlanningService dispatchPlanningService;

    @BeforeEach
    void setUp() {
        dispatchPlanningService = new DispatchPlanningService(
                deliveryOrderRepository,
                vehicleRepository,
                new DispatchAllocator(new DistanceCalculator()),
                new DispatchPlanMapper()
        );
    }

    @Test
    void marksOrderAsUnassignedWhenNoVehicleHasEnoughCapacity() {
        Vehicle vehicle = new Vehicle(
                "VEH001",
                10,
                12.9716,
                77.5946,
                "Bengaluru"
        );

        DeliveryOrder order = new DeliveryOrder(
                "ORD001",
                12.9716,
                77.5946,
                "MG Road, Bengaluru",
                11,
                Priority.HIGH
        );

        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
        when(deliveryOrderRepository.findAll()).thenReturn(List.of(order));

        DispatchPlanResponse response = dispatchPlanningService.generatePlan();

        assertEquals(1, response.dispatchPlan().size());
        assertTrue(response.dispatchPlan().get(0).assignedOrders().isEmpty());

        assertEquals(1, response.unassignedOrders().size());
        assertEquals(
                "ORD001",
                response.unassignedOrders().get(0).orderId()
        );
    }

    @Test
    void assignsHighPriorityOrderBeforeACloserLowPriorityOrder() {
        Vehicle vehicle = new Vehicle(
                "VEH001",
                10,
                0.0,
                0.0,
                "Origin"
        );

        DeliveryOrder highPriorityOrder = new DeliveryOrder(
                "ORD-HIGH",
                0.0,
                1.0,
                "High-priority destination",
                10,
                Priority.HIGH
        );

        DeliveryOrder lowPriorityOrder = new DeliveryOrder(
                "ORD-LOW",
                0.0,
                0.0,
                "Low-priority destination",
                10,
                Priority.LOW
        );

        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
        when(deliveryOrderRepository.findAll()).thenReturn(
                List.of(lowPriorityOrder, highPriorityOrder)
        );

        DispatchPlanResponse response = dispatchPlanningService.generatePlan();

        assertEquals(
                "ORD-HIGH",
                response.dispatchPlan()
                        .get(0)
                        .assignedOrders()
                        .get(0)
                        .orderId()
        );

        assertEquals(
                "ORD-LOW",
                response.unassignedOrders().get(0).orderId()
        );
    }

    @Test //For e.g. on x axis Vehicle 2 is at 0, order 1 at 9 vehicle 1 at 17 and order 2 at 20. Then optimal solution
    // would be for vehicle 1 to go for order2 (total 3 km) and vehicle 2 to go for order1 (total 9 km)
    void selectsTheGloballyShortestPairWithinTheSamePriority() {
        Vehicle vehicleAtSeventeen = new Vehicle(
                "VEH001",
                100,
                0.0,
                17.0,
                "Point 17"
        );

        Vehicle vehicleAtZero = new Vehicle(
                "VEH002",
                100,
                0.0,
                0.0,
                "Point 0"
        );

        DeliveryOrder orderAtNine = new DeliveryOrder(
                "ORD001",
                0.0,
                9.0,
                "Point 9",
                10,
                Priority.HIGH
        );

        DeliveryOrder orderAtTwenty = new DeliveryOrder(
                "ORD002",
                0.0,
                20.0,
                "Point 20",
                10,
                Priority.HIGH
        );

        when(vehicleRepository.findAll()).thenReturn(
                List.of(vehicleAtZero, vehicleAtSeventeen)
        );

        when(deliveryOrderRepository.findAll()).thenReturn(
                List.of(orderAtNine, orderAtTwenty)
        );

        DispatchPlanResponse response = dispatchPlanningService.generatePlan();

        assertEquals(
                "ORD002",
                response.dispatchPlan().stream()
                        .filter(plan -> plan.vehicleId().equals("VEH001"))
                        .findFirst()
                        .orElseThrow()
                        .assignedOrders()
                        .get(0)
                        .orderId()
        );

        assertEquals(
                "ORD001",
                response.dispatchPlan().stream()
                        .filter(plan -> plan.vehicleId().equals("VEH002"))
                        .findFirst()
                        .orElseThrow()
                        .assignedOrders()
                        .get(0)
                        .orderId()
        );
    }

    @Test //This way capacity is optimized for more orders to be assigned
    void choosesVehicleWithSmallestSufficientRemainingCapacityWhenDistanceTies() {
        Vehicle largeVehicle = new Vehicle(
                "VEH001",
                100,
                0.0,
                0.0,
                "Origin"
        );

        Vehicle bestFitVehicle = new Vehicle(
                "VEH002",
                15,
                0.0,
                0.0,
                "Origin"
        );

        DeliveryOrder order = new DeliveryOrder(
                "ORD001",
                0.0,
                0.0,
                "Same location",
                10,
                Priority.HIGH
        );

        when(vehicleRepository.findAll()).thenReturn(
                List.of(largeVehicle, bestFitVehicle)
        );

        when(deliveryOrderRepository.findAll()).thenReturn(List.of(order));

        DispatchPlanResponse response = dispatchPlanningService.generatePlan();

        assertEquals(
                "ORD001",
                response.dispatchPlan().stream()
                        .filter(plan -> plan.vehicleId().equals("VEH002"))
                        .findFirst()
                        .orElseThrow()
                        .assignedOrders()
                        .get(0)
                        .orderId()
        );

        assertTrue(
                response.dispatchPlan().stream()
                        .filter(plan -> plan.vehicleId().equals("VEH001"))
                        .findFirst()
                        .orElseThrow()
                        .assignedOrders()
                        .isEmpty()
        );
    }

}