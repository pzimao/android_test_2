package cn.edu.uestc.apptest.thread;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.ApkDump;
import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.utils.DeviceManager;
import cn.edu.uestc.utils.ExecUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class AppInstallThread extends Thread {

    private final Logger logger;

    private String appFolder;
    private String appBackupFolder;
    private long intervalTimeForInstall;
    private static final String sql = "update app set test_state = ? where id = ?";

    public AppInstallThread() {
        logger = LogManager.getLogger("APP安装线程");
        try {
            Properties properties = new Properties();
            InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
            assert is != null;
            properties.load(is);
            appFolder = properties.getProperty("appFolder");
            appBackupFolder = properties.getProperty("appBackupFolder");
            intervalTimeForInstall = Integer.valueOf(properties.getProperty("intervalTimeForInstall"));


            logger.info("APK文件下载位置: " + appFolder);
            logger.info("APP安装间隔时间: " + intervalTimeForInstall / 1000 + "秒");


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件出错");
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        File apkFolder = new File(appFolder);
        boolean workFlag = true;
        while (workFlag) {
            try {
                // 只处理以这目录下.apk结尾的文件
                Arrays.asList(apkFolder.listFiles((file) -> file.getName().endsWith(".apk"))).forEach(apkFile -> {
                    // 检查设备状态
                    while (!DeviceManager.isStarted) {
                        logger.info("暂停， 等待设备重启");
                        try {
                            Thread.sleep(60000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
//                    logger.info("dump apk : " + apkFile.getAbsolutePath());
                    // todo 2019-10-17
//                    new ApkDump(apkFile).dumpAndSave();

                    // apk文件名不允许有中文等字符，需要先改名才能安装
                    // 名字改成id.apk再去安装，比如 100000.apk
                    // 从原始名字中解析出APP ID
                    String appId = "";
                    int idIndex = apkFile.getName().indexOf('_');
                    if (idIndex == -1) {
                        // 这个文件名字 1234.apk
                        logger.info("异常的文件名" + apkFile.getAbsolutePath());
                        appId = apkFile.getName().split("\\.")[0];

                    } else {
                        // 1234_安卓应用.apk
                        appId = apkFile.getName().substring(0, idIndex);
                    }
                    File installFile = new File(apkFile.getParentFile(), appId + ".apk");
                    apkFile.renameTo(installFile);
                    String result = ExecUtil.exec("adb -s 127.0.0.1:7555 install \"" + installFile.getAbsolutePath() + "\"");
                    if (result.contains("Failure")) {
                        logger.info("安装失败");
                        // 更新app表的相关字段
                        DBManager.execute(DataSource.APP_TEST_DB, sql, "2", appId);
                    } else {
                        logger.info(apkFile.getName() + ":安装完成");
                    }

                    if (!installFile.renameTo(new File(appBackupFolder + apkFile.getName()))) {
                        // 如果改名返回false，就直接删掉
                        if (apkFile.delete()) {
                            logger.info("删除了文件 " + apkFile.getAbsolutePath());
                        }
                    }
                    try {
                        Thread.sleep(intervalTimeForInstall);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
                try {
                    // 一轮后休息2分钟
                    Thread.sleep(2 * 60 * 1000);
                } catch (Exception e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    // 一轮后休息2分钟
                    logger.info("等待设备就绪...");
                    Thread.sleep(60 * 1000);
                } catch (Exception e1) {
                }
            }
        }
    }
}
