
package com.example.database;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class ActivitySummaryTest {

    private int[] calculateSummary(List<HistoryItem> items) {
        int totalSteps = 0;
        int totalShakes = 0;
        int recordSteps = 0;
        int recordShakes = 0;

        for (HistoryItem item : items) {
            totalSteps += item.getTotalSteps();
            totalShakes += item.getTotalShakes();

            recordSteps = Math.max(
                    recordSteps, item.getTotalSteps()
            );

            recordShakes = Math.max(
                    recordShakes, item.getTotalShakes()
            );
        }

        return new int[] {
                totalSteps, totalShakes,
                recordSteps, recordShakes
        };
    }

    private List<HistoryItem> createTestData() {
        List<HistoryItem> items = new ArrayList<>();

        items.add(new HistoryItem(
                1, "Session 1", "10.00",
                100, 10f, 20, 2f, true, true
        ));

        items.add(new HistoryItem(
                2, "Session 2", "15.00",
                150, 10f, 30, 2f, true, true
        ));

        items.add(new HistoryItem(
                3, "Session 3", "8.00",
                80, 10f, 10, 1.25f, true, true
        ));

        return items;
    }

    @Test
    public void testTotalAndNewRecord() {
        int[] result = calculateSummary(createTestData());

        assertArrayEquals(
                new int[]{330, 60, 150, 30},
                result
        );
    }

    @Test
    public void testRecalculateAfterDelete() {
        List<HistoryItem> items = createTestData();

        // Simulate deleting Session 2
        items.remove(1);

        int[] result = calculateSummary(items);

        assertArrayEquals(
                new int[]{180, 30, 100, 20},
                result
        );
    }

    @Test
    public void testEmptyHistory() {
        int[] result = calculateSummary(new ArrayList<>());

        assertArrayEquals(
                new int[]{0, 0, 0, 0},
                result
        );
    }
}
