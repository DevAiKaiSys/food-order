package com.example.food_order.dto.response;

import com.example.food_order.util.DateTimeUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class ApiResponse<T> {

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("status_msg")
    private String statusMsg;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("span_id")
    private String spanId;

    @JsonProperty("data")
    private T data;

    public static <T> ApiResponse<T> success(T data, String spanId) {
        return ApiResponse.<T>builder()
                .statusCode("MDB-200")
                .statusMsg("SUCCESS")
                .timestamp(DateTimeUtil.formatDateTime(OffsetDateTime.now()))
                .spanId(spanId)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, String spanId) {
        return ApiResponse.<T>builder()
                .statusCode("MDB-" + code)
                .statusMsg(message)
                .timestamp(DateTimeUtil.formatDateTime(OffsetDateTime.now()))
                .spanId(spanId)
                .build();
    }
}
