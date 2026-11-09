package com.example.cs364_assigment2;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;

    // ===== [คนที่ 3] Shake Counter =====
    private ShakeDetector shakeDetector;
    private TextView liveShake, summaryShakeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. ผูก View และตั้งค่า Adapter ตามปกติก่อน
        // ===== [คนที่ 3] Shake Counter: ผูก View + ตั้ง listener =====
        liveShake = findViewById(R.id.liveShake);
        summaryShakeValue = findViewById(R.id.summaryShakeValue);
        shakeDetector = new ShakeDetector(this);
        shakeDetector.setOnShakeListener(total -> updateShakeUI(total));
        updateShakeUI(0);

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        // สมมติว่ามี historyList และ new HistoryAdapter(...) แล้ว
        historyAdapter = new HistoryAdapter(this, historyList);
        rvHistory.setAdapter(historyAdapter);

        // 2. swipeCallback
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final Paint paint = new Paint();
            private final Paint textPaint = new Paint();
            private Bitmap deleteIcon;

            {
                // กำหนดสีพื้นหลังแดง
                paint.setColor(Color.parseColor("#B83B3B"));

                // กำหนดรูปแบบตัวหนังสือ "ลบ"
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(40f);
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                textPaint.setTextAlign(Paint.Align.CENTER);

                // ดึงไอคอนมาเตรียมไว้และย่อขนาดให้พอดี (ขนาด 24dp ~ 64px)
                Drawable iconDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.delete); // เปลี่ยนเป็น R.drawable.trash หรือ ic_delete
                if (iconDrawable != null) {
                    deleteIcon = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(deleteIcon);
                    iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    // ย้อมสีไอคอนเป็นสีขาว
                    iconDrawable.setTint(Color.WHITE);
                    iconDrawable.draw(canvas);
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                historyAdapter.removeItem(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                if (dX < 0) { // กำลังปัดไปทางซ้าย
                    // 1. วาดแถบพื้นหลังสีแดงขอบมน
                    RectF background = new RectF(
                            (float) itemView.getRight() + dX,
                            (float) itemView.getTop(),
                            (float) itemView.getRight(),
                            (float) itemView.getBottom()
                    );
                    c.drawRoundRect(background, 24f, 24f, paint);

                    // คำนวณจุดกึ่งกลางแกน X ของปุ่มลบ (อยู่ห่างจากขอบขวาเข้ามา 70px)
                    float centerX = itemView.getRight() - 90f;
                    float centerY = itemView.getTop() + ((float) itemView.getHeight() / 2);

                    // แสดงเมื่อเริ่มปัดกว้างพอ
                    if (Math.abs(dX) > 80) {
                        // 2. วาดคำว่า "ลบ" ด้านบน (ขยับขึ้นจากกึ่งกลางเล็กน้อย)
                        c.drawText("ลบ", centerX, centerY - 15f, textPaint);

                        // 3. วาดไอคอนถังขยะใต้คำว่า "ลบ"
                        if (deleteIcon != null) {
                            float iconLeft = centerX - ((float) deleteIcon.getWidth() / 2);
                            float iconTop = centerY + 10f; // วางห่างจากจุดกึ่งกลางลงมาข้างล่าง
                            c.drawBitmap(deleteIcon, iconLeft, iconTop, null);
                        }
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(rvHistory);
    }

    // ===== [คนที่ 3] Shake Counter =====
    @Override
    protected void onResume() {
        super.onResume();
        if (shakeDetector != null && shakeDetector.isAvailable()) shakeDetector.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (shakeDetector != null) shakeDetector.stop();
    }

    private void updateShakeUI(int total) {
        liveShake.setText(String.valueOf(total));
        summaryShakeValue.setText(String.valueOf(total));
    }

    /** ให้ส่วนบันทึกกิจกรรม (SQLite) เรียกใช้ตอนกดปุ่ม "บันทึกกิจกรรม" */
    public int getSessionShakes() { return shakeDetector.getShakeCount(); }

    /** shakes/min ของ session ปัจจุบัน สำหรับแสดงในประวัติกิจกรรม */
    public float getSessionShakesPerMinute() { return shakeDetector.getShakesPerMinute(); }

    /** เรียกหลังบันทึกกิจกรรมเสร็จ เพื่อเริ่มนับ session ใหม่ */
    public void resetShakeSession() {
        shakeDetector.reset();
        updateShakeUI(0);
    }
}