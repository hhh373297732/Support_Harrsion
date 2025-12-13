# =========================================================
# 1. Android & Compose 基础规则
# =========================================================
# 保留 Android Lifecycle
-keep class androidx.lifecycle.** { *; }

# 保留 Compose 运行时所需的类
-keep class androidx.compose.** { *; }

# 保留枚举值的标准方法 (对 Compose 和 JSON 库都很重要)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =========================================================
# 2. OpenAI Java SDK (关键部分)
# =========================================================
# 你的日志显示 OpenAI 的 Builder 验证非常慢。
# 保留这些类可以防止 R8 破坏其复杂的泛型结构，有助于稳定运行。
-keep class com.openai.** { *; }
-keep interface com.openai.** { *; }

# 确保 OpenAI 的数据模型不被混淆，否则 JSON 转换会失败
-keep class com.openai.models.** { *; }

# =========================================================
# 3. Jackson (OpenAI SDK 内部依赖)
# =========================================================
# ⚠️ 必须配置。解决 "JsonMissing cannot be serialized" 报错。
# OpenAI SDK 内部依赖 Jackson 进行序列化，混淆其注解或内部类会导致运行时崩溃。
-keep class com.fasterxml.jackson.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**

# =========================================================
# 4. FastJSON2 (你的 JSON 库)
# =========================================================
# FastJSON2 严重依赖反射来查找构造函数和字段。
-keep class com.alibaba.fastjson2.** { *; }
-keepclassmembers class * {
    @com.alibaba.fastjson2.annotation.JSONField <fields>;
    @com.alibaba.fastjson2.annotation.JSONField <methods>;
    @com.alibaba.fastjson2.annotation.JSONType <methods>;
    @com.alibaba.fastjson2.annotation.JSONType <fields>;
}

# =========================================================
# 5. Apache Commons Lang3
# =========================================================
# 这是一个工具库，通常保留其公开 API 即可。
-keep class org.apache.commons.lang3.** { *; }

# =========================================================
# 6. 其他基础配置
# =========================================================
# 保留 Kotlin 元数据 (对反射和 Coroutines 至关重要)
-keep class kotlin.Metadata { *; }
-keep class kotlinx.coroutines.** { *; }

# 忽略 OkHttp 的警告 (OpenAI 网络层依赖)
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Lombok 是编译时工具，不会打入 APK，因此不需要任何 ProGuard 规则。

-dontwarn java.lang.invoke.MethodHandleProxies
-dontwarn java.lang.reflect.AnnotatedParameterizedType
-dontwarn java.lang.reflect.AnnotatedType