package com.rishitbal.dispatch_load_balancer.controller;

import com.rishitbal.dispatch_load_balancer.dto.ApiResponse;
import com.rishitbal.dispatch_load_balancer.dto.DispatchPlanResponse;
import com.rishitbal.dispatch_load_balancer.dto.OrdersRequest;
import com.rishitbal.dispatch_load_balancer.dto.VehiclesRequest;
import com.rishitbal.dispatch_load_balancer.service.DispatchDataService;
import com.rishitbal.dispatch_load_balancer.service.DispatchPlanningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {
    private final DispatchDataService dispatchDataService;
    private final DispatchPlanningService dispatchPlanningService;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse> acceptOrders(@Valid @RequestBody OrdersRequest request){
        dispatchDataService.upsertOrders(request.orders());

        return ResponseEntity.ok(new ApiResponse("Delivery orders accepted.", "success"));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<ApiResponse> acceptVehicles(@Valid @RequestBody VehiclesRequest request){
        dispatchDataService.upsertVehicles(request.vehicles());

        return ResponseEntity.ok(new ApiResponse("Vehicles details accepted.", "success"));
    }


}
