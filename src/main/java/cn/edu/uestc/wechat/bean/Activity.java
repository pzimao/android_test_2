package cn.edu.uestc.wechat.bean;

public enum Activity {
    // 如果都没有匹配，则返回这个activity
    NullActivity("未匹配的Activity"),
    // 系统activity
    RecentsActivity("com.android.systemui/.recents.RecentsActivity"),
    // 桌面
    Launcher("com.vphone.launcher/.Launcher"),

    // 微信启动页
    WeChatSplashActivity("com.tencent.mm/.app.WeChatSplashActivity"),
    // 小程序主页
    AppBrandLauncherUI("com.tencent.mm/.plugin.appbrand.ui.AppBrandLauncherUI"),
    AppBrandLaunchProxyUI("com.tencent.mm/.plugin.appbrand.launching.AppBrandLaunchProxyUI"),

    // 小程序打开后的某个状态
    AppBrandPluginUI("com.tencent.mm/.plugin.appbrand.ui.AppBrandPluginUI"),
    AppBrandSearchUI("com.tencent.mm/.plugin.appbrand.ui.AppBrandSearchUI"),
    AppBrandUI("com.tencent.mm/.plugin.appbrand.ui.AppBrandUI"),
    AppBrandUI1("com.tencent.mm/.plugin.appbrand.ui.AppBrandUI1"),
    AppBrandUI2("com.tencent.mm/.plugin.appbrand.ui.AppBrandUI2"),
    AppBrandUI3("com.tencent.mm/.plugin.appbrand.ui.AppBrandUI3"),
    AppBrandUI4("com.tencent.mm/.plugin.appbrand.ui.AppBrandUI4"),
    AppBrandUI5("com.tencent.mm/.plugin.appbrand.ui.AppBrandUI5"),
    AppBrandUI6("com.tencent.mm/.plugin.appbrand.ui.AppBrandUI6"),
    // 小程序（微信小游戏？）加载前的加载
    AppBrandXWebDownloadProxyUI("com.tencent.mm/.plugin.appbrand.ui.AppBrandXWebDownloadProxyUI"),
    // 微信搜索
    FTSMainUI("com.tencent.mm/.plugin.fts.ui.FTSMainUI"), //
    // 搜索小程序,有2种activity
    FTSSearchTabWebViewUI("com.tencent.mm/.plugin.webview.ui.tools.fts.FTSSearchTabWebViewUI"),

    // 内嵌浏览器
    WebviewMpUI("com.tencent.mm/.plugin.webview.ui.tools.WebviewMpUI"),
    // 微信主界面
    LauncherUI("com.tencent.mm/.ui.LauncherUI"), // 微信主activity
    // 已关注公众号的聊天页面
    ChattingUI("com.tencent.mm/.ui.chatting.ChattingUI"),


    WzUI("com.tencent.mm/.plugin.profile.ui.ContactInfoUI"),
    WyUI("com.tencent.mm/.plugin.brandservice.ui.timeline.preload.ui.TmplWebViewTooLMpUI");


    public String name;

    Activity(String name) {
        this.name = name;
    }

    public static Activity getActivityByName(String name) {
        for (Activity activity : Activity.values()) {
            if (activity.name.equals(name)) {
                return activity;
            }
        }
        return NullActivity;
    }
}
