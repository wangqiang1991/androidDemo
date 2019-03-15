package com.hande.goochao.commons;

import android.content.Context;
import android.content.Intent;

import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.activity.LoginActivity;
import com.sobot.chat.SobotApi;
import com.sobot.chat.api.enumtype.SobotChatTitleDisplayMode;
import com.sobot.chat.api.model.Information;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/10/25.
 */
public class ZhichiUtils {

    public static void startZhichi(Context context,Map<String,String> customerFields) {
        if (AppSessionCache.getInstance().isLogin(context)) {
            Information info = new Information();
            info.setAppkey("8130569c6f9b47149fab171e2aafb9f7");
//            info.setAppkey("4fec80e2dd9349e08916b54a02316c06"); //测试
            info.setCustomerFields(customerFields);
            JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(context);
            String memberId = JsonUtils.getString(loginResult, "memberId", "");
            String headImg = JsonUtils.getString(loginResult, "head", "");
            String nickName = JsonUtils.getString(loginResult, "nickName", "");
            String phone = JsonUtils.getString(loginResult, "phone", "");
            info.setUid(memberId);
            info.setUname(nickName);
            info.setFace(headImg);
            info.setTel(phone);
            SobotApi.setChatTitleDisplayMode(context, SobotChatTitleDisplayMode.ShowFixedText, "构巢客服");
            SobotApi.startSobotChat(context, info);
        }else {
            Intent intent = new Intent(context , LoginActivity.class);
            context.startActivity(intent);
        }

    }

    public static JSONObject getUserUnReadCount(Context context) {
        JSONObject data = new JSONObject();
        try {
            if (AppSessionCache.getInstance().isLogin(context)) {
                data.put("haveLog", true);
                String uid = JsonUtils.getString(AppSessionCache.getInstance().getLoginResult(context), "memberId", "");
                int count = SobotApi.getUnreadMsg(context, uid);
                data.put("count", count);
            } else {
                data.put("haveLog", false);
                data.put("count", null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static boolean haveUnRead(Context context){
        boolean have;
        String uid = JsonUtils.getString(AppSessionCache.getInstance().getLoginResult(context), "memberId", "");
        int count = SobotApi.getUnreadMsg(context , uid);
        have = count != 0;
        return have;
    }
}
