package com.rishitbal.dispatch_load_balancer.dto;

import java.util.Map;

public record ValidationErrorResponse(
        int status,
        String message,
        Map<String, String> errors
) {
}
