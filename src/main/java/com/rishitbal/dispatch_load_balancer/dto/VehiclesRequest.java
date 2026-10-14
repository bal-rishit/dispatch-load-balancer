package com.rishitbal.dispatch_load_balancer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VehiclesRequest(
        @NotEmpty(message = "vehicles must not be empty")
        List<@Valid VehicleRequest> vehicles
) {
}
