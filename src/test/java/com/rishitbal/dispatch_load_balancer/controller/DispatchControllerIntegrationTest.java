package com.rishitbal.dispatch_load_balancer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rishitbal.dispatch_load_balancer.domain.DeliveryOrder;
import com.rishitbal.dispatch_load_balancer.domain.Priority;
import com.rishitbal.dispatch_load_balancer.domain.Vehicle;
import com.rishitbal.dispatch_load_balancer.repository.DeliveryOrderRepository;
import com.rishitbal.dispatch_load_balancer.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DispatchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeliveryOrderRepository deliveryOrderRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @BeforeEach
    void clearDatabase() {
        deliveryOrderRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    @Test
    void acceptsDataAndReturnsDispatchPlan() throws Exception {
        mockMvc.perform(post("/api/dispatch/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicles": [
                                    {
                                      "vehicleId": "VEH001",
                                      "capacity": 100,
                                      "currentLatitude": 12.9716,
                                      "currentLongitude": 77.5946,
                                      "currentAddress": "Bengaluru"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orders": [
                                    {
                                      "orderId": "ORD001",
                                      "latitude": 12.9716,
                                      "longitude": 77.5946,
                                      "address": "MG Road, Bengaluru",
                                      "packageWeight": 10,
                                      "priority": "HIGH"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dispatchPlan[0].vehicleId")
                        .value("VEH001"))
                .andExpect(jsonPath("$.dispatchPlan[0].totalLoad")
                        .value(10))
                .andExpect(jsonPath(
                        "$.dispatchPlan[0].assignedOrders[0].orderId"
                ).value("ORD001"))
                .andExpect(jsonPath("$.unassignedOrders").isEmpty());
    }

    @Test
    void rejectsInvalidOrderRequestWithStructuredValidationError() throws Exception {
        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "orders": [
                                {
                                  "orderId": "",
                                  "latitude": 91,
                                  "longitude": 77.5946,
                                  "address": "",
                                  "packageWeight": 0,
                                  "priority": "HIGH"
                                }
                              ]
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed."))
                .andExpect(jsonPath(
                        "$.errors['orders[0].orderId']"
                ).exists());
    }

    @Test
    void rejectsUnknownPriorityWithMalformedRequestError() throws Exception {
        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "orders": [
                                {
                                  "orderId": "ORD001",
                                  "latitude": 12.9716,
                                  "longitude": 77.5946,
                                  "address": "MG Road, Bengaluru",
                                  "packageWeight": 10,
                                  "priority": "URGENT"
                                }
                              ]
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("request body is malformed."))
                .andExpect(jsonPath("$.errors.request").exists());
    }

    @Test
    void rejectsPlanGenerationWhenNoVehiclesOrOrdersExist() throws Exception {
        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(
                        "No vehicles or delivery orders are available to generate a dispatch plan."
                ));
    }

    @Test
    void rejectsPlanGenerationWhenNoVehiclesExist() throws Exception {
        deliveryOrderRepository.save(new DeliveryOrder(
                "ORD001",
                12.9716,
                77.5946,
                "MG Road, Bengaluru",
                10,
                Priority.HIGH
        ));

        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(
                        "No vehicles are available to generate a dispatch plan."
                ));
    }

    @Test
    void rejectsPlanGenerationWhenNoOrdersExist() throws Exception {
        vehicleRepository.save(new Vehicle(
                "VEH001",
                100,
                12.9716,
                77.5946,
                "Bengaluru"
        ));

        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(
                        "No delivery orders are available to generate a dispatch plan."
                ));
    }

}