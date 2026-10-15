package com.rishitbal.dispatch_load_balancer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rishitbal.dispatch_load_balancer.domain.Priority;
import com.rishitbal.dispatch_load_balancer.dto.DeliveryOrderRequest;
import com.rishitbal.dispatch_load_balancer.dto.VehicleRequest;
import com.rishitbal.dispatch_load_balancer.repository.DeliveryOrderRepository;
import com.rishitbal.dispatch_load_balancer.repository.VehicleRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class DispatchDataServiceTest {

    @Autowired
    private DeliveryOrderRepository deliveryOrderRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private DispatchDataService dispatchDataService;

    @BeforeEach
    void setUp() {
        dispatchDataService = new DispatchDataService(
                deliveryOrderRepository,
                vehicleRepository
        );
    }

    @Test
    void savesNewOrder() {
        DeliveryOrderRequest request = new DeliveryOrderRequest(
                "ORD-NEW",
                12.9716,
                77.5946,
                "MG Road, Bengaluru",
                10L,
                Priority.HIGH
        );

        dispatchDataService.upsertOrders(List.of(request));

        assertEquals(1, deliveryOrderRepository.count());

        var savedOrder = deliveryOrderRepository
                .findByOrderId("ORD-NEW")
                .orElseThrow();

        assertEquals("MG Road, Bengaluru", savedOrder.getAddress());
        assertEquals(10L, savedOrder.getPackageWeight());
        assertEquals(Priority.HIGH, savedOrder.getPriority());
    }

    @Test
    void updatesExistingOrderInsteadOfCreatingDuplicate() {
        DeliveryOrderRequest initialRequest = new DeliveryOrderRequest(
                "ORD001",
                12.9716,
                77.5946,
                "MG Road, Bengaluru",
                10L,
                Priority.HIGH
        );

        dispatchDataService.upsertOrders(List.of(initialRequest));

        DeliveryOrderRequest updatedRequest = new DeliveryOrderRequest(
                "ORD001",
                13.0827,
                80.2707,
                "Anna Salai, Chennai",
                20L,
                Priority.MEDIUM
        );

        dispatchDataService.upsertOrders(List.of(updatedRequest));

        assertEquals(1, deliveryOrderRepository.count());

        var savedOrder = deliveryOrderRepository
                .findByOrderId("ORD001")
                .orElseThrow();

        assertEquals(20L, savedOrder.getPackageWeight());
        assertEquals(Priority.MEDIUM, savedOrder.getPriority());
        assertEquals("Anna Salai, Chennai", savedOrder.getAddress());
    }

    @Test
    void savesNewVehicle() {
        VehicleRequest request = new VehicleRequest(
                "VEH-NEW",
                100L,
                12.9716,
                77.6413,
                "Indiranagar, Bengaluru"
        );

        dispatchDataService.upsertVehicles(List.of(request));

        assertEquals(1, vehicleRepository.count());

        var savedVehicle = vehicleRepository
                .findByVehicleId("VEH-NEW")
                .orElseThrow();

        assertEquals(100L, savedVehicle.getCapacity());
        assertEquals(
                "Indiranagar, Bengaluru",
                savedVehicle.getCurrentAddress()
        );
    }

    @Test
    void updatesExistingVehicleInsteadOfCreatingDuplicate() {
        VehicleRequest initialRequest = new VehicleRequest(
                "VEH001",
                100L,
                12.9716,
                77.6413,
                "Indiranagar, Bengaluru"
        );

        dispatchDataService.upsertVehicles(List.of(initialRequest));

        VehicleRequest updatedRequest = new VehicleRequest(
                "VEH001",
                150L,
                13.0674,
                80.2376,
                "T Nagar, Chennai"
        );

        dispatchDataService.upsertVehicles(List.of(updatedRequest));

        assertEquals(1, vehicleRepository.count());

        var savedVehicle = vehicleRepository
                .findByVehicleId("VEH001")
                .orElseThrow();

        assertEquals(150L, savedVehicle.getCapacity());
        assertEquals("T Nagar, Chennai", savedVehicle.getCurrentAddress());
    }

}