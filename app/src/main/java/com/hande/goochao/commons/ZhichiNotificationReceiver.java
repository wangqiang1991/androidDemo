package com.hande.goochao.commons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/10/26.
 */
public class ZhichiNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ZhichiUtils.startZhichi(context , null);
    }
}