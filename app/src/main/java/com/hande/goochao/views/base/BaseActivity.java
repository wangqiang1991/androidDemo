package com.hande.goochao.views.base;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.BuildProperties;

import org.xutils.x;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Wangem on 2018/1/8.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            if (isExcludeDevices()) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(android.R.color.black, this.getTheme()));
            } else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        // 设置小米和魅族手机的状态栏字体颜色为深色
        boolean success = setMiuiStatusBarDarkMode(this, true);
        if (!success) {
            success = setMeizuStatusBarDarkIcon(this, true);
        }
        // 如果是小米或者魅族手机并且状态栏字体颜色成功被修改，那么就将状态栏背景设置为白色或者透明
        if (success) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        x.view().inject(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        x.view().inject(this);
    }

    //设置成白色的背景，字体颜色为黑色。
    public static boolean setMiuiStatusBarDarkMode(Activity activity, boolean darkmode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //设置成白色的背景，字体颜色为黑色。
    public static boolean setMeizuStatusBarDarkIcon(Activity activity, boolean dark) {
        boolean result = false;
        if (activity != null) {
            try {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                activity.getWindow().setAttributes(lp);
                result = true;
            } catch (Exception e) {
            }
        }
        return result;
    }

    public boolean isExcludeDevices() {
        String brand = Build.BRAND;
        if (TextUtils.isEmpty(brand)) {
            return false;
        }
        try {
            BuildProperties properties = BuildProperties.newInstance();
            // 判断品牌
            if ("ivvi".equals(brand.toLowerCase())) {
                // 排除ivvi 5.1.1系统，他是貌似android 23 6.0.1系统，所以要排除掉，将他降级为5.x系统
                // 获取真实的版本号
                String version = properties.getProperty("ro.build.version.release", "");
                if ("5.1.1".equals(version)) {
                    return true;
                }
            }
        } catch (IOException e) {
            AppLog.e("加载build.prod失败", e);
        }

        return false;
    }
}
