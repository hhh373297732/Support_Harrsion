package com.support.harrsion.agent.device;

import com.support.harrsion.dto.screenshot.Screenshot;

public interface ScreenshotCallback {
    /**
     * 当截图数据准备好时被调用。
     * @param screenshot 包含 Base64 数据的截图对象
     */
    void onScreenshotReady(Screenshot screenshot);

    /**
     * 截图失败时被调用。
     */
    void onScreenshotFailed(String error);
}