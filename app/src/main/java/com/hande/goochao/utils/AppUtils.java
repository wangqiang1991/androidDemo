package com.hande.goochao.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.hande.goochao.config.AppConst;
import com.hande.goochao.receive.NetworkStateRecevie;
import com.hande.goochao.session.AppSession;

import java.util.List;

/**
 * Created by yanshen on 2015/11/12.
 */
public class AppUtils {

    /**
     * 获取本包信息
     *
     * @param context
     * @return
     */
    public static PackageInfo getPackageInfoInfo(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info;
        } catch (Exception e) {
            AppLog.e("err", e);
            return null;
        }
    }

    /**
     * 获取手机序列化
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            String deviceId = telephonyManager.getDeviceId();
            return deviceId;
        }
        return null;
    }

    /**
     * 是否无网络环境
     *
     * @return
     */
    public static boolean isNoNetwork() {
        try {
            Integer state = AppSession.getInstance().get(AppConst.SESSION_NETWORK_STATE);
            return state != null && state == NetworkStateRecevie.NONETWORK;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 是否Wifi环境
     *
     * @return
     */
    public static boolean isWifi() {
        try {
            Integer state = AppSession.getInstance().get(AppConst.SESSION_NETWORK_STATE);
            return state != null && state == NetworkStateRecevie.WIFI;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }


    /**
     * 是否前端运行
     *
     * @param context
     * @return
     */
    public static boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(context.getPackageName())) {
            return true;
        }

        return false;
    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context
     * @param className 某个界面名称
     */
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static int getAppVersionCode(Context context) {
        int versioncode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versioncode = pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e("获取App versionCode出错", e);
        }
        return versioncode;
    }

    public static String getAppVersionName(Context context) {
        String versionName = "1.0";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e("获取App versionCode出错", e);
        }
        return versionName;
    }


    /**
     * 判断是否安装了某个app
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled(Context context, String packageName ) {
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for ( int i = 0; i < pinfo.size(); i++ ) {
            String name = pinfo.get(i).packageName;
            System.out.println(name);
            if(name.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

}
