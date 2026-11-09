package com.example.cs364_assigment2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

/** Wraps the accelerometer and reports shakes counted by {@link ShakeCounter}. */
public class ShakeDetector implements SensorEventListener {

    public interface OnShakeListener {
        /** Called on every counted shake. */
        void onShake(int totalShakes);
    }

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

    /** Start counting (new session). Call from onResume or when the session begins. */
    public void start() {
        if (accelerometer == null) return;
        counter.reset();
        startElapsedMs = SystemClock.elapsedRealtime();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    /** Stop listening (call from onPause) to save battery. */
    public void stop() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
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
        if (counter.onAcceleration(e.values[0], e.values[1], e.values[2], nowMs) && listener != null) {
            listener.onShake(counter.getShakeCount());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
