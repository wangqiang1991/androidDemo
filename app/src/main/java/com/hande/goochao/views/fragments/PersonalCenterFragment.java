package com.hande.goochao.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.controller.BadgeView;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.AddressActivity;
import com.hande.goochao.views.activity.AfterSaleActivity;
import com.hande.goochao.views.activity.CollectionActivity;
import com.hande.goochao.views.activity.HelpCenterActivity;
import com.hande.goochao.views.activity.MyOrderActivity;
import com.hande.goochao.views.activity.CouponsActivity;
import com.hande.goochao.views.activity.LikesActivity;
import com.hande.goochao.views.activity.OrderCommentsActivity;
import com.hande.goochao.views.activity.PersonPlanActivity;
import com.hande.goochao.views.activity.PersonalInformationActivity;
import com.hande.goochao.views.activity.SaleGiftActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CircleImageView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by Wangem on 2018/2/8.
 */

@ContentView(R.layout.fragment_personal_center)
public class PersonalCenterFragment extends BaseFragment implements View.OnClickListener {

    @ViewInject(R.id.headImg)
    private CircleImageView headImg;
    @ViewInject(R.id.txtName)
    private TextView txtName;
    @ViewInject(R.id.layAddress)
    private View layAddress;
    @ViewInject(R.id.layHelp)
    private View layHelp;
    @ViewInject(R.id.all_order)
    private TextView allOrder;
    @ViewInject(R.id.layWaitPayment)
    private View layWaitPayment;
    @ViewInject(R.id.layWaitDeliver)
    private View layWaitDeliver;
    @ViewInject(R.id.layWaitReceive)
    private View layWaitReceive;
    @ViewInject(R.id.layWaitComment)
    private View layWaitComment;
    @ViewInject(R.id.layAfterSale)
    private View layAfterSale;
    @ViewInject(R.id.layCollection)
    private View layCollection;
    @ViewInject(R.id.lay_plan)
    private View layPlan;

    @ViewInject(R.id.pendingPayCount)
    private BadgeView pendingPayCount;
    @ViewInject(R.id.pendingDeliverCount)
    private BadgeView pendingDeliverCount;
    @ViewInject(R.id.pendingReceiveCount)
    private BadgeView pendingReceiveCount;
    @ViewInject(R.id.pendingCommentCount)
    private BadgeView pendingCommentCount;
    @ViewInject(R.id.pendingAfterSaleCount)
    private BadgeView pendingAfterSaleCount;
    @ViewInject(R.id.layShare)
    private View layShare;


    private boolean created = false;
    @ViewInject(R.id.layCoupons)
    private View layCoupons;
    @ViewInject(R.id.layLike)
    private View layLike;

    private JSONObject userData;
    private String memberId;

    private static final int FEEDBACK_CODE = 1;

    private GlideRequests glide;

    //分享地址
    private String shareUrl;

    //textView
    @ViewInject(R.id.plan_txt_person)
    private TextView planTxt;
    @ViewInject(R.id.coupon_txt)
    private TextView couponTxt;
    @ViewInject(R.id.like_txt)
    private TextView likeTxt;
    @ViewInject(R.id.collection_txt)
    private TextView collectionTxt;
    @ViewInject(R.id.address_txt)
    private TextView addressTxt;
    @ViewInject(R.id.share_txt)
    private TextView shareTxt;
    @ViewInject(R.id.help_txt)
    private TextView helpTxt;
    @ViewInject(R.id.daiFuKuan)
    private TextView daiFuKuanTxt;
    @ViewInject(R.id.daiFaHuo)
    private TextView daiFaHuoTxt;
    @ViewInject(R.id.daiShouHuo)
    private TextView daiShouHuoTxt;
    @ViewInject(R.id.daiPinJia)
    private TextView daiPinJiaTxt;
    @ViewInject(R.id.tuiKuanShouHou)
    private TextView tuiKuanShouHouTxt;



//    @Override
//    public void onResume() {
//        super.onResume();
//        EventBusNotification notification1 = new EventBusNotification(EventBusNotification.event_bus_person_center_onResume);
//        EventBus.getDefault().post(notification1);
//        if (AppSessionCache.getInstance().getLoginResult(getContext()) != null) {
//            bindUserInfo();
//            getOrderCount();
//        }
//    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((ToolBarActivity) getActivity()).showToolBar();
            ((ToolBarActivity) getActivity()).showTitle();
            getActivity().setTitle("我的构巢");
            EventBusNotification notification1 = new EventBusNotification(EventBusNotification.event_bus_person_center_onResume);
            EventBus.getDefault().post(notification1);
            if (AppSessionCache.getInstance().getLoginResult(getContext()) != null) {
                bindUserInfo();
                getOrderCount();
            }
        }
    }

    private void bindUserInfo() {
        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(getActivity());
        memberId = JsonUtils.getString(loginResult,"memberId","");

        //读取保存至登陆信息
        loadUserInfo(memberId);

        //从登陆信息中读取昵称，头像
        String nickName = JsonUtils.getString(loginResult,"nickName","");
        txtName.setText(nickName);
        String head = JsonUtils.getString(loginResult,"head","");
        if (!TextUtils.isEmpty(head)) {
            ImageUtils.loadImage(glide, ImageUtils.resize(head,320,320), headImg, R.mipmap.loadpicture);
        } else {
            headImg.setImageResource(R.mipmap.me_profilepic);
        }

    }


    private void getOrderCount() {
        HttpRequest.get(AppConfig.Order_Count, null, null, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    JSONObject countObject = JsonUtils.getJsonObject(response,"data",null);
                    if (countObject != null) {
                        int payCount = JsonUtils.getInt(countObject, "pendingPayment" ,0);
                        if (payCount > 0) {
                            pendingPayCount.setVisibility(View.VISIBLE);
                            pendingPayCount.setText(payCount + "");
                        } else {
                            pendingPayCount.setVisibility(View.GONE);
                        }
                        int deliverCount = JsonUtils.getInt(countObject, "pendingDeliver", 0);
                        if (deliverCount > 0) {
                            pendingDeliverCount.setVisibility(View.VISIBLE);
                            pendingDeliverCount.setText(deliverCount + "");
                        } else {
                            pendingDeliverCount.setVisibility(View.GONE);
                        }

                        int receiveCount = JsonUtils.getInt(countObject, "pendingReceived", 0);
                        if (receiveCount > 0) {
                            pendingReceiveCount.setVisibility(View.VISIBLE);
                            pendingReceiveCount.setText(receiveCount + "");
                        } else {
                            pendingReceiveCount.setVisibility(View.GONE);
                        }

                        int commentCount = JsonUtils.getInt(countObject, "pendingComments", 0);
                        if (commentCount > 0) {
                            pendingCommentCount.setVisibility(View.VISIBLE);
                            pendingCommentCount.setText(commentCount + "");
                        } else {
                            pendingCommentCount.setVisibility(View.GONE);
                        }

                        int saleService = JsonUtils.getInt(countObject, "saleService", 0);
                        if (saleService > 0) {
                            pendingAfterSaleCount.setVisibility(View.VISIBLE);
                            pendingAfterSaleCount.setText(saleService + "");
                        } else {
                            pendingAfterSaleCount.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable ex) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getActivity().setTitle("我的构巢");
        ((ToolBarActivity) getActivity()).showToolBar();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadActivityUrl();

        layAddress.setOnClickListener(this);
        layHelp.setOnClickListener(this);
        headImg.setOnClickListener(this);
        layCoupons.setOnClickListener(this);
        layLike.setOnClickListener(this);
        layCollection.setOnClickListener(this);
        layPlan.setOnClickListener(this);

        if (!created) {
            glide = GlideApp.with(getActivity());
            created = true;
            layAddress.setOnClickListener(this);
            layHelp.setOnClickListener(this);
            headImg.setOnClickListener(this);
            layCoupons.setOnClickListener(this);
            layLike.setOnClickListener(this);
            layCollection.setOnClickListener(this);

            allOrder.setOnClickListener(this);
            layWaitPayment.setOnClickListener(this);
            layWaitDeliver.setOnClickListener(this);
            layWaitReceive.setOnClickListener(this);
            layWaitComment.setOnClickListener(this);
            layAfterSale.setOnClickListener(this);
            layShare.setOnClickListener(this);


            pendingPayCount.setVisibility(View.GONE);
            pendingDeliverCount.setVisibility(View.GONE);
            pendingReceiveCount.setVisibility(View.GONE);
            pendingCommentCount.setVisibility(View.GONE);
            pendingAfterSaleCount.setVisibility(View.GONE);

            if (AppSessionCache.getInstance().getLoginResult(getContext()) != null) {
                bindUserInfo();
                getOrderCount();
            }
            textInit();
        }
    }

    private void textInit(){
        planTxt.setText("我领取" + "&" + "关注的软装方案");
        WindowUtils.boldMethod(planTxt);

        WindowUtils.boldMethod(couponTxt);
        WindowUtils.boldMethod(likeTxt);
        WindowUtils.boldMethod(collectionTxt);
        WindowUtils.boldMethod(addressTxt);
        WindowUtils.boldMethod(shareTxt);
        WindowUtils.boldMethod(helpTxt);
        WindowUtils.boldMethod(daiFuKuanTxt);
        WindowUtils.boldMethod(daiFaHuoTxt);
        WindowUtils.boldMethod(daiShouHuoTxt);
        WindowUtils.boldMethod(daiPinJiaTxt);
        WindowUtils.boldMethod(tuiKuanShouHouTxt);
        WindowUtils.boldMethod(allOrder);
    }

    private void gotoOrderView(View targetView) {
        Intent intent = new Intent(getActivity(), MyOrderActivity.class);
        if (targetView == allOrder) {
            intent.putExtra("selectedIndex", 0);
        } else if (targetView == layWaitPayment) {
            intent.putExtra("selectedIndex", 1);
        } else if (targetView == layWaitDeliver) {
            intent.putExtra("selectedIndex", 2);
        } else if (targetView == layWaitReceive) {
            intent.putExtra("selectedIndex", 3);
        }
        startActivity(intent);
    }

    private void gotoOrderCommentsActivity() {
        Intent intent = new Intent(getActivity(), OrderCommentsActivity.class);
        startActivity(intent);
    }

    private void gotoAfterSaleActivity() {
        Intent intent = new Intent(getActivity(), AfterSaleActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v == layAddress) {
            Intent intent = new Intent(getActivity(), AddressActivity.class);
            startActivity(intent);
        } else if (v == layHelp) {
//            Intent intent = new Intent(getActivity(), HelpCenterActivity.class);
//            startActivity(intent);
            String helpUrl = AppConfig.HELP_CENTER_URL;
            Intent intent = new Intent(getContext(), SaleGiftActivity.class);
            intent.putExtra("url",helpUrl);
            startActivity(intent);
        } else if (v == headImg) {
            Intent intent = new Intent(getActivity(), PersonalInformationActivity.class);
            startActivity(intent);
        } else if (v == layLike){
            Intent intent = new Intent(getActivity(), LikesActivity.class);
            startActivityForResult(intent,FEEDBACK_CODE);
        } else if (v == layCoupons){
            Intent intent = new Intent(getActivity(), CouponsActivity.class);
            startActivity(intent);
        } else if (v == allOrder || v == layWaitPayment || v == layWaitDeliver || v == layWaitReceive) {
            gotoOrderView(v);
        } else if (v == layCollection){
            Intent intent = new Intent(getActivity(), CollectionActivity.class);
            startActivity(intent);
        } else if (v == layWaitComment) {
            gotoOrderCommentsActivity();
        } else if (v == layAfterSale) {
            gotoAfterSaleActivity();
        } else if (v == layShare) {
            onShare();
        }else if (v == layPlan) {
            Intent intent = new Intent(getActivity(), PersonPlanActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 首页获取活动相关地址
     */
    private void loadActivityUrl() {

        HttpRequest.get(AppConfig.GET_ACTIVITY, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject data = JsonUtils.getJsonObject(response, "data", null);
                    shareUrl = JsonUtils.getString(data,"shareUrl" ,"");
                }
            }

            @Override
            public void onError(Throwable ex) {

            }
        });
    }

    private void onShare() {
        Intent intent = new Intent(getContext(), SaleGiftActivity.class);
        intent.putExtra("url",shareUrl);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FEEDBACK_CODE && resultCode == getActivity().RESULT_OK) {
            AlertManager.showSuccessToast(getActivity(),"提交成功", false);
        }
    }

    private void loadUserInfo(String memberId) {

        HttpRequest.get(AppConfig.GET_USER_INFORMATION + memberId, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    userData = JsonUtils.getJsonObject(response, "data", null);
                    JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(getActivity());
                    try {
                        loginResult.put("nickName",JsonUtils.getString(userData,"nickName",""));
                        loginResult.put("head",JsonUtils.getString(userData,"head",""));
                        loginResult.put("birthday",JsonUtils.getString(userData,"birthday",""));
                        loginResult.put("gender",JsonUtils.getString(userData,"gender",""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppSessionCache.getInstance().setLoginResult(loginResult,getActivity());
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
            }
        });
    }

}
