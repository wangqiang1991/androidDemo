package com.hande.goochao.utils;

import java.math.BigDecimal;

/**
 * Created by yanshen on 2015/11/13.
 */
public class NumberUtils {

    /**
     * 转换为整型,转换失败返回默认值
     * @param value
     * @return
     */
    public static int toInt(String value, int defaultValue){
        try{
            return Integer.parseInt(value);
        } catch (NumberFormatException ex){
            ex.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * 如果有小数点，显示小数点, 如果没有小数点显示整数
     * @param num
     * @return
     */
    public static String doubleTrans(double num){
        if(num % 1.0 == 0){
            return String.valueOf((long)num);
        }
        // 保留一位小数
        return String.format("%.f", num);
    }

    /**
     * 两数相加
     * @param left
     * @param right
     * @return
     */
    public static float add(float left, float right) {
        BigDecimal leftDecimal = new BigDecimal(Float.toString(left));
        BigDecimal rightDecimal = new BigDecimal(Float.toString(right));
        return leftDecimal.add(rightDecimal).floatValue();
    }

    /**
     * 两数相减
     * @param left
     * @param right
     * @return
     */
    public static float subtract(float left, float right) {
        BigDecimal leftDecimal = new BigDecimal(Float.toString(left));
        BigDecimal rightDecimal = new BigDecimal(Float.toString(right));
        return leftDecimal.subtract(rightDecimal).floatValue();
    }

    /**
     * 两数相乘
     * @param left
     * @param right
     * @return
     */
    public static float multiply(float left, float right) {
        BigDecimal leftDecimal = new BigDecimal(Float.toString(left));
        BigDecimal rightDecimal = new BigDecimal(Float.toString(right));
        return leftDecimal.multiply(rightDecimal).floatValue();
    }

    /**
     * 两数相除
     * @param left
     * @param right
     * @return
     */
    public static float divide(float left, float right) {
        BigDecimal leftDecimal = new BigDecimal(Float.toString(left));
        BigDecimal rightDecimal = new BigDecimal(Float.toString(right));
        return leftDecimal.divide(rightDecimal).floatValue();
    }

    /**
     * 保留两位小数,向下取整
     * @param value
     * @return
     */
    public static float decimal(double value) {
        // 乘以100是为了保留2位小数,floor是小于该数的最大整数,除以一百是还原金额
        return (float) (Math.floor(value * 100) / 100);
    }

    /**
     * 保留两位小数,向下取整
     * @param value
     * @return
     */
    public static double decimalDouble(double value) {
        // 乘以100是为了保留2位小数,floor是小于该数的最大整数,除以一百是还原金额
        double v = value * 100;
        return (Math.round(v) / 100d);
    }

}
