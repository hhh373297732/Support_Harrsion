package com.support.harrsion.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import com.support.harrsion.dto.action.ClickCommand;


@SuppressLint("AccessibilityPolicy")
public class AutoClickService extends AccessibilityService {


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(commandReceiver, new IntentFilter(ClickCommand.ACTION), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(commandReceiver, new IntentFilter(ClickCommand.ACTION));
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(commandReceiver);
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {}

    @Override
    public void onInterrupt() {}

    private final BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ClickCommand.ACTION.equals(intent.getAction())) return;

            String type = intent.getStringExtra(ClickCommand.TYPE);
            if (ClickCommand.TYPE_CLICK.equals(type)) {
                float x = intent.getFloatExtra(ClickCommand.X, 0);
                float y = intent.getFloatExtra(ClickCommand.Y, 0);
                clickByXY(x, y);
            }
        }
    };

    /**
     * 坐标点击（单位：px）
     */
    private void clickByXY(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 80);

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(stroke)
                        .build();

        dispatchGesture(gesture, null, null);
    }
}
