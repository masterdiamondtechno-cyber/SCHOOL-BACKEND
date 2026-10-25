package com.schoolmanagement;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== Bad / malformed JSON body =====
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadJson(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 400);
        response.put("status", "FAILED");
        response.put("message", "Invalid or malformed JSON request body");
        response.put("data", null);
        return ResponseEntity.badRequest().body(response);
    }

    // ===== 400 - validation errors =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Validation failed");
        response.put("code", 400);
        response.put("status", "FAILED");
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.badRequest().body(response);
    }

    // ===== 404 - no endpoint found =====
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoHandlerFoundException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 404);
        response.put("status", "FAILED");
        response.put("message", "API endpoint not found: " + ex.getRequestURL());
        response.put("data", null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ===== RuntimeException - returns the actual error message =====
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 400);
        response.put("status", "FAILED");
        response.put("message", ex.getMessage());
        response.put("data", null);
        return ResponseEntity.badRequest().body(response);
    }

    // ===== Generic fallback - catches anything not handled above =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 500);
        response.put("status", "FAILED");
        response.put("message", "Internal server error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        response.put("data", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
