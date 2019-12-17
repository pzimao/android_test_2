package cn.edu.uestc.apptest.animal;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WxappCrawler2 {
    private static Logger logger = LogManager.getLogger("DubaCrawler");

    private static HttpClient httpClient = HttpClients.createMinimal();
    private static HttpPost httpPost = new HttpPost();


    private static Pattern itemPattern = Pattern.compile("<a href=\"detail.html\\?id=.*?<\\/a>");
    private static Pattern appIdPattern = Pattern.compile("detail\\.html\\?id=(\\d+)");
    private static Pattern namePattern = Pattern.compile("data\\-name=\"(.*?)\"");
    private static Pattern descPattern = Pattern.compile("\"brief\">(.*?)<\\/p>");
    private static Pattern labelPattern = Pattern.compile("分 类：<\\/em>(.*?)<\\/p>");
    private static Pattern hotPattern = Pattern.compile("热 度：<\\/em><span class=\"star-bg\"><span class=\"star\" style=\"width: (.*?)%\">");

    public static String sql = "insert into wxxcx_duba values (?, ?, ?, ?, ?)";
    ;

    public void initial() {
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
        httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpPost.addHeader("Host", "www.duba.com");
        httpPost.addHeader("Cookie", "t_manual=0; second_nav_icons_close=1; userIDF=1572400343009x2mxf; __kp=srup9izubhp4dy9wyprfhvtd5czh; __kt=1572400344; orpv=1; WEATHER_COOKIE_CITY_KEY=101270101%7C%E6%88%90%E9%83%BD; changevertips=1; act=10/30:8; _dbsg=ij7t5mdfksda8fksdafka19b04cef36a; infoc_client_uuid=27bbe5b7e319e843597a54d334518480");
        httpPost.addHeader("Origin", "");
        httpPost.addHeader("Referer", "");
    }


    public void parsePage() {
        // 读取原始内容
        String content = "";
        try {
            content = FileUtils.readFileToString(new File("C:\\Users\\pzima\\Desktop\\duba.txt"), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Matcher matcher = itemPattern.matcher(content);
        while (matcher.find()) {
            // 获取到每个APP，解析APP的详细信息。
            String appId = "";
            String appName = "";
            String appDesc = "";
            String category = "";
            String hot = "";
            // 以下内容从子页面获得
            String publishTime = "";
            String followers = "";
            String author = "";
            String appInfo = matcher.group();
            Matcher tempMatcher = appIdPattern.matcher(appInfo);
            if (tempMatcher.find()) {
                appId = tempMatcher.group(1);
            }
            tempMatcher = namePattern.matcher(appInfo);
            if (tempMatcher.find()) {
                appName = tempMatcher.group(1);
            }
            tempMatcher = descPattern.matcher(appInfo);
            if (tempMatcher.find()) {
                appDesc = tempMatcher.group(1).replace(" ", "");
            }
            tempMatcher = labelPattern.matcher(appInfo);
            if (tempMatcher.find()) {
                category = tempMatcher.group(1);
            }
            tempMatcher = hotPattern.matcher(appInfo);
            if (tempMatcher.find()) {
                hot = tempMatcher.group(1);
            }
            System.out.println(appId);
            System.out.println(appName);
            System.out.println(appDesc);
            System.out.println(category);
            System.out.println(hot);
            DBManager.execute(DataSource.APP_TEST_DB, sql, appId, appName, category, appDesc, hot);
        }
    }


    public String getContent(String url) {
        System.out.println("请求的URL： " + url);
        httpPost.setURI(URI.create(url));
        httpPost.setHeader("Referer", "http://www.duba.com/wxapp/search.html?type=tag&&keyword=道客阅读");
        httpPost.setHeader("Origin", "www.duba.com");
        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            return unicodeToString(result);

        } catch (UnknownHostException e) {
            logger.warn("请检查主机网络状况");
        } catch (Exception e) {
//            e.printStackTrace();
            httpPost.reset();
        }
        return "{}";
    }

    HashMap<String, String[]> map = new HashMap<>(4096);

    public void saveToDB(LinkedList<String[]> list) {
        for (String[] array : list) {
            map.putIfAbsent(array[0], array);
            for (String item : array) {
                System.out.print(item);
            }
            System.out.println();
        }
        System.out.println();
        return;
    }


    public static String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");

        Matcher matcher = pattern.matcher(str);

        char ch;

        while (matcher.find()) {

            ch = (char) Integer.parseInt(matcher.group(2), 16);

            str = str.replace(matcher.group(1), ch + "");

        }

        return str;

    }


    public static void main(String[] args) throws Exception {
        WxappCrawler2 wxappCrawler = new WxappCrawler2();
//        System.out.println(wxappCrawler.getContent("http://www.duba.com/wxapp/detail.html?id=101316"));
        wxappCrawler.parsePage();
//        wxappCrawler.initial();
    }
}
