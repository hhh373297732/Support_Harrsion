package com.support.harrsion.config;

public interface AppConfig {

    class Model {
        public static String baseUrl = "https://open.bigmodel.cn/api/paas/v4";
        public static String modelName = "autoglm-phone";
        public static String apiKey = "af8c20abc40d466ab939c07ca7359912.ZnyCoeosnZYZGQTJ";
    }

    class Agent {
        public static int maxSteps = 100;
        public static boolean verbose = true;
    }
}
