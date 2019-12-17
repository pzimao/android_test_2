package cn.edu.uestc.utils;

public class APKUtil {
    public static String getApkPackageName(String apkFilePath) {

        StringBuilder stringBuilder = new StringBuilder("aapt dump badging \"").append(apkFilePath).append("\" | findstr package");
        String apkPackageName = ExecUtil.exec(stringBuilder.toString());
        System.out.println(apkPackageName);
        if ("".equals(apkPackageName) || apkPackageName.contains("Invalid file")) {
            return "";
        }
        apkPackageName = apkPackageName.substring(apkPackageName.indexOf('\'') + 1);
        apkPackageName = apkPackageName.substring(0, apkPackageName.indexOf('\''));
        return apkPackageName;
    }

    public static void main(String[] args) {
        getApkPackageName("D:\\app_test\\190664_可以提的棋牌游戏_能提现赚钱的手机游戏下载_社会新闻_多特软件资讯.apk");
    }
}
