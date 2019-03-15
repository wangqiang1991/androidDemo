package com.hande.goochao.views.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.xinge.MessageReceiver;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/10/17.
 */
public class XinGeLogActivity extends ToolBarActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_xinge_layout);

//        //接受通知参数
//        String paramsStr = MessageReceiver.params;
//        System.out.print(paramsStr);
        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(XinGeLogActivity.this);
        Uri uri = getIntent().getData();
        if (uri != null){
            String url = uri.toString();
            String p1 = uri.getQueryParameter("params");
            try {
                JSONObject jsonObject = new JSONObject(p1);
                int messageType = JsonUtils.getInt(jsonObject,"messageType",0);
                String messageId = JsonUtils.getString(jsonObject ,"messageId","");
                clickNotLog(messageId);
                if(loginResult == null){

                }else {

                }
                if (messageType == 1 || messageType == 2){
                    Intent intentMain = new Intent(this, MainActivity.class);
                    startActivity(intentMain);
                    Intent intent = new Intent(this,MyOrderActivity.class);
                    if (messageType == 1){
                        intent.putExtra("selectedIndex", 2);
                    } else {
                        intent.putExtra("selectedIndex", 3);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivities(new Intent[] {intentMain, intent});
                    finish();
                }else if (messageType >= 3 && messageType <= 8 ){
                    Intent intentMain = new Intent(this, MainActivity.class);
                    startActivity(intentMain);
                    Intent intent = new Intent(this, AfterSaleActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivities(new Intent[] {intentMain, intent});
                    finish();
                }else if (messageType == 9 ){
                    String goodsId = JsonUtils.getString(jsonObject ,"goodsId","");
                    Intent intentMain = new Intent(this, MainActivity.class);
                    startActivity(intentMain);
                    Intent intent = new Intent(this, NewProductInformationActivity.class);
                    intent.putExtra("goodsId", goodsId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivities(new Intent[] {intentMain, intent});
                    finish();
                } else if (messageType == 10 ){
                    String activityUrl = JsonUtils.getString(jsonObject, "activityUrl", "");
                    String activityId = JsonUtils.getString(jsonObject, "activityId", "");
                    Intent intentMain = new Intent(this, MainActivity.class);
                    startActivity(intentMain);
                    Intent intent = new Intent(this, SaleGiftActivity.class);
                    if (activityUrl.indexOf("?") == -1){
                        intent.putExtra("url", activityUrl + "?activityId=" + activityId);
                    }else {
                        intent.putExtra("url", activityUrl + "&activityId=" + activityId);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivities(new Intent[] {intentMain, intent});
                    finish();
                }else if (messageType == 11 ){
                    String subjectId = JsonUtils.getString(jsonObject,"subjectId","");
                    Intent intentMain = new Intent(this, MainActivity.class);
                    startActivity(intentMain);
                    Intent intent = new Intent(this, MagazineDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("subjectArticleId", subjectId);
                    startActivities(new Intent[] {intentMain, intent});
                    finish();
                }else {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 点击数加1接口
     */
    private void clickNotLog(String messageId){

            HttpRequest.head(AppConfig.MESSAGE_UP + messageId , null, null, JSONObject.class, new RequestCallback<JSONObject>() {
                @Override
                public void onComplete(boolean success, JSONObject response) {
                }

                @Override
                public void onSuccess(JSONObject response) {
                }

                @Override
                public void onError(Throwable ex) {
                }
            });
    }
}
