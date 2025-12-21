package com.support.harrsion.voice;

public enum VoiceState {
    VOICE_START,   // 从静音 → 有声
    VOICE_ACTIVE,  // 持续说话
    VOICE_END,     // 从有声 → 静音
    SILENCE        // 持续静音
}
