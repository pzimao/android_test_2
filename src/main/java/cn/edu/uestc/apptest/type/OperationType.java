package cn.edu.uestc.apptest.type;

public enum OperationType {
    AUTOMATIC("0"),
    MANUAL("1"),
    STATIC("2");

    public String index;

    OperationType(String index) {
        this.index = index;
    }
}
