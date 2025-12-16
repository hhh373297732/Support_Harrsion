package com.support.harrsion.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * 共享视图模型
 *
 * @author harrsion
 * @date 2025/12/16
 */
public class SharedViewModel extends ViewModel {

    private MutableLiveData<UiState> uiState = new MutableLiveData<>();

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    /**
     * 任务完成
     *
     * @param welcomeAreaVisible 是否显示欢迎界面
     * @param message 消息
     */
    public void taskCompleted(boolean welcomeAreaVisible, String message) {
        UiState newState = new UiState(welcomeAreaVisible, message);
        uiState.postValue(newState);
    }

    public static class UiState {
        public boolean welcomeAreaVisible;
        public String message;

        public UiState(boolean welcomeAreaVisible, String message) {
            this.welcomeAreaVisible = welcomeAreaVisible;
            this.message = message;
        }
    }
}