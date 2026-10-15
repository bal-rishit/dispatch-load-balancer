package com.rishitbal.dispatch_load_balancer.dto;

import com.rishitbal.dispatch_load_balancer.domain.Priority;
import jakarta.validation.constraints.*;


// Request body format of order
public record DeliveryOrderRequest(

        @NotBlank(message = "orderId is required")
        String orderId,

        @NotNull(message = "latitude is required")
        @DecimalMin(value = "-90.0", message = "latitude must be atleast -90")
        @DecimalMax(value = "90.0", message = "latitude must not exceed 90")
        Double latitude,

        @NotNull(message = "longitude is required")
        @DecimalMin(value = "-180.0", message = "longitude must be atleast -180")
        @DecimalMax(value = "180.0", message = "longitude must not exceed 180")
        Double longitude,

        @NotBlank(message = "address is required")
        String address,

        @NotNull(message = "packageWeight is required")
        @Positive(message = "packageWeight must be above 0")
        Long packageWeight,

        @NotNull(message = "priority is required")
        Priority priority

) {
}
