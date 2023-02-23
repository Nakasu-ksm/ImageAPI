package com.animetrace.animeimage.Utils;

import com.alibaba.fastjson2.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class Utils {
    public static void return_Json(String message, int code, HttpServletResponse response) throws IOException {
        response.setContentType("text/json; charset=utf-8");
        PrintWriter printWriter = null;
        printWriter = response.getWriter();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);
        jsonObject.put("code", code);
        printWriter.append(jsonObject.toString());
    }
    public static void return_Json(String message, int code, HttpServletResponse response,String customer) throws IOException {
        response.setContentType("text/json; charset=utf-8");
        PrintWriter printWriter = null;
        printWriter = response.getWriter();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(customer, message);
        jsonObject.put("code", code);
        printWriter.append(jsonObject.toString());
    }

    public static String removeXss(String dirtyMessage) {
        return Jsoup.clean(dirtyMessage, Safelist.none());
    }

    public static String[] getCleanValue(String... str) {
        for(int i = 0; i < str.length; ++i) {
            if (str[i] != null) {
                String temp = removeXss(str[i]);
                if (temp != null && !temp.trim().isEmpty()) {
                    str[i] = temp;
                } else {
                    str[i] = null;
                }
            } else {
                str[i] = null;
            }
        }

        return str;
    }

}
