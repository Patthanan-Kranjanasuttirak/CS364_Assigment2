
package com.example.database;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class StepCounterTest {

    @Test
    public void testDetectOneStep() {
        StepCounter counter = new StepCounter();

        // ค่าปกติ
        counter.processAcceleration(0, 9.81f, 0, 1000);

        // จุด Peak: ความเร่งสูงขึ้น
        counter.processAcceleration(6, 9.81f, 0, 1200);

        // กลับสู่ค่าปกติ เพื่อให้ตรวจพบ Peak ก่อนหน้า
        counter.processAcceleration(0, 9.81f, 0, 1400);

        assertEquals(1, counter.getSteps());
    }

    @Test
    public void testDetectFiveSteps() {
        StepCounter counter = new StepCounter();

        long time = 1000;

        for (int i = 0; i < 5; i++) {
            counter.processAcceleration(0, 9.81f, 0, time);
            time += 200;

            counter.processAcceleration(6, 9.81f, 0, time);
            time += 200;

            counter.processAcceleration(0, 9.81f, 0, time);
            time += 200;
        }

        assertEquals(5, counter.getSteps());
    }
}
