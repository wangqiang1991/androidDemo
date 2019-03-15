package com.hande.goochao.utils;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by yanshen on 2015/11/5.
 */
public class AppLog {


    private static final String TAG = "AppLog";

    // log4j日志，将日志信息输出到文件保存
    private static Logger logger = Logger.getLogger("AppLog");

    /**
     * 初始化LOG4J
     */
    public static void initLog4j(){
        RollingFileAppender rollingFileAppender;
        Layout fileLayout = new PatternLayout("%d %-5p [%c{2}]-[%L] %m%n");

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                String path = Environment.getExternalStorageDirectory() + File.separator + "goochao" + File.separator + "logs";
                File dir = new File(path);
                if (!dir.exists()) {
                    boolean res = dir.mkdirs();
                    AppLog.i("创建文件夹状态:" + res + ",path=" + path);
                }
                rollingFileAppender = new RollingFileAppender(fileLayout, path + File.separator + "app.txt");
            } catch (IOException e) {
                throw new RuntimeException("Exception configuring log system", e);
            }

            rollingFileAppender.setMaxBackupIndex(5);
            rollingFileAppender.setMaximumFileSize(1024 * 1024 * 5);
            rollingFileAppender.setImmediateFlush(true);
            logger.removeAllAppenders();
            logger.addAppender(rollingFileAppender);
        }
    }

    private static String generateLog(String msg) {
        long l = Thread.currentThread().getId();
        Date localDate = new Date(System.currentTimeMillis());
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append(DateUtils.format(localDate, "yyyy-MM-dd HH:mm:ss:SSS"));
        localStringBuilder.append(" ");
        localStringBuilder.append(Process.myPid());
        localStringBuilder.append("-");
        localStringBuilder.append(l);
        localStringBuilder.append("/");
        localStringBuilder.append("构巢");
        localStringBuilder.append(" ");
        if (!TextUtils.isEmpty(msg)) {
            StackTraceElement localStackTraceElement = Thread.currentThread().getStackTrace()[4];
            localStringBuilder.append(localStackTraceElement.getClassName());
            localStringBuilder.append(":");
            localStringBuilder.append(localStackTraceElement.getMethodName());
            localStringBuilder.append("() [Line ");
            localStringBuilder.append(localStackTraceElement.getLineNumber());
            localStringBuilder.append("] ");
            localStringBuilder.append(msg);
        }
        return localStringBuilder.toString();
    }


    public static void i(String msg){
        msg = generateLog(msg);
        Log.i(TAG, msg);
        logger.info(msg);
    }

    public static void i(String msg, Throwable e){
        msg = generateLog(msg);
        Log.i(TAG, msg, e);
        logger.info(msg, e);
    }

    public static void d(String msg){
        msg = generateLog(msg);
        Log.d(TAG, msg);
        logger.debug(msg);
    }

    public static void d(String msg, Throwable e){
        msg = generateLog(msg);
        Log.d(TAG, msg, e);
        logger.debug(msg, e);
    }

    public static void w(String msg){
        msg = generateLog(msg);
        Log.w(TAG, msg);
        logger.warn(msg);
    }

    public static void w(String msg, Throwable e){
        msg = generateLog(msg);
        Log.w(TAG, msg, e);
        logger.warn(msg, e);
    }

    public static void e(String msg){
        msg = generateLog(msg);
        Log.e(TAG, msg);
        logger.error(msg);
    }

    public static void e(String msg, Throwable e){
        msg = generateLog(msg);
        Log.e(TAG, msg, e);
        logger.error(msg, e);
        /*try {
            MobclickAgent.reportError(CloudWatchApplication.application, e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public static void e() {
    }
}
