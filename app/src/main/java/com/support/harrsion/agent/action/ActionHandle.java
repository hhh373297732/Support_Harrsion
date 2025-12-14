package com.support.harrsion.agent.action;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson2.JSON;
import com.support.harrsion.agent.utils.DeviceUtil;
import com.support.harrsion.config.AppPackage;
import com.support.harrsion.dto.action.ActionResult;
import com.support.harrsion.service.AccessibilityService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.val;

public class ActionHandle {

    private final Context context;

    public ActionHandle(Context context) {
        this.context = context;
    }

    public ActionResult execute(Map<String, Object> action, int width, int height) {
        String actionType = String.valueOf(action.get("_metadata"));
        if (actionType.equals("finish")) {
            return ActionResult.builder()
                    .success(true)
                    .shouldFinish(true)
                    .message(String.valueOf(action.get("message")))
                    .build();
        }
        if (!actionType.equals("do")) {
            return ActionResult.builder()
                    .success(false)
                    .shouldFinish(true)
                    .message("Unknown action type:" + actionType)
                    .build();
        }

        String actionName = String.valueOf(action.get("action"));

        return _getHandler(action, actionName, width, height);

    }

    private ActionResult _getHandler(Map<String, Object> action, String actionName, int width, int height) {
        switch (actionName) {
            case "Launch":
                return _handleLaunch(action);
            case "Tap":
                return _handleTap(action, width, height);
            case "Type":
                return _handleType(action);
            case "Swipe":
                return _handleSwipe(action, width, height);
            default:
                return ActionResult.builder()
                        .success(true)
                        .shouldFinish(true)
                        .message("No implement action")
                        .build();
        }
    }

    private ActionResult _handleLaunch(Map<String, Object> action) {
        String appName = String.valueOf(action.get("app"));
        if (appName.isEmpty())
            return ActionResult.builder()
                    .success(false)
                    .shouldFinish(false)
                    .message("No app name specified")
                    .build();
        boolean result = _launchApp(appName);
        if (result)
            return ActionResult.builder()
                    .success(true)
                    .shouldFinish(false)
                    .build();
        return ActionResult.builder()
                .success(false)
                .shouldFinish(false)
                .message("App not found: " + appName)
                .build();
    }

    private ActionResult _handleTap(Map<String, Object> action, int width, int height) {
        List<Float> element = JSON.parseArray(String.valueOf(action.get("element")), Float.class);
        List<Float> local = DeviceUtil.convertRelativeToAbsolute(element, width, height);

        if (action.containsKey("message")) {
            // todo: Check for sensitive operation
        }

        AccessibilityService.getInstance().clickByXY(local.get(0), local.get(1));

        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    private ActionResult _handleSwipe(Map<String, Object> action, int width, int height) {
        List<Float> start = JSON.parseArray(String.valueOf(action.get("start")), Float.class);
        List<Float> end = JSON.parseArray(String.valueOf(action.get("end")), Float.class);

        if (start.size() != 2 || end.size() != 2) {
            return ActionResult.builder()
                    .success(false)
                    .shouldFinish(false)
                    .message("Missing swipe coordinates")
                    .build();
        }

        List<Float> startLocal = DeviceUtil.convertRelativeToAbsolute(start, width, height);
        List<Float> endLocal = DeviceUtil.convertRelativeToAbsolute(end, width, height);

        AccessibilityService.getInstance().swipe(startLocal.get(0), startLocal.get(1),
                endLocal.get(0), endLocal.get(1), 300);

        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    private ActionResult _handleType(Map<String, Object> action) {
        String text = String.valueOf(action.get("text"));
        AccessibilityService.getInstance().inputText("");
        AccessibilityService.getInstance().inputText(text);

        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    private boolean _launchApp(String appName) {
        PackageManager pm = this.context.getPackageManager();
        String packageName = Objects.requireNonNull(AppPackage.fromLabel(appName)).pkg;
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
            return true;
        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "目标App未安装或无法启动", Toast.LENGTH_SHORT).show();
            });
            return false;
        }
    }
}
