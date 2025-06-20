package com.example.restservice.model;

import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(String status, String message, T data, Object metadata) {

    public static <T> ResponseEntity<ApiResponse<T>> ok(String status, String message, T data, Object metadata) {
        ApiResponse<T> apiResponse = new ApiResponse<>(status, message, data, metadata);
        return ResponseEntity.ok(apiResponse);
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String status, String message, T data, Object metadata) {
        ApiResponse<T> apiResponse = new ApiResponse<>(status, message, data, metadata);
        return ResponseEntity.internalServerError().body(apiResponse);
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String status, String message, T data, Object metadata) {
        ApiResponse<T> apiResponse = new ApiResponse<>(status, message, data, metadata);
        return ResponseEntity.badRequest().body(apiResponse);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok() {
        return ok("success", null, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message) {
        return ok("success", message, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok("success", null, data, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ok("success", message, data, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError() {
        return internalServerError("error", null, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return internalServerError("error", message, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(T data) {
        return internalServerError("error", null, data, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String message, T data) {
        return internalServerError("error", message, data, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest() {
        return badRequest("error", null, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return badRequest("error", message, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(T data) {
        return badRequest("error", null, data, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message, T data) {
        return badRequest("error", message, data, null);
    }
}
