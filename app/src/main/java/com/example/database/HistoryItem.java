package com.example.database;

public class HistoryItem {
    private long id;

    private String dateTime;
    private String duration;

    private int totalSteps;
    private float stepRate;

    private int totalShakes;
    private float shakeRate;

    private boolean isWalk;
    private boolean isShake;

    public HistoryItem(
            String dateTime,
            String duration,
            int totalSteps,
            float stepRate,
            int totalShakes,
            float shakeRate,
            boolean isWalk,
            boolean isShake
    ) {
        this(-1, dateTime, duration, totalSteps, stepRate,
                totalShakes, shakeRate, isWalk, isShake);
    }
    public HistoryItem(
            long id,
            String dateTime,
            String duration,
            int totalSteps,
            float stepRate,
            int totalShakes,
            float shakeRate,
            boolean isWalk,
            boolean isShake
    ) {
        this.id = id;
        this.dateTime = dateTime;
        this.duration = duration;
        this.totalSteps = totalSteps;
        this.stepRate = stepRate;
        this.totalShakes = totalShakes;
        this.shakeRate = shakeRate;
        this.isWalk = isWalk;
        this.isShake = isShake;
    }

    public long getId() {
        return id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getDuration() {
        return duration;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public float getStepRate() {
        return stepRate;
    }

    public int getTotalShakes() {
        return totalShakes;
    }

    public float getShakeRate() {
        return shakeRate;
    }

    public boolean isWalk() {
        return isWalk;
    }

    public boolean isShake() {
        return isShake;
    }
}