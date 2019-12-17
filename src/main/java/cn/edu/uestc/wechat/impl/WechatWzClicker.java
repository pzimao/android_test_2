package cn.edu.uestc.wechat.impl;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.*;
import cn.edu.uestc.wechat.bean.*;
import cn.edu.uestc.wechat.dao.WzSave;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.ResultSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WechatWzClicker {

    public static final Logger logger = LogManager.getLogger("微信文章信息抓取");
    public static final Random random = new Random();

    public void test(String gzhmc) {
        // 进入微信首页
        EmulatorStateManager.gotoView(View.V0);

        // 点击放大镜图标
        int[] position = XMLUtil.getBoundary(Resource.SEARCH_IMAGE_X).getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));

        // 点击公众号选项
        position = XMLUtil.getBoundary(Resource.GZH).getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 输入要搜索的公众号的名字
        Boundary boundary = Boundary.NULL_BOUNDARY;
        for (char c : gzhmc.toCharArray()) {
            ExecUtil.exec(String.format("adb shell am broadcast -a ADB_INPUT_TEXT --es msg '%c'", c));
            // 2019-8-12更新点击位置
            // 点击第一项
            // position = XMLUtil.getBoundary(Resource.XCX_RESULT_LIST_0).getPositionToBottom(0.88);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boundary = XMLUtil.getBoundary(Resource.TEXT_FORMAT.replace("{}", gzhmc));
            if (boundary != Boundary.NULL_BOUNDARY) {
                break;
            }
        }


        if (boundary == Boundary.NULL_BOUNDARY) {
            logger.info("没有找到相关公众号");
            return;
        }
        position = boundary.getCenterPosition();
        if(position[0] == 0) {
            return;
        }
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));

        // 2019-8-12 补充，可能先进入汇聚页面，需要多一步点击
        boundary = XMLUtil.getBoundary(Resource.TEXT_FORMAT.replace("{}", gzhmc));
        position = boundary.getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));


        // 进入公众号信息展示首页
        View currentView = EmulatorStateManager.getCurrentView();
        if (currentView == View.V190) {
            // 1. 如果是未关注公众号，点击第一个进入公众号
            position = XMLUtil.getBoundary(Resource.XCX_SEARCH_BUTTON).getCenterPosition();
            ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));

        }

        // 2019-8-12更新，没有【全部消息】选项了...
        int[] sjPosition = XMLUtil.getTopBoundary(Resource.WZ_SJ).getCenterPosition();

        // 开始点击文章
        BufferedImage image0 = ImageUtil.takeSnapshot();
        BufferedImage image1 = image0;
        String[] wzXpaths = {Resource.WZID_1, Resource.WZID_2, Resource.WZID_3};
        do {
            if(position[0] == 0) {
                return;
            }
            do {
                if(position[0] == 0) {
                    return;
                }
                position = XMLUtil.getTopBoundary(sjPosition[1], wzXpaths).getCenterPosition();
                ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1] + new Random().nextInt(10)));
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (ImageUtil.computeImageSimilarity(ImageUtil.takeSnapshot(), image1) > 0.95);


            // 滑一段距离以发请求
            do {
                ExecUtil.exec(String.format("adb shell input swipe 100 %d 100 %d 1000", 800, 300));
                ExecUtil.exec(String.format("adb shell input swipe 100 %d 100 %d 1000", 800, 300));
            } while (!new File("d:/fiddler_weixin_wz/read_num.txt").exists());
            // 通过控件确定坐标，而不是写死坐标。2019-8-8 临时添加，刷新文章页面。因为经过观察发现，刷新时发出的请求才是预期的。
            int[] position1 = XMLUtil.getBoundary(Resource.XCX_SEARCH_BUTTON).getCenterPosition();
            ExecUtil.exec(String.format("adb shell input tap %d %d", position1[0], position1[1]));
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 这里确定刷新按钮的位置
            position1 = XMLUtil.getBoundary(Resource.WECHAT_REFRESH).getCenterPosition();
            ExecUtil.exec(String.format("adb shell input tap %d %d", position1[0], position1[1]));
            try {
                while (!new File("d:/fiddler_weixin_wz/wz.txt").exists()) {
                    Thread.sleep(3000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("离开文章页面，准备操作下一篇文章");
            ExecUtil.exec("adb shell input keyevent 4");
            // 等待模拟器响应
            try {
                Thread.sleep(2 * 1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 此时能抓到2个包，1：文章页面；2：阅读计数等信息
            // 上滑一段距离以更新点击内容
            ExecUtil.exec(String.format("adb shell input swipe 100 %d 100 %d 1000", 688, 400));

            image0 = image1;
            image1 = ImageUtil.takeSnapshot();
            System.out.println(ImageUtil.computeImageSimilarity(image1, image0));
        }
        while (ImageUtil.computeImageSimilarity(image1, image0) < 0.95 && (System.currentTimeMillis() / 1000 - process()) < 60 * 60 * 24 * 14);

        logger.info("所有文章都点完了");

    }


    public static long process() {

        File readNumFile = new File("d:/fiddler_weixin_wz/read_num.txt");
        File wzFile = new File("d:/fiddler_weixin_wz/wz.txt");
        long time = System.currentTimeMillis() / 1000;
        try {
            String readInfoFileContent = FileUtils.readFileToString(readNumFile, "utf-8");
            String wzFileContent = FileUtils.readFileToString(wzFile, "GB2312");
            JSONObject jsonObject = new JSONObject(readInfoFileContent).getJSONObject("appmsgstat");
            int readNum = jsonObject.getInt("read_num");
            int realReadNum = jsonObject.getInt("real_read_num");
            int likeNum = jsonObject.getInt("like_num");

            Matcher matcher = wzUrlPattern.matcher(wzFileContent);
            matcher.find();
            String url = matcher.group(1);
            matcher = publishDatatimePattern.matcher(wzFileContent);
            matcher.find();
            String publishTimestamp = matcher.group(1);
            matcher = titlePattern.matcher(wzFileContent);
            matcher.find();
            String title = matcher.group(1);
            matcher = bizPattern.matcher(wzFileContent);
            matcher.find();
            String biz = matcher.group(1);
            matcher = snPattern.matcher(wzFileContent);
            matcher.find();
            String sn = matcher.group(1);
            matcher = midPattern.matcher(wzFileContent);
            matcher.find();
            String mid = matcher.group(1);
            matcher = idxPattern.matcher(wzFileContent);
            matcher.find();
            String idx = matcher.group(1);
            matcher = gzhmcPattern.matcher(wzFileContent);
            matcher.find();
            String gzhmc = matcher.group(1);

            ReadInfo readInfo = new ReadInfo(readNum, likeNum, realReadNum);
            Wz wz = new Wz(biz, title, publishTimestamp, url, idx, sn, mid);
            time = new WzSave().save(gzhmc, wz, readInfo);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (readNumFile.exists()) {
                readNumFile.delete();
            }
            if (wzFile.exists()) {
                wzFile.delete();
            }
        }
        return time;
    }

    // 文章URL
    public static Pattern wzUrlPattern = Pattern.compile("var msg_link = \"(\\S+)\";");
    // 发表时间，是一个时间戳
    public static Pattern publishDatatimePattern = Pattern.compile("var ct = \"(\\d{10})\";");
    // 文章标题
    public static Pattern titlePattern = Pattern.compile("var msg_title = \"(.+?)\";");
    // 公众号biz
    public static Pattern bizPattern = Pattern.compile("__biz=(\\w{14}==)");
    // 公众号名称
    public static Pattern gzhmcPattern = Pattern.compile("var title =\"(.*?)\";");
    // sn
    public static Pattern snPattern = Pattern.compile("sn=([a-z0-9]+)");
    // mid
    public static Pattern midPattern = Pattern.compile("var appmsgid = '' \\|\\| '(\\d+)'\\|\\| \"\"");
    // idx ：指当天的第几篇文章
    public static Pattern idxPattern = Pattern.compile("var msg_daily_idx = \"(\\d+)\"");


    public static void main(String[] args) throws Exception {

//        if (1 == 1) {
//
//
//            // 2019-8-12更新，没有【全部消息】选项了...
//            int[] sjPosition = XMLUtil.getTopBoundary(Resource.WZ_SJ).getCenterPosition();
//
//
//            String[] wzXpaths = {Resource.WZID_1, Resource.WZID_2, Resource.WZID_3};
//            int[] position = XMLUtil.getTopBoundary(sjPosition[1], wzXpaths).getCenterPosition();
//            System.out.println(position[0] + " " + position[1]);
//            return;
//        }
        String sql = "select mc from wxgzh1";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);
        while (resultSet.next()) {
            String mc = resultSet.getString(1);
            if (mc.length() > 0) {
                new WechatWzClicker().test(mc);
            }
        }

//        WechatWzClicker.process();
//        Matcher matcher = WechatWzClicker.publishDatatimePattern.matcher(FileUtils.readFileToString(new File("23"), "GB2312"));
//        System.out.println(matcher.find());
    }
}
