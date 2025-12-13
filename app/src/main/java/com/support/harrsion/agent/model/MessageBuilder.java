package com.support.harrsion.agent.model;

import com.alibaba.fastjson2.JSON;
import com.openai.core.JsonArray;
import com.openai.core.JsonObject;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionContentPart;
import com.openai.models.chat.completions.ChatCompletionContentPartImage;
import com.openai.models.chat.completions.ChatCompletionContentPartText;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
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
//        Log.d("Agent", "Creating system message: " + content);
        return ChatCompletionMessageParam.ofSystem(ChatCompletionSystemMessageParam.builder()
                .content(content)
                .build());
    }

    /**
     * Create a user message with optional image.
     * @param option user message
     * @return Message dictionary.
     */
    public static ChatCompletionMessageParam createUserMessage(UserMessageOption option) {
        List<ChatCompletionContentPart> parts = new ArrayList<>();
        if (option.getImageBase64() != null) {
            ChatCompletionContentPart imageUrl = ChatCompletionContentPart
                    .ofImageUrl(ChatCompletionContentPartImage.builder()
                            .imageUrl(ChatCompletionContentPartImage.ImageUrl.builder()
                                    .url("data:image/webp;base64," + option.getImageBase64().replaceAll("\n", ""))
                                    .build())
                            .build());
            parts.add(imageUrl);
//            Log.d("Agent", "Creating user message - img: " + "data:image/png;base64," + option.getImageBase64());
        }
        ChatCompletionContentPart text = ChatCompletionContentPart
                .ofText(ChatCompletionContentPartText.builder()
                        .text(option.getText())
                        .build());
        parts.add(text);
//        Log.d("Agent", "Creating user message - text: " + option.getText());
        ChatCompletionUserMessageParam.Content contentList = ChatCompletionUserMessageParam.Content.ofArrayOfContentParts(parts);
        return ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder()
                .content(contentList)
                .build());
    }

    public static ChatCompletionMessageParam createAssistantMessage(String content) {
        return ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder()
                .content(content)
                .build());
    }

    /**
     * Remove image content from a message to save context space.
     * @param message Message dictionary.
     * @return Message with images removed.
     */
    public static ChatCompletionMessageParam removeImagesFromMessage(ChatCompletionMessageParam message) {
        if (message.isUser()) {
            if (message.user().get().content().isArrayOfContentParts()) {
                List<ChatCompletionContentPart> newList = message.user().get().content().asArrayOfContentParts().stream()
                        .filter(ChatCompletionContentPart::isText).collect(Collectors.toList());
                return ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder()
                        .content(ChatCompletionUserMessageParam.Content.ofArrayOfContentParts(newList))
                        .build());
            } else {
                return message;
            }
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
