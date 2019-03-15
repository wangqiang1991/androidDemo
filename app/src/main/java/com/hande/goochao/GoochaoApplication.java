package com.hande.goochao;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.util.Log;

import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.CrashHandler;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ContextUtils;
import com.hande.goochao.views.components.refresh.GCBallPulseFooter;
import com.hande.goochao.views.components.refresh.GCGifPulseHeader;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UploadManager;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.sobot.chat.SobotApi;
import com.sobot.chat.SobotUIConfig;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xutils.image.ImageDecoder;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;


/**
 * Created by Wangem on 2018/1/8.
 */

public class GoochaoApplication extends Application {

    private static final String TAG = "GoochaoApplication";

    private static GoochaoApplication application;
    public static UploadManager uploadManager;

    private IWXAPI iwxapi;

    //static 代码段可以防止内存泄露
    static {

        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(android.R.color.black, android.R.color.black);//全局设置主题颜色
//                GCBallPulseHeader header = new GCBallPulseHeader(context);
                //GCRefreshHeader header = new GCRefreshHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header

                GCGifPulseHeader header = new GCGifPulseHeader(context);
                return header;
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new GCBallPulseFooter(context);
            }
        });
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        /*x.Ext.setDebug(BuildConfigUtils.isDev());

        // 调用支付宝沙箱
        if (BuildConfigUtils.isDev()) {
            EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
        }*/

        try {
            Field field = ImageDecoder.class.getDeclaredField("supportWebP");
            field.setAccessible(true);
            Log.i(TAG, "修改xutils属性之前，" + field.get(ImageDecoder.class));
            field.set(ImageDecoder.class, false);
            Log.i(TAG, "修改xutils属性之后，" + field.get(ImageDecoder.class));
        } catch (Exception ex) {

        }

        // 初始化Crash日志采集
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext(), this);
        GoochaoApplication.application = this;

        int appVersion = 1;
        try {
            // 获取当前的版本号
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.e("err", e);
        }
        try {
            // 相对与sdcard/Android/data/{package}/cache/obj/
            AppSessionCache.init(new File(ContextUtils.getExternalCacheDir(this, AppConfig.OBJECT_CACHE_DIR)), appVersion);
        } catch (IOException e) {
            AppLog.e("err", e);
            onTerminate();
            android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
        }
        registerToWx();

        // 初始化七牛上传组件
        initQiniuUpload();

        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String channel = appInfo.metaData.getString("CHANNEL");
            AppSession.getInstance().put(AppConst.CHANNEL, channel);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "onCreate: 获取渠道失败", e);
        }

        /**
         * 初始化sdk(智齿客服)
         * @param context 上下文  必填
         * @param appkey  用户的appkey  必填 如果是平台版用户需要传总公司的appkey
         * @param uid     用户的唯一标识，不能传一样的值，可以为空
         */
        SobotApi.initSobotSDK(this, "8130569c6f9b47149fab171e2aafb9f7", "");
//        SobotApi.initSobotSDK(this, "4fec80e2dd9349e08916b54a02316c06", ""); // 测试
        /**
         * 是否开启消息提醒
         */
        SobotApi.setNotificationFlag(this,true,R.mipmap.gc_ic_launcher,R.mipmap.gc_ic_launcher);
        //客服标题栏样式修改
        SobotUIConfig.sobot_titleBgColor = R.color.WHITE;
        SobotUIConfig.sobot_titleTextColor = R.color.BLACK;

    }

    /**
     * 功能：微信SDK注册
     */
    private void registerToWx() {
        iwxapi = WXAPIFactory.createWXAPI(this,AppConfig.WX_APPID,true);
        boolean success = iwxapi.registerApp(AppConfig.WX_APPID);
        if (success) {
            AppLog.i("注册微信SDK成功");
        }
    }

    /**
     * 初始化七牛上传
     */
    private void initQiniuUpload() {
        Configuration config = new Configuration.Builder()
                .chunkSize(256 * 1024)  //分片上传时，每片的大小。 默认 256K
                .putThreshhold(512 * 1024)  // 启用分片上传阀值。默认 512K
                .connectTimeout(10) // 链接超时。默认 10秒
                .responseTimeout(60) // 服务器响应超时。默认 60秒
                //.recorder(recorder)  // recorder 分片上传时，已上传片记录器。默认 null
//                .recorder(recorder, keyGen)  // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
//                .zone(Zone.zone0) // 设置区域，指定不同区域的上传域名、备用域名、备用IP。默认 Zone.zone0
                .build();
// 重用 uploadManager。一般地，只需要创建一个 uploadManager 对象
        uploadManager = new UploadManager(config);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        x.image().clearMemCache();
        AppLog.i("mem 清理图片内存空间");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        x.image().clearMemCache();
        AppLog.i("mem low 清理图片内存空间");
    }

    public static GoochaoApplication getApplication() {
        return application;
    }


}
