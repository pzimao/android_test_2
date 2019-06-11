package cn.edu.uestc.wechat.bean;

import java.util.Comparator;

public enum View implements Comparator<View> {
    /**
     * 首页，规定后退是操作小程序，前进是操作URL
     * 判定依据：1. 左上角“微信”；2. 会话列表
     * 后退：“发现”
     * 前进："会话"
     */
    V0(0, Activity.LauncherUI, new String[]{Resource.CHAT_HOME, Resource.CHAT_SESSION_LIST_ITEM_X}, Resource.WECHAT_FINDS, Resource.CHAT_SESSION_LIST_ITEM_X),

    /**
     * 发现页
     * 判定依据：“发现”；“小程序”
     * 后退：“微信”
     * 前进：“小程序”
     */
    V110(110, Activity.LauncherUI, new String[]{Resource.WECHAT_FINDS, Resource.WECHAT_FINDS_XCX}, Resource.WECHAT_CHATS, Resource.WECHAT_FINDS_XCX),
    /**
     * 小程序主页
     * 判定依据:"小程序"
     * 后退：左上角
     * 前进：“🔍”
     */
    V115(115, Activity.AppBrandLauncherUI, new String[]{Resource.WECHAT_FINDS_XCX}, Resource.WEB_CLOSE_BUTTON_X, Resource.XCX_SEARCH_BUTTON),

    /**
     * 小程序搜索页，输入前
     * 判定依据：”搜索小程序“
     * 后退：左上角后退按钮
     * 前进: ”搜索小程序“
     */
    V120(120, Activity.AppBrandSearchUI, new String[]{Resource.WECHAT_SEARCH_XCX}, Resource.SEARCH_BACK_BUTON_X, Resource.WECHAT_SEARCH_XCX),

    /**
     * 小程序搜索页，输入后
     * 判定依据：”×“
     * 后退：左上角后退按钮
     * 前进：”×“
     */
    V125(125, Activity.AppBrandSearchUI, new String[]{Resource.CLEAR_BUTTON_X}, Resource.SEARCH_BACK_BUTON_X, Resource.CLEAR_BUTTON_X), // 搜索结果

    /**
     * 小程序搜索后
     * 判定依据：”×“；android.view.view（不知道什么时候会出现）
     * 后退：左上角后退按钮
     * 前进：”×“
     */
    V130(130, Activity.AppBrandSearchUI, new String[]{Resource.CLEAR_BUTTON_X, Resource.VIEW_VIEW}, Resource.SEARCH_BACK_BUTON_X, Resource.CLEAR_BUTTON_X), // 搜索结果


    // 小程序、小程序授权页面，使用后退或者跳转activity离开小程序页，不需要处理权限申请对话框
    V160(160, Activity.AppBrandUI, new String[]{Resource.XCX_PAGE_PERMISSION_AGREE}, Resource.XCX_PAGE_PERMISSION_AGREE, null), // 小程序
    V165(165, Activity.AppBrandPluginUI, new String[]{Resource.XCX_PLUGIN_PROGRESSBAR}, Resource.XCX_BACK_BUTTON, null), // 小程序
    V175(175, Activity.AppBrandPluginUI, new String[]{Resource.XCX_LOADING_STATE_3}, Resource.XCX_PAGE_IMAGE_CLOSE_BUTTON, null), // 小程序
    /**
     * 小程序页面
     * 后退：右上角圆圈
     * 前进：null(目前不需要前进)
     */
    V180(180, Activity.AppBrandUI, new String[]{Resource.XCX_PLUGIN_BACK, Resource.XCX_PAGE_IMAGE_CLOSE_BUTTON}, Resource.XCX_PAGE_IMAGE_CLOSE_BUTTON, null), // 小程序
    /**
     * 聊天框
     * 判定依据: 文本输入框；"+"图标
     * 后退:左上角后退图标
     * 前进:文本输入框
     */
    V201(201, Activity.LauncherUI, new String[]{Resource.XCX_TEXT_FIELD, Resource.XCX_MORE}, Resource.CHAT_BACK_BUTTON_X, Resource.XCX_TEXT_FIELD), // 对话框打开后的页面
    /**
     * 聊天框
     * 判定依据: 文本输入框；"发送"
     * 后退:左上角后退图标
     * 前进:"发送"按钮
     */
    V202(202, Activity.LauncherUI, new String[]{Resource.XCX_TEXT_FIELD, Resource.CHAT_SEND_MESSAGE_BUTTON}, Resource.CHAT_BACK_BUTTON_X, Resource.CHAT_SEND_MESSAGE_BUTTON), // 对话框打开后的页面


    /**
     * 浏览器
     */
    V203(203, Activity.WebviewMpUI, new String[]{Resource.WEB_CLOSE_BUTTON_X}, Resource.WEB_CLOSE_BUTTON_X, null), // url点开后的网页


    /**
     * 以下是目前未使用的view
     */
    V_NULL(404, null, null, null, null),
    /**
     * 总类别搜索页
     */
    V101(101, Activity.FTSMainUI, new String[]{Resource.SEARCH_TYPE_4_X, Resource.WECHAT_SEARCH_ALL}, Resource.SEARCH_BACK_BUTON_X, Resource.SEARCH_TYPE_4_X), // 搜索0
    // 小程序加载前的加载视图，可能会很耗时，也可能卡住
    V119(119, Activity.AppBrandXWebDownloadProxyUI, new String[]{Resource.XCX_LOADING_RESOURCE, Resource.XCX_SEARCH_RESULT_LOADING}, null, null); // 小程序
    // 小程序搜索页，搜索前
//    V103(103, Activity.FTSSearchTabWebViewUI, new String[]{Resource.SEARCH_TEXT_FIELD_X}, Resource.SEARCH_BACK_BUTON_X, null),

    public int index;
    public Activity activity; // view所属的activity
    public String[] resources; // 用来标识view的元素数组

    public String backward; // view页面上可以实现后退的元素

    public String forward; // view页面上可以进入的元素

    View(int index, Activity activity, String[] resources, String backward, String forward) {
        this.index = index;
        this.activity = activity;
        this.resources = resources;

        this.backward = backward;
        this.forward = forward;
    }


    @Override
    public int compare(View o1, View o2) {
        return Integer.compare(o1.index, o2.index);
    }
}
