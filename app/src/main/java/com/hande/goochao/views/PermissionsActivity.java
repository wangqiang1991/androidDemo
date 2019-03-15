package com.hande.goochao.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.hande.goochao.BuildConfig;
import com.hande.goochao.commons.permissions.PermissionsManager;
import com.hande.goochao.views.base.ToolBarActivity;

/**
 * Created by Wangem on 2018/3/22.
 */

public class PermissionsActivity extends ToolBarActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private boolean permissionsComplete;
    private Bundle savedInstanceState;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        // 申请存储权限
        if (!PermissionsManager.requestStorage(this)) {
            // 等待用户授权
            return;
        }
        permissionsComplete = true;
        doCreate(this.savedInstanceState);
    }

    @Override
    protected final void onResume() {
        super.onResume();
        if (permissionsComplete) {
            doResume();
        }
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        if (permissionsComplete) {
            doDestroy();
        }
    }

    @Override
    protected final void onPause() {
        super.onPause();
        if (permissionsComplete) {
            doPause();
        }
    }

    @Override
    protected final void onStart() {
        super.onStart();
        if (permissionsComplete) {
            doStart();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (permissionsComplete) {
            doReStart();
        }
    }

    @Override
    protected final void onStop() {
        super.onStop();
        if (permissionsComplete) {
            doStop();
        }
    }

    protected void doCreate(Bundle savedInstanceState) {

    }

    protected void doResume() {

    }

    protected void doDestroy() {

    }

    protected void doPause() {

    }

    protected void doStart() {

    }

    protected void doReStart() {

    }

    protected void doStop() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionsManager.REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                permissionsComplete = true;
                doCreate(this.savedInstanceState);
            } else {
                finish();
            }
        }
    }

}
