package com.support.harrsion.agent.model;

import android.util.Log;

import com.alibaba.fastjson2.JSON;
import com.openai.core.JsonField;
import com.openai.models.beta.threads.messages.ImageUrl;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionContentPart;
import com.openai.models.chat.completions.ChatCompletionContentPartImage;
import com.openai.models.chat.completions.ChatCompletionContentPartText;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionStoreMessage;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.support.harrsion.dto.model.UserMessageOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class for building conversation messages.
 */
public class MessageBuilder {


    public static ChatCompletionMessageParam createSystemMessage(String content) {
        Log.d("Agent", "Creating system message: " + content);
        return ChatCompletionMessageParam.ofSystem(ChatCompletionSystemMessageParam.builder().content(content).build());
    }

    /**
     * Create a user message with optional image.
     * @param option user message
     * @return Message dictionary.
     */
    public static ChatCompletionMessageParam createUserMessage(UserMessageOption option) {
        List<ChatCompletionContentPart> parts = new ArrayList<>();
        if (!option.getImageBase64().isEmpty()) {
            ChatCompletionContentPart imageUrl = ChatCompletionContentPart
                    .ofImageUrl(ChatCompletionContentPartImage.builder()
                            .imageUrl(ChatCompletionContentPartImage.ImageUrl.builder()
                                    .url("data:image/png;base64," + option.getImageBase64())
                                    .build())
                            .build());
            parts.add(imageUrl);
            Log.d("Agent", "Creating user message - img: " + "data:image/png;base64," + option.getImageBase64());
        }
        ChatCompletionContentPart text = ChatCompletionContentPart
                .ofText(ChatCompletionContentPartText.builder().text(option.getText()).build());
        parts.add(text);
        Log.d("Agent", "Creating user message - text: " + option.getText());

        ChatCompletionUserMessageParam.Content content1 = ChatCompletionUserMessageParam.Content.ofArrayOfContentParts(parts);
        return ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder().content(content1).build());

    }

    public static ChatCompletionMessageParam createAssistantMessage(String content) {
        return ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder().content(content).build());
    }

    /**
     * Remove image content from a message to save context space.
     * @param message Message dictionary.
     * @return Message with images removed.
     */
    public static Map<String, Object> removeImagesFromMessage(Map<String, Object> message) {
        if (message.get("content") instanceof List<?>) {
            List<?> contentList = (List<?>) message.get("content");

            List<Map<String, Object>> filtered = contentList.stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> (Map<String, Object>) item)
                    .filter(item -> "text".equals(item.get("type")))
                    .collect(Collectors.toList());

            message.put("content", filtered);
        }

        return message;
    }

    /**
     * Build screen info string for the model.
     * @param currentApp Current app name.
     * @return JSON string with screen info.
     */
    public static String buildScreenInfo(String currentApp) {
        return JSON.toJSONString(Map.of("current_app", currentApp));
    }
}
