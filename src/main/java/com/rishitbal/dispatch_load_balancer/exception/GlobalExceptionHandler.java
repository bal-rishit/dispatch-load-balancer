package com.rishitbal.dispatch_load_balancer.exception;

import com.rishitbal.dispatch_load_balancer.dto.ApiResponse;
import com.rishitbal.dispatch_load_balancer.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException exception){
        Map<String,String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(),error.getDefaultMessage()));

        ValidationErrorResponse response = new ValidationErrorResponse(
                400,
                "Validation failed.",
                errors
        );

        return ResponseEntity.badRequest().body(response);

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleMalformedJson(HttpMessageNotReadableException exception){

        ValidationErrorResponse response = new ValidationErrorResponse(
                400,
                "request body is malformed.",
                Map.of("request", "Request body contains malformed Json or invalid value.")
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DispatchDataUnavailableException.class)
    public ResponseEntity<ApiResponse> handleDispatchDataUnavailable(
            DispatchDataUnavailableException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse(
                        exception.getMessage(),
                        "error"
                ));
    }



}
