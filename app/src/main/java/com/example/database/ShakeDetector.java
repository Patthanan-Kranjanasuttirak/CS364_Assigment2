package com.example.database;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

/** Wraps the accelerometer and reports shakes counted by {@link ShakeCounter}. */
public class ShakeDetector implements SensorEventListener {

    public interface OnShakeListener {
        /** Called on every counted shake. */
        void onShake(int totalShakes);
    }
    private boolean isListening = false;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final ShakeCounter counter = new ShakeCounter();
    private OnShakeListener listener;

    private long startElapsedMs = -1;

    public ShakeDetector(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager == null ? null
                : sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public boolean isAvailable() { return accelerometer != null; }

    public void setOnShakeListener(OnShakeListener l) { listener = l; }

    /** Resume listening without resetting the current session count. */
    public void start() {
        if (accelerometer == null) return;

        // กำหนดเวลาเริ่มต้นเฉพาะครั้งแรกเท่านั้น
        if (startElapsedMs < 0) {
            startElapsedMs = SystemClock.elapsedRealtime();
        }

        // เริ่มรับข้อมูล Sensor โดยไม่ Reset จำนวน Shake
        isListening = sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
        );
    }

    /** Stop listening (call from onPause) to save battery. */
    public void stop() {
        if (sensorManager != null && isListening) {
            sensorManager.unregisterListener(this);
            isListening = false;
        }
    }

    public int getShakeCount() { return counter.getShakeCount(); }

    public float getShakesPerMinute() {
        if (startElapsedMs < 0) return 0f;
        return ShakeCounter.shakesPerMinute(counter.getShakeCount(),
                SystemClock.elapsedRealtime() - startElapsedMs);
    }

    public void reset() {
        counter.reset();
        startElapsedMs = SystemClock.elapsedRealtime();
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        long nowMs = e.timestamp / 1_000_000L;

        // ตรวจสอบข้อมูล Accelerometer ที่ ShakeDetector ได้รับ
        Log.d("SHAKE_TEST",
                "X=" + e.values[0]
                        + ", Y=" + e.values[1]
                        + ", Z=" + e.values[2]);

        // ตรวจจับการเขย่า
        boolean detected = counter.onAcceleration(
                e.values[0],
                e.values[1],
                e.values[2],
                nowMs
        );

        Log.d("SHAKE_TEST",
                "Detected=" + detected
                        + ", Total=" + counter.getShakeCount());

        if (detected && listener != null) {
            Log.d("SHAKE_TEST",
                    "Shake detected: " + counter.getShakeCount());

            listener.onShake(counter.getShakeCount());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
