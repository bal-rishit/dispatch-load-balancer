package com.rishitbal.dispatch_load_balancer.dto;

import jakarta.validation.constraints.*;

public record VehicleRequest(
        @NotBlank(message = "vehicleId is required")
        String vehicleId,

        @NotNull(message = "capacity is required")
        @Positive(message = "capacity should be above 0")
        Long capacity,

        @NotNull(message = "latitude is required")
        @DecimalMin(value = "-90.0", message = "latitude must be atleast -90")
        @DecimalMax(value = "90.0", message = "latitude must not exceed 90")
        Double currentLatitude,

        @NotNull(message = "longitude is required")
        @DecimalMin(value = "-180.0", message = "longitude must be atleast -180")
        @DecimalMax(value = "180.0", message = "longitude must not exceed 180")
        Double currentLongitude,

        @NotBlank(message = "address is required")
        String currentAddress
) {
}
