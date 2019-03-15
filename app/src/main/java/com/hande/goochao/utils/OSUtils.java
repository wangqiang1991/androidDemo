package com.hande.goochao.utils;

import android.text.TextUtils;

import java.io.IOException;

/**
 * 系统帮组类
 * Created by yanshen on 2016/1/6.
 */
public class OSUtils {

    /**
     * 是否是魅族手机
     * @return
     */
    public static boolean isMeiZu(){
        try {
            BuildProperties buildProperties = BuildProperties.newInstance();
            boolean brand = "Meizu".equals(buildProperties.getProperty("ro.product.brand"));
//            String version = buildProperties.getProperty("ro.custom.build.version");
            String version = buildProperties.getProperty("ro.build.version.incremental");
            boolean flyme = false;
            if(brand && !TextUtils.isEmpty(version)){
               flyme = version.contains("Flyme_OS");
            }
            return brand && flyme;
        } catch (IOException e) {
           return  false;
        }
    }

    /**
     * 功能：判断是否是华为手机
     * @return
     */
    public static boolean isHuawei() {
        try {
            BuildProperties buildProperties = BuildProperties.newInstance();
            boolean brand = false;
            String brandName = buildProperties.getProperty("ro.product.brand");
            if (brandName != null && brandName.toUpperCase().contains("HUAWEI")) {
                brand = true;
            }

            return brand;
        } catch (IOException e) {
            e.printStackTrace();
            return  false;
        }
    }

}
