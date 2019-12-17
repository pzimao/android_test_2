package cn.edu.uestc.apptest;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class MyJFrame extends JFrame {

    // 记录跳过的APP数量
    private int skipCount;

    // 定义一些必要的组件
    private JTextField appNameLabel;
    private JTextField appPkgNameLabel;
    private JPanel panel;
    private MyTable candidateTable;
    private MyTable filteredTable;
    private JLabel footLabel;

    private JButton submitButton;
    private JButton skipButton;
    private JTextField filterDegreeTextField;

    private JLabel label1;
    private JLabel label2;
    private JLabel appIdLabel1;
    private JLabel appIdLabel2;

    // 信息更新板块
    private JTextField jInputField;
    private JComboBox jComboBox;

    public MyJFrame() {
        InitialComponent();
    }

    private void InitialComponent() {

        /**
         * 创建控件对象
         */
        panel = new JPanel();
        appIdLabel1 = new JLabel();
        appIdLabel2 = new JLabel();
        label1 = new JLabel();
        label2 = new JLabel();
        skipButton = new JButton();
        filterDegreeTextField = new JTextField();
        appNameLabel = new JTextField();
        appPkgNameLabel = new JTextField();
        submitButton = new JButton();
        jComboBox = new JComboBox();
        jInputField = new JTextField();
        candidateTable = new MyTable(500, 60, 450, 700);
        filteredTable = new MyTable(500, 60, 450, 700);


        footLabel = new JLabel();
        jInputField = new JTextField();

        JScrollPane candidateScrollpane = new JScrollPane(candidateTable);
        JScrollPane filteredScrollpane = new JScrollPane(filteredTable);

        panel.add(skipButton);
        panel.add(appNameLabel);
        panel.add(appPkgNameLabel);
        panel.add(filterDegreeTextField);
        panel.add(submitButton);
        panel.add(candidateScrollpane);
        panel.add(filteredScrollpane);
        panel.add(footLabel);
        panel.add(label1);
        panel.add(label2);
        panel.add(appIdLabel1);
        panel.add(appIdLabel2);
        panel.add(jInputField);
        panel.add(jComboBox);
        this.add(panel);


        /**
         * 以下为控件绑定监听事件
         */

        // 提交按钮点击事件
        submitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String appId = appIdLabel2.getText();
                candidateTable.processData(appId);
                filteredTable.processData(appId);
                updateTable();
            }
        });

        // 跳过按钮的点击事件
        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skipCount++;
                updateTable();
            }
        });

//        candidateTable.addPropertyChangeListener();
        // 下拉列表的选择事件
        jComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                //如果选中了一个
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    //这里写你的任务 ，比如取到现在的值
                    String text = (String) jComboBox.getSelectedItem();
                    System.out.println(text);
                    jInputField.setText(text);
                }
            }
        });


        /**
         * 以下设置显示样式
         */
        Font defaultFont1 = new Font("楷体", Font.PLAIN, 18);
        Font defaultFont2 = new Font("", Font.PLAIN, 18);
        this.setTitle("域名标注");


        setLayout(null);
        setSize(1440, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 总面板
        panel.setSize(this.getWidth(), this.getHeight());
        panel.setLocation(0, 0);
        panel.setLayout(null);

        // appId标签
        appIdLabel1.setText("APP ID:");
        appIdLabel1.setLocation(10, 10);
        appIdLabel1.setSize(75, 28);
        appIdLabel1.setFont(defaultFont1);

        appIdLabel2.setLocation(85, 10);
        appIdLabel2.setSize(90, 28);
        appIdLabel2.setFont(defaultFont1);
        appIdLabel2.setHorizontalAlignment(SwingConstants.LEFT);

        label1.setText("筛出至少");
        label1.setLocation(10, 50);
        label1.setSize(180, 28);
        label1.setFont(MyFont.ZH_FONT);


        label2.setText("个APP引用的域名");
        label2.setFont(MyFont.ZH_FONT);
        label2.setLocation(150, 50);
        label2.setSize(180, 28);

        skipButton.setText("跳过");
        skipButton.setFont(MyFont.ZH_FONT);
        skipButton.setLocation(460, 10);
        skipButton.setSize(98, 58);


        filterDegreeTextField.setSize(45, 25);
        filterDegreeTextField.setLocation(105, 50);
        filterDegreeTextField.setFont(MyFont.EN_FONT);
        filterDegreeTextField.setHorizontalAlignment(SwingConstants.RIGHT);

        appNameLabel.setSize(420, 28);
        appNameLabel.setLocation(570, 10);
        appNameLabel.setFont(MyFont.EN_FONT);


        appPkgNameLabel.setSize(420, 28);
        appPkgNameLabel.setLocation(570, 40);
        appPkgNameLabel.setFont(MyFont.EN_FONT);


        submitButton.setText("下一组");
        submitButton.setSize(120, 58);
        submitButton.setLocation(1000, 10);
        submitButton.setFont(MyFont.ZH_FONT);


        footLabel.setSize(200, 14);
        footLabel.setLocation(20, 762);
        footLabel.setFont(MyFont.ZH_FONT);


        jInputField.setText("修改后的内容");
        jInputField.setSize(250, 28);
        jInputField.setLocation(1130, 75);
        jInputField.setFont(MyFont.EN_FONT);

        jComboBox.setSize(250, 30);
        jComboBox.setLocation(1130, 155);


        candidateScrollpane.setLocation(570, 75);
        candidateScrollpane.setSize(550, 500);


        filteredScrollpane.setLocation(10, 75);
        filteredScrollpane.setSize(550, 500);
    }

    // 这个方法除了更新表格以外，还会更新下拉列表的内容
    public ArrayList<String[]> updateTable() {

        int filterDegree = 10;
        try {
            filterDegree = Integer.valueOf(filterDegreeTextField.getText());
        } catch (Exception e) {
            filterDegreeTextField.setText("10");
        }
        String preSql = "select domain, freq from (select domain, count(*) as freq from app_domain  GROUP BY domain) as t where freq > " + String.valueOf(filterDegree) + " ORDER BY freq desc ";
        String sql = "select * from `视图1_所有域名` where id in ( select * from (select DISTINCT app_domain.app_id from app_domain where label = 0 and app_domain.app_id in (select DISTINCT id from `视图1_所有域名`) limit " + skipCount + ", 1) as t)";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);

        // 需要在表格中显示的数据
        ArrayList<String[]> candidateList = new ArrayList<>();
        ArrayList<String[]> filteredList = new ArrayList<>();
        // 2019-7-19显示成三级域名需要去重
        HashSet<String> shortDomainSet = new HashSet<>();
        try {
            ResultSet preResultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, preSql);
            HashSet<String> preSet = new HashSet<>();
            while (preResultSet.next()) {
                preSet.add(preResultSet.getString(1));
            }
            while (resultSet.next()) {
                String[] strings = new String[5];
                for (int j = 0; j < 5; j++) {
                    strings[j] = resultSet.getString(j + 1);
                }
                // 2019-7-19把全域名处理成三级域名
                strings[3] = DomainUtil.getShortDomain1(strings[3]);
                if (!shortDomainSet.add(strings[3])) {
                    continue;
                }
                // todo 2019-4-4 测试功能，频繁出现的域名从查询结果中排除
                if (preSet.contains(strings[3])) {
                    filteredList.add(strings);
                } else {
                    candidateList.add(strings);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        candidateTable.setData(candidateList);
        filteredTable.setData(filteredList);
        if (candidateList.size() == 0) {
            candidateList = filteredList;
        }
        appIdLabel2.setText(candidateList.get(0)[0]);
        appNameLabel.setText(candidateList.get(0)[1]);
        appPkgNameLabel.setText(candidateList.get(0)[2]);


        jComboBox.setMaximumRowCount(20);
        jComboBox.removeAllItems();
        jComboBox.addItem("选择要修改的域名");
        for (String[] strings : candidateList) {
            jComboBox.addItem(strings[3]);
        }
        for (String[] strings : filteredList) {
            jComboBox.addItem(strings[3]);
        }
        return filteredList;
    }

    public static void main(String[] args) {
        new MyJFrame().setVisible(true);
    }
}

class MyTable extends JTable {
    private ArrayList<String[]> list;

    MyTable(int x, int y, int width, int height) {
        this.getTableHeader().setFont(MyFont.ZH_FONT);
        this.setRowHeight(28);
        this.setFont(MyFont.EN_FONT);
        this.setLocation(0, 0);
        this.setSize(width, height);

    }

    public void setData(ArrayList<String[]> list) {
        this.list = list;
        Object[][] showDates = new Object[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            String[] string = list.get(i);
            String domain = DomainUtil.getShortDomain1(string[3]);
            String domainDesc = string[4];
            if (domain.length() > 26) {
                domain = "..." + domain.substring(domain.length() - 26);
            }

            String[] subDate = new String[]{domain, domainDesc};
            showDates[i] = subDate;
        }

        this.setModel(new DefaultTableModel(showDates, new String[]{"域名", "域名信息"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
//                return false;
            }
        });

        // 设置为右对齐
        TableColumn column = this.getColumnModel().getColumn(0);
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.RIGHT);
        column.setCellRenderer(render);

        column = this.getColumnModel().getColumn(1);
        render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.RIGHT);

        column.setCellRenderer(render);

        this.setSize(60, 90);
        this.setLocation(0, 0);
    }

    public void processData(String appId) {
        String sql = "update app_domain set label = ? where app_id = ? and domain = ?";
        for (int i = 0, j = 0; i < this.getRowCount(); i++, j++) {
            if (j < this.getSelectedRows().length) {
                while (i < this.getSelectedRows()[j]) {
                    DBManager.execute(DataSource.APP_TEST_DB, sql, "-1", appId, this.list.get(i)[3]);
                    i++;
                }
                DBManager.execute(DataSource.APP_TEST_DB, sql, "1", appId, this.list.get(i)[3]);
            } else {
                do {
                    DBManager.execute(DataSource.APP_TEST_DB, sql, "-1", appId, this.list.get(i)[3]);
                } while (++i < this.getRowCount());
                break;
            }
        }
    }
}

abstract class DomainUtil {
    static HashSet<String> topLevelDomainSet = new HashSet<>(2000);

    static {
        String sql = "select top_level_domain from top_level_domain";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);
        try {
            while (resultSet.next()) {
                topLevelDomainSet.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 这个方法从后往前判断每个段
    // 一个错误, .sohu在顶级域名表里：
    // 输入：224.img.pp.sohu.com.cn			输出：img.pp.sohu.com.cn

    public static String getShortDomain0(String domain) {
        String[] parts = domain.split("\\.");
        for (int i = parts.length; i > 0; i--) {
            if (!topLevelDomainSet.contains("." + parts[i - 1])) {
                if (i > 1) {
                    return String.join(".", Arrays.copyOfRange(parts, i - 2, parts.length));
                }
                break;
            }
        }
        return domain;
    }

    // 这个方法认为顶级域名最多只有2段
    // *.taobao.com这种域名判断不了
    public static String getShortDomain1(String domain) {
        String[] parts = domain.split("\\.");
        if (parts.length < 4) {
            return domain;
        }
        if (!topLevelDomainSet.contains("." + parts[parts.length - 1])) {
            return domain;
        }
        if (topLevelDomainSet.contains("." + parts[parts.length - 2])) {
            return String.join(".", Arrays.copyOfRange(parts, parts.length - 4, parts.length));
        }
        return String.join(".", Arrays.copyOfRange(parts, parts.length - 3, parts.length));
    }
}

// 枚举字体类型
abstract class MyFont {
    static final Font ZH_FONT = new Font("楷体", Font.PLAIN, 18);
    static final Font EN_FONT = new Font("", Font.PLAIN, 18);
}