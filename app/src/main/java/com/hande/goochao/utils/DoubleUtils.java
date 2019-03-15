package com.hande.goochao.utils;

import java.text.DecimalFormat;

/**
 * Created by Wangenmao on 2018/3/14.
 */

public class DoubleUtils {

    public static String format(double value) {
        DecimalFormat df = new DecimalFormat("######0.00");
        return df.format(value).replace(".00", "");
    }


}
