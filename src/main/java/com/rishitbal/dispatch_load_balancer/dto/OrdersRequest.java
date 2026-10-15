package com.rishitbal.dispatch_load_balancer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

//Request body containing list of all the orders
public record OrdersRequest(
        @NotEmpty(message = "orders cant be empty")
        List<@Valid DeliveryOrderRequest> orders
) {
}
