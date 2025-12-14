package com.support.harrsion.service;

import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import lombok.Getter;


@SuppressLint("AccessibilityPolicy")
public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    @Getter
    private static AccessibilityService instance;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {}

    @Override
    public void onInterrupt() {}

    /**
     * 坐标点击（单位：px）
     */
    public void clickByXY(float x, float y) {
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

    public boolean inputText(String text) {
        AccessibilityNodeInfo focusNode = getRootInActiveWindow();
        if (focusNode == null) return false;

        AccessibilityNodeInfo target = findFocusedEditText(focusNode);
        if (target == null) return false;

        Bundle args = new Bundle();
        args.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );

        return target.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                args
        );
    }

    private AccessibilityNodeInfo findFocusedEditText(AccessibilityNodeInfo node) {
        if (node == null) return null;

        if (node.isFocused() && "android.widget.EditText".contentEquals(node.getClassName())) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo result = findFocusedEditText(node.getChild(i));
            if (result != null) return result;
        }
        return null;
    }
}
