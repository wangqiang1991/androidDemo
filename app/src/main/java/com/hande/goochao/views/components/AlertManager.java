package com.hande.goochao.views.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hande.goochao.R;
import com.hande.goochao.utils.AppUtils;


/**
 * Created by yanshen on 2015/11/5.
 */
public final class AlertManager {

    private static Toast currentToast;


    public static void show(Context context, String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener){
        show(context, context.getString(R.string.alert_title), message, positiveListener,
                negativeListener);
    }

    public static void show(Context context, String title, String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener){
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton(R.string.alert_positive, positiveListener)
                .setNegativeButton(R.string.alert_neutral, negativeListener)
                .setMessage(message).show();
    }

    public static void show(Context context, String title, String message, String positiveText, String negativeText, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener){
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton(positiveText, positiveListener)
                .setNegativeButton(negativeText, negativeListener)
                .setMessage(message).show();
    }

    public static void show(Context context, String message, String positiveText, String negativeText, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener){
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(R.string.alert_title)
                .setPositiveButton(positiveText, positiveListener)
                .setNegativeButton(negativeText, negativeListener)
                .setMessage(message).show();
    }

    public static void show(Context context, String message, DialogInterface.OnClickListener onClickListener){
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(R.string.alert_title)
                .setPositiveButton(R.string.alert_positive, onClickListener)
                .setMessage(message).show();
    }

    public static void toast(Context context, String message){
        toast(context, message, false);
    }

    public static void toast(Context context, String message, boolean showlong){
        if (context == null || message == null) {
            return;
        }
        int length;
        if(showlong) {
            length = Toast.LENGTH_LONG;
        } else {
            length = Toast.LENGTH_SHORT;
        }
        // 避免覆盖
        if(currentToast != null){
            currentToast.cancel();
            currentToast = null;
        }
        currentToast = Toast.makeText(context, message, length);
        currentToast.show();
    }


    public static void toastForForeground(Context context, String message, boolean showlong){
        if(!AppUtils.isRunningForeground(context)){
            // 应用程序在后台
            return;
        }
        int length;
        if(showlong) {
            length = Toast.LENGTH_LONG;
        } else {
            length = Toast.LENGTH_SHORT;
        }
        // 避免覆盖
        if(currentToast != null){
            currentToast.cancel();
            currentToast = null;
        }
        currentToast = Toast.makeText(context, message, length);
        currentToast.show();
    }





    /**
     * 显示错误信息
     * @param context
     */
    public static void showErrorInfo(Context context) {
        showErrorToast(context, "服务器繁忙", false);
    }

    public static void showActionSheetDialog(Context context,String[] items, final SelectListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.itemClick(which);
                    }
                })
                .setCancelable(true)
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }


    /**
     * 显示错误
     * @param context
     * @param message
     */
    public static void showErrorToast(Context context, String message, boolean showLong) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.loading_error_layout, null);
        // 设置提示的文本
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(message);
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
        currentToast = new Toast(context);
        currentToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        if (showLong) {
            currentToast.setDuration(Toast.LENGTH_LONG);
        } else {
            currentToast.setDuration(Toast.LENGTH_SHORT);
        }
        currentToast.setView(view);
        currentToast.show();
    }


    /**
     * 功能：显示成功
     * @param context
     * @param message
     */
    public static void showSuccessToast(Context context,String message, boolean showLong) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.loading_success_layout, null);
        // 设置提示的文本
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(message);

        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
        currentToast = new Toast(context);
        currentToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        if (showLong) {
            currentToast.setDuration(Toast.LENGTH_LONG);
        } else {
            currentToast.setDuration(Toast.LENGTH_SHORT);
        }
        currentToast.setView(view);
        currentToast.show();
    }

    /**
     * 功能：展示默认提示Toast
     * @param context   上下文对象
     * @param message   提示的文本
     * @param showLong  提示的时间
     */
    public static void showNormalToast(Context context, String message, boolean showLong) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.toast_normal_layout, null);
        // 设置提示的文本
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(message);

        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
        currentToast = new Toast(context);
        currentToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        if (showLong) {
            currentToast.setDuration(Toast.LENGTH_LONG);
        } else {
            currentToast.setDuration(Toast.LENGTH_SHORT);
        }
        currentToast.setView(view);
        currentToast.show();
    }


    /**
     * 功能：展示确认框
     * @param context   上下文对象
     * @param message   提示的文本
     * @param leftText  左边按钮文字
     * @param rightText 右边按钮文字
     * @param dialogType    类型
     * @param callBack  点击监听回调对象
     */
    public static void showConfirmDialog(Context context, String message, String leftText, String rightText, ConfirmDialog.ConfirmDialogType dialogType, ConfirmDialog.CallBack callBack) {
        ConfirmDialog alertDialog = new ConfirmDialog(context, dialogType);
        alertDialog.setMsg(message);
        alertDialog.setLeftButtonText(leftText);
        alertDialog.setRightButtonText(rightText);
        alertDialog.setCallBack(callBack);
        alertDialog.show();
    }



    public interface SelectListener {
        void itemClick(int index);
    }
}
