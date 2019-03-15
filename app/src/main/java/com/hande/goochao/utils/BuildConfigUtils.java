package com.hande.goochao.utils;

import com.hande.goochao.BuildConfig;

/**
 * Created by Wangem on 2018/3/26.
 */

public class BuildConfigUtils {

    /**
     * 是否开发环境
     * @return
     */
    public static boolean isDev() {
        return BuildConfig.BUILD_TYPE.equals("debug");
    }

    /**
     * 是否测试环境
     * @return
     */
    public static boolean isTest() {
        return BuildConfig.BUILD_TYPE.equals("env_test");
    }

    /**
     * 是否生产环境
     * @return
     */
    public static boolean isProd() {
        return BuildConfig.BUILD_TYPE.equals("release");
    }

}
