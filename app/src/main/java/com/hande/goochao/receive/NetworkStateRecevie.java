package com.hande.goochao.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;


/**
 * Created by yanshen on 2015/11/20.
 */
public class NetworkStateRecevie extends BroadcastReceiver {


    public static final int WIFI = 1;
    public static final int TRAFFIC = 2;
    public static final int NONETWORK = 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 监听到网络变化后需要重置DNS
//        RestfulUtils.useProxy(null, 0);
        NetworkStateRecevie.setNetWorkState(context);
    }

    public static void setNetWorkState(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        String type = null;
        try {
            type = activeInfo != null ? activeInfo.getTypeName().toLowerCase() : "";
        } catch (Exception e) {
            AppLog.e("err", e);
        }
        if("wifi".equals(type) &&  wifiInfo.isConnected()){
//            AlertManager.toastForForeground(context, "wifi环境", true);
            AppSession.getInstance().put(AppConst.SESSION_NETWORK_STATE, NetworkStateRecevie.WIFI);
        } else if(mobileInfo.isConnected()){
//            AlertManager.toastForForeground(context, "当前使用2G/3G/4G网络，接听电话会导致网络中断！", true);
            AppSession.getInstance().put(AppConst.SESSION_NETWORK_STATE, NetworkStateRecevie.TRAFFIC);
        } else {
//            AlertManager.toastForForeground(context, "网络已断开", true);
            AppSession.getInstance().put(AppConst.SESSION_NETWORK_STATE, NetworkStateRecevie.NONETWORK);
        }
    }
}
