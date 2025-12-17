package com.support.harrsion.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.support.harrsion.MainActivity;
import com.support.harrsion.viewModel.SharedViewModel;

/**
 * 任务完成全局广播
 *
 * @author harrsion
 * @date 2025/12/16
 */
public class TaskBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_TASK_COMPLETED = "com.support.harrsion.action.AGENT_TASK_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TaskBroadcastReceiver", "收到广播，action: " + intent.getAction());
        if (ACTION_TASK_COMPLETED.equals(intent.getAction())) {
            String resultMessage = intent.getStringExtra("result_message");
            Log.d("TaskBroadcastReceiver", "收到任务完成消息: " + resultMessage);
            if (context instanceof MainActivity) {
                ViewModelProvider viewModelProvider = new ViewModelProvider((ViewModelStoreOwner) context,
                        ViewModelProvider.AndroidViewModelFactory.getInstance(((MainActivity) context).getApplication()));
                SharedViewModel sharedViewModel = viewModelProvider.get(SharedViewModel.class);
                sharedViewModel.taskCompleted(false, resultMessage);
            }
        }
    }


}
