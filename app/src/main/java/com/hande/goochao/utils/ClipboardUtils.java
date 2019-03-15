package com.hande.goochao.utils;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.hande.goochao.views.components.AlertManager;


/**
 * Created by Wangem on 2017/8/20.
 */
public class ClipboardUtils {


    /**
     * 功能：设置剪切板数据
     * @param text
     */
    public static void setClipboardText(String text,Context context) {
        // 从API11开始android推荐使用android.content.ClipboardManager
        // 为了兼容低版本我们这里使用旧版的android.text.ClipboardManager，虽然提示deprecated，但不影响使用。
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 将文本内容放到系统剪贴板里。
        cm.setText(text);
        if (!TextUtils.isEmpty(text)) {
            AlertManager.showSuccessToast(context, "复制成功", false);
        }
    }

}
