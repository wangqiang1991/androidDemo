package com.hande.goochao.commons.webview_handle;

import android.content.Context;
import android.content.Intent;

import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.activity.ProductListActivity;
import com.hande.goochao.views.components.CustomLoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/11/9.
 */
public class ToProductListInvokeHandler implements InvokeHandler {

    private JSONArray categoryArray = new JSONArray();
    private String firstCategoryId;
    private String secondCategoryId;
    private JSONArray childrenArray;
    private CustomLoadingDialog loadingDialog;

    @Override
    public String invokeHandler(String paramsStr, Context context) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("data", "success");
        JSONObject paramsObject = new JSONObject(paramsStr);
        JSONObject value = JsonUtils.getJsonObject(paramsObject, "params", null);
        firstCategoryId = JsonUtils.getString(value, "firstCategoryId", "");
        secondCategoryId = JsonUtils.getString(value, "secondCategoryId", "");

        loadingDialog = new CustomLoadingDialog(context);
        loadingDialog.setLoadingText("加载中");
        loadAllCategory(context);

        return result.toString();
    }

    /**
     * 加载一级类目和二级类目
     */
    private void loadAllCategory(final Context context) {
        loadingDialog.show();

        HttpRequest.get(AppConfig.FIRST_AND_SECOND_CATEGORY, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray dataArray = JsonUtils.getJsonArray(response, "data", new JSONArray());

                    for (int i = 0; i < dataArray.length(); i++) {
                        try {
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            JSONArray tempArray = new JSONArray();
                            JSONObject allObject = new JSONObject();
                            allObject.put("name", "全部");
                            tempArray.put(0, allObject);

                            JSONArray childrenArray = JsonUtils.getJsonArray(dataObject, "children", new JSONArray());

                            for (int j = 0; j < childrenArray.length(); j++) {
                                tempArray.put(childrenArray.getJSONObject(j));
                            }
                            dataObject.put("children", tempArray);
                            categoryArray.put(dataObject);
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    for (int i = 0 ; i < categoryArray.length();i++){
                        String value = JsonUtils.getString(JsonUtils.getJsonItem(categoryArray,i,null),"categoryId","");
                        if (value.equals(firstCategoryId)){
                            childrenArray = JsonUtils.getJsonArray(JsonUtils.getJsonItem(categoryArray,i,null),"children",null);
                            if (secondCategoryId.equals("")) {
                                Intent intent = new Intent(context, ProductListActivity.class);
                                intent.putExtra("categoryId", firstCategoryId);
                                intent.putExtra("all", "1");
                                intent.putExtra("firstId", firstCategoryId);
                                intent.putExtra("categoryList", childrenArray.toString());
                                context.startActivity(intent);
                            } else {
                                Intent intent = new Intent(context, ProductListActivity.class);
                                intent.putExtra("categoryId", secondCategoryId);
                                intent.putExtra("firstId", firstCategoryId);
                                intent.putExtra("categoryList", childrenArray.toString());
                                intent.putExtra("all", "");
                                context.startActivity(intent);
                            }
                            return;
                        }
                    }

                } else {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onError(Throwable ex) {
                loadingDialog.dismiss();
            }
        });
    }
}
