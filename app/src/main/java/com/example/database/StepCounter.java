package com.example.database;

public class StepCounter {

    private int steps = 0;

    private float previousMagnitude = 0;
    private float previousPreviousMagnitude = 0;

    private long lastStepTime = 0;

    // ค่าที่ใช้เริ่มต้นสำหรับตรวจจับก้าว
    private static final float THRESHOLD = 1.2f;

    // ป้องกันการนับซ้ำจากการสั่นเร็ว ๆ
    private static final long MIN_STEP_INTERVAL = 300;

    public int processAcceleration(
            float x,
            float y,
            float z,
            long timestamp) {

        // คำนวณขนาดความเร่งรวม
        float magnitude = (float) Math.sqrt(
                x * x + y * y + z * z
        );

        // ตัดผลของแรงโน้มถ่วงออก
        float linearMagnitude =
                Math.abs(magnitude - 9.81f);

        // ตรวจสอบว่าค่าก่อนหน้าเป็นจุดสูงสุดหรือไม่
        boolean isPeak =
                previousMagnitude > previousPreviousMagnitude
                        && previousMagnitude > linearMagnitude
                        && previousMagnitude > THRESHOLD;

        // ถ้าเป็น peak และห่างจากก้าวก่อนหน้าอย่างน้อย 300 ms
        if (isPeak &&
                (timestamp - lastStepTime) >= MIN_STEP_INTERVAL) {

            steps++;
            lastStepTime = timestamp;
        }

        // เก็บค่าก่อนหน้าไว้ใช้ตรวจสอบรอบต่อไป
        previousPreviousMagnitude = previousMagnitude;
        previousMagnitude = linearMagnitude;

        return steps;
    }

    public int getSteps() {
        return steps;
    }

    public void reset() {
        steps = 0;
        previousMagnitude = 0;
        previousPreviousMagnitude = 0;
        lastStepTime = 0;
    }
}