package com.example.cs364_assigment2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ShakeCounterTest {
    private static final long DT = 20; // 50 Hz sampling

    /** Simulate `n` shakes, one every `periodMs`, strong back-and-forth swing (about 25 m/s^2). */
    private int simulateShakes(ShakeCounter c, int n, long periodMs, long[] clock) {
        for (int i = 0; i < n; i++) {
            for (long t = 0; t < periodMs; t += DT) {
                float swing = (t < 80) ? ((t < 40) ? 25f : -25f) : 0f; // one shake = out & back in 80 ms
                c.onAcceleration(swing, 0, 9.81f, clock[0]);
                clock[0] += DT;
            }
        }
        return c.getShakeCount();
    }

    @Test public void tenShakesCountedAsTen() {
        assertEquals(10, simulateShakes(new ShakeCounter(), 10, 600, new long[]{1000}));
    }

    @Test public void oneShakeWithTwoPeaksCountedOnce() {
        assertEquals(1, simulateShakes(new ShakeCounter(), 1, 600, new long[]{1000}));
    }

    @Test public void walkingIsNotCounted() {
        ShakeCounter c = new ShakeCounter();
        long t = 0;
        for (int i = 0; i < 500; i++) { // ~10 s of walking, +-3 m/s^2 at ~2 Hz
            float a = (float) (3.0 * Math.sin(2 * Math.PI * 2 * t / 1000.0));
            c.onAcceleration(0, a, 9.81f, t);
            t += DT;
        }
        assertEquals(0, c.getShakeCount());
    }

    @Test public void phoneAtRestCountsNothing() {
        ShakeCounter c = new ShakeCounter();
        for (int i = 0; i < 250; i++) c.onAcceleration(0, 0, 9.81f, i * DT);
        assertEquals(0, c.getShakeCount());
    }

    @Test public void shakesPerMinute() {
        assertEquals(12f, ShakeCounter.shakesPerMinute(6, 30000), 0.001f);
        assertEquals(0f, ShakeCounter.shakesPerMinute(5, 0), 0.001f);
    }
}
