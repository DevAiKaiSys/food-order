package com.example.food_order.util;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

public class TracerUtil {

    public static String getSpanId(Tracer tracer) {
        if (tracer == null) {
            return "unknown";
        }
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            return currentSpan.context().spanId();
        }
        return "unknown";
    }
}
