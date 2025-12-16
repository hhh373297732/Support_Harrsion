package com.support.harrsion.agent.model;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.support.harrsion.dto.model.ModelConfig;
import com.support.harrsion.dto.model.ModelResponse;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 模型客户端
 *
 * @author harrsion
 * @date 2025/12/15
 */
public class ModelClient {

    private final ModelConfig config;
    private final OpenAIClient client;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    public ModelClient(ModelConfig config) {
        this.config = config;
        this.client = OpenAIOkHttpClient.builder()
                .baseUrl(this.config.getBaseUrl())
                .apiKey(this.config.getApiKey())
                .timeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Send a request to the model.
     *
     * @param messages List of message dictionaries in OpenAI format.
     * @return ModelResponse containing thinking and action.
     */
    public ModelResponse request(List<ChatCompletionMessageParam> messages) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .messages(messages)
                .model(this.config.getModelName())
                .maxCompletionTokens(this.config.getMaxTokens())
//                .topP(this.config.getTopP())
//                .frequencyPenalty(this.config.getFrequencyPenalty())
                .temperature(this.config.getTemperature())
                .build();
        ChatCompletion response = this.client.chat().completions().create(params);
        String rawContent = response.choices().get(0).message().content().get();
//         Parse thinking and action from response
        String[] parseResponse = _parseResponse(rawContent);
        String thinking = parseResponse[0];
        String action = parseResponse[1];

        return ModelResponse.builder()
                .thinking(thinking)
                .action(action)
                .rawContent(rawContent)
                .build();
    }

    /**
     * Parse the model response into thinking and action parts.
     *
     * Parsing rules:
     * 1. If content contains 'finish(message=', everything before is thinking,
     *    everything from 'finish(message=' onwards is action.
     * 2. If rule 1 doesn't apply but content contains 'do(action=',
     *    everything before is thinking, everything from 'do(action=' onwards is action.
     * 3. Fallback: If content contains '<answer>', use legacy parsing with XML tags.
     * 4. Otherwise, return empty thinking and full content as action.
     *
     * @param content Raw response content.
     * @return Tuple of (thinking, action).
     */
    private String[] _parseResponse(String content) {
        // Rule 1: Check for finish(message=
        if (content.contains("finish(message=")) {
            String[] parts = content.split("finish\\(message=");
            String thinking = parts[0].strip();
            String action = "finish(message=" + parts[1];
            return new String[]{thinking, action};
        }

        // Rule 2: Check for do(action=
        if (content.contains("do(action=")) {
            String[] parts = content.split("do\\(action=");
            String thinking = parts[0].strip();
            String action = "do(action=" + parts[1];
            return new String[]{thinking, action};
        }

        // Rule 3: Fallback to legacy XML tag parsing
        if (content.contains("<answer>")) {
            String[] parts = content.split("<answer>");
            String thinking = parts[0].replace("<think>", "")
                    .replace("</think>", "").strip();
            String action = parts[1].replace("</answer>", "").strip();
            return new String[]{thinking, action};
        }

        // Rule 4: No markers found, return content as action
        return new String[]{"", content};
    }

    /**
     * 异步请求模型
     *
     * @param messages 消息列表
     * @param callback 回调处理接口
     */
    public void requestAsync(List<ChatCompletionMessageParam> messages, Callback callback) {
        executor.execute(() -> {
            try {
                ModelResponse result = request(messages);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }


    /**
     * 模型请求回调处理接口
     */
    public interface Callback {
        void onSuccess(ModelResponse response);
        void onError(Exception e);
    }
}
