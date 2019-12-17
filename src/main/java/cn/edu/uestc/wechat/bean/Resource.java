package cn.edu.uestc.wechat.bean;

public class Resource {

    // 一共4个tag：微信、通讯录、发现、我
    public static final String CHAT_HOME_0 = "//node[contains(@text, '微信')]";
    public static final String CHAT_HOME_1 = "//node[contains(@text, '通讯录')]";
    public static final String CHAT_HOME_2 = "//node[contains(@text, '发现')]";
    public static final String CHAT_HOME_3 = "//node[contains(@text, '我')]";

    // 点击类
    public static final String CHAT_TEXT_FIELD_X = "//node[@resource-id='com.tencent.mm:id/amh']";
    // 2019-8-12 微信列表ID更新
    public static final String CHAT_SESSION_LIST_ITEM_X = "//node[@resource-id='com.tencent.mm:id/bag']";

    public static final String CHAT_SEND_MESSAGE_BUTTON = "//node[@resource-id='com.tencent.mm:id/aqj']"; // 聊天页面的发送按钮
    public static final String CHAT_LATEST_MESSAGE_BOX = "(//node[@resource-id='com.tencent.mm:id/pp'])[last()]"; // 最新的一条消息
    public static final String CHAT_BACK_BUTTON_X = "//node[@resource-id='com.tencent.mm:id/lr']"; // 聊天页面的后退按钮
    public static final String XCX_SEARCH_TEXT_FIELD_0 = "//node[@resource-id='com.tencent.mm:id/q8']";
    public static final String SEARCH_TEXT_FIELD_X = "//node[@resource-id='com.tencent.mm:id/l3']";
    // 2019-8-12 更新位置
//    public static final String XCX_RESULT_LIST_0 = "//node[@resource-id='com.tencent.mm:id/bho']"; // TODO 需要进一步确认
    public static final String TEXT_FORMAT = "//node[@text='{}']"; // 这是个字符串格式。使用时需要动态修改text的内容，把{}替换成要查找的字串。
    public static final String RESOURCE_FORMAT = "//node[@resource-id='{}']"; // 这是个字符串格式。使用时需要动态修改text的内容，把{}替换成要查找的字串。
    public static final String XCX_RESULT_LIST_1_NAME = "//node[@resource-id='com.tencent.mm:id/l3']"; // 点过来的小程序名字框，在text属性里

    // 搜索框清除按钮
    public static final String CLEAR_BUTTON_X = "//node[@resource-id='com.tencent.mm:id/m7']"; // 清除文本框内容

    public static final String SEARCH_IMAGE_X = "//node[@resource-id='com.tencent.mm:id/r9']"; // 首页的查询按钮

    public static final String SEARCH_TYPE_4_X = "//node[@text='小程序']"; // 查询页中【小程序】选项
    public static final String SEARCH_TYPE_3_X = "//node[@text='公众号']"; // 查询页中【公众号】选项
    public static final String SEARCH_BACK_BUTON_X = "//node[@resource-id='com.tencent.mm:id/m4']"; // 查询页的后退按钮

    public static final String XCX_SEARCH_RESULT_VIEW = "(//node[@class='android.webkit.WebView'])[last()]"; // 搜索结果
    public static final String XCX_SEARCH_RESULT_LOADING = "//node[@resource-id='com.tencent.mm:id/a70']"; // 搜索结果上的加载图标

    public static final String XCX_BACK_BUTTON = "//node[@resource-id='com.tencent.mm:id/oo']"; // 小程序后退按钮
    public static final String XCX_PLUGIN_PROGRESSBAR = "//node[@resource-id='com.tencent.mm:id/oz']"; // 小程序后退按钮
    // 小程序页面，显示小程序标题或者正在加载
    public static final String XCX_LOADING_WORD = "//node[@resource-id='com.tencent.mm:id/ox']"; // 加载中字样
    public static final String XCX_LOADING_STATE_1 = "//node[@resource-id='com.tencent.mm:id/w4']";
    public static final String XCX_LOADING_STATE_2 = "//node[@resource-id='com.tencent.mm:id/w5']";
    // 小程序加载过程中的标题
    public static final String XCX_LOADING_STATE_3 = "//node[@resource-id='com.tencent.mm:id/y7']"; // 小程序加载时的小程序名称
    public static final String XCX_LOADING_STATE_4 = "//node[@resource-id='com.tencent.mm:id/w7']";

    public static final String XCX_LOADING_PROCESSBAR = "//node[@resource-id='com.tencent.mm:id/oz']"; // 加载状态图标
    // 微信小游戏加载时的界面
    public static final String XCX_GAME_LOADING_TITLE = "//node[@resource-id='com.tencent.mm:id/xr']"; // 微信小程序（游戏类）加载时游戏的名称
    public static final String XCX_CLOSE_BUTTON_X = "//node[@resource-id='com.tencent.mm:id/kx']"; // 小程序页面关闭按钮的id

    // 小程序授权页面后退按钮
    public static final String XCX_PLUGIN_BACK = "//node[@resource-id='com.tencent.mm:id/om']";
    public static final String XCX_PLUGIN_RESOURCE = "//node[@resource-id='com.tencent.mm:id/u9']";

    // 加载进度条，小程序加载前的加载页面会有
    public static final String XCX_LOADING_RESOURCE = "//node[@resource-id='com.tencent.mm:id/a6z']";
    // 备用
    public static final String XCX_PAGE_FORWARD_ITEM = "com.tencent.mm:id/l_";

    // xpath
    public static final String XCX_PAGE_IMAGE_MORE_BUTTON = "(//node[@class='android.widget.ImageButton'])[1]";
    public static final String XCX_PAGE_IMAGE_CLOSE_BUTTON = "(//node[@class='android.widget.ImageButton'])[2]";

    // 小程序页面权限
    public static final String XCX_PAGE_PERMISSION_AGREE = "//node[@resource-id='com.tencent.mm:id/st']";

    public static final String GZH_RELATED_GZH = "//node[@text='相关公众号']";

    // 发现页
    public static final String WECHAT_CHATS = "//node[@text='微信']";
    public static final String WECHAT_CONTACTS = "(//node[@resource-id='com.tencent.mm:id/d9a'])[2]"; // 微信app底部的tag，从左往右是: 微信，通讯录，发现，我。这里的[2]表示第2个：联系人。 这个地方可以根据控件次序定位，也可以根据控件里的文字定位。

    public static final String WECHAT_FINDS_XCX = "//node[@text='小程序']";
    public static final String WECHAT_FINDS = "//node[@text='发现']";
    public static final String WECHAT_SEARCH_XCX = "//node[@text='搜索小程序']";
    public static final String WECHAT_SEARCH_ALL = "//node[@text='搜索指定内容']";
    public static final String WECHAT_REFRESH = "//node[@text='刷新']";

    // 2019-8-12 更新id
    public static final String XCX_SEARCH_BUTTON = "//node[@resource-id='com.tencent.mm:id/ln']";
    public static final String XCX_TEXT_FIELD = "//node[@resource-id='com.tencent.mm:id/aqc']"; // 聊天页面的文本框
    public static final String XCX_MORE = "//node[@resource-id='com.tencent.mm:id/aqi']"; // 聊天页面的“+”按钮
    public static final String VIEW_VIEW = "//node[@class='android.view.View']";

    // 浏览器页面相关
    public static final String WEB_CLOSE_BUTTON_X = "//node[@resource-id='com.tencent.mm:id/m0']";
    public static final String WEB_MORE_BUTTON_X = "//node[@resource-id='com.tencent.mm:id/ln']";
    public static final String WEB_PAGE = "//node[@resource-id='android:id/text1']"; // url 页面打开后的状态

    // 2019-8-12 增加文章id
    public static final String WZID_1 = "//node[@resource-id='com.tencent.mm:id/biz_slot_title_neat_tv']";
    public static final String WZID_2 = "//node[@resource-id='com.tencent.mm:id/b40']";
    public static final String WZID_3 = "//node[@resource-id='com.tencent.mm:id/b49']";
    // 文章发布时间
    public static final String WZ_SJ = "//node[@resource-id='com.tencent.mm:id/b48']";

    // 2019-8-12 添加公众号聊天页面内容区域
    public static final String GZH_LT_NR = "//node[@resource-id='com.tencent.mm:id/ae']";

    // 2019-8-12 微信ID更新 搜索分流页的公众号选项
    public static final String GZH = "//node[@resource-id='com.tencent.mm:id/c2v']";
    // 公众号展示页的全部消息选项
    public static final String QBXX = "//node[@resource-id='com.tencent.mm:id/b1i']";

    // 2019-12-16 公众号logo 控件id
    public static final String LOGO = "//node[@resource-id='com.tencent.mm:id/b7h']";
}
