package com.rishitbal.dispatch_load_balancer.dto;

import java.util.Map;


//Response returned when request validation fails
public record ValidationErrorResponse(
        int status, // 400
        String message, // "validation failed"
        Map<String, String> errors
) {
}
