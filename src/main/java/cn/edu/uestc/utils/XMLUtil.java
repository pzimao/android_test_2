package cn.edu.uestc.utils;

import cn.edu.uestc.wechat.bean.Boundary;
import cn.edu.uestc.wechat.bean.Resource;
import cn.edu.uestc.wechat.bean.View;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

public class XMLUtil {

    private static final Logger logger = LogManager.getLogger("XML解析器");


    /**
     * 获取最新的document
     * 通过xpath获取控件边界
     *
     * @param xpath
     * @return
     */
    public static Boundary getBoundary(String xpath) {
        if (xpath == null) {
            return Boundary.NULL_BOUNDARY;
        }
        Element connectorElement = (Element) EmulatorStateManager.getUIXmlDocument().selectSingleNode(xpath);
        if (connectorElement == null) { // 提供的document不含xpath路径
            return Boundary.NULL_BOUNDARY;
        }
        String result = connectorElement.attributeValue("bounds");
        return new Boundary(result);
    }


    /**
     * 2019-8-12 添加方法
     * 获取最新的document
     * 通过xpath获取最上边的控件边界
     *
     * @param xpaths
     * @return
     */
    public static Boundary getTopBoundary(String... xpaths) {
        if (xpaths.length == 0) {
            return Boundary.NULL_BOUNDARY;
        }
        Document document = EmulatorStateManager.getUIXmlDocument();

        ArrayList<Boundary> boundaryList = new ArrayList<>();

        for (String xpath : xpaths) {
            Element connectorElement = (Element) document.selectSingleNode(xpath);
            if (connectorElement == null) { // 提供的document不含xpath路径
                continue;
            }
            String result = connectorElement.attributeValue("bounds");
            boundaryList.add(new Boundary(result));
        }

        Boundary boundary = Boundary.NULL_BOUNDARY;
        for (Boundary boundary1 : boundaryList) {
            if (boundary1 != Boundary.NULL_BOUNDARY) {
                if (boundary == Boundary.NULL_BOUNDARY) {
                    boundary = boundary1;
                } else {
                    if (boundary.getCenterPosition()[1] > boundary1.getCenterPosition()[1]) {
                        boundary = boundary1;
                    }
                }
            }
        }
        return boundary;
    }


    /**
     * 2019-8-12 添加方法
     * 获取最新的document
     * 通过xpath获取指定边界内最上边的控件边界
     *
     * @param xpaths
     * @return
     */
    public static Boundary getTopBoundary(int upLine, String... xpaths) {
        if (xpaths.length == 0) {
            return Boundary.NULL_BOUNDARY;
        }
        Document document = EmulatorStateManager.getUIXmlDocument();

        ArrayList<Boundary> boundaryList = new ArrayList<>();

        for (String xpath : xpaths) {
            List<Node> nodeList = document.selectNodes(xpath);
            if (nodeList.size() == 0) { // 提供的document不含xpath路径
                continue;
            }
            for (Node node : nodeList) {
                String result = node.getParent().attributeValue("bounds");
                boundaryList.add(new Boundary(result));
            }
        }

        Boundary boundary = Boundary.NULL_BOUNDARY;
        for (Boundary boundary1 : boundaryList) {
            if (boundary1 != Boundary.NULL_BOUNDARY && boundary1.getPositionToBottom(1)[1] >= upLine) {
                if (boundary == Boundary.NULL_BOUNDARY) {
                    boundary = boundary1;
                } else {
                    if (boundary.getCenterPosition()[1] > boundary1.getCenterPosition()[1]) {
                        boundary = boundary1;
                    }
                }
            }
        }
        return boundary;
    }


    public static boolean checkView(Document document, View view) {
        if (view == View.V_NULL) {
            return false;
        }
        for (String xpath : view.resources) {
            if (document.selectSingleNode(xpath) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 用在各种加载页面状态判断
     * 会不停地更新Document
     *
     * @param xpaths
     * @return
     */
    public static String getText(String... xpaths) {
        Document document = EmulatorStateManager.getUIXmlDocument();
        Element connectorElement;
        for (String xpath : xpaths) {
            connectorElement = (Element) document.selectSingleNode(xpath);
            if (connectorElement != null) {
                String result = connectorElement.attributeValue("text");
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 获取页面上的文字
     * 2019-12-12
     */
    public static String[] getGzhmc(String... xpath) {
        Document document = EmulatorStateManager.getUIXmlDocument();
        List<Node> connectorElement = document.selectNodes(xpath[0]);
        System.out.println(connectorElement.size());
        String[] result = new String[connectorElement.size()];
        for (int i = 0; i < result.length; i++) {
            Node node = connectorElement.get(i);
            String gzhmc = "";
            L1:
            for (int j = 1; j < xpath.length; j++) {
                for (Node textNode : node.selectNodes(xpath[j])) {
                    String text = ((Element) textNode).attributeValue("text");
                    if (text == null || "".equals(text) || text.length() > 20) {
                        continue;
                    }
                    gzhmc = text;
                    break L1;
                }
            }
            result[i] = gzhmc;
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(XMLUtil.getText(Resource.XCX_LOADING_STATE_3, Resource.XCX_GAME_LOADING_TITLE));
    }
}
