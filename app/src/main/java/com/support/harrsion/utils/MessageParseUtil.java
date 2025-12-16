package com.support.harrsion.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息解析工具类
 *
 * @author harrsion
 * @date 2025/12/15
 */
public class MessageParseUtil {

    public static Map<String, Object> parseAction(String response) {
        response = response.trim();

        if (response.startsWith("do")) {
            return parseDoAction(response);
        } else if (response.startsWith("finish")) {
            return parseFinishAction(response);
        } else {
            throw new IllegalArgumentException("Failed to parse action: " + response);
        }
    }

    private static Map<String, Object> parseFinishAction(String response) {
        Map<String, Object> action = new HashMap<>();
        action.put("_metadata", "finish");

        // 提取 message 参数
        // 格式: finish(message="some message")
        String message = extractFinishMessage(response);
        action.put("message", message);

        return action;
    }

    private static Map<String, Object> parseDoAction(String response) {
        try {
            // 方法1: 简单的字符串解析
            return parsePythonDict(response);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to eval action: " + response, e);
        }
    }

    private static String extractFinishMessage(String response) {
        // 使用正则表达式提取 message 内容
        // 匹配 finish(message="...") 或 finish(message='...')
        Pattern pattern = Pattern.compile("finish\\(message=[\"'](.*?)[\"']\\)");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // 如果正则不匹配，尝试简单的方法
        if (response.startsWith("finish(message=")) {
            // 去除 "finish(message=" 前缀和最后的 ")"
            String content = response.substring("finish(message=".length());
            if (content.endsWith(")")) {
                content = content.substring(0, content.length() - 1);
            }
            // 去除可能的引号
            content = content.replaceAll("^[\"']|[\"']$", "");
            return content;
        }

        return "";
    }

    private static Map<String, Object> parsePythonDict(String pythonStr) {
        Map<String, Object> result = new HashMap<>();
        result.put("_metadata", "do");

        if (pythonStr == null || pythonStr.isEmpty()) {
            return result;
        }

        String str = pythonStr.trim();
        if (str.startsWith("do(") && str.endsWith(")")) {
            str = str.substring(3, str.length() - 1);
        }

        for (String pair : splitTopLevel(str)) {
            String[] kv = pair.split("=", 2);
            if (kv.length != 2) continue;

            String key = kv[0].trim();
            String value = kv[1].trim();

            result.put(key, parsePythonValue(value));
        }

        return result;
    }

    private static List<String> splitTopLevel(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        int depth = 0;
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // 处理字符串
            if (c == '"' || c == '\'') {
                if (inQuotes && c == quoteChar) {
                    inQuotes = false;
                } else if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                }
                current.append(c);
                continue;
            }

            if (!inQuotes) {
                if (c == '[' || c == '(' || c == '{') depth++;
                else if (c == ']' || c == ')' || c == '}') depth--;
                else if (c == ',' && depth == 0) {
                    result.add(current.toString().trim());
                    current.setLength(0);
                    continue;
                }
            }

            current.append(c);
        }

        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }

    private static Object parsePythonValue(String value) {
        // 去除可能的引号
        value = value.trim();

        // 检查是否为字符串
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }

        // 检查是否为布尔值
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }

        // 检查是否为 null/None
        if (value.equalsIgnoreCase("null") || value.equalsIgnoreCase("none")) {
            return null;
        }

        // 检查是否为数字
        try {
            // 尝试解析为整数
            if (value.matches("-?\\d+")) {
                return Long.parseLong(value);
            }
            // 尝试解析为浮点数
            if (value.matches("-?\\d+(\\.\\d+)?")) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            // 不是数字，返回原字符串
        }

        // 默认返回字符串
        return value;
    }
}
