package com.hande.goochao.commons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sobot.chat.utils.LogUtils;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/10/25.
 */
public class ZhichiReceiver extends BroadcastReceiver {

    private OnMessageReceive onMessageReceive;

    public ZhichiReceiver(OnMessageReceive onMessageReceive) {
        this.onMessageReceive = onMessageReceive;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int noReadNum = intent.getIntExtra("noReadCount", 0);//未读消息数
        String content = intent.getStringExtra("content");//新消息内容
        LogUtils.i("新消息内容:"+content + "，未读消息数是:" + noReadNum);

        onMessageReceive.onReceive(content, noReadNum);
    }

    public interface OnMessageReceive {
        void onReceive(String content, int noReadNum);
    }
}

