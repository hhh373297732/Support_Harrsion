package com.support.harrsion.agent.action;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.support.harrsion.config.AppPackage;
import com.support.harrsion.dto.action.ActionResult;

import java.util.Map;

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

        return _getHandler(action, actionName);

    }

    private ActionResult _getHandler(Map<String, Object> action, String actionName) {
        switch (actionName) {
            case "Launch":
                return _handleLaunch(action);
            default:
                return null;
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

    private boolean _launchApp(String appName) {
        PackageManager pm = this.context.getPackageManager();

        Intent launchIntent = pm.getLaunchIntentForPackage(AppPackage.fromLabel(appName).pkg);

        if (launchIntent != null) {
            // 启动 App
            context.startActivity(launchIntent);
            return true;
        } else {
            // App 未安装
            Toast.makeText(context, "目标App未安装或无法启动", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
