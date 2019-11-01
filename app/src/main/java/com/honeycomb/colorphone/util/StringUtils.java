package com.honeycomb.colorphone.util;

public class StringUtils {

    public static String getProvince(String string) {
        int index = string.indexOf("province");
        if (index != -1) {
            String str = string.substring(index + 11);
            return str.substring(0, str.indexOf("\""));
        } else {
            return "";
        }
    }

    public static String getCity(String string) {
        int index = string.indexOf("city");
        if (index != -1) {
            String str = string.substring(index + 7);
            return str.substring(0, str.indexOf("\""));
        } else {
            return "";
        }
    }

    public static String getOperator(String string) {
        int index = string.indexOf("operator");
        if (index != -1) {
            String str = string.substring(index + 11);
            return str.substring(0, str.indexOf("\""));
        } else {
            return "";
        }
    }
}
