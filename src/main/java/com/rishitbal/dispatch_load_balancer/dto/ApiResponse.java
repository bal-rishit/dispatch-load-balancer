package com.rishitbal.dispatch_load_balancer.dto;

// Response format sent by api endpoints along with relevant data
public record ApiResponse(
        String message, // eg: "Delivery orders accepted"
        String status  // eg: "sucess"
) {
}
