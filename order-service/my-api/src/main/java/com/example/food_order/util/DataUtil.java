package com.example.food_order.util;

import java.util.UUID;

public class DataUtil {
    public static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
