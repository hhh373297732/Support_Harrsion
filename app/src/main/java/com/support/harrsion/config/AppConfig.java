package com.support.harrsion.config;

public interface AppConfig {

    class Model {
        public static String baseUrl = "https://open.bigmodel.cn/api/paas/v4";
        public static String modelName = "autoglm-phone";
        public static String apiKey = "64e0624cf2d94696ab0f53f6c4360ff1.yd36K7YZrTGKni9w";
    }

    class Agent {
        public static int maxSteps = 100;
        public static boolean verbose = true;
    }

    class Foreground {
        public static int WAKE_UP_SERVICE_ID = 1;
        public static int SCREEN_SHOT_SERVICE_ID = 2;
        public static int AGENT_SERVICE_ID = 3;
    }

    class Channel {
        public static String WAKE_UP_SERVICE_CHANNEL = "WAKE_UP_SERVICE_CHANNEL";
        public static String SCREEN_SHOT_SERVICE_CHANNEL = "SCREEN_SHOT_SERVICE_CHANNEL";
    }
}
