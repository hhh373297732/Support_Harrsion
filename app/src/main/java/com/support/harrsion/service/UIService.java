package com.support.harrsion.service;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.support.harrsion.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * UI交互服务
 * 处理键盘管理、提示词点击事件、logo加载等UI相关功能
 *
 * @author harrsion
 * @date 2025/12/17
 */
public class UIService {
    private Context context;
    private MainActivity mainActivity;

    public UIService(Context context, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
    }

    /**
     * 设置首页提示词点击事件
     */
    public void setupPromptWordsListeners(EditText inputText) {
        TextView prompt1 = mainActivity.findViewById(com.support.harrsion.R.id.prompt_1);
        TextView prompt2 = mainActivity.findViewById(com.support.harrsion.R.id.prompt_2);
        
        View.OnClickListener promptClickListener = v -> {
            if (v instanceof TextView) {
                String promptText = ((TextView) v).getText().toString();
                inputText.setText(promptText);
                mainActivity.sendMessage(inputText);
            }
        };
        
        if (prompt1 != null) {
            prompt1.setOnClickListener(promptClickListener);
        }
        
        if (prompt2 != null) {
            prompt2.setOnClickListener(promptClickListener);
        }
    }

    /**
     * 设置点击聊天区域隐藏键盘的功能
     */
    public void setupKeyboardHiding() {
        ScrollView scrollView = mainActivity.findViewById(com.support.harrsion.R.id.content_scroll_view);
        View welcomeArea = mainActivity.findViewById(com.support.harrsion.R.id.welcome_area);
        View messagesContainer = mainActivity.findViewById(com.support.harrsion.R.id.messages_container);

        if (scrollView != null) {
            scrollView.setOnClickListener(v -> hideKeyboard());
        }

        if (welcomeArea != null) {
            welcomeArea.setOnClickListener(v -> hideKeyboard());
        }

        if (messagesContainer != null) {
            messagesContainer.setOnClickListener(v -> hideKeyboard());
        }
    }

    /**
     * 隐藏软键盘
     */
    public void hideKeyboard() {
        try {
            EditText inputText = mainActivity.findViewById(com.support.harrsion.R.id.input_text);
            if (inputText != null && inputText.isFocused()) {
                inputText.clearFocus();
            }
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mainActivity.getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("UIService", "Error hiding keyboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从assets加载logo图片
     */
    public void loadLogoImage() {
        ImageView logoImage = mainActivity.findViewById(com.support.harrsion.R.id.logo_image);
        try {
            InputStream inputStream = context.getAssets().open("logo.png");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            logoImage.setImageBitmap(bitmap);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}