package com.hande.goochao.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.hande.goochao.views.components.AlertManager;

import java.util.regex.Pattern;


/**
 * Created by Wangem on 2017/2/21.
 */
public class PhoneUtils {

    /**
     * 手机验证规则
     */
    public static final String MOBILE_REGEX = "^1[3,4,5,7,8][0-9]{9}$";

    public static void makeCall(Context context,String phone) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            AlertManager.toast(context, "授权失败");
            return;
        }
        context.startActivity(intent);
    }

    /**
     * 验证手机号
     *
     * @param mobile
     * @return
     */
    public static boolean validateMobile(String mobile) {
        mobile = mobile.replaceAll(" ","");
        if (TextUtils.isEmpty(mobile)) return false;
        return Pattern.matches(MOBILE_REGEX, mobile);
    }
}
