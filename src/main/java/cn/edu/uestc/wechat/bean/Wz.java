package cn.edu.uestc.wechat.bean;

public class Wz {

    private String biz;
    private String title;
    private String publishTimestamp;
    private String url;
    private String idx;
    private String sn;
    private String mid;

    public Wz(String biz, String title, String publishTimestamp, String url, String idx, String sn, String mid) {
        this.biz = biz;
        this.title = title;
        this.publishTimestamp = publishTimestamp;
        this.url = url;
        this.idx = idx;
        this.sn = sn;
        this.mid = mid;
    }

    public String getBiz() {
        return biz;
    }

    public String getTitle() {
        return title;
    }

    public String getPublishTimestamp() {
        return publishTimestamp;
    }

    public String getUrl() {
        return url;
    }

    public String getIdx() {
        return idx;
    }

    public String getSn() {
        return sn;
    }

    public String getMid() {
        return mid;
    }

}
