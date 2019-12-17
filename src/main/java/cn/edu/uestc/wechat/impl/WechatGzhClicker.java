package cn.edu.uestc.wechat.impl;

import cn.edu.uestc.utils.*;
import cn.edu.uestc.wechat.bean.Resource;
import cn.edu.uestc.wechat.bean.View;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WechatGzhClicker {
    public static final Logger logger = LogManager.getLogger("银行公众号抓取");

    public static void main(String[] args) {
        crawlGzhwz("阿坝政务服务");
    }

    /**
     * 输入银行名称，获取相关的公众号
     *
     * @param yhmc
     */
    public static void crawlGzhmc(String yhmc) {
        // 点击[添加朋友]
        // 点击[微信号/手机号]
        // 点击[微信号/手机号]
        // 输入银行名字
        // 点击[搜索:%s]
        // 点击[搜一搜  %s]
        // 点击[公众号]
        // 滑到底
        // 提取出所有的公众号名字

        int[] position = null;
        String[] xpaths = {
                "resource-id='com.tencent.mm:id/ra'",
                "text='添加朋友'",
                "text='微信号/手机号'",
                "text='{}'",
                "text='搜索:{}'",
                "text='搜一搜  {}'",
                "text='公众号'"
        };
        // 获取银行列表
        List<String> yhList = new ArrayList<>();
        EmulatorStateManager.gotoView(View.V0);

        for (int i = 0; i < xpaths.length; i++) {
            String xpath = "//node[@{}]".replace("{}", xpaths[i].replace("{}", yhmc));
            System.out.println(xpath);
            do {// todo 可能卡死在这里。
                try {
                    position = XMLUtil.getBoundary(xpath).getCenterPosition();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (position[0] == 0);

            ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));

            if (i + 1 < xpaths.length && "text='{}'".equals(xpaths[i + 1])) {
                for (char c : yhmc.toCharArray()) {
                    ExecUtil.exec(String.format("adb shell am broadcast -a ADB_INPUT_TEXT --es msg '%c'", c));
                }
            }
            position[0] = 0;
        }

        BufferedImage image0 = null;
        BufferedImage image1 = ImageUtil.takeSnapshot();
        do {
            image0 = image1;
            ExecUtil.exec(String.format("adb shell input swipe 3 %d 3 %d 2000", 800, 300));
            image1 = ImageUtil.takeSnapshot();
        } while (ImageUtil.computeImageSimilarity(image0, image1) < 0.9);

        // todo 存储跟这家银行相关的公众号
        System.out.println(Arrays.toString(XMLUtil.getGzhmc("//node[@resource-id='search_result']/node", "node[1]/node[3]/node", "node[1]/node[2]/node")));
    }
    /**
     * 输入公众号名称，进入公众号首页
     *
     * @param gzhmc
     */
    public static void crawlGzhwz(String gzhmc) {
        /**
         * 从当前页跳到公众号搜索页
         */
        // 点首页🔍
        // 点公众号
        logger.info("进入公众号搜索页");
        EmulatorStateManager.gotoView(View.V0);
        int[] position = XMLUtil.getBoundary(Resource.SEARCH_IMAGE_X).getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
        position = XMLUtil.getBoundary(Resource.SEARCH_TYPE_3_X).getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
        logger.info("输入公众号名称 [" + gzhmc + "]");
        String resource = Resource.TEXT_FORMAT.replace("{}", gzhmc);
        for (char c : gzhmc.toCharArray()) {
            ExecUtil.exec(String.format("adb shell am broadcast -a ADB_INPUT_TEXT --es msg '%c'", c));

            position = XMLUtil.getBoundary(resource).getPositionToBottom(0.95);
            if (position[0] != 0) {
                ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
                break;
            }
        }
        position = XMLUtil.getTopBoundary(resource).getPositionToBottom(0.5);
        System.out.println(position[0] + " " + position[1]);
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
        logger.info("点击结果列表中的");

        // 每日关注的公众号数量是有限的
        // 关注这个公众号
        resource = Resource.TEXT_FORMAT.replace("{}", "关注公众号");
        position = XMLUtil.getBoundary(resource).getCenterPosition();

        if (position[0] != 0) {
            // 还未关注
            // 关注并返回到历史文章页面
            ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
            ExecUtil.exec("adb shell input keyevent 4");
        } else {
            // 已关注
        }

        // 存储公众号logo
        // 步骤：
        // 1. 清空模拟器中的微信下载文件夹；
        // 2. 保存公众号logo到模拟器；
        // 3. logo导出到windows
        ExecUtil.exec("adb shell rm /storage/emulated/legacy/tencent/MicroMsg/WeiXin/.*");
        position = XMLUtil.getBoundary(Resource.LOGO).getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
        // 长按500ms以上
        ExecUtil.exec(String.format("adb shell input swipe %d %d %d %d 8000", position[0], position[1], position[0], position[1]));
        position = XMLUtil.getBoundary(Resource.TEXT_FORMAT.replace("{}", "保存到手机")).getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
        ExecUtil.exec(String.format("adb pull \"/storage/emulated/legacy/tencent/MicroMsg/WeiXin/.*", "d:/" + gzhmc + ".jpg"));
    }


}
