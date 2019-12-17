package cn.edu.uestc.wechat.impl;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.utils.EmulatorStateManager;
import cn.edu.uestc.utils.ExecUtil;
import cn.edu.uestc.utils.XMLUtil;
import cn.edu.uestc.wechat.bean.Activity;
import cn.edu.uestc.wechat.bean.Boundary;
import cn.edu.uestc.wechat.bean.Resource;
import cn.edu.uestc.wechat.bean.View;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class WechatTester {

    public static final Logger logger = LogManager.getLogger("微信测试");
    private String dstPath;
    private WechatMessageClicker messageClicker;

    /**
     * 拿到微信小程序安装包在模拟器上的路径
     *
     * @return
     */
    public ArrayList<String> getFolderName() {
        ArrayList<String> list = new ArrayList<>();
        String execStr = ExecUtil.exec("adb shell ls /data/data/com.tencent.mm/MicroMsg/");
        for (String line : execStr.split("\n")) {
            if (line.length() != 32) {
                continue;
            }
            boolean isAppbrandExists = !"".equals(ExecUtil.exec(String.format("adb shell \"ls /data/data/com.tencent.mm/MicroMsg/%s | grep appbrand\"", line)));
            if (isAppbrandExists) {
                list.add(line);
            }
        }
        return list;
    }

    /**
     * 更新wxapkg、wxxcx、wxapkg表
     * 更新小程序文件信息到数据库
     *
     * @param wxxcx_id
     * @param wxapkPath
     */
    public void saveToDB(int wxxcx_id, File wxapkPath) {
        File[] wxapkgs = wxapkPath.listFiles();
        String updateSql = "update wxxcx set file_export_state = 1 where id = ?";
        String queryWxapkg = "select id from wxapkg where wxapkg_name = ?";
        String insertToWxapkgSql = "insert into wxapkg (wxapkg_name, path)  values (?, ?)";
        String queryId = "select last_insert_id()";
        String insertToWxxcxWxapkgSql = "insert into wxxcx_wxapkg(wxxcx_id, wxapkg_id) values (?, ?)";
        try {
            // 更新点击状态
            DBManager.execute(DataSource.APP_TEST_DB, updateSql, String.valueOf(wxxcx_id));
            for (File wxapkg : wxapkgs) {
                // 先检查保存小程序安装包id是否已存在
                ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, queryWxapkg, wxapkg.getName());
                int wxapkg_id;
                if (resultSet.next()) {
                    wxapkg_id = resultSet.getInt(1);
                } else {
                    DBManager.execute(DataSource.APP_TEST_DB, insertToWxapkgSql, wxapkg.getName(), wxapkg.getPath());
                    resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, queryId);
                    resultSet.next();
                    wxapkg_id = resultSet.getInt(1);
                }
                // 关系也要保存吖
                DBManager.execute(DataSource.APP_TEST_DB, insertToWxxcxWxapkgSql, String.valueOf(wxxcx_id), String.valueOf(wxapkg_id));
            }
        } catch (NullPointerException e) {
            logger.info("没抓到wxapkg文件");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从数据表获取要测试的微信小程序的集合
     * sql:select id, name from wxxcx where file_export_state = 0
     *
     * @return
     */
    public HashMap<Integer, String> getAppMap() {
        // 查找要测试的微信小程序，把它们放到集合中
        HashMap<Integer, String> appMap = new HashMap<>();
        String querySql = "select id, name from wxxcx where file_export_state = 0";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, querySql);
        try {
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String name = resultSet.getString(2);
                appMap.put(id, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appMap;
    }

    /**
     * 模拟点击微信小程序
     *
     * @param appName
     */
    public void click(String appName) {
        /**
         * 点击之前清空模拟器小程序文件夹
         */
        logger.info("清空小程序文件夹");
        for (String folderName : getFolderName()) {
            ExecUtil.exec(String.format("adb shell rm /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/*", folderName));
        }
        /**
         * 从当前页跳到小程序搜索页
         */
        logger.info("进入小程序搜索页");
        EmulatorStateManager.gotoView(View.V120);

        logger.info("输入小程序名称 [" + appName + "]");

        for (char c : appName.toCharArray()) {
            ExecUtil.exec(String.format("adb shell am broadcast -a ADB_INPUT_TEXT --es msg '%c'", c));
        }

        logger.info("点击结果列表中的第1个小程序");

        String resource = Resource.TEXT_FORMAT.replace("{}", appName); // 把appName转为小写? 有的地方是小写、有的地方是大写...
        int[] position = XMLUtil.getBoundary(resource).getPositionToBottom(0.95);
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));

        double initScale = 0.88;
        L2:
        while (initScale > 0.4) {
            //logger.info(EmulatorStateManager.getCurrentView());
            // 看到小程序logo，todo 可能会同时出现小程序和公众号，如何确定点击位置?
            // 目前的解决办法是：从上往下点、直到小程序或者超出边界为止。
            Boundary boundary = XMLUtil.getBoundary(resource);
            if (boundary == null) {
                boundary = XMLUtil.getBoundary(resource);
            }
            if (boundary == null) {
                break L2;
            }
            position = boundary.getPositionToBottom(initScale -= 0.1);
            ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));


            if (!EmulatorStateManager.getCurrentActivity().name.contains("com.tencent.mm/.plugin.appbrand.ui.AppBrandUI")) { // 不是想要的页面
                int count = 0; // 等待次数
                L3:
                while (EmulatorStateManager.getCurrentActivity() == Activity.AppBrandXWebDownloadProxyUI) { // 可能会在这个页面卡死
                    if (count % 11 == 10) {
                        ExecUtil.exec("adb shell input keyevent 4");
                        logger.info("页面加载超时");
                        continue L2;
                    }
                    logger.info("小程序加载前的页面加载");
                    count++;
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!EmulatorStateManager.getCurrentActivity().name.contains("com.tencent.mm/.plugin.appbrand.ui.AppBrandUI")) { // 再次判断
                    continue;
                }
            }
            break L2;
        }

        logger.info("可能在加载真正的小程序");
        // 这个页面会卡在V125一段时间才到小程序加载页面
        for (int i = 0; i < 3; i++) {
            if (EmulatorStateManager.getCurrentView() == View.V125) {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        int count = 0; //等待计数，超时退出
        String title = "";
        do {
            title = XMLUtil.getText(Resource.XCX_LOADING_STATE_3, Resource.XCX_GAME_LOADING_TITLE);

            if (title == null) {
                /**
                 * title是null时表示加载完成了
                 */
                break;
            }
            logger.info("正在加载: " + title);
        } while (count++ <= 100); //检查是否正在加载页面

        // 需要检查是否含圆圈图片按钮(小程序关闭按钮)，如果含，则是小程序，否则不是
        if (XMLUtil.getBoundary(Resource.XCX_PAGE_IMAGE_CLOSE_BUTTON) == null) {
            logger.info("点进来的不是小程序页面");
            ExecUtil.exec("adb shell input keyevent 4");
        }

        logger.info("###小程序点击完成!####");
    }

    /**
     * 导出微信小程序安装文件
     *
     * @param appName
     */
    public void exportFile(String appName) {
        File tmpFile = new File(dstPath);
        File pkgFile = new File(tmpFile, "pkg");
        if (pkgFile.exists()) {
            FileUtils.deleteQuietly(pkgFile);
        }
        for (String folderName : getFolderName()) {

            tmpFile.mkdirs();
            Arrays.asList(tmpFile.listFiles()).forEach(file -> file.delete());

            // 2019-10-21 先导出小程序安装包文件然后再从模拟器删除
            ExecUtil.exec(String.format("adb pull \"/data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/\" %s", folderName, tmpFile));
            ExecUtil.exec(String.format("adb shell rm /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/.*", folderName));

            if (pkgFile.exists()) {
                if (!pkgFile.renameTo(new File(tmpFile.getAbsolutePath() + File.separator + appName))) {
                    FileUtils.deleteQuietly(pkgFile);
                }
            }
        }
    }

    public WechatTester(String fileExportPath, WechatMessageClicker wechatMessageClicker) {
        // wxapkg文件保存到Windows上的路径
        this.dstPath = fileExportPath;
        this.messageClicker = wechatMessageClicker;
    }


    public void test() {
        /**
         * 小程序在表中的id和名称的对应，id不是appid
         */
        HashMap<Integer, String> map = getAppMap();
        for (Integer id : map.keySet()) {
            String appName = map.get(id);
            try {
                click(appName); // 执行点击
                exportFile(appName);// 导出安装包文件

                if (messageClicker != null) { // 抓取小程序信息,桥接？？
                    logger.info("开始处理抓包得到的文件、appid、 biz等");
                    messageClicker.test(id);
                }
                saveToDB(id, new File(dstPath, appName)); // 结果存储
            } catch (Exception e) {
                e.printStackTrace();
                EmulatorStateManager.restart(); // 异常后重启微信
            }
        }
    }

    public static void main(String[] args) {
        while (true) {
            try {
                new WechatTester("d:/weixin", new WechatMessageClicker("d:/fiddler_gen")).test();
            } catch (Exception e) {
                logger.error(e.getMessage());
                EmulatorStateManager.restart(); // 异常后重启微信
            }
        }
    }
}


