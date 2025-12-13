package com.support.harrsion.config;

public enum AppPackage {
    WECHAT("微信", "com.tencent.mm"),
    QQ("QQ", "com.tencent.mobileqq"),
    XHS("小红书", "com.xingin.xhs"),
    MEITUAN("美团", "com.sankuai.meituan"),
    WEIBO("微博", "com.sina.weibo");

    public final String label;
    public final String pkg;

    AppPackage(String label, String pkg) {
        this.label = label;
        this.pkg = pkg;
    }

    public static AppPackage fromLabel(String label) {
        for (AppPackage a : values()) {
            if (a.label.equals(label)) return a;
        }
        return null;
    }
}
