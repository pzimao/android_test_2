package cn.edu.uestc.utils;

import cn.edu.uestc.wechat.bean.Boundary;
import cn.edu.uestc.wechat.bean.View;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

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
        if (xpath == null) { // 卫语句
            return Boundary.NULL_BOUNDARY;
        }
        Element connectorElement = (Element) EmulatorStateManager.getUIXmlDocument().selectSingleNode(xpath);
        if (connectorElement == null) { // 提供的document不含xpath路径
            return Boundary.NULL_BOUNDARY;
        }
        String result = connectorElement.attributeValue("bounds");
        return new Boundary(result);
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
}
