package com.hande.goochao.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Created by Wangem on 2018/3/15.
 */

public class PriceUtils {

    /**
     * 去掉多余的小数点
     * @param priceStr
     * @return
     */
    public static String beautify(String priceStr) {
        if (StringUtils.isEmpty(priceStr)) {
            return priceStr;
        }
        if (!priceStr.endsWith(".00") && !priceStr.endsWith(".0")) {
            return priceStr;
        }
        return priceStr.replace(".00", "").replace(".0", "");
    }

    /**
     * 去掉多余的小数点
     * @param price
     * @return
     */
    public static String beautify(float price) {
        String priceStr = String.valueOf(price);
        return beautify(priceStr);
    }

    /**
     * 去掉多余的小数点
     * @param price
     * @return
     */
    public static String beautify(double price) {
        String priceStr = String.valueOf(price);
        return beautify(priceStr);
    }

}
