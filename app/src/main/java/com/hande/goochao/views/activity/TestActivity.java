package com.hande.goochao.views.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;

import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.ZoomView;

import org.json.JSONObject;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;

public class TestActivity extends ToolBarActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    @ViewInject(R.id.test_btn)
    private Button testBtn;

    @ViewInject(R.id.zoom_view)
    private ZoomView zoomView;

    private AnimationProgressBar animationProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        setTitle("GOOCHAO");
        animationProgressBar = new AnimationProgressBar(this);
        int permission = ActivityCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有写的权限，去申请写的权限，会弹出对话框
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
        // 初始化日志
        AppLog.initLog4j();

        zoomView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertManager.toast(TestActivity.this, "ZoomView Click");
            }
        });
    }

    @Event(value = R.id.test_btn, type = View.OnClickListener.class)
    private void onClick(View view) {
//        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();

        String url = "https://wx.goochao.com/app/api/category";
        Map<String, String> params = new HashMap<>();
        params.put("categoryId", "MX0l97KQ");
        /*HttpRequest.get(url, null, params, new TypeToken<Response<List<HashMap>>>() {}.getType(), new RequestCallback<Response<List<HashMap>>>() {
            @Override
            public void onSuccess(Response<List<HashMap>> response) {
                AppLog.i(response.getCode() + "  " + response.getData());
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
            }
        });*/

        animationProgressBar.show();
        HttpRequest.get(url, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                animationProgressBar.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                AppLog.i(response.toString());
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(TestActivity.this, "", false);
            }
        });
    }
}
