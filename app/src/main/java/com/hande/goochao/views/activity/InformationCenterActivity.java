package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.ZhichiUtils;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.DateUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;

import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/11/6.
 */
public class InformationCenterActivity extends ToolBarActivity implements View.OnClickListener {

    @ViewInject(R.id.information_center_load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.information_center_loading)
    private LoadingView loadingView;

    @ViewInject(R.id.txt_gouchaojingxuan)
    private TextView gouchaojingxuan;
    @ViewInject(R.id.txt_huodongtongzhi)
    private TextView huodongtongzhi;
    @ViewInject(R.id.txt_xitongxiaoxi)
    private TextView xitongxiaoxi;
    @ViewInject(R.id.txt_kefu)
    private TextView kefu;

    @ViewInject(R.id.recommend_things_desc)
    private TextView recommendsDesc;
    @ViewInject(R.id.sales_activity_desc)
    private TextView salesDesc;
    @ViewInject(R.id.system_message_desc)
    private TextView systemDesc;
    @ViewInject(R.id.recommends_date)
    private TextView recommendsDate;
    @ViewInject(R.id.sales_date)
    private TextView salesDate;
    @ViewInject(R.id.system_date)
    private TextView systemDate;

    @ViewInject(R.id.layout_recommends_message)
    private View recommendsLayout;
    @ViewInject(R.id.layout_activity_message)
    private View activityLayout;
    @ViewInject(R.id.layout_system_message)
    private View systemLayout;
    @ViewInject(R.id.layout_service)
    private View serviceLayout;

    @ViewInject(R.id.recommends_sign)
    private View recommendsSign;
    @ViewInject(R.id.sales_sign)
    private View salesSign;
    @ViewInject(R.id.system_sign)
    private View systemSign;
    @ViewInject(R.id.service_sign)
    private View serviceSign;

    private int sysUnreadCount;
    private int goodsUnreadCount;
    private int subjectUnreadCount;
    private int activityUnreadCount;

    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_center);
        setTitle("消息中心");
        hideBackText();

        inflater = LayoutInflater.from(this);

        setClick();

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadFailView.setVisibility(View.GONE);
                loadNoReadMessageCount();
            }
        });

        loadNoReadMessageCount();
        boldText();
    }

    /**
     * 查询分别未读信息数量
     */
    private void loadNoReadMessageCount() {

        loadingView.setVisibility(View.VISIBLE);
        HttpRequest.get(AppConfig.UNREAD_COUNT, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject jsonObject = JsonUtils.getJsonObject(response, "data", null);
                    sysUnreadCount = JsonUtils.getInt(jsonObject, "sysUnreadCount", 0);
                    goodsUnreadCount = JsonUtils.getInt(jsonObject, "goodsUnreadCount", 0);
                    subjectUnreadCount = JsonUtils.getInt(jsonObject, "subjectUnreadCount", 0);
                    activityUnreadCount = JsonUtils.getInt(jsonObject, "activityUnreadCount", 0);

                    initMessageSign();
                    loadLastMessage();
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Throwable ex) {
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete(boolean success, JSONObject response) {
            }
        });
    }

    /**
     * 查询最新消息信息
     */
    private void loadLastMessage() {

        HttpRequest.get(AppConfig.LAST_MESSAGE, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject jsonObject = JsonUtils.getJsonObject(response, "data", null);

                    initPage(jsonObject);
                    initMessageSign();
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Throwable ex) {
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onClick(View v) {
         if (v == recommendsLayout) {
            toRecommends();
        } else if (v == activityLayout) {
            toSales();
        } else if (v == systemLayout) {
            toSystem();
        } else if (v == serviceLayout) {
            ZhichiUtils.startZhichi(this, null);
        }
    }

    private void toRecommends() {
        Intent intent = new Intent(this, RecommendsMessageActivity.class);
        startActivity(intent);
    }

    private void toSales() {
        Intent intent = new Intent(this, SalesMessageActivity.class);
        startActivity(intent);
    }

    private void toSystem() {
        Intent intent = new Intent(this, SystemMessageActivity.class);
        startActivity(intent);
    }

    private void setClick() {
        recommendsLayout.setOnClickListener(this);
        activityLayout.setOnClickListener(this);
        systemLayout.setOnClickListener(this);
        serviceLayout.setOnClickListener(this);
    }

    private void boldText() {
        WindowUtils.boldMethod(gouchaojingxuan);
        WindowUtils.boldMethod(huodongtongzhi);
        WindowUtils.boldMethod(xitongxiaoxi);
        WindowUtils.boldMethod(kefu);
        WindowUtils.boldMethod(recommendsDesc);
        WindowUtils.boldMethod(salesDesc);
        WindowUtils.boldMethod(systemDesc);
    }

    //初始化是否有未读信息标志
    private void initMessageSign() {
        boolean serviceHave = ZhichiUtils.haveUnRead(this);
        if (serviceHave) {
            serviceSign.setVisibility(View.VISIBLE);
        } else {
            serviceSign.setVisibility(View.GONE);
        }

        if (goodsUnreadCount == 0 && subjectUnreadCount == 0) {
            recommendsSign.setVisibility(View.GONE);
        } else {
            recommendsSign.setVisibility(View.VISIBLE);
        }

        if (activityUnreadCount == 0) {
            salesSign.setVisibility(View.GONE);
        } else {
            salesSign.setVisibility(View.VISIBLE);
        }

        if (sysUnreadCount == 0) {
            systemSign.setVisibility(View.GONE);
        } else {
            systemSign.setVisibility(View.VISIBLE);
        }
    }

    //初始化页面内容
    private void initPage(JSONObject jsonObject) {

        if (JsonUtils.getJsonObject(jsonObject, "sysMessage", null) != null) {
            String systemDescStr = JsonUtils.getString(jsonObject, "sysMessage.content", "");
            systemDesc.setText(systemDescStr);
            String systemDateStr = JsonUtils.getString(jsonObject, "sysMessage.createdAt", "");
            systemDate.setText(DateUtils.timeStampToStr(Long.parseLong(systemDateStr), "yyyy-MM-dd HH:mm"));
            systemDate.setVisibility(View.VISIBLE);
        }

        if (JsonUtils.getJsonObject(jsonObject, "activityMessage", null) != null) {
            String salesDescStr = JsonUtils.getString(jsonObject, "activityMessage.messageDesc", "");
            salesDesc.setText(salesDescStr);
            String salesDateStr = JsonUtils.getString(jsonObject, "activityMessage.createdAt", "");
            salesDate.setText(DateUtils.timeStampToStr(Long.parseLong(salesDateStr), "yyyy-MM-dd HH:mm"));
            salesDate.setVisibility(View.VISIBLE);
        }

        if (JsonUtils.getJsonObject(jsonObject, "goodsMessage", null) != null &&
                JsonUtils.getJsonObject(jsonObject, "subjectMessage", null) != null) {
            long goodsDate = JsonUtils.getLong(jsonObject, "goodsMessage.createdAt", 0);
            long articleDate = JsonUtils.getLong(jsonObject, "subjectMessage.createdAt", 0);
            if (goodsDate >= articleDate) {
                String recommendsDescStr = JsonUtils.getString(jsonObject, "goodsMessage.messageDesc", "");
                recommendsDesc.setText(recommendsDescStr);
                String recommendsDateStr = goodsDate + "";
                recommendsDate.setText(DateUtils.timeStampToStr(Long.parseLong(recommendsDateStr), "yyyy-MM-dd HH:mm"));
                recommendsDate.setVisibility(View.VISIBLE);
            } else {
                String recommendsDescStr = JsonUtils.getString(jsonObject, "subjectMessage.messageDesc", "");
                recommendsDesc.setText(recommendsDescStr);
                String recommendsDateStr = articleDate + "";
                recommendsDate.setText(DateUtils.timeStampToStr(Long.parseLong(recommendsDateStr), "yyyy-MM-dd HH:mm"));
                recommendsDate.setVisibility(View.VISIBLE);
            }
        }

        if (JsonUtils.getJsonObject(jsonObject, "goodsMessage", null) == null &&
                JsonUtils.getJsonObject(jsonObject, "subjectMessage", null) != null) {
            long articleDate = JsonUtils.getLong(jsonObject, "subjectMessage.createdAt", 0);
            String recommendsDescStr = JsonUtils.getString(jsonObject, "subjectMessage.messageDesc", "");
            recommendsDesc.setText(recommendsDescStr);
            String recommendsDateStr = articleDate + "";
            recommendsDate.setText(DateUtils.timeStampToStr(Long.parseLong(recommendsDateStr), "yyyy-MM-dd HH:mm"));
            recommendsDate.setVisibility(View.VISIBLE);
        }

        if (JsonUtils.getJsonObject(jsonObject, "goodsMessage", null) != null &&
                JsonUtils.getJsonObject(jsonObject, "subjectMessage", null) == null) {
            long goodsDate = JsonUtils.getLong(jsonObject, "goodsMessage.createdAt", 0);
            String recommendsDescStr = JsonUtils.getString(jsonObject, "goodsMessage.messageDesc", "");
            recommendsDesc.setText(recommendsDescStr);
            String recommendsDateStr = goodsDate + "";
            recommendsDate.setText(DateUtils.timeStampToStr(Long.parseLong(recommendsDateStr), "yyyy-MM-dd HH:mm"));
            recommendsDate.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        loadNoReadMessageCount();
        super.onResume();
    }
}
