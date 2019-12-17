package cn.edu.uestc.apptest.animal;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DmengxiangCrawler {
    private static Logger logger = LogManager.getLogger("DmengxiangCrawler");

    private static HttpClient httpClient = HttpClients.createMinimal();
    private static HttpGet httpPost = new HttpGet();

    private static ArrayList<String> applistList = new ArrayList<>(); // 榜单列表(首页给的列表)
    private static ArrayList<String> categoryList = new ArrayList<>(); // 类别列表(排行页给的列表)

    private static Pattern appIdPattern = Pattern.compile("\\/app\\/([0-9a-f]{8})\\.html");
    private static Pattern namePattern = Pattern.compile("class=\"app-detail-title\">(.*?)<\\/span>");
    private static Pattern authorPattern = Pattern.compile("<p><span>作者：(.*?)<\\/span>");
    private static Pattern descPattern = Pattern.compile("<span class=\"app-description\">[\\s]*(.*)[\\s]*<\\/span>");
    private static Pattern labelPattern = Pattern.compile("<span>标签：<\\/span>\\s+<strong>(.*)?<\\/strong>");

    public static String sql = "insert into wxxcx_dmengxiang values (?, ?, ?, ?, ?)";
    ;

    public void initial() {
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
        httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpPost.addHeader("Cookie", "PHPSESSID=396ecfb7cc0565090d47b728d5ce781e");
        httpPost.addHeader("Origin", "https://www.damengxiang.me");
        httpPost.addHeader("Referer", "https://www.damengxiang.me");
        httpPost.addHeader("Host", "www.damengxiang.me");
        httpPost.addHeader(":authority", "www.damengxiang.me");
        httpPost.addHeader(":method", "GET");
        httpPost.addHeader(":path", "/categories/all.html?s=0&l=2000");
        httpPost.addHeader(":scheme", "https");
    }

    /**
     * 解析单个页面上的小程序id
     */
    public boolean extractAppInfo(String content, HashSet<String> appSet) {
        // 从content中提取app的链接，添加到appSet
        boolean result = false;
        Matcher tempMatcher = appIdPattern.matcher(content);
        while (tempMatcher.find()) {
            if (appSet.add(tempMatcher.group(1))) {
                result = true;
            }
        }
        return result;
    }

    public String getContent(String baseUrl, String param) {
        if (!"".equals(param)) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("ckey", ""));
            params.add(new BasicNameValuePair("page", param));
            params.add(new BasicNameValuePair("q", ""));
//            httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        }
        System.out.println("请求的URL： " + baseUrl);
        httpPost.setURI(URI.create(baseUrl));

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

    public void crawl() throws Exception {
        HashSet<String> appSet = new HashSet<>();
        String content = FileUtils.readFileToString(new File("C:\\Users\\pzima\\Desktop\\dmengxiang.txt"), "utf-8");
        extractAppInfo(content, appSet);
        logger.info("已发现: " + appSet.size() + "个app。");
        // 把appSet里面的app处理掉
        for (String appId : appSet) {
            content = getContent("https://www.damengxiang.me/app/" + appId + ".html", "");
            // 解析出小程序名称、描述、标签等信息。
            String appName = "";
            String appDesc = "";
            String appLabel = "";
            String appAuthor = "";

            Matcher tempMatcher = namePattern.matcher(content);
            if (tempMatcher.find()) {
                appName = tempMatcher.group(1);
            }
            tempMatcher = descPattern.matcher(content);
            if (tempMatcher.find()) {
                appDesc = tempMatcher.group(1);
            }
            tempMatcher = labelPattern.matcher(content);
            if (tempMatcher.find()) {
                appLabel = tempMatcher.group(1);
            }
            tempMatcher = authorPattern.matcher(content);
            if (tempMatcher.find()) {
                appAuthor = tempMatcher.group(1);
            }
            DBManager.execute(DataSource.APP_TEST_DB, sql, appId, appName, appLabel, appDesc, appAuthor);
            Thread.sleep(300);
        }
    }

    public static void main(String[] args) throws Exception {
//        WxappCrawler crawler = new WxappCrawler();
//        crawler.crawl();
//        String sql = "insert into wxxcx values (?, ?, ?, ?, ?, 0, 0)";
//        for (String id : crawler.map.keySet()) {
//            String[] array = crawler.map.get(id);
//            DBManager.execute(DataSource.APP_TEST_DB, sql, array[0], array[2], array[1], array[3], array[4]);
//        }
        DmengxiangCrawler wxappCrawler = new DmengxiangCrawler();
        wxappCrawler.initial();
        wxappCrawler.crawl();


    }
}
