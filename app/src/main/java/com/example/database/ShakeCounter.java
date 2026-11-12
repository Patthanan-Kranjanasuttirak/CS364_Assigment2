package com.example.database;

/**
 * Pure-Java shake detection algorithm (no Android dependency, so it can be unit-tested).
 *
 * Pipeline:
 *  1. Low-pass filter estimates gravity, subtract it  -> linear acceleration (m/s^2)
 *  2. magnitude = |linear acceleration|
 *  3. Threshold: magnitude > SHAKE_THRESHOLD counts as a shake candidate
 *     (walking ~2-6 m/s^2, a real shake ~12-30 m/s^2)
 *  4. Anti double-count: (a) cooldown MIN_SHAKE_INTERVAL_MS between shakes,
 *     (b) re-arm only after magnitude falls below RESET_THRESHOLD (hysteresis),
 *     so the back-and-forth swing of ONE shake is counted once.
 */
public class ShakeCounter {

    public static final float SHAKE_THRESHOLD = 12.0f;      // m/s^2 (linear, gravity removed)
    public static final float RESET_THRESHOLD = 6.0f;       // m/s^2, must drop below to re-arm
    public static final long  MIN_SHAKE_INTERVAL_MS = 250;  // cooldown between two counted shakes
    private static final float GRAVITY_ALPHA = 0.8f;        // low-pass filter coefficient

    private final float[] gravity = new float[3];
    private boolean gravityInit = false;
    private boolean armed = true;
    private long lastShakeMs = -1;
    private int shakeCount = 0;

    /** Feed one accelerometer sample. Returns true if this sample produced a NEW counted shake. */
    public boolean onAcceleration(float x, float y, float z, long timestampMs) {
        if (!gravityInit) {
            gravity[0] = x; gravity[1] = y; gravity[2] = z;
            gravityInit = true;
        } else {
            gravity[0] = GRAVITY_ALPHA * gravity[0] + (1 - GRAVITY_ALPHA) * x;
            gravity[1] = GRAVITY_ALPHA * gravity[1] + (1 - GRAVITY_ALPHA) * y;
            gravity[2] = GRAVITY_ALPHA * gravity[2] + (1 - GRAVITY_ALPHA) * z;
        }
        float lx = x - gravity[0];
        float ly = y - gravity[1];
        float lz = z - gravity[2];
        float magnitude = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);

        if (magnitude < RESET_THRESHOLD) {
            armed = true;
            return false;
        }
        if (armed && magnitude > SHAKE_THRESHOLD
                && (lastShakeMs < 0 || timestampMs - lastShakeMs >= MIN_SHAKE_INTERVAL_MS)) {
            armed = false;
            lastShakeMs = timestampMs;
            shakeCount++;
            return true;
        }
        return false;
    }

    public int getShakeCount() { return shakeCount; }

    /** Shakes per minute = shakes / (elapsed seconds / 60). Returns 0 if no time has passed. */
    public static float shakesPerMinute(int shakes, long elapsedMs) {
        if (elapsedMs <= 0) return 0f;
        return shakes / (elapsedMs / 60000f);
    }

    public void reset() {
        gravityInit = false;
        armed = true;
        lastShakeMs = -1;
        shakeCount = 0;
    }
}
