package com.honeycomb.colorphone.util;

import java.text.DecimalFormat;

public class NumberUtils {

    private static DecimalFormat twoDecimalsFormat = new DecimalFormat("0.00");
    private static DecimalFormat fiveDecimalsFormat = new DecimalFormat("0.00000");

    public static String reserveTwoDecimals(double value) {
        return reserveDecimals(twoDecimalsFormat, value);
    }

    public static String reserveFiveDecimals(double value) {
        return reserveDecimals(fiveDecimalsFormat, value);
    }

    private static String reserveDecimals(DecimalFormat format, double value) {
        String result = format.format(value);
        //为解决俄语下符号问题，将 "," 转化为 "."
        return result.replace(",", ".");
    }
}
