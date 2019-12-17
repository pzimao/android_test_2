package cn.edu.uestc.apptest.animal;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WxappCrawler {
    private static Logger logger = LogManager.getLogger("WxappCrawler");

    private static HttpClient httpClient = HttpClients.createMinimal();
    private static HttpPost httpPost = new HttpPost();
    // 2个类
    public static String[] CATEGORY_URL = {"https://wxapp.top/category/", "https://wxapp.top/?c=index_rank"};
    public static String[] APPLIST_URL = {"https://wxapp.top/applist/", "https://wxapp.top/"};
    // 小程序的详情页面
    public static String[] APP_URL = {"https://wxapp.top/app/", "https://wxapp.top/"};

    private static ArrayList<String> applistList = new ArrayList<>(); // 榜单列表(首页给的列表)
    private static ArrayList<String> categoryList = new ArrayList<>(); // 类别列表(排行页给的列表)

    private static Pattern appIdPattern = Pattern.compile("https:\\/\\/wxapp\\.top\\/app\\/(\\d+)");
    private static Pattern namePattern = Pattern.compile("<h2>(.*)<\\/h2>");
    private static Pattern descPattern = Pattern.compile("desc_div'>(.*?)<\\/div>");
    private static Pattern labelPattern = Pattern.compile("<div>标签：(.*?>(.*?)<\\/span>)+<\\/div>");

    public static String sql = "insert into wxxcx values (?, ?, '', ?, ?, 0, 0, '')";
    ;

    public void initial() {
        applistList.add("672");
        applistList.add("670");
        applistList.add("180");
        applistList.add("669");
        applistList.add("671");
        applistList.add("673");

        categoryList.add("1");
        categoryList.add("2");
        categoryList.add("3");
        categoryList.add("4");
        categoryList.add("5");
        categoryList.add("6");
        categoryList.add("7");
        categoryList.add("8");
        categoryList.add("9");
        categoryList.add("10");
        categoryList.add("11");
        categoryList.add("12");
        categoryList.add("13");
        categoryList.add("15");
        categoryList.add("17");
        categoryList.add("18");
        categoryList.add("19");
        categoryList.add("21");
        categoryList.add("23");
        categoryList.add("24");
        categoryList.add("55");
        categoryList.add("56");
        categoryList.add("58");

        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3724.8 Safari/537.36");
        httpPost.addHeader("Accept", "application/json, text/plain, */*");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.addHeader("Cookie", "PHPSESSID=lktlijl6l8lf4hfvvnm01887a7");
        httpPost.addHeader("Origin", "");
        httpPost.addHeader("Referer", "");
    }


    public void crawlAppId() {
        // 解析出所有小程序的url
        for (String applist : applistList) {
            parsePage(APPLIST_URL, applist);
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (String category : categoryList) {
            parsePage(CATEGORY_URL, category);
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void parsePage(String[] baseUrl, String paramStr) {
        String content = getContent(baseUrl, paramStr);

        Matcher matcher = appIdPattern.matcher(content);
        HashSet<String> appIdSet = new HashSet<>();
        while (matcher.find()) {
            appIdSet.add(matcher.group(1));
        }
        crawlAppInfo(appIdSet);
        // todo 处理翻页
        // 检查一共有多少页，然后循环
        Pattern nextPage = Pattern.compile("<a href=\"http:\\/\\/wxapp.top\\/.*?ps=(\\d+)\">【下一页】<\\/a>");
        matcher = nextPage.matcher(content);
        if (matcher.find()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            parsePage(baseUrl, paramStr + "?&ps=" + nextPage);
        }
//            System.out.println(content);
    }

    /**
     * 解析每个小程序
     */
    public void crawlAppInfo(HashSet<String> appIdSet) {
        for (String appId : appIdSet) {
            String content = getContent(APP_URL, appId);

            Matcher matcher = namePattern.matcher(content);
            String name = "";
            if (matcher.find()) {
                name = matcher.group(1);
            }
            matcher = descPattern.matcher(content);
            String desc = "";
            if (matcher.find()) {
                desc = matcher.group(1);
            }
            matcher = labelPattern.matcher(content);
            String label = "";
            while (matcher.find()) {
                label = label + matcher.group(2) + ",";
            }
            DBManager.execute(DataSource.APP_TEST_DB, sql, appId, name, label, desc);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getContent(String[] baseUrl, String param) {
        System.out.println("请求的URL： " + baseUrl[0] + param);
        httpPost.setURI(URI.create(baseUrl[0] + param));
        httpPost.setHeader("Referer", baseUrl[1]);
        httpPost.setHeader("Origin", baseUrl[1]);
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

    public LinkedList<List<NameValuePair>> getParams() {
        LinkedList<List<NameValuePair>> paramList = new LinkedList<>();


        for (int i = 0; i < 2; i++) { // 选榜
            for (int j = 0; j < 30; j++) { // 选类别
                for (int k = 0; k < 2; k++) { // 选时间


                    List<NameValuePair> list = new ArrayList<NameValuePair>();
                    list.add(new BasicNameValuePair("type", String.valueOf(i)));
                    list.add(new BasicNameValuePair("typeid", String.valueOf(j)));
                    list.add(new BasicNameValuePair("date", String.valueOf(k)));
                    list.add(new BasicNameValuePair("size", String.valueOf(30)));
                    list.add(new BasicNameValuePair("token", ""));
                    paramList.add(list);
                }
            }
        }

        return paramList;
    }

    public static void main(String[] args) throws Exception {
//        WxappCrawler crawler = new WxappCrawler();
//        crawler.crawl();
//        String sql = "insert into wxxcx values (?, ?, ?, ?, ?, 0, 0)";
//        for (String id : crawler.map.keySet()) {
//            String[] array = crawler.map.get(id);
//            DBManager.execute(DataSource.APP_TEST_DB, sql, array[0], array[2], array[1], array[3], array[4]);
//        }
        WxappCrawler wxappCrawler = new WxappCrawler();
        wxappCrawler.initial();
        wxappCrawler.crawlAppId();
    }
}
