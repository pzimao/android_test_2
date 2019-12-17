package cn.edu.uestc.apptest.type;

public enum PermissionSourceType {
    XML("0"),
    CLASS("1");

    public String index;

    PermissionSourceType(String index) {
        this.index = index;
    }
}
