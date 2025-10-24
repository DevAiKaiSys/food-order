package com.example.food_order.util;

import com.example.food_order.entity.Order;

import java.time.ZonedDateTime;

public class TestUtil {
    // ฟังก์ชันที่ใช้ในการตั้งค่า CreatedAt และ UpdatedAt สำหรับ Order
    public static void setTimestamps(Order order) {
        ZonedDateTime now = ZonedDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
    }
}
