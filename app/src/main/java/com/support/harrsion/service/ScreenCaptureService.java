package com.support.harrsion.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.support.harrsion.agent.utils.DeviceUtil;
import com.support.harrsion.config.AppConfig;
import com.support.harrsion.dto.screenshot.Screenshot;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * 屏幕截图服务
 *
 * @author harrsion
 * @date 2025/12/15
 */
public class ScreenCaptureService extends Service {

    private static final String TAG = "ScreenCaptureService";
    private static final String ACTION_SCREENSHOT = "com.support.harrsion.ACTION_SCREENSHOT";

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;

    private int mWidth, mHeight, mDensityDpi;
    private Handler mHandler;
    private Bitmap rawBitmap = null;
    private Bitmap finalBitmap = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. 设置屏幕参数
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        mDensityDpi = metrics.densityDpi;
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;

        // 2. 获取 Manager
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // 3. 创建 Handler 用于处理 ImageReader 的回调
        mHandler = new Handler(Looper.getMainLooper());

        // 4. 启动前台服务通知
        DeviceUtil.createNotificationChannel(this,
                AppConfig.Channel.SCREEN_SHOT_SERVICE_CHANNEL, "屏幕截图服务");
        Notification notification = DeviceUtil.buildNotification(this,
                AppConfig.Channel.SCREEN_SHOT_SERVICE_CHANNEL, "屏幕截图服务",
                "您的屏幕内容正在被应用捕获。");
        startForeground(AppConfig.Foreground.SCREEN_SHOT_SERVICE_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_SCREENSHOT.equals(action)) {
                // todo: 延迟设定防止服务启动过快，图片保存失败，具体时长待优化
                mHandler.postDelayed(this::takeScreenshot, 500);
                Log.d(TAG, "收到截图指令并执行。");
                return START_STICKY;
            } else {
                // 第一次启动（收到授权结果）
                int resultCode = intent.getIntExtra("resultCode", 0);
                Intent data = intent.getParcelableExtra("data");

                if (resultCode != 0 && data != null) {
                    // 收到授权结果，初始化 MediaProjection
                    setUpMediaProjection(resultCode, data);
                    setUpVirtualDisplay();
                    Log.d(TAG, "服务初始化完成，等待截图指令...");
                    // 服务保持运行
                    return START_STICKY;
                }
            }

            // 如果没有授权数据，停止自身
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    /**
     * 初始化 MediaProjection 对象。
     */
    private void setUpMediaProjection(int resultCode, Intent data) {
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        if (mMediaProjection == null) {
            Toast.makeText(this, "MediaProjection 初始化失败", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }
        // 监听 MediaProjection 生命周期，在被系统停止时停止服务
        mMediaProjection.registerCallback(new MediaProjection.Callback() {
            @Override
            public void onStop() {
                Log.i(TAG, "MediaProjection 被停止");
                mHandler.post(() -> Toast.makeText(getApplicationContext(), "屏幕捕获已停止", Toast.LENGTH_SHORT).show());
                stopSelf();
            }
        }, mHandler);
    }

    /**
     * 设置 ImageReader 和 VirtualDisplay。
     */
    private void setUpVirtualDisplay() {
        // 创建 ImageReader：捕获屏幕数据的容器
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);

        // 创建 VirtualDisplay：将屏幕内容重定向到 ImageReader 的 Surface
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "ScreenCaptureDisplay",
                mWidth, mHeight, mDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),
                null, mHandler
        );
    }

    /**
     * 执行实际的截图操作。
     */
    private void takeScreenshot() {
        // 确保 ImageReader 有最新的图像
        Image image = mImageReader.acquireLatestImage();

        try (image) {
            if (image == null) {
                Log.e(TAG, "获取图像失败: Image is null");
                mHandler.post(() -> Toast.makeText(getApplicationContext(), "截图失败：请重试", Toast.LENGTH_SHORT).show());
                stopSelf();
                return;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            Image.Plane plane = image.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();

            int pixelStride = plane.getPixelStride();
            int rowStride = plane.getRowStride();
            // 计算完整的 Bitmap 宽度 (包含字节对齐的 Padding)
            int rowPixels = rowStride / pixelStride;

            // 1. 创建原始 Bitmap (包含右侧的无效填充区域)
            rawBitmap = Bitmap.createBitmap(
                    rowPixels,
                    height,
                    Bitmap.Config.ARGB_8888
            );
            // 复制数据，此时 rawBitmap 内部是完整的屏幕数据 + padding
            rawBitmap.copyPixelsFromBuffer(buffer);

            // 及时释放 Image 资源
            image.close();

            // 2. 准备缩放和裁剪
            float scale = (float) 480 / width;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            // 3. 一次性 裁剪 + 缩放
            // 裁剪: 从 rawBitmap 中截取 (0, 0) 到 (width, height) 的有效区域 -> 去除了右侧 Padding
            // 缩放: 应用 matrix 将其缩放到 targetWidth
            finalBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, width, height, matrix, true);

            // 4. 释放巨大的原始 Bitmap
            if (rawBitmap != finalBitmap) {
                rawBitmap.recycle();
                rawBitmap = null;
            }

            // 5. 压缩到 ByteArray
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            // 选择压缩格式，WebP 通常比 JPEG 更小
            Bitmap.CompressFormat format;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ 推荐使用 WEBP_LOSSY
                format = Bitmap.CompressFormat.WEBP_LOSSY;
            } else {
                format = Bitmap.CompressFormat.WEBP;
            }

            finalBitmap.compress(format, 50, os);

            // 6. 释放最终 Bitmap
            finalBitmap.recycle();
            finalBitmap = null;

            // 7. 转换为 Base64 (使用 NO_WRAP 去除换行符)
            byte[] bytes = os.toByteArray();
            String base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP);

            Log.d(TAG, "Final Base64 length: " + base64Data.length());

            Screenshot screenshot = Screenshot.builder()
                    .base64Data(base64Data)
                    .height(height)
                    .width(width)
                    .isSensitive(false)
                    .build();
            DeviceUtil.handleScreenshotResult(screenshot, null);
        } catch (Exception e) {
            Log.e(TAG, "截图处理或保存失败", e);
            if (rawBitmap != null) {
                rawBitmap.recycle();
            }
            if (finalBitmap != null) {
                finalBitmap.recycle();
            }
            if (image != null) {
                image.close();
            }
            DeviceUtil.handleScreenshotResult(null, "截图处理或保存失败");
        }
    }

    /**
     * 释放所有 MediaProjection 相关的资源。
     */
    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        Log.i(TAG, "MediaProjection 资源已释放");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tearDownMediaProjection();
    }
}