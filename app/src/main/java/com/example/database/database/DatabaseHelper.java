
package com.example.database.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.database.HistoryItem;

import java.util.ArrayList;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database configuration
    private static final String DATABASE_NAME = "activity_history.db";
    private static final int DATABASE_VERSION = 2;

    // Table name
    public static final String TABLE_ACTIVITIES = "activities";

    // Column names
    public static final String COL_ID = "id";
    public static final String COL_DATE_TIME = "date_time";
    public static final String COL_DURATION = "duration";
    public static final String COL_TOTAL_STEPS = "total_steps";
    public static final String COL_STEP_RATE = "step_rate";
    public static final String COL_TOTAL_SHAKES = "total_shakes";
    public static final String COL_SHAKE_RATE = "shake_rate";
    public static final String COL_IS_WALK = "is_walk";
    public static final String COL_IS_SHAKE = "is_shake";

    // Create table SQL
    private static final String CREATE_TABLE_ACTIVITIES =
            "CREATE TABLE " + TABLE_ACTIVITIES + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DATE_TIME + " TEXT NOT NULL, " +
                    COL_DURATION + " REAL NOT NULL DEFAULT 0, " +
                    COL_TOTAL_STEPS + " INTEGER NOT NULL DEFAULT 0, " +
                    COL_STEP_RATE + " REAL NOT NULL DEFAULT 0, " +
                    COL_TOTAL_SHAKES + " INTEGER NOT NULL DEFAULT 0, " +
                    COL_SHAKE_RATE + " REAL NOT NULL DEFAULT 0, " +
                    COL_IS_WALK + " INTEGER NOT NULL DEFAULT 0, " +
                    COL_IS_SHAKE + " INTEGER NOT NULL DEFAULT 0" +
                    ")";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create database table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ACTIVITIES);
    }

    // Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {

            db.execSQL(
                    "ALTER TABLE " + TABLE_ACTIVITIES +
                            " ADD COLUMN " + COL_IS_WALK +
                            " INTEGER NOT NULL DEFAULT 0"
            );

            db.execSQL(
                    "ALTER TABLE " + TABLE_ACTIVITIES +
                            " ADD COLUMN " + COL_IS_SHAKE +
                            " INTEGER NOT NULL DEFAULT 0"
            );
        }
    }

    // INSERT: Save activity
    public long insertActivity(HistoryItem item) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        double durationValue;

        try {
            durationValue = Double.parseDouble(item.getDuration());
        } catch (NumberFormatException e) {
            durationValue = 0;
        }

        values.put(COL_DATE_TIME, item.getDateTime());
        values.put(COL_DURATION, durationValue);
        values.put(COL_TOTAL_STEPS, item.getTotalSteps());
        values.put(COL_STEP_RATE, item.getStepRate());
        values.put(COL_TOTAL_SHAKES, item.getTotalShakes());
        values.put(COL_SHAKE_RATE, item.getShakeRate());
        values.put(COL_IS_WALK, item.isWalk() ? 1 : 0);
        values.put(COL_IS_SHAKE, item.isShake() ? 1 : 0);

        return db.insert(TABLE_ACTIVITIES, null, values);
    }

    // SELECT: Get all activities
    public ArrayList<HistoryItem> getAllActivities() {

        ArrayList<HistoryItem> historyList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_ACTIVITIES,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        );

        try {

            while (cursor.moveToNext()) {

                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_ID)
                );

                String dateTime = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_DATE_TIME)
                );

                double durationValue = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COL_DURATION)
                );

                String duration = String.format(
                        Locale.US, "%.2f", durationValue
                );

                int totalSteps = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_TOTAL_STEPS)
                );

                float stepRate = cursor.getFloat(
                        cursor.getColumnIndexOrThrow(COL_STEP_RATE)
                );

                int totalShakes = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_TOTAL_SHAKES)
                );

                float shakeRate = cursor.getFloat(
                        cursor.getColumnIndexOrThrow(COL_SHAKE_RATE)
                );

                boolean isWalk = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_IS_WALK)
                ) == 1;

                boolean isShake = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_IS_SHAKE)
                ) == 1;

                HistoryItem item = new HistoryItem(
                        id,
                        dateTime,
                        duration,
                        totalSteps,
                        stepRate,
                        totalShakes,
                        shakeRate,
                        isWalk,
                        isShake
                );

                historyList.add(item);
            }

        } finally {
            cursor.close();
        }

        return historyList;
    }

    // DELETE: Delete activity by ID
    public int deleteActivity(long id) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_ACTIVITIES,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }
}
