package com.support.harrsion.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.support.harrsion.R;
import com.support.harrsion.agent.utils.DeviceUtil;
import com.support.harrsion.dto.screenshot.Screenshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Date;

public class ScreenCaptureService extends Service {

    private static final String TAG = "ScreenCaptureService";
    private static final int NOTIFICATION_ID = 101;
    private static final String NOTIFICATION_CHANNEL_ID = "ScreenCaptureChannel";
    private static final String ACTION_SCREENSHOT = "com.support.harrsion.ACTION_SCREENSHOT";

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;

    private int mWidth, mHeight, mDensityDpi;
    private Handler mHandler;

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
        createNotificationChannel();
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_SCREENSHOT.equals(action)) {
                // 🚀 收到截图指令，立即执行截图
                mHandler.post(this::takeScreenshot);
                Log.d(TAG, "收到截图指令并执行。");
                // 服务保持运行
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
        if (image == null) {
            Log.e(TAG, "获取图像失败: Image is null");
            Toast.makeText(this, "截图失败：请重试", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        try {
            // 1. 获取图像参数
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;

            // 2. 创建 Bitmap
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);

            // 3. 裁剪掉可能存在的填充像素
            if (rowPadding > 0) {
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // 压缩为 PNG 格式 (无损且支持透明度，推荐用于截图)
            // 如果对文件大小要求更高，可以使用 JPEG，但会损失画质。
            bitmap.compress(Bitmap.CompressFormat.WEBP, 80, outputStream);

            byte[] byteArray = outputStream.toByteArray();

            // 释放 Bitmap 内存
            bitmap.recycle();

            String base64Data = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.d(TAG, "Base64 数据长度: " + base64Data.length());

            Screenshot screenshot = Screenshot.builder()
                    .base64Data(base64Data)
                    .height(height)
                    .width(width)
                    .isSensitive(false)
                    .build();
            DeviceUtil.handleScreenshotResult(screenshot, null);
        } catch (Exception e) {
            Log.e(TAG, "截图处理或保存失败", e);
            DeviceUtil.handleScreenshotResult(null, "截图处理或保存失败");
        } finally {
            // **必须关闭 Image** 否则会内存泄漏
            image.close();
        }
    }

    /**
     * 将 Bitmap 保存到公共 Pictures 目录。
     */
    private void saveBitmap(Bitmap bitmap) {
        String filename = "Screenshot_" + new Date().getTime() + ".png";
        File dir = getExternalFilesDir(null); // 示例：保存到 App 的私有外部存储
        // 实际应用中，您可能需要保存到公共目录，那需要处理 Android Q+ 的 Scoped Storage
        File file = new File(dir, filename);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            mHandler.post(() -> Toast.makeText(getApplicationContext(), "截图成功，保存到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show());
            Log.i(TAG, "截图保存成功: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "保存文件失败", e);
            mHandler.post(() -> Toast.makeText(getApplicationContext(), "保存截图失败", Toast.LENGTH_SHORT).show());
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

    // --- 前台通知相关代码 ---

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "屏幕捕获服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification buildNotification() {
        // 可以在这里添加一个指向 MainActivity 的 PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("屏幕捕获正在运行")
                    .setContentText("您的屏幕内容正在被应用捕获。")
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // 替换为你的应用图标
                    .build();
        }
        return null;
    }
}