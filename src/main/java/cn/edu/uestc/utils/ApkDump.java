package cn.edu.uestc.utils;

import cn.edu.uestc.apptest.thread.DownloadThread;
import cn.edu.uestc.apptest.type.OperationType;
import cn.edu.uestc.apptest.type.PermissionSourceType;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApkDump {


    // 用来匹配域名的正则表达式
    static final Pattern domainPattern = Pattern.compile("https?:\\/\\/((\\w+\\.)+\\w+)");
    // 用来匹配权限字符串的正则表达式，权限字符串以字母打头（排除数字打头的情况）
    static final Pattern classPermissionPattern = Pattern.compile("([a-zA-Z]([\\w_]+\\.)+permission(\\.[\\w_]+)+)");
    // 用来匹配XML dump得到的权限字符串
    static final Pattern xmlPermissionPatternXml = Pattern.compile("'(.*)'");

    public LinkedList<String> contentList;
    String dex2jarPath;
    File apkFile;

    Logger logger = LogManager.getLogger("APK解析");

    public ApkDump(File apkFile) {
        try {
            // 读取dex2jar工具路径
            Properties properties = new Properties();
            InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
            assert is != null;
            properties.load(is);
            dex2jarPath = properties.getProperty("dex2jarPath");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件出错");
            System.exit(-1);
        }
        this.apkFile = apkFile;
    }


    private File decompile(File apkFile) {
        logger.info("执行反编译...");
        File jarFile = new File(apkFile.getAbsolutePath().replace(".apk", ".jar"));

        if (jarFile.exists()) { // 如果目标jar已存在，则先删除
            jarFile.delete();
        }

        ExecUtil.exec(dex2jarPath + " \"" + apkFile.getAbsolutePath() + "\" -o \"" + jarFile.getAbsolutePath() + "\" --force");

        // 每次反编译完成，可能会有错误日志，忽略这个日志。
        File exceptionFile = new File(apkFile.getName().replace(".apk", "-error.zip"));
        if (exceptionFile.exists()) {
            logger.info("删除异常日志 " + exceptionFile.getAbsolutePath());
            exceptionFile.delete();
        }
        return jarFile;
    }

    public File unzip(File jarFile) {
        logger.info("解压jar...");
        File dstFile = new File(jarFile.getParent(), jarFile.getName().substring(0, jarFile.getName().lastIndexOf(".jar")));
        ExecUtil.exec("unzip -qq -o \"" + jarFile.getAbsolutePath() + "\" -d \"" + dstFile.getAbsolutePath() + "\"");
        logger.info("解压完成，删除jar");
        jarFile.delete();
        return dstFile;
    }

    /**
     * 这是一个递归调用
     */
    private LinkedList<String> parseStringList(File rootFile) {

        if (contentList == null) {
            logger.info("获取.class文件内容");
            contentList = new LinkedList<>();
        }

        File[] files = rootFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                parseStringList(file);
            } else if (file.getName().endsWith(".class")) {
                try {
                    contentList.add(FileUtils.readFileToString(file, "US-ASCII"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            // 逐个删除unzip得到的文件
            file.delete();
        }
        rootFile.delete();
        return contentList;
    }

    public HashSet<String> getDomainSet() {
        logger.info("匹配域名字符串...");
        HashSet<String> domainSet = new HashSet<>();
        for (String content : contentList) {
            Matcher matcher = domainPattern.matcher(content);
            while (matcher.find()) {
                domainSet.add(matcher.group(1));
            }
        }
        return domainSet;
    }

    public HashSet<String> getPermissionSetFromXML() {

        String cmd = "aapt d permissions \"" + apkFile.getAbsolutePath() + "\"";
        String result = ExecUtil.exec(cmd);
        Matcher matcher = xmlPermissionPatternXml.matcher(result);
        HashSet<String> permissionSet = new HashSet<>();
        while (matcher.find()) {
            permissionSet.add(matcher.group(1));
        }
        return permissionSet;
    }

    public HashSet<String> getPermissionSetFromClass() {
        HashSet<String> permissionSet = new HashSet<>();
        for (String content : contentList) {
            Matcher matcher = classPermissionPattern.matcher(content);
            while (matcher.find()) {
                permissionSet.add(matcher.group(1));
            }
        }
        return permissionSet;
    }


    public void dump() {
        contentList = null;
        if (!apkFile.exists()) {
            logger.info("文件不存在，请检查路径");
            return;
        }
        parseStringList(unzip(decompile(apkFile)));
    }

    public void dumpAndSave() {
        try {
            // 2019-7-16修改
//            String appId = apkFile.getAbsolutePath().substring(apkFile.getAbsolutePath().lastIndexOf('\\') + 1).split("_")[0].split("\\.")[0];
            String appId = "";
            if (apkFile.getName().split("_").length > 0) {
                appId = apkFile.getName().split("_")[0];
            } else {
                appId = apkFile.getName().split("\\.")[0];
            }
            // 2019-7-16 修改结束
            dump();

            // 获取到3组数据
            // 1. 域名；
            // 2. 声明的权限；
            // 3. 程序中出现的权限。
            HashSet<String> domainSet = getDomainSet();
            HashSet<String> xmlPermissionSet = getPermissionSetFromXML();
            HashSet<String> classPermissionSet = getPermissionSetFromClass();

            // 把这3组数据插入表
            DBManager.insertAppDomains(appId, domainSet, OperationType.STATIC);
            DBManager.insertAppPermissions(appId, xmlPermissionSet, PermissionSourceType.XML);
            DBManager.insertAppPermissions(appId, classPermissionSet, PermissionSourceType.CLASS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(apkFile.getAbsolutePath() + "文件异常，跳过反编译");
        }
    }

    public static void main(String[] args) {
        String pathname = "D:\\app_test_complete"; // .apk所在的文件夹
        File file = new File(pathname);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".apk")) {
//                new ApkDump(files[i]).dumpAndSave(); // 这行用ExecutorService exec...exec.shutdown();那些代码替换，可以避免超时卡住。
//                files[i].delete(); // 弄完以后删除.apk

            }
        }
    }
}