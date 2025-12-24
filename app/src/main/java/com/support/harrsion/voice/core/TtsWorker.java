package com.support.harrsion.voice.core;

import android.util.Log;
import com.support.harrsion.dto.xtts.XTTSParams;
import com.support.harrsion.voice.manager.XTTSManager;

import lombok.Setter;

/**
 * 播报工人：负责语音合成。
 * 它的核心职责是：说话，并在说完时通知 Kernel。
 */
public class TtsWorker {

    private static final String TAG = "TtsWorker";

    private final XTTSManager ttsManager;
    @Setter
    private Callback callback;

    public interface Callback {
        void onTtsStart();
        void onTtsComplete();
    }

    public TtsWorker(XTTSManager ttsManager) {
        this.ttsManager = ttsManager;
        initInternalListener();
    }

    // 初始化内部监听，把 XTTSManager 的事件转化成 Worker 的事件
    private void initInternalListener() {
        // 这里假设我们在之前修改 XTTSManager 时已经添加了 setCompletionListener
        // 如果没有，我们需要利用 XTTSManager 现有的回调机制

        // 这里的逻辑稍微依赖于你 XTTSManager 的具体实现
        // 为了稳健，我们通过匿名内部类或者 setter 来桥接
        // (注：由于没有修改 XTTSManager 的源码，这里演示最通用的桥接方式，
        //  实际项目中最好直接在 XTTSManager 里加一个 Listener 接口)

        /* 重要提示：
           请确保之前的 XTTSManager.java 中，onComplete() 方法里
           不要直接调用 output.stop()，否则会吞尾音。
           并且，我们需要一种方式知道 TTS 播完了。
        */

        ttsManager.setCompletionListener(() -> {
            Log.d(TAG, "TTS Finished");
            if (callback != null) {
                callback.onTtsComplete();
            }
        });
    }

    public void speak(String text) {
        // 默认参数，也可以传参
        XTTSParams params = new XTTSParams();
        Log.d(TAG, "Speaking: " + text);

        // 通知外部：我要开始说了
        if (callback != null) {
            callback.onTtsStart();
        }

        ttsManager.speak(text, params);
    }

    public void stop() {
        ttsManager.stop();
    }
}