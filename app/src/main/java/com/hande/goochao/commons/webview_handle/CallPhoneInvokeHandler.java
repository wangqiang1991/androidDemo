package com.hande.goochao.commons.webview_handle;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.components.ConfirmDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/10/10.
 */
public class CallPhoneInvokeHandler implements InvokeHandler {

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    @Override
    public String invokeHandler(String paramsStr, final Context context) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("data", "success");

        final Activity activity = (Activity)context;

        JSONObject paramsObject = new JSONObject(paramsStr);
        JSONObject value = JsonUtils.getJsonObject(paramsObject,"params",null);
        final  String phone = JsonUtils.getString(value,"phone","");

        ConfirmDialog alertDialog = new ConfirmDialog(context, ConfirmDialog.ConfirmDialogType.ConfirmDialogType_Warning);
        alertDialog.setMsg(phone)
                .setLeftButtonText("取消")
                .setRightButtonText("呼叫")
                .setCallBack(new ConfirmDialog.CallBack() {
                    @Override
                    public void buttonClick(Dialog dialog, boolean leftClick) {
                        dialog.dismiss();
                        if (!leftClick) {
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                        Manifest.permission.CALL_PHONE)) {
                                    Toast.makeText(context, "请于授权后返回拨打！", Toast.LENGTH_LONG).show();

                                    Intent newIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                    newIntent.setData(uri);
                                    context.startActivity(newIntent);
                                }else{
                                    ActivityCompat.requestPermissions(activity,
                                            new String[]{Manifest.permission.CALL_PHONE},
                                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                                }
                            }else {
                                context.startActivity(intent);
                            }
                        }
                    }
                });
        alertDialog.show();

        return result.toString();
    }
}
