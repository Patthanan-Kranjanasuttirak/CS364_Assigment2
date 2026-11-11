package com.example.cs364_assigment2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements SensorEventListener {

    // ===== Step Counter =====
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private StepCounter stepCounter;

    // แสดงจำนวนก้าว
    private TextView stepTextView;
    private TextView summaryStepValue;

    // ===== Shake Counter =====
    private ShakeDetector shakeDetector;
    private TextView liveShake;
    private TextView summaryShakeValue;

    // ===== Activity History =====
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList;

    // ===== Activity Session =====
    private int currentSteps = 0;
    private long sessionStartTime;
    private Button btnSaveActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        sessionStartTime = SystemClock.elapsedRealtime();

        stepTextView = findViewById(R.id.liveSteps);
        summaryStepValue = findViewById(R.id.summaryStepValue);
        btnSaveActivity = findViewById(R.id.btnSaveActivity);

        // ==========================================
        // STEP COUNTER
        // ==========================================

        // สร้าง StepCounter
        stepCounter = new StepCounter();

        // เรียก SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // เรียก Accelerometer
        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
        );

        // ==========================================
        // SHAKE COUNTER
        // ==========================================

        liveShake = findViewById(R.id.liveShake);
        summaryShakeValue = findViewById(R.id.summaryShakeValue);

        shakeDetector = new ShakeDetector(this);

        shakeDetector.setOnShakeListener(total -> {
            updateShakeUI(total);
        });

        updateShakeUI(0);

        // ==========================================
        // ACTIVITY HISTORY
        // ==========================================

        rvHistory = findViewById(R.id.rvHistory);

        historyList = new ArrayList<>();

        historyAdapter = new HistoryAdapter(historyList);

        rvHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvHistory.setAdapter(historyAdapter);
        btnSaveActivity.setOnClickListener(v -> saveActivity());
    }


    // ==========================================
    // เมื่อ Activity กลับมาแสดง
    // ==========================================

    @Override
    protected void onResume() {
        super.onResume();

        // เริ่ม Shake Detector
        if (shakeDetector != null && shakeDetector.isAvailable()) {
            shakeDetector.start();
        }

        // เริ่ม Accelerometer
        if (accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
            );
        }
    }


    // ==========================================
    // เมื่อ Activity ถูกพัก
    // ==========================================

    @Override
    protected void onPause() {
        super.onPause();

        // หยุด Shake Detector
        if (shakeDetector != null) {
            shakeDetector.stop();
        }

        // หยุด Accelerometer
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }


    // ==========================================
    // รับข้อมูลจาก Accelerometer
    // ==========================================

    @Override
    public void onSensorChanged(SensorEvent event) {

        // ตรวจว่าเป็น Accelerometer หรือไม่
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // ค่าแกน X Y Z
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // timestamp
            long timestamp = event.timestamp / 1_000_000L;

            // ส่งข้อมูลให้ StepCounter
            int steps = stepCounter.processAcceleration(
                    x,
                    y,
                    z,
                    timestamp
            );

            currentSteps = steps;

            // แสดงจำนวนก้าวใน Log
            System.out.println("Steps: " + steps);

            // ถ้ามี TextView สำหรับแสดงก้าว
            if (stepTextView != null) {
                stepTextView.setText(String.valueOf(steps));
            }
            if (summaryStepValue != null) {
                summaryStepValue.setText(String.valueOf(steps));
            }
        }
    }


    // ==========================================
    // Sensor Accuracy
    // ==========================================

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ไม่ต้องทำอะไร
    }

    private void saveActivity() {

        // ==========================
        // คำนวณเวลา
        // ==========================

        long elapsedMillis =
                SystemClock.elapsedRealtime() - sessionStartTime;

        float durationMinutes =
                elapsedMillis / 60000f;

        if (durationMinutes < 0.01f) {
            durationMinutes = 0.01f;
        }

        // ==========================
        // ข้อมูล Steps
        // ==========================

        int totalSteps = currentSteps;

        float stepRate =
                totalSteps / durationMinutes;

        // ==========================
        // ข้อมูล Shake
        // ==========================

        int totalShakes =
                getSessionShakes();

        float shakeRate =
                getSessionShakesPerMinute();

        // ==========================
        // วันที่ / เวลา
        // ==========================

        String dateTime =
                new SimpleDateFormat(
                        "dd MMM HH:mm",
                        Locale.getDefault()
                ).format(new Date());

        // ==========================
        // แสดงระยะเวลา
        // ==========================

        String durationText =
                String.format(
                        Locale.getDefault(),
                        "%.2f",
                        durationMinutes
                );

        // ==========================
        // สร้าง History
        // ==========================

        HistoryItem item = new HistoryItem(
                dateTime,
                durationText,
                totalSteps,
                stepRate,
                totalShakes,
                shakeRate,
                totalSteps > 0,
                totalShakes > 0
        );

        // เพิ่มรายการใหม่ไว้ด้านบน
        historyList.add(0, item);

        historyAdapter.notifyItemInserted(0);

        rvHistory.scrollToPosition(0);

        // ==========================
        // Reset session
        // ==========================

        currentSteps = 0;

        stepCounter = new StepCounter();

        resetShakeSession();

        sessionStartTime =
                SystemClock.elapsedRealtime();

        // Reset หน้าจอ
        if (stepTextView != null) {
            stepTextView.setText("0");
        }
    }

    // ==========================================
    // SHAKE UI
    // ==========================================

    private void updateShakeUI(int total) {

        if (liveShake != null) {
            liveShake.setText(String.valueOf(total));
        }

        if (summaryShakeValue != null) {
            summaryShakeValue.setText(String.valueOf(total));
        }
    }


    // ==========================================
    // สำหรับใช้ตอนบันทึก Activity
    // ==========================================

    public int getSessionShakes() {

        if (shakeDetector == null) {
            return 0;
        }

        return shakeDetector.getShakeCount();
    }


    public float getSessionShakesPerMinute() {

        if (shakeDetector == null) {
            return 0;
        }

        return shakeDetector.getShakesPerMinute();
    }


    public void resetShakeSession() {

        if (shakeDetector != null) {
            shakeDetector.reset();
        }

        updateShakeUI(0);
    }
}