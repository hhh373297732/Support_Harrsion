package com.support.harrsion.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import lombok.Getter;

/**
 * 操作服务，基于辅助服务实现
 *
 * @author harrsion
 * @date 2025/12/15
 */
public class ActionService extends AccessibilityService {

    @Getter
    private static ActionService instance;

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

    /**
     * 滑动屏幕
     *
     * @param startX 开始 X 坐标（px）
     * @param startY 开始 Y 坐标（px）
     * @param endX   结束 X 坐标（px）
     * @param endY   结束 Y 坐标（px）
     * @param duration 滑动时长（毫秒）
     */
    public void swipe(float startX, float startY, float endX, float endY, long duration) {
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, duration);

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(stroke)
                        .build();


        dispatchGesture(gesture, null, null);
    }

    /**
     * 输入文本
     *
     * @param text 文本
     * @return 是否成功
     */
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

    /**
     * 返回上级
     *
     * @return 是否成功
     */
    public boolean goBack() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /**
     * 跳转桌面
     *
     * @return 是否成功
     */
    public boolean goHome() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    /**
     * 寻找当前页面的焦点输入框
     *
     * @param node 节点
     * @return 输入框
     */
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
