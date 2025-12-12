package com.support.harrsion.agent;

import android.content.Context;
import android.util.Log;

import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.support.harrsion.agent.device.AgentScreenshotCallback;
import com.support.harrsion.agent.model.MessageBuilder;
import com.support.harrsion.agent.model.ModelClient;
import com.support.harrsion.agent.utils.DeviceUtil;
import com.support.harrsion.dto.StepResult;
import com.support.harrsion.dto.agent.AgentConfig;
import com.support.harrsion.dto.model.ModelConfig;
import com.support.harrsion.dto.model.ModelResponse;
import com.support.harrsion.dto.model.UserMessageOption;
import com.support.harrsion.dto.screenshot.Screenshot;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Agent implements AgentScreenshotCallback {

    private final AgentConfig agentConfig;
    private final ModelConfig modelConfig;
    private final List<ChatCompletionMessageParam> _context = new ArrayList<>();
    private final Context appContext;

    private int _stepCount = 0;
    private ModelClient modelClient;
    private String _currentTask; // 🌟 新增：存储当前任务

    public Agent(Context context, ModelConfig modelConfig) {
        this.appContext = context;
        this.modelConfig = modelConfig;
        this.agentConfig = new AgentConfig();
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
        this.reset();
    }

    /**
     * Run the agent to complete a task.
     *
     * @param task Natural language description of the task.
     * @return Final message from the agent.
     */
    public void run(String task) {
        // init context and step count
        this._context.clear();
        this._stepCount = 0;
        this._currentTask = task; // 存储任务

        // 🌟 第一步：启动异步截图，并将 Agent 自身作为回调传递
        this._executeStepAsync(true);


        // First step with user prompt
//        StepResult result = _executeStep(task, true);

//        if (result.getFinished()) {
//            return Optional.of(result.getMessage()).orElse("Task completed");
//        }
//
//        while (this._stepCount < this.agentConfig.getMaxSteps()) {
//            result = _executeStep(task, true);
//
//            if (result.getFinished()) {
//                return Optional.of(result.getMessage()).orElse("Task completed");
//            }
//        }
//        return "Max steps reached";
    }

    /**
     * Execute a single step of the agent.
     * Useful for manual control or debugging.
     *
     * @param task Task description (only needed for first step).
     * @return StepResult with step details.
     */
//    private StepResult step(String task) {
//        boolean isFirst = this._context.isEmpty();
//        if (isFirst && StringUtils.isEmpty(task)) {
//            throw new RuntimeException("Task is required for the first step");
//        }
//        return this._executeStep(task, isFirst);
//    }

    /**
     * Reset the agent state for a new task.
     */
    private void reset() {
        this._context.clear();
        this._stepCount = 0;
    }

    /**
     * Execute a single step of the agent loop.
     *
     * @param userPrompt
     * @param isFirst
     * @return
     */
//    private StepResult _executeStep(String userPrompt, boolean isFirst) {
//        this._stepCount++;
//
//        DeviceUtil.triggerScreenshot(this.appContext);
//        String currentApp = DeviceUtil.getHardwareDeviceName();
//
//        String screenInfo = MessageBuilder.buildScreenInfo(currentApp);
//        if (isFirst) {
//            this._context.add(MessageBuilder.createSystemMessage(this.agentConfig.getSystemPrompt()));
//
//            String textContent = String.format("%s\n\n%s", userPrompt, screenInfo);
//
//            this._context.add(MessageBuilder.createUserMessage(
//                    UserMessageOption.builder().text(textContent).imageBase64(screenshot.getBase64Data()).build()));
//        } else {
//            String textContent = String.format("** Screen Info **\n\n%s", screenInfo);
//
//            this._context.add(MessageBuilder.createUserMessage(
//                    UserMessageOption.builder().text(textContent).imageBase64(screenshot.getBase64Data()).build()));
//        }
//
//        // Get model response
//        ModelResponse response = this.modelClient.request(this._context);
//
//        // Parse action from response
////        action = parse_action(response.action)
//
//        return StepResult.builder()
//                .success(true)
//                .finished(true)
//                .build();
//    }

    /**
     * 启动异步截图，并等待回调。
     * @param isFirst 是否为第一步
     */
    private void _executeStepAsync(boolean isFirst) {
        if (this._stepCount >= this.agentConfig.getMaxSteps()) {
            Log.d("Agent", "Max steps reached");
            return; // 结束流程
        }

        DeviceUtil.triggerScreenshot(this.appContext, this);
    }

    @Override
    public void onScreenshotReady(Screenshot screenshot) {
        // 🌟 截图数据到手！现在可以继续执行 LLM 逻辑
        this._stepCount++;

        // 检查是否为第一步
        boolean isFirst = this._stepCount == 1;


        String currentApp = DeviceUtil.getHardwareDeviceName();

        String screenInfo = MessageBuilder.buildScreenInfo(currentApp);

        if (isFirst) {
            this._context.add(MessageBuilder.createSystemMessage(this.agentConfig.getSystemPrompt()));
            String textContent = String.format("%s\n\n%s", this._currentTask, screenInfo);
            this._context.add(MessageBuilder.createUserMessage(
                    UserMessageOption.builder().text(textContent).imageBase64(screenshot.getBase64Data()).build()));
        } else {
            String textContent = String.format("** Screen Info **\n\n%s", screenInfo);
            this._context.add(MessageBuilder.createUserMessage(
                    UserMessageOption.builder().text(textContent).imageBase64(screenshot.getBase64Data()).build()));
        }

        // 2. 获取模型响应
        this.modelClient.requestAsync(this._context, new ModelClient.Callback() {
            @Override
            public void onSuccess(ModelResponse response) {
                Log.d("Agent", "Model Response: " + response.getRawContent());
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

        // 3. 解析 Action 并执行
        // Action action = parse_action(response.action);
        // StepResult result = execute_action(action);

        // 4. 检查是否完成，如果未完成，继续下一步异步流程
        // if (!result.getFinished()) {
        //     this._executeStepAsync(false); // 再次启动异步截图，进入下一轮循环
        // } else {
        //    Log.d("Agent", "Task completed: " + result.getMessage());
        // }
    }

    @Override
    public void onScreenshotFailed(String error) {
        Log.e("Agent", "Screenshot failed: " + error);
    }
}
