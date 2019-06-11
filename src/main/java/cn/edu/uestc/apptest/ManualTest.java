package cn.edu.uestc.apptest;

import cn.edu.uestc.utils.ExecUtil;
import cn.edu.uestc.utils.TcpdumpUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManualTest {

    public static void test() {
        final Logger logger = LogManager.getLogger("manual test: ");
        HashSet<String> whiteSet = new HashSet<>();
        // 排除已经安装的app
        Matcher matcher0 = Pattern.compile("(\\w+\\.)+\\w+\\r?").matcher(ExecUtil.exec("adb shell pm list package -3"));
        while (matcher0.find()) {
            String packageName = matcher0.group();
            System.out.println("排除: " + packageName);
            whiteSet.add(packageName);
        }
        while (true) {
            try {
                Matcher matcher = Pattern.compile("(\\w+\\.)+\\w+\\n?").matcher(ExecUtil.exec("adb shell pm list package -3"));
                while (matcher.find()) {

                    String packageName = matcher.group().trim();
                    if (!whiteSet.contains(packageName)) {
                        // 是新安装的APP
                        logger.info("开始抓【" + packageName + "】的数据包");
                        new TcpdumpUtil(packageName).start();

                        logger.info("开始测试【 " + packageName + "】，请手动操作APP");
                        logger.info("在这里输入任意字符可以结束测试...");

                        // 等待控制台输入
                        new Scanner(System.in).next();

                        logger.info("【" + packageName + "】" + "结束测试");
                        ExecUtil.exec("adb remove " + packageName);
                        logger.info("【" + packageName + "】" + "已经被卸载了");
                        String pids = ExecUtil.exec("adb shell pidof tcpdump").trim();
                        Matcher pidMatcher = Pattern.compile("\\d+").matcher(pids);
                        while (pidMatcher.find()) {
                            String pid = pidMatcher.group();
                            ExecUtil.exec("adb shell kill " + pid);
                            logger.info("kill抓包进程");
                        }
                    }
                }
                logger.info("请手动安装APP");
                Thread.sleep(10 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ManualTest.test();
    }
}
