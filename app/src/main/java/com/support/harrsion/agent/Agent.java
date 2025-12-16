package com.support.harrsion.agent;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.alibaba.fastjson2.JSON;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.support.harrsion.agent.action.ActionHandle;
import com.support.harrsion.agent.model.MessageBuilder;
import com.support.harrsion.agent.model.ModelClient;
import com.support.harrsion.utils.DeviceUtil;
import com.support.harrsion.utils.MessageParseUtil;
import com.support.harrsion.config.AppConfig;
import com.support.harrsion.dto.action.ActionResult;
import com.support.harrsion.dto.agent.AgentConfig;
import com.support.harrsion.dto.model.ModelConfig;
import com.support.harrsion.dto.model.ModelResponse;
import com.support.harrsion.dto.model.UserMessageOption;
import com.support.harrsion.dto.screenshot.Screenshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Agent 处理器
 *
 * @author harrsion
 * @date 2025/12/15
 */
public class Agent implements DeviceUtil.ScreenshotCallback {

    private final AgentConfig agentConfig;
    private final ModelConfig modelConfig;
    private final List<ChatCompletionMessageParam> _context = new ArrayList<>();
    private final Context appContext;

    private int _stepCount = 0;
    private ModelClient modelClient;
    private String _currentTask;
    private String _currentApp;
    private ActionHandle actionHandle;

    public Agent(Context context, ModelConfig modelConfig) {
        this.appContext = context;
        this.modelConfig = modelConfig;
        this.agentConfig = new AgentConfig(AppConfig.Agent.maxSteps, AppConfig.Agent.verbose);
        this._init();
    }

    public Agent(Context context, ModelConfig modelConfig, AgentConfig agentConfig) {
        this.appContext = context;
        this.modelConfig = modelConfig;
        this.agentConfig = agentConfig;
        this._init();
    }

    private void _init() {
        this.modelClient = new ModelClient(this.modelConfig);
        this.actionHandle = new ActionHandle(this.appContext);
        this.reset();
    }

    /**
     * Run the agent to complete a task.
     *
     * @param task Natural language description of the task.
     * @return Final message from the agent.
     */
    public void run(String task) {
        this.reset();
        this._currentTask = task;

        this._checkModelApi();
    }

    /**
     * Reset the agent state for a new task.
     */
    private void reset() {
        this._context.clear();
        this._context.add(MessageBuilder.createSystemMessage(this.agentConfig.getSystemPrompt()));
        this._stepCount = 0;
    }


    /**
     * 启动异步截图，并等待回调。
     */
    private void _executeStepAsync() {
        if (this._stepCount >= this.agentConfig.getMaxSteps()) {
            Log.d("Agent", "Max steps reached");
            return; // 结束流程
        }

        DeviceUtil.triggerScreenshot(this.appContext, this);
    }

    /**
     * 检查模型API是否可用。
     */
    private void _checkModelApi() {
        this.modelClient.requestAsync(List.of(MessageBuilder
                        .createUserMessage(UserMessageOption.builder().text("Hi,回复一个ok").build()))
                , new ModelClient.Callback() {
            @Override
            public void onSuccess(ModelResponse response) {
                if (_currentApp == null) {
                    _currentApp = DeviceUtil.getHardwareDeviceName();
                }
                if (agentConfig.getVerbose()) {
                    Log.d("Agent", "✅ Model API checks passed!");
                    Log.d("Agent", "=".repeat(50));
                    Log.d("Agent", "Phone Agent - AI-powered phone automation");
                    Log.d("Agent", "=".repeat(50));
                    Log.d("Agent", "Model：" + modelConfig.getModelName());
                    Log.d("Agent", "Base URL：" + modelConfig.getBaseUrl());
                    Log.d("Agent", "Max Steps：" + agentConfig.getMaxSteps());
                    Log.d("Agent", "Device：" + _currentApp);
                    Log.d("Agent", "=".repeat(50));
                }
                _executeStepAsync();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.d("Agent", "❌ Model API check failed. Please fix the issues above.");
            }
        });
    }

    /**
     * 截图回调处理
     *
     * @param screenshot 包含 Base64 数据的截图对象
     */
    @Override
    public void onScreenshotReady(Screenshot screenshot) {
        this._stepCount++;

        boolean isFirst = this._stepCount == 1;

        String screenInfo = MessageBuilder.buildScreenInfo(this._currentApp);

        String textContent;
        if (isFirst) {
            textContent = String.format("%s\n\n%s", this._currentTask, screenInfo);
        } else {
            textContent = String.format("** Screen Info **\n\n%s", screenInfo);
        }
        this._context.add(MessageBuilder.createUserMessage(
                UserMessageOption.builder()
                        .text(textContent)
                        .imageBase64(screenshot.getBase64Data())
                        .build()));

        this.modelClient.requestAsync(this._context, new ModelClient.Callback() {
            @Override
            public void onSuccess(ModelResponse response) {
//                Log.d("Agent", "Model Response: " + response.getRawContent());
                Map<String, Object> action = MessageParseUtil.parseAction(response.getAction());
                if (agentConfig.getVerbose()) {
                    Log.d("Agent", "=".repeat(50));
                    Log.d("Agent", "\uD83D\uDCAD thinking:");
                    Log.d("Agent", "-".repeat(50));
                    Log.d("Agent", response.getThinking());
                    Log.d("Agent", "-".repeat(50));
                    Log.d("Agent", "\uD83C\uDFAF action:");
                    Log.d("Agent", JSON.toJSONString(action));
                    Log.d("Agent", "=".repeat(50));
                }

                // 删除历史记录的图片信息，减少无效token
                _context.set(_context.size() - 1, MessageBuilder
                        .removeImagesFromMessage(_context.get(_context.size() - 1)));

                // 执行动作
                ActionResult result = actionHandle.execute(action, screenshot.getWidth(), screenshot.getHeight());

                try{
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 添加思考和操作上下文
                _context.add(MessageBuilder.createAssistantMessage(
                        String.format("<think>%s</think><answer>%s</answer>",
                                response.getThinking(), response.getAction())));

                Boolean finished;
                String metadata = String.valueOf(action.get("_metadata"));
                Log.d("Agent", "当前action metadata: " + metadata);
                Log.d("Agent", "result.getShouldFinish(): " + result.getShouldFinish());
                Log.d("Agent", "result.getMessage(): " + result.getMessage());
                
                // 检查是否应该完成任务
                if (metadata.equals("finish") || result.getShouldFinish() || metadata.contains("finish")) {
                    finished = true;
                } else {
                    finished = false;
                }
                Log.d("Agent", "最终任务完成状态: " + finished);

                if (finished) {
                    String completionMessage;
                    
                    // 确保能获取到完成消息
                    if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                        completionMessage = result.getMessage();
                    } else if (action.containsKey("message")) {
                        completionMessage = String.valueOf(action.get("message"));
                    } else {
                        // 如果没有明确的完成消息，使用默认消息
                        completionMessage = "任务完成！";
                    }
                    
                    Log.d("Agent", "最终使用的完成消息: " + completionMessage);
                    
                    if (agentConfig.getVerbose()) {
                        Log.d("Agent", "🎉 " + "=".repeat(47));
                        Log.d("Agent", "✅ task_completed：" + completionMessage);
                        Log.d("Agent", "=".repeat(50));
                    }
                    
                    // 发送全局广播，将结果传递给MainActivity
                    Log.d("Agent", "准备发送任务完成全局广播...");
                    Intent broadcastIntent = new Intent("com.support.harrsion.AGENT_TASK_COMPLETED");
                    broadcastIntent.putExtra("result_message", completionMessage);
                    broadcastIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    
                    // 指定广播只发送给当前应用程序
                    broadcastIntent.setPackage(appContext.getPackageName());
                    
                    Log.d("Agent", "全局广播消息内容: " + completionMessage);
                    Log.d("Agent", "全局广播Intent: " + broadcastIntent);
                    Log.d("Agent", "全局广播Action: " + broadcastIntent.getAction());
                    Log.d("Agent", "全局广播Flags: " + broadcastIntent.getFlags());
                    Log.d("Agent", "全局广播Package: " + broadcastIntent.getPackage());
                    
                    try {
                        appContext.sendBroadcast(broadcastIntent);
                        Log.d("Agent", "任务完成全局广播已发送");
                    } catch (Exception e) {
                        Log.e("Agent", "发送全局广播失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    return;
                }

                if (_stepCount < agentConfig.getMaxSteps()) {
                    _executeStepAsync();
                } else {
                    Log.d("Agent", "❌ Max steps reached");
                }

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * 截图失败处理
     *
     * @param error
     */
    @Override
    public void onScreenshotFailed(String error) {
        Log.e("Agent", "Screenshot failed: " + error);
    }
}
