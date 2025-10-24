package com.example.food_order.dto.response;

import com.example.food_order.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private String statusCode;

    private String statusMsg;
    private String timestamp;

    private String spanId;
    private T data;

    // Helper method for Success with statusCode defaulting to "MDB-200"
    public static <T> ApiResponse<T> success(T data, String spanId) {
        return success(data, spanId, "MDB-200"); // Default statusCode to "MDB-200"
    }

    // Helper method for Success with a custom statusCode
    public static <T> ApiResponse<T> success(T data, String spanId, String statusCode) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .statusMsg("SUCCESS")
                .timestamp(DateTimeUtil.formatDateTime(OffsetDateTime.now()))
                .spanId(spanId)
                .data(data)
                .build();
    }

    // Helper method สำหรับ Error Response
    public static <T> ApiResponse<T> error(String message, String spanId, String statusCode) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .statusMsg(message)
                .timestamp(DateTimeUtil.formatDateTime(OffsetDateTime.now()))
                .spanId(spanId)
                .build();
    }
}
