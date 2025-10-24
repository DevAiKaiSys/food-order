package com.example.food_order.dto.response;

import com.example.food_order.util.TracerUtil;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private final Tracer tracer;

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().getName().contains("ApiResponse");
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {

        String path = request.getURI().getPath();
        if (!path.startsWith("/api/")) {
            return body;
        }

        // ถ้า body เป็น ApiResponse อยู่แล้ว (เช่น กรณี Error Handling) ไม่ต้องหุ้มซ้ำ
        if (body instanceof ApiResponse) {
            return body;
        }

        // ⬅️ ตรวจสอบ HTTP Status Code จาก response
        HttpStatus status = getHttpStatus(returnType);

        // หุ้มด้วย ApiResponse ตาม status code
        if (status == HttpStatus.CREATED) {
            return ApiResponse.success(body, TracerUtil.getSpanId(tracer), "MDB-201");
        }

        // หุ้ม Response ปกติด้วย ApiResponse.success
        return ApiResponse.success(body, TracerUtil.getSpanId(tracer));
    }

    /**
     * ดึง HTTP Status จาก @ResponseStatus annotation
     */
    private HttpStatus getHttpStatus(MethodParameter returnType) {
        // ตรวจสอบ @ResponseStatus ที่ method
        ResponseStatus methodAnnotation = returnType.getMethodAnnotation(ResponseStatus.class);
        if (methodAnnotation != null) {
            return methodAnnotation.value();
        }

        // ตรวจสอบ @ResponseStatus ที่ class (Controller level)
        ResponseStatus classAnnotation = returnType.getContainingClass().getAnnotation(ResponseStatus.class);
        if (classAnnotation != null) {
            return classAnnotation.value();
        }

        // Default เป็น 200 OK
        return HttpStatus.OK;
    }
}