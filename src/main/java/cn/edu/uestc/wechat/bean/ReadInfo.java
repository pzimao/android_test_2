package cn.edu.uestc.wechat.bean;

public class ReadInfo {
    private int readNum;
    private int likeNum;
    private int realReadNum;

    public ReadInfo(int readNum, int likeNum, int realReadNum) {
        this.readNum = readNum;
        this.likeNum = likeNum;
        this.realReadNum = realReadNum;
    }

    public int getReadNum() {
        return readNum;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public int getRealReadNum() {
        return realReadNum;
    }
}
