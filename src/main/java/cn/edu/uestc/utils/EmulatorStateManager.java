package cn.edu.uestc.utils;

import cn.edu.uestc.wechat.bean.Activity;
import cn.edu.uestc.wechat.bean.View;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmulatorStateManager { // 这个类可以认为是无状态的
    private static Logger logger = LogManager.getLogger("模拟器状态管理器");
    // pattern对象的创建较低效，所以把它设置为静态常量
    private static final Pattern activityPattern = Pattern.compile("Run #\\d+: \\w+\\{\\w+ \\w+ ((\\w*?\\.)+\\w*\\/(\\.\\w+)*\\w+)");

    public static Activity getCurrentActivity() {
        ArrayList<Activity> activityList = getCurrentActivityList();
        if (activityList.size() == 0) {
            // todo 如果activity数量是0，说明模拟器不正常
            // 临时解决方案: 尝试一次连接模拟器
            ExecUtil.exec("adb connect 127.0.0.1:62001");
            activityList = getCurrentActivityList();
        }
        if (activityList.size() == 0) {
            return Activity.NullActivity; // 返回一个表示异常地activity，避免返回null
        }
        return activityList.get(0);
    }

    private static ArrayList<Activity> getCurrentActivityList() {
        ArrayList<Activity> activityList = new ArrayList<>();

        String cmd = "adb shell dumpsys activity activities";
        String result = ExecUtil.exec(cmd);
        Matcher matcher = activityPattern.matcher(result);
        while (matcher.find()) {
            activityList.add(Activity.getActivityByName(matcher.group(1)));
        }
        return activityList;
    }


    /**
     * 获取UI布局文件
     * 保证获取到document，否则将一直循环
     *
     * @return
     */
    public static Document getUIXmlDocument() {
        int count = 5;
        while (count-- > 0) { // 截图操作可能由于页面变化而失败，所以一直重试直到截图成功,一个坑：有的小程序页面获取不到xml，所以要设置重试次数上限，不然会无限循环
            // 先截图，保存在模拟器里
            ExecUtil.exec("adb shell uiautomator dump /sdcard/ui.xml");
            // 从模拟器导出来
            ExecUtil.exec("adb pull /sdcard/ui.xml ./ui.xml");
            // 创建对象，把图片读到内存
            SAXReader saxReader = new SAXReader();
            Document document;
            try {
                document = saxReader.read(new File("./ui.xml"));
            } catch (Exception e) {
                ExecUtil.exec("adb kill-server");
                ExecUtil.exec("adb connect 127.0.0.1:62001");
                continue;
            }
            if (document != null) {
                return document;
            }
        }
        // 尝试多次无果就重启微信
        EmulatorStateManager.restart();
        return getUIXmlDocument();
    }

    /**
     * 拿到当前模拟器的页面布局文件
     * 更新currentDocument
     *
     * @return
     */
    public static View getCurrentView() {
        Document currentDocument = getUIXmlDocument();
        for (View view : View.values()) {
            if (XMLUtil.checkView(currentDocument, view)) {
                logger.info("当前view 是 " + view);
                return view;
            }
        }
        return View.V_NULL; // V_NULL表示未识别view，返回一个表示空的状态避免直接返回null
    }

    /**
     * 重启微信
     * 一直等到达微信主页面才会退出
     */
    public static void restart() {
        long delay = 15000; // 初始等待时间15秒
        do {
            ExecUtil.exec("adb shell am start -W -S com.tencent.mm/.ui.LauncherUI");
            try {
                Thread.sleep(delay += delay); // 在每次重启中，失败会加倍等待时间
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (getCurrentView() != View.V0);
    }

    /**
     * 跳转到微信首页
     * 一直等到达微信主页面才会退出
     */
    private static void gotoDefaultView() {
        // 当前activity跟主页activity不一致，先跳到正确的activity
        while (!getCurrentActivity().equals(Activity.LauncherUI)) {
            ExecUtil.exec("adb shell am start -R 3 com.tencent.mm/.ui.LauncherUI");
        }
        // 操作次数上限 15，防止出现死循环
        int count = 15;
        View currentView;
        while ((currentView = getCurrentView()).compareTo(View.V0) > 0) { // 后退或者点击后退可以到达微信主页面
            if (currentView.backward == null) { // 如果标识后退的图标/按钮不存在，那么就按后退键
                ExecUtil.exec("adb shell input keyevent 4");
            } else {
                int[] position = XMLUtil.getBoundary(currentView.backward).getCenterPosition(); // 这里不会NPE,因为设置了空对象
                ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
            }
            count--;
            // 后退次数达到上限，直接重启微信
            if (count < 0) {
                restart();
                return;
            }
        }
    }

    /**
     * 这个方法要保证进入targetView
     *
     * @param targetView
     */
    public static void gotoView(View targetView) {
        View currentView = getCurrentView();
        if (currentView == null || currentView.equals(View.V_NULL) || currentView.index / 100 != targetView.index / 100) {
            // 需要经过首页
            gotoDefaultView();
        }

        while ((currentView = getCurrentView()) != targetView) {
            String xpath = "";
            if (currentView == View.V0) { // V0页面的前进和后退特别处理
                if (targetView.index >= 200) {
                    xpath = currentView.forward;
                } else {
                    xpath = currentView.backward;
                }
            } else {
                if (currentView.compareTo(targetView) > 0) { // 当前页面在目标页面后，需要后退
                    xpath = currentView.backward;
                } else {
                    xpath = currentView.forward; // 当前页面在目标页面前，需要前进
                }
            }
            if (xpath == null) { // 一般指这个view没有元素可以实现点击后退，那么就通过按后退键实现后退
                ExecUtil.exec("adb shell input keyevent 4");
            } else {
                int[] position = XMLUtil.getBoundary(xpath).getCenterPosition();
                ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(getCurrentView());
    }
}
