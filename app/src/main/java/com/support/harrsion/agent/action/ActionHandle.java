package com.support.harrsion.agent.action;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.alibaba.fastjson2.JSON;
import com.support.harrsion.agent.utils.DeviceUtil;
import com.support.harrsion.config.AppPackage;
import com.support.harrsion.dto.action.ActionResult;
import com.support.harrsion.service.ActionService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 操作处理器
 *
 * @author harrsion
 * @date 2025/12/15
 */
public class ActionHandle {

    private final Context context;

    public ActionHandle(Context context) {
        this.context = context;
    }

    /**
     * 执行操作
     *
     * @param action 操作
     * @param width  屏幕宽度
     * @param height 屏幕高度
     * @return ActionResult
     */
    public ActionResult execute(Map<String, Object> action, int width, int height) {
        // 获取操作类型，当前项目主要引用的autoglm-phone模型，处理的数据结构也是基于该模型的返回
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

    /**
     * 获取操作处理器
     *
     * @param action     操作
     * @param actionName 操作名称
     * @param width      屏幕宽度
     * @param height     屏幕高度
     * @return ActionResult
     */
    private ActionResult _getHandler(Map<String, Object> action, String actionName, int width, int height) {
        switch (actionName) {
            case "Launch":
                return _handleLaunch(action);
            case "Tap":
                return _handleTap(action, width, height);
            case "Double Tap":
                return _handleDoubleTap(action, width, height);
            case "Type":
                return _handleType(action);
            case "Swipe":
                return _handleSwipe(action, width, height);
            case "Back":
                return _handleBack();
            case "Home":
                return _handleHome();
            case "Wait":
                return _handleWait(action);
            case "Take_over":
                return _handleTakeOver();
            default:
                return ActionResult.builder()
                        .success(true)
                        .shouldFinish(true)
                        .message("No implement action")
                        .build();
        }
    }

    /**
     * 处理启动操作
     *
     * @param action 操作
     * @return ActionResult
     */
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

    /**
     * 处理点击操作
     *
     * @param action 操作
     * @param width  屏幕宽度
     * @param height 屏幕高度
     * @return ActionResult
     */
    private ActionResult _handleTap(Map<String, Object> action, int width, int height) {
        if (!action.containsKey("element")) {
            return ActionResult.builder()
                    .success(false)
                    .shouldFinish(false)
                    .message("No element coordinates")
                    .build();
        }
        List<Float> element = JSON.parseArray(String.valueOf(action.get("element")), Float.class);
        List<Float> local = DeviceUtil.convertRelativeToAbsolute(element, width, height);

        if (action.containsKey("message")) {
            return ActionResult.builder()
                    .success(false)
                    .shouldFinish(true)
                    .message("User cancelled sensitive operation")
                    .build();
        }

        ActionService.getInstance().clickByXY(local.get(0), local.get(1));

        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    /**
     * 处理双击操作
     *
     * @param action 操作
     * @param width  屏幕宽度
     * @param height 屏幕高度
     * @return ActionResult
     */
    private ActionResult _handleDoubleTap(Map<String, Object> action, int width, int height) {
        if (!action.containsKey("element")) {
            return ActionResult.builder()
                    .success(false)
                    .shouldFinish(false)
                    .message("No element coordinates")
                    .build();
        }
        List<Float> element = JSON.parseArray(String.valueOf(action.get("element")), Float.class);
        List<Float> local = DeviceUtil.convertRelativeToAbsolute(element, width, height);

        if (action.containsKey("message")) {
            return ActionResult.builder()
                    .success(false)
                    .shouldFinish(true)
                    .message("User cancelled sensitive operation")
                    .build();
        }

        ActionService.getInstance().clickByXY(local.get(0), local.get(1));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ActionService.getInstance().clickByXY(local.get(0), local.get(1));
        }, 100);

        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    /**
     * 处理滑动操作
     *
     * @param action 操作
     * @return ActionResult
     */
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

        ActionService.getInstance().swipe(startLocal.get(0), startLocal.get(1),
                endLocal.get(0), endLocal.get(1), 300);

        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    /**
     * 处理输入操作
     *
     * @param action 操作
     * @return ActionResult
     */
    private ActionResult _handleType(Map<String, Object> action) {
        String text = String.valueOf(action.get("text"));
        ActionService.getInstance().inputText(text);

        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    /**
     * 处理返回上级操作
     *
     * @return ActionResult
     */
    private ActionResult _handleBack() {
        ActionService.getInstance().goBack();
        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    /**
     * 处理返回主页操作
     *
     * @return ActionResult
     */
    private ActionResult _handleHome() {
        ActionService.getInstance().goHome();
        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    /**
     * 处理等待操作
     *
     * @return ActionResult
     */
    private ActionResult _handleWait(Map<String, Object> action) {
        // 默认等待一秒
        long duration = 1000;
        if (action.containsKey("duration")) {
            duration = Long.parseLong(String.valueOf(action.get("duration"))
                    .replace("seconds", "").trim());
        }

        try {
            Thread.sleep(duration);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    /**
     * 处理挂起操作
     *
     * @return ActionResult
     */
    private ActionResult _handleTakeOver() {
        // 等待1.5秒处理验证码
        long duration = 1500;
        try {
            Thread.sleep(duration);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ActionResult.builder()
                .success(true)
                .shouldFinish(false)
                .build();
    }

    /**
     * 启动App
     *
     * @param appName App名称
     * @return boolean
     */
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
