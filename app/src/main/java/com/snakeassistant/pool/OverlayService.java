
package com.snakeassistant.pool;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private View panel;
    private WindowManager.LayoutParams params;

    private int startX;
    private int startY;
    private float touchX;
    private float touchY;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this,
                    "Allow display over other apps first.",
                    Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        windowManager = (WindowManager)
                getSystemService(WINDOW_SERVICE);

        panel = createPanel();

        int windowType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 30;
        params.y = 150;

        try {
            windowManager.addView(panel, params);
        } catch (Exception e) {
            Toast.makeText(this,
                    "Couldn't open floating panel.",
                    Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    private View createPanel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(18, 14, 18, 14);
        box.setBackgroundColor(Color.rgb(20, 28, 40));

        TextView title = new TextView(this);
        title.setText("🐍 SNAKE PRACTICE");
        title.setTextColor(Color.rgb(100, 255, 218));
        title.setTextSize(16);
        title.setPadding(0, 0, 0, 8);
        box.addView(title);

        TextView info = new TextView(this);
        info.setText("Manual practice tools");
        info.setTextColor(Color.WHITE);
        info.setTextSize(12);
        box.addView(info);

        Button guideButton = new Button(this);
        guideButton.setText("AIM GUIDE: ON");
        guideButton.setOnClickListener(v -> {
            boolean enabled = guideButton.getText().toString()
                    .endsWith("ON");
            guideButton.setText(enabled
                    ? "AIM GUIDE: OFF" : "AIM GUIDE: ON");
            info.setText(enabled
                    ? "Guide paused — practice manually"
                    : "Guide enabled — manual practice");
        });
        box.addView(guideButton);

        Button closeButton = new Button(this);
        closeButton.setText("CLOSE PANEL");
        closeButton.setOnClickListener(v -> stopSelf());
        box.addView(closeButton);

        box.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = startX
                                + (int) (event.getRawX() - touchX);
                        params.y = startY
                                + (int) (event.getRawY() - touchY);
                        try {
                            windowManager.updateViewLayout(panel, params);
                        } catch (Exception ignored) {
                        }
                        return true;
                }
                return false;
            }
        });

        return box;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null && panel != null) {
            try {
                windowManager.removeView(panel);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
                                 }
