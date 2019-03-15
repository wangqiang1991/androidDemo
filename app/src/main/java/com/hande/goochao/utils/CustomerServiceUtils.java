package com.hande.goochao.utils;

import android.content.Context;
import android.content.Intent;

import com.hande.goochao.config.AppConfig;
import com.hande.goochao.views.components.WebViewActivity;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Wangem on 2018/3/22.
 */

public class CustomerServiceUtils {


    public static void goService(CustomerServiceModel model, Context context) {
        StringBuilder url = new StringBuilder(AppConfig.SERVICE_URL);

        if (StringUtils.isNotEmpty(model.uname)) {
            url.append("&uname=" + model.uname);
        }

        if (StringUtils.isNotEmpty(model.realname)) {
            url.append("&realname=" + model.realname);
        }

        if (StringUtils.isNotEmpty(model.face)) {
            url.append("&face=" + model.face);
        }

        if (StringUtils.isNotEmpty(model.email)) {
            url.append("&email=" + model.email);
        }

        JSONObject customerFields = new JSONObject();
        if (StringUtils.isNotEmpty(model.orderNumber)) {
            JsonUtils.put(customerFields, "customField1", model.orderNumber);
        }

        if (StringUtils.isNotEmpty(model.goodsTitle)) {
            JsonUtils.put(customerFields, "customField2", model.goodsTitle);
        }

        if (StringUtils.isNotEmpty(model.afterWorkNumber)) {
            JsonUtils.put(customerFields, "customField3", model.afterWorkNumber);
        }

        if (StringUtils.isNotEmpty(model.goodsNumber)) {
            JsonUtils.put(customerFields, "customField4", model.goodsNumber);
        }

        String customerFieldsStr = customerFields.toString();
        url.append("&customerFields=" + encodeString(customerFieldsStr));

        // 进入客服
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("title", "构巢客服");
        intent.putExtra("url", url.toString());
        context.startActivity(intent);

    }

    private static String encodeString(String customerFieldsStr) {
        try {
            return URLEncoder.encode(customerFieldsStr, "utf-8");
        } catch (UnsupportedEncodingException e) {
            AppLog.e("url encoding fail", e);
            return "";
        }
    }


    public static class CustomerServiceModel {
        private String uname;
        private String realname;
        private String face;
        private String email;

        private String orderNumber;
        private String goodsTitle;
        private String afterWorkNumber;
        private String goodsNumber;

        public String getUname() {
            return uname;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }

        public String getRealname() {
            return realname;
        }

        public void setRealname(String realname) {
            this.realname = realname;
        }

        public String getFace() {
            return face;
        }

        public void setFace(String face) {
            this.face = face;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public String getGoodsTitle() {
            return goodsTitle;
        }

        public void setGoodsTitle(String goodsTitle) {
            this.goodsTitle = goodsTitle;
        }

        public String getAfterWorkNumber() {
            return afterWorkNumber;
        }

        public void setAfterWorkNumber(String afterWorkNumber) {
            this.afterWorkNumber = afterWorkNumber;
        }

        public String getGoodsNumber() {
            return goodsNumber;
        }

        public void setGoodsNumber(String goodsNumber) {
            this.goodsNumber = goodsNumber;
        }
    }
}
