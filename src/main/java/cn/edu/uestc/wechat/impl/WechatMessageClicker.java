package cn.edu.uestc.wechat.impl;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.*;
import cn.edu.uestc.wechat.bean.Resource;
import cn.edu.uestc.wechat.bean.View;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WechatMessageClicker {
    public static final Random random = new Random();
    private Logger logger = LogManager.getLogger("URL点击线程");
    Pattern bizPattern = Pattern.compile("(\\w{14}==)");
    Pattern appidPattern = Pattern.compile("(wx[a-f0-9]{16})");
    File idFileFolder;
    private long nextClickTime;
    private int clickCount;

    public WechatMessageClicker(String idFilePath) {
        this.idFileFolder = new File(idFilePath);
        nextClickTime = System.currentTimeMillis();
    }

    public ArrayList<String> constructMessage(String id) {
        ArrayList<String> rawMessageList = new ArrayList<>();
        if (id.contains("==")) {
            // biz
            rawMessageList.add("https://mp.weixin.qq.com/mp/getverifyinfo?__biz=" + id + "&from=singlemessage#wechat_webview_type=1&wechat_redirect");
            rawMessageList.add("https://mp.weixin.qq.com/mp/getverifyinfo?__biz=" + id + "&type=reg_info#wechat_redirect");
        } else if (id.contains("wx")) {
            rawMessageList.add("https://mp.weixin.qq.com/mp/waverifyinfo?action=get&appid=" + id + "#wechat_webview_type=1&wechat_redirect");
        } else {
            logger.info("Warning: Unrecognized pattern of ID:  " + id);
        }
        return rawMessageList;
    }

    public void updateNextTime() {
        this.nextClickTime = System.currentTimeMillis() + 10000;
        this.clickCount++;
    }

    public void waitForClick() {
//        if (clickCount % 11 == 10) {
//            // 等待一段时间
//
//            long waitTime = random.nextInt(1 * 60 * 1000) + 1 * 60 * 1000;
//            logger.info("等待 " + waitTime / 1000 / 60 + " 分钟");
//            try {
//                Thread.sleep(waitTime);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        while (System.currentTimeMillis() < this.nextClickTime) {
            try {
                logger.info("速度控制，等待...");
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.updateNextTime(); // 这像是个桶漏，保证URL点击速度不高于6个/分钟->每分钟最多点6个
    }

    public void sendMessage(String rawMessage) {
        this.waitForClick();
        logger.info("打开聊天框");
        // 打开聊天框
        EmulatorStateManager.gotoView(View.V201);
        // 获取文本框焦点
        int[] position = XMLUtil.getBoundary(View.V201.forward).getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));

        logger.info("发送消息");
        // 输入消息并更新view
        String sendMessage = "adb shell input text \"" + rawMessage.replace("&", "\\&").replace(" ", "") + "\"";
        ExecUtil.exec(sendMessage);
        position = XMLUtil.getBoundary(Resource.CHAT_SEND_MESSAGE_BUTTON).getCenterPosition();

        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
    }

    /**
     * 点击最近的一条消息
     */
    public boolean clickMessage() {
        logger.info("点击最近的消息");
        EmulatorStateManager.gotoView(View.V201);
        // 获取最近的一条消息的位置
        int[] position = XMLUtil.getBoundary(Resource.CHAT_LATEST_MESSAGE_BOX).getPositionToRight(0.5);
        // 点击位置随机偏移，不然可能点不到消息
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1] + random.nextInt(60) - 30));


        int count = 10;
        while (EmulatorStateManager.getCurrentView() != View.V203 && count > 0) { //
            try {
                // 2019-5-22 上午添加
                ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1] + random.nextInt(60) - 30));
                Thread.sleep(random.nextInt(4000) + 2000); // 随机等待2-6秒
                count--;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String title = XMLUtil.getText(Resource.WEB_PAGE); // 获取浏览器标题
        if ("验证".equals(title)) {
            // todo 处理请求失败的情况
            logger.info("返回的是验证页面!");
            return false;
        } else {
            logger.info("###完成#[" + title + "]");
            return true;
        }
    }

    /**
     * 读文件构造url，发送到聊天界面，并点击
     *
     * @param
     */
    public void test(Integer id) {

        File[] files = idFileFolder.listFiles(file -> file.getName().endsWith("1.txt"));
        for (File file : files) {
            String content = "";
            try {
                content = FileUtils.readFileToString(file, Charset.defaultCharset());
            } catch (Exception e) {
                e.printStackTrace();
                content = "";
            }
            HashSet<String> idSet = new HashSet<>();
            Matcher matcher = appidPattern.matcher(content);
            while (matcher.find()) {
                idSet.add(matcher.group(1));
            }
            matcher = bizPattern.matcher(content);
            while (matcher.find()) {
                idSet.add(matcher.group(1));
            }
            for (String appid : idSet) {
                ArrayList<String> rawMessageList = constructMessage(appid);
                for (String message : rawMessageList) {
                    sendMessage(message);
                    if (clickMessage()) {
                        save(id);
                        break;
                    }
                }
            }
            file.delete();
        }
    }

    public void save(Integer id) {
        // 文件处理完之后，更新数据库
        File tempFolder = idFileFolder;
        List<File> fileList = Arrays.asList(tempFolder.listFiles((file) -> file.getName().length() > 15 && file.getName().endsWith(".html") && !file.getName().startsWith("_")));
        // 更新wxxcx1、 wxgzh1表
        String updateSql = "update wxxcx set app_id = ?, appid_extract_state = 1 where id = ?";

        for (File file : fileList) {
            DBManager.execute(DataSource.APP_TEST_DB, SQLUtil.getSQL(Parser.parseInfo(file)));
            if (id != null) {
                DBManager.execute(DataSource.APP_TEST_DB, updateSql, file.getName().substring(0, file.getName().indexOf('.')), String.valueOf(id));
            }
            if (!file.renameTo(new File(file.getParent() + "/_" + file.getName()))) {
                file.delete();
            }
        }
    }

    /**
     * 读biz构造url，发送到聊天界面，并点击
     *
     * @param
     */
    public void test() {
        // 从表中获取记录，然后开始测试
        String sql = "select * from wxgzh1 where zt = '' and qyqc = ''  and jgmc = '' and rzsj = ''";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);
        try {
            while (resultSet.next()) {
                String biz = resultSet.getString(1);
                ArrayList<String> rawMessageList = constructMessage(biz);
                if (rawMessageList.size() == 0) {
                    String sqltmp = "update wxgzh1 set zt = 'unrecognized', qyqc = 'unrecognized', rzsj = 'unrecognized' where biz='" + biz + "'";
                    DBManager.execute(DataSource.APP_TEST_DB, sqltmp);
                    logger.info(sqltmp);
                    continue;
                }
                for (String message : rawMessageList) {
                    sendMessage(message);
                    if (clickMessage()) {
                        save(null);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getIdFileFolder() {
        return this.idFileFolder;
    }

    public static void main(String[] args) {
        while (true) {
            try {
                new WechatMessageClicker("D:/fiddler_gen").test();
            } catch (Exception e) {
                e.printStackTrace();
                // 重启一下微信
                EmulatorStateManager.restart();
            }
        }
    }
}
