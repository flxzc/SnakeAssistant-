
package com.snakeassistant.pool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView status;
    private boolean guidesEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.txtStatus);
        Button guides = findViewById(R.id.btnGuides);
        Button bank = findViewById(R.id.btnBank);
        Button spin = findViewById(R.id.btnSpin);
        Button overlay = findViewById(R.id.btnOverlay);

        guides.setOnClickListener(v -> {
            guidesEnabled = !guidesEnabled;
            guides.setText(guidesEnabled
                    ? "AIMING GUIDES: ON"
                    : "AIMING GUIDES: OFF");
            status.setText(guidesEnabled
                    ? "Aiming guides enabled for practice."
                    : "Aiming guides paused.");
        });

        bank.setOnClickListener(v -> showBankCalculator());
        spin.setOnClickListener(v -> showSpinLab());
        overlay.setOnClickListener(v -> requestOverlayPermission());
    }

    private void showBankCalculator() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        layout.setPadding(pad, pad, pad, pad);

        EditText tableWidth = field("Table width (e.g. 100)", "100");
        EditText ballX = field("Cue ball X (0 to width)", "25");
        EditText targetX = field("Target X (0 to width)", "75");

        layout.addView(tableWidth);
        layout.addView(ballX);
        layout.addView(targetX);

        new AlertDialog.Builder(this)
                .setTitle("Bank Shot Practice Calculator")
                .setMessage("Enter horizontal table coordinates.")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Calculate", (dialog, which) -> {
                    try {
                        double width = Double.parseDouble(
                                tableWidth.getText().toString().trim());
                        double cue = Double.parseDouble(
                                ballX.getText().toString().trim());
                        double target = Double.parseDouble(
                                targetX.getText().toString().trim());

                        if (width <= 0 || cue < 0 || cue > width
                                || target < 0 || target > width) {
                            throw new IllegalArgumentException();
                        }

                        double mirrorTarget = 2 * width - target;
                        double denominator = mirrorTarget - cue;

                        if (Math.abs(denominator) < 0.0001) {
                            status.setText(
                                    "No unique bank point for these inputs.");
                            return;
                        }

                        double bankPoint = width * (target - cue)
                                / (2 * target - cue);

                        if (!Double.isFinite(bankPoint)
                                || bankPoint < 0 || bankPoint > width) {
                            bankPoint = Double.NaN;
                        }

                        String result;
                        if (Double.isNaN(bankPoint)) {
                            result = "These coordinates don't produce a "
                                    + "valid single-rail bank point with "
                                    + "this simplified model.";
                        } else {
                            result = String.format(
                                    java.util.Locale.US,
                                    "Estimated rail contact X: %.2f "
                                            + "(table width %.2f). "
                                            + "Simplified practice geometry; "
                                            + "cushion and ball effects vary.",
                                    bankPoint, width);
                        }

                        new AlertDialog.Builder(this)
                                .setTitle("Bank Shot Result")
                                .setMessage(result)
                                .setPositiveButton("OK", null)
                                .show();

                        status.setText("Bank shot calculation complete.");
                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Enter valid numbers and keep X values "
                                        + "within the table width.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private void showSpinLab() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        layout.setPadding(pad, pad, pad, pad);

        EditText spin = field("Spin (-100 left, 100 right)", "0");
        EditText power = field("Power (0 to 100)", "50");
        EditText angle = field("Shot angle (0 to 90 degrees)", "30");

        layout.addView(spin);
        layout.addView(power);
        layout.addView(angle);

        new AlertDialog.Builder(this)
                .setTitle("Spin & Power Practice Lab")
                .setMessage("Try different values to record a practice setup.")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save setup", (dialog, which) -> {
                    try {
                        double s = Double.parseDouble(
                                spin.getText().toString().trim());
                        double p = Double.parseDouble(
                                power.getText().toString().trim());
                        double a = Double.parseDouble(
                                angle.getText().toString().trim());

                        if (s < -100 || s > 100 || p < 0 || p > 100
                                || a < 0 || a > 90) {
                            throw new IllegalArgumentException();
                        }

                        status.setText(String.format(
                                java.util.Locale.US,
                                "Practice setup saved: spin %.0f, "
                                        + "power %.0f, angle %.0f°",
                                s, p, a));
                        Toast.makeText(this,
                                "Practice setup recorded on screen.",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Check the allowed ranges and try again.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private EditText field(String hint, String value) {
        EditText edit = new EditText(this);
        edit.setSingleLine(true);
        edit.setHint(hint);
        edit.setText(value);
        return edit;
    }

    private void requestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            status.setText("Floating-panel permission is already enabled. "
                    + "The panel service is the next step.");
            Toast.makeText(this,
                    "Permission granted. Floating panel comes next.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Allow floating panel")
                .setMessage("Snake Assistant needs permission to display "
                        + "a practice panel over other apps. Android will "
                        + "open the relevant settings page.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Open settings", (dialog, which) -> {
                    Intent intent = new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .show();
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }
                                   }
