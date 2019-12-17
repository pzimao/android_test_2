package cn.edu.uestc.wechat.dao;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.wechat.bean.ReadInfo;
import cn.edu.uestc.wechat.bean.Wz;

import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class WzSave {

    // 查公众号表
    public static String sql1 = "select * from wxgzh1 where biz = ?";
    // 插公众号表
    public static String sql2 = "insert into wxgzh1 (biz, mc) values (?, ?)";
    // 查文章表
    public static String sql3 = "select id from wxgzh_wz where biz = ? and mid = ? and sn = ? and idx = ?";
    // 插文章表
    public static String sql4 = "insert into wxgzh_wz (biz, title, publish_datetime, url, idx, sn, mid) values (?, ?, ?, ?, ?, ?, ?)";
    // 获取最新插入的一行的id
    public static String sql5 = "SELECT LAST_INSERT_ID()";
    // 插阅读信息表
    public static String sql6 = "insert into wxgzhwz_read_info (wz_id, read_num, like_num, real_read_num, record_datatime) values (?, ?, ?, ?, now())";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public WzSave() {

    }

    public long save(String mc, Wz wz, ReadInfo readInfo) {
        try {
            // 第一步查看公众号表是否已存在相应的biz的记录
            ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql1, wz.getBiz());
            if (!resultSet.next()) {
                // 需要先把这个公众号的biz和名称插入到公众号表
                DBManager.execute(DataSource.APP_TEST_DB, sql2, wz.getBiz(), mc);
            }
            // 第二步查看文章表是否已有此文章的记录
            resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql3, wz.getBiz(), wz.getMid(), wz.getSn(), wz.getIdx());
            String wzId = "";
            if (resultSet.next()) {
                wzId = String.valueOf(resultSet.getInt(1));
            } else {
                DBManager.execute(DataSource.APP_TEST_DB, sql4, wz.getBiz(), wz.getTitle(), sdf.format(new Date(Long.valueOf(wz.getPublishTimestamp()) * 1000L)), wz.getUrl(), wz.getIdx(), wz.getSn(), wz.getMid());
                resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql5);
                if (resultSet.next()) {
                    wzId = String.valueOf(resultSet.getInt(1));
                }
            }
            // 第三步往阅读统计表插
            DBManager.execute(DataSource.APP_TEST_DB, sql6, wzId, String.valueOf(readInfo.getReadNum()), String.valueOf(readInfo.getLikeNum()), String.valueOf(readInfo.getRealReadNum()));
            return Long.valueOf(wz.getPublishTimestamp());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis() / 1000;
    }
}
