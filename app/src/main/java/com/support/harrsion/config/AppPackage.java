package com.support.harrsion.config;

public enum AppPackage {
    // --- Social & Communications ---
    WECHAT("微信", "com.tencent.mm"),
    QQ("QQ", "com.tencent.mobileqq"),
    WEIBO("微博", "com.sina.weibo"),
    XHS("小红书", "com.xingin.xhs"),
    DOUBAN("豆瓣", "com.douban.frodo"),
    ZHIHU("知乎", "com.zhihu.android"),
    TELEGRAM("Telegram", "org.telegram.messenger"),
    WHATSAPP("WhatsApp", "com.whatsapp"),
    TWITTER("Twitter", "com.twitter.android"),
    X("X", "com.twitter.android"),
    QUORA("Quora", "com.quora.android"),
    REDDIT("Reddit", "com.reddit.frontpage"),

    // --- E-commerce & Shopping ---
    TAOBAO("淘宝", "com.taobao.taobao"),
    JINGDONG("京东", "com.jingdong.app.mall"),
    PINDUODUO("拼多多", "com.xunmeng.pinduoduo"),
    TAOBAO_SHANGOU("淘宝闪购", "com.taobao.taobao"), // Note: Same package as Taobao
    JINGDONG_MIAOSONG("京东秒送", "com.jingdong.app.mall"), // Note: Same package as JD
    TEMU("Temu", "com.einnovation.temu"),

    // --- Maps & Navigation ---
    GAODE_MAP("高德地图", "com.autonavi.minimap"),
    BAIDU_MAP("百度地图", "com.baidu.BaiduMap"),
    GOOGLE_MAPS("Google Maps", "com.google.android.apps.maps"),
    OSMAND("Osmand", "net.osmand"),

    // --- Food & Services ---
    MEITUAN("美团", "com.sankuai.meituan"),
    DIANPING("大众点评", "com.dianping.v1"),
    ELEME("饿了么", "me.ele"),
    KFC("肯德基", "com.yek.android.kfc.activitys"),
    MCDONALD("McDonald", "com.mcdonalds.app"),

    // --- Travel ---
    CTRIP("携程", "ctrip.android.view"),
    RAILWAY_12306("铁路12306", "com.MobileTicket"),
    QUNAR("去哪儿", "com.Qunar"),
    DIDI("滴滴出行", "com.sdu.did.psnger"),
    BOOKING_COM("Booking.com", "com.booking"),
    EXPEDIA("Expedia", "com.expedia.bookings"),

    // --- Video & Entertainment ---
    BILIBILI("bilibili", "tv.danmaku.bili"),
    DOUYIN("抖音", "com.ss.android.ugc.aweme"),
    KUAISHOU("快手", "com.smile.gifmaker"),
    TENCENT_VIDEO("腾讯视频", "com.tencent.qqlive"),
    IQIYI("爱奇艺", "com.qiyi.video"),
    YOUKU("优酷视频", "com.youku.phone"),
    MANGO_TV("芒果TV", "com.hunantv.imgo.activity"),
    HONGGUO_SHORTPLAY("红果短剧", "com.phoenix.read"),
    TIKTOK("Tiktok", "com.zhiliaoapp.musically"),
    VLC("VLC", "org.videolan.vlc"),

    // --- Music & Audio ---
    NETEASE_MUSIC("网易云音乐", "com.netease.cloudmusic"),
    QQ_MUSIC("QQ音乐", "com.tencent.qqmusic"),
    QISHUI_MUSIC("汽水音乐", "com.luna.music"),
    XIMALAYA("喜马拉雅", "com.ximalaya.ting.android"),
    PI_MUSIC_PLAYER("PiMusicPlayer", "com.Project100Pi.themusicplayer"),
    RETRO_MUSIC("RetroMusic", "code.name.monkey.retromusic"),

    // --- Reading ---
    FANQIE_NOVEL("番茄小说", "com.dragon.read"),
    QIMAO_NOVEL("七猫免费小说", "com.kmxs.reader"),
    GOOGLE_PLAY_BOOKS("Google Play Books", "com.google.android.apps.books"),

    // --- Productivity & Tools ---
    FEISHU("飞书", "com.ss.android.lark"),
    QQ_MAIL("QQ邮箱", "com.tencent.androidqqmail"),
    GMAIL("Gmail", "com.google.android.gm"),
    GOOGLE_CALENDAR("Google Calendar", "com.google.android.calendar"),
    GOOGLE_DRIVE("Google Drive", "com.google.android.apps.docs"),
    GOOGLE_DOCS("Google Docs", "com.google.android.apps.docs.editors.docs"),
    GOOGLE_SLIDES("Google Slides", "com.google.android.apps.docs.editors.slides"),
    GOOGLE_TASKS("Google Tasks", "com.google.android.apps.tasks"),
    GOOGLE_KEEP("Google Keep", "com.google.android.keep"),
    JOPLIN("Joplin", "net.cozic.joplin"),
    SIMPLE_CALENDAR_PRO("SimpleCalendarPro", "com.scientificcalculatorplus.simplecalculator.basiccalculator.mathcalc"),
    SIMPLE_SMS_MESSENGER("SimpleSMSMessenger", "com.simplemobiletools.smsmessenger"),
    FILES("Files", "com.android.fileexplorer"),
    GOOGLE_FILES("GoogleFiles", "com.google.android.apps.nbu.files"),

    // --- AI & Tools ---
    DOUBAO("豆包", "com.larus.nova"),

    // --- Health & Fitness ---
    KEEP("keep", "com.gotokeep.keep"),
    MEIYOU("美柚", "com.lingan.seeyou"),
    DUOLINGO("Duolingo", "com.duolingo"),
    GOOGLE_FIT("Google Fit", "com.google.android.apps.fitness"),
    BROCCOLI("Broccoli", "com.flauschcode.broccoli"),

    // --- News & Information ---
    TENCENT_NEWS("腾讯新闻", "com.tencent.news"),
    JINRI_TOUTIAO("今日头条", "com.ss.android.article.news"),

    // --- Real Estate ---
    BEIKE("贝壳找房", "com.lianjia.beike"),
    ANJUKE("安居客", "com.anjuke.android.app"),

    // --- Finance ---
    TONGHUASHUN("同花顺", "com.hexin.plat.android"),
    BLUECOINS("Bluecoins", "com.rammigsoftware.bluecoins"),

    // --- Games ---
    HONKAI_STAR_RAIL("崩坏：星穹铁道", "com.miHoYo.hkrpg"),
    LOVE_AND_DEEP_SPACE("恋与深空", "com.papegames.lysk.cn"),

    // --- System & Core Apps ---
    ANDROID_SETTINGS("Android System Settings", "com.android.settings"),
    AUDIO_RECORDER("AudioRecorder", "com.android.soundrecorder"),
    CHROME("Chrome", "com.android.chrome"),
    CLOCK("Clock", "com.android.deskclock"),
    GOOGLE_CLOCK("Google Clock", "com.google.android.deskclock"),
    CONTACTS("Contacts", "com.android.contacts"),
    GOOGLE_CONTACTS("Google Contacts", "com.google.android.contacts"),
    GOOGLE_PLAY_STORE("Google Play Store", "com.android.vending");

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
