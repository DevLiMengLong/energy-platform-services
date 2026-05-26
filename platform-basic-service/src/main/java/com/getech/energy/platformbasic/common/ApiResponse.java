package com.getech.energy.platformbasic.common;

public record ApiResponse<T>(String code, String message, String traceId, T data) {

    public static <T> ApiResponse<T> ok(T data, String traceId) {
        return new ApiResponse<>("SUCCESS", "OK", traceId, data);
    }

    public static <T> ApiResponse<T> fail(String code, String message, String traceId) {
        return new ApiResponse<>(code, message, traceId, null);
    }
}
