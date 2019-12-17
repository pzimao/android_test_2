package cn.edu.uestc.utils;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.apptest.type.OperationType;
import cn.edu.uestc.apptest.type.PermissionSourceType;
import cn.edu.uestc.apptest.animal.ChinazCrawler;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

public class DBManager {


    // 检查域名是否已存在
    static final String sql1 = "select * from domain where domain = ?";
    // 插入域名
    static final String sql2 = "INSERT INTO `domain` (`domain`, `domain_desc`) VALUES (?, ?)";
    // 插入APP 域名 对应关系
    static final String sql3 = "insert into app_domain(app_id, domain, source, label) values(? ,?, ?, ?)";
    // 检查app 与域名对应关系是否已存在
    static final String sql4 = "select * from app_domain where app_id = ? and domain = ?";


    // 检查权限是否已存在
    static final String sql5 = "select * from permission where permission = ?";
    // 插入权限
    static final String sql6 = "INSERT INTO `permission` (`permission`) VALUES (?)";
    // 插入APP 权限 对应关系
    static final String sql7 = "insert into app_permission(app_id, permission, `xml`, `class`) values(? ,?, ?, ?)";
    // 检查app 与权限对应关系是否已存在
    static final String sql8 = "select * from app_permission where app_id = ? and permission = ?";

    // 更新
    static final String sql10 = "update app_permission set `xml` = ? where app_id = ? and permission = ?";
    static final String sql11 = "update app_permission set `class` = ? where app_id = ? and permission = ?";

    // 根据app包名获取appId
    static final String sql12 = "select * from app where actual_pkg_name = ?";
    private volatile static Connection crawlerDBConnection;
    private volatile static Connection appTestDBConnection;
    private volatile static Connection urlDBConnection;
    private final static Logger logger = Logger.getLogger("数据库管理线程");

    public static Connection getConnection(DataSource dataSource) {
        // 利用反射拿到静态字段
        Connection connection;

        try {
            Field connectionFiled = DBManager.class.getDeclaredField(dataSource.getName() + "Connection");
            connection = (Connection) connectionFiled.get(null);
            // 双检锁
            // 2019-6-10 增加connection.isValid()以判断连接是否有效
            if (connection == null || !connection.isValid(30)) {
                synchronized (DBManager.class) {
                    if (connection == null || !connection.isValid(30)) {
                        // 此时需要创建数据库连接对象
                        StringBuilder sb = new StringBuilder();
                        Properties properties = new Properties();
                        InputStream is = DBManager.class.getClassLoader().getResourceAsStream("settings.properties");
                        properties.load(is);

                        String address = properties.getProperty(dataSource.getName() + "Address");
                        String port = properties.getProperty(dataSource.getName() + "Port");
                        String name = properties.getProperty(dataSource.getName() + "Name");
                        String username = properties.getProperty(dataSource.getName() + "Username");
                        String password = properties.getProperty(dataSource.getName() + "Password");
                        String url = sb.append("jdbc:mysql://")
                                .append(address)
                                .append(":")
                                .append(port)
                                .append("/")
                                .append(name)
                                .append("?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT")
                                .toString();
                        connection = DriverManager.getConnection(url, username, password);
                        connectionFiled.set(null, connection);
                        logger.info("数据库连接未创建或已失效，连接 : " + url);
                    }
                }
            }

        } catch (Exception e) {
            // todo 操作报异常，本次操作会失败
            e.printStackTrace();
            connection = null;
        }
        return connection;
    }

    public static Object execute(DataSource dataSource, String sql, String... args) {
        logger.info(sql + "\t" + Arrays.toString(args));
        Object result = null;
        try {
            PreparedStatement preparedStatement = DBManager.getConnection(dataSource).prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setString(i + 1, args[i]);
            }
            if (sql.toLowerCase().startsWith("select")) {
                // 查询语句
                result = preparedStatement.executeQuery();
            } else {
                result = preparedStatement.execute();
            }

        } catch (Exception e) {
            // todo 处理这里的异常
            e.printStackTrace();
        }
        return result;
    }

    public static void insertDomains(HashSet<String> domainSet) {
        // 更新app域名表
        for (String domain : domainSet) {
            logger.info("查询域名 " + "\t" + domain);
            // 检查domain表
            ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql1, domain);
            try {
                if (!resultSet.next()) {
                    // domain表不包含这个域名，则添加
                    // 先爬取域名备案
                    String domain_desc = ChinazCrawler.getNameByDomain(domain);
                    DBManager.execute(DataSource.APP_TEST_DB, sql2, domain, domain_desc);
                    logger.info("发现新域名 " + domain + " " + domain_desc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public static void insertAppDomains(String appId, HashSet<String> domainSet, OperationType op) {
        appId = appId.replace(".apk", "");
        insertDomains(domainSet);
        for (String domain : domainSet) {
            // 对每个app id 保存一遍
            // todo 2019-4-4 可能可以添加测试功能，即若一个域名频繁出现，则直接标注-1？

            // 先检查app与域名对应是否已存在
            try {
                if (!((ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql4, appId, domain)).next()) {
                    DBManager.execute(DataSource.APP_TEST_DB, sql3, appId, domain, op.index, "0");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void insertPermissions(HashSet<String> permissionSet) {
        for (String permission : permissionSet) {
            // 检查permission表
            ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql5, permission);
            try {
                if (!resultSet.next()) {
                    // permission表不包含这个，则添加
                    DBManager.execute(DataSource.APP_TEST_DB, sql6, permission);
                    logger.info("发现新权限 " + permission);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void insertAppPermissions(String appId, HashSet<String> permissionSet, PermissionSourceType source) {
        appId = appId.replace(".apk", "");
        insertPermissions(permissionSet);
        for (String permission : permissionSet) {
            // 先检查app与权限对应是否已存在
            try {
                if (!((ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql8, appId, permission)).next()) {
                    if (source == PermissionSourceType.XML) {
                        DBManager.execute(DataSource.APP_TEST_DB, sql7, String.valueOf(appId), permission, "1", "0");
                    } else {
                        DBManager.execute(DataSource.APP_TEST_DB, sql7, String.valueOf(appId), permission, "0", "1");
                    }
                } else {
                    // 更新
                    if (source == PermissionSourceType.XML) {
                        DBManager.execute(DataSource.APP_TEST_DB, sql10, "1", String.valueOf(appId), permission);
                    } else {
                        DBManager.execute(DataSource.APP_TEST_DB, sql11, "1", String.valueOf(appId), permission);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static HashSet<Integer> getAppId(String packageName) {
        HashSet<Integer> appIdSet = new HashSet<>();
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql12, packageName);
        try {
            while (resultSet.next()) {
                Integer appId = resultSet.getInt(1);
                appIdSet.add(appId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appIdSet;
    }

    public static void main(String[] args) {
        System.out.println(DBManager.getConnection(DataSource.APP_TEST_DB));
    }
}
