package cn.edu.uestc.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ExecUtil {
    private static Logger logger = LogManager.getLogger("cmd执行");

    public static String exec(String command) {
        logger.info(command);
        String result = "";
        try {
            Process pr = Runtime.getRuntime().exec(command);

            //用一个读输出流类去读
            InputStreamReader isr = new InputStreamReader(pr.getInputStream(), Charset.forName("GB2312"));
            //用缓冲器读行
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            //直到读完为止
            while ((line = br.readLine()) != null) {
                result += (line + "\n");
            }
            br.close();
            pr.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        result = result.trim();
        return result;
    }

    public static void main(String[] args) {
        System.out.println(ExecUtil.exec("PING baidu.com"));
    }
}
