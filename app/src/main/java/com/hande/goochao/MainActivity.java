package com.hande.goochao;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.ZhichiNotificationReceiver;
import com.hande.goochao.commons.ZhichiReceiver;
import com.hande.goochao.commons.ZhichiUtils;
import com.hande.goochao.commons.controller.BadgeView;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.CustomerServiceUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.UpgradeCheckUtils;
import com.hande.goochao.views.PermissionsActivity;
import com.hande.goochao.views.activity.CollectionActivity;
import com.hande.goochao.views.activity.InformationCenterActivity;
import com.hande.goochao.views.activity.LoginActivity;
import com.hande.goochao.views.activity.NewProductInformationActivity;
import com.hande.goochao.views.activity.SearchActivity;
import com.hande.goochao.views.activity.SettingsActivity;
import com.hande.goochao.views.activity.ShopCartActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.ClearEditText;
import com.hande.goochao.views.fragments.InspirationFragment;
import com.hande.goochao.views.fragments.NewHomeFragment;
import com.hande.goochao.views.fragments.NewShoppingMallFragment;
import com.hande.goochao.views.fragments.NewShoppingMallFragmentV2;
import com.hande.goochao.views.fragments.PersonalCenterFragment;
import com.hande.goochao.views.widget.tablayout.mFragmentTabHost;
import com.sobot.chat.api.model.ConsultingContent;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.utils.ZhiChiConstant;
import com.tencent.android.tpush.XGBasicPushNotificationBuilder;
import com.tencent.android.tpush.XGCustomPushNotificationBuilder;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.XGPushNotificationBuilder;
import com.tencent.android.tpush.common.Constants;

import org.apache.commons.lang.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Wangem on 2018/1/31.
 */
public class MainActivity extends PermissionsActivity implements TabHost.OnTabChangeListener, View.OnClickListener, ZhichiReceiver.OnMessageReceive {

    @ViewInject(android.R.id.tabhost)
    private mFragmentTabHost tabHost;
    private ImageView searchBtn;
    private TextView searchShopBtn;
    private ImageView cartBtn;
    private BadgeView cartCountView;
    private ImageView serviceBtn;
    private ImageView cartPersonBtn;
    private BadgeView cartCountPersonView;
    private View noReadCountView;
    private View rightView;
    private View rightShopMallView;
    private View rightPersonView;
    private View cartShopBtn;
    private BadgeView cartCountShopView;
    public int selectPage = 0;

    private Message m;

    private boolean haveUnRead;

    private ZhichiReceiver zhichiReceiver;
    private ZhichiNotificationReceiver zhichiNotificationReceiver;

    private String memberId = "";
    /**
     * @see SearchActivity
     */
    private int searchType = SearchActivity.SEARCH_ALL;

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    @Override
    public void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化日志
        AppLog.initLog4j();

        //开启信鸽的日志输出，线上版本不建议调用
        XGPushConfig.enableDebug(this, true);
        XGPushConfig.getToken(this);
        // 1.获取设备Token
        Handler handler = new HandlerExtension(MainActivity.this);
        m = handler.obtainMessage();
        initCustomPushNotificationBuilder(getApplicationContext());
        xgRegister();

        tabHost.setup(this, super.getSupportFragmentManager(), R.id.contentLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            tabHost.getTabWidget().setDividerDrawable(null);
        }
        tabHost.setOnTabChangedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.isStateSaved()) {
            try {
                Field field = fragmentManager.getClass().getDeclaredField("mStateSaved");
                field.setAccessible(true);
                field.set(fragmentManager, false);
                AppLog.i("设置mStateSaved=" + field.get(fragmentManager));
            } catch (Exception ex) {
                AppLog.e("err", ex);
            }
        }
        initRightView();
        setTitle("构巢");
        initTab();
        updateTab();

        //客服注册广播
        regReceiver();

        getUnReadCount();
        EventBus.getDefault().register(this);

        checkUpgrade();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        boolean toPlanList = intent.getBooleanExtra("toPlanList",false);
        if (toPlanList){
            selectPage = 0;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    EventBusNotification notification1 = new EventBusNotification(EventBusNotification.event_bus_plan_list);
                    EventBus.getDefault().post(notification1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setCurrentTab(2);
                        }
                    }, 5);
                }
            }, 0);
        }
        super.onNewIntent(intent);
    }

    private void checkUpgrade() {
        UpgradeCheckUtils.check(new UpgradeCheckUtils.UpgradeCheckListener() {
            @Override
            public void onUpgrade(boolean force, String versionName, int versionCode, String upgradeContent, final String downloadUrl) {
                String cancelText = "下次再说";
                if (force) {
                    cancelText = "关闭";
                }
                final boolean close = force;

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("发现新版本" + versionName);
                builder.setMessage(upgradeContent);
                builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        openDownloadForBrowse(downloadUrl);
                        if (close) {
                            android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
                builder.setCancelable(false);
                builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (close) {
                            android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();

//                ConfirmDialog alertDialog = new ConfirmDialog(MainActivity.this, ConfirmDialog.ConfirmDialogType.ConfirmDialogType_Warning);
//                alertDialog.setMsg("发现新版本" + versionName)
//                        .setLeftButtonText(cancelText)
//                        .setRightButtonText("立即更新")
//                        .setCallBack(new ConfirmDialog.CallBack() {
//                            @Override
//                            public void buttonClick(Dialog dialog, boolean leftClick) {
//                                if (!leftClick) {
//                                    openDownloadForBrowse(downloadUrl);
//                                    if (close) {
//                                        android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
//                                    } else {
//                                        dialog.dismiss();
//                                    }
//                                }else {
//                                    if (close) {
//                                        android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
//                                    } else {
//                                        dialog.dismiss();
//                                    }
//                                }
//                            }
//                        });
//                alertDialog.show();

            }

            @Override
            public void onCheckFail(Throwable ex) {
                AppLog.e("检查更新失败", ex);
            }

            @Override
            public void onLastVersion() {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void openDownloadForBrowse(String downloadUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
        startActivity(intent);
    }

    private void initRightView() {
        rightView = LayoutInflater.from(this).inflate(R.layout.layout_home_right, null);
        rightView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        searchBtn = rightView.findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(this);
        cartBtn = rightView.findViewById(R.id.cart_btn);
        cartBtn.setOnClickListener(this);
        cartCountView = rightView.findViewById(R.id.cart_count_view);
        cartCountView.hide();

        rightPersonView = LayoutInflater.from(this).inflate(R.layout.layout_home_right_person, null);
        rightPersonView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        serviceBtn = rightPersonView.findViewById(R.id.service_btn);
        serviceBtn.setOnClickListener(this);
        cartPersonBtn = rightPersonView.findViewById(R.id.cart_btn);
        cartPersonBtn.setOnClickListener(this);
        cartCountPersonView = rightPersonView.findViewById(R.id.cart_count_view);
        noReadCountView = rightPersonView.findViewById(R.id.noRead_count_view);
        cartCountPersonView.hide();

        rightShopMallView = LayoutInflater.from(this).inflate(R.layout.layout_shopmall_right,null);
        rightShopMallView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        searchShopBtn = rightShopMallView.findViewById(R.id.search_edit_shop_right);
        searchShopBtn.setOnClickListener(this);
        cartShopBtn = rightShopMallView.findViewById(R.id.cart_btn_shop_right);
        cartShopBtn.setOnClickListener(this);
        cartCountShopView = rightShopMallView.findViewById(R.id.cart_count_shop_view);
        cartCountShopView.hide();
    }

    @Override
    protected void doResume() {
        // 是否是最后一个选项卡
        if (tabHost.getCurrentTab() == tabHost.getTabWidget().getChildCount() - 1) {
            getUnReadCount();
        }

        saveLastLogTime(); // 记录最后一次登陆使用
        showHead();
        getShoppingCartCount();
        updateTab();
    }

    private void showHead() {
        if (!AppSessionCache.getInstance().isLogin(this)) {
            showHeadBtn(R.mipmap.nav_profilepic, true);
            return;
        }
        JSONObject userInfo = AppSessionCache.getInstance().getLoginResult(this);
        String head = JsonUtils.getString(userInfo, "head", null);
        if (TextUtils.isEmpty(head)) {
            showHeadBtn(R.mipmap.nav_profilepic, true);
        } else {
            showHeadBtn(head, true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    private void initTab() {
        final String[] tabs = TabDb.tabsTxt;
        for (int i = 0; i < tabs.length; i++) {
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(tabs[i]).setIndicator(getTabView(i));
            tabHost.addTab(tabSpec, TabDb.fragments[i], null);
            tabHost.setTag(i);
        }
        tabHost.setCurrentTab(0);

        tabHost.getTabWidget().getChildAt(tabs.length - 1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppSessionCache.getInstance().getLoginResult(MainActivity.this) == null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    tabHost.setCurrentTab(tabs.length - 1);
                }
            }
        });
    }

    public void setCurrentTab(int index) {
        tabHost.setCurrentTab(index);
    }

    private void updateTab() {
        TabWidget tabWidget = tabHost.getTabWidget();
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
            View view = tabWidget.getChildAt(i);
            TextView iv = view.findViewById(R.id.tab_txt);
            ImageView ic = view.findViewById(R.id.imgIcon);
            if (i == tabHost.getCurrentTab()) {
                iv.setTextColor(getResources().getColor(R.color.BLACK));
                ic.setImageResource(TabDb.selectIds[i]);
            } else {
                iv.setTextColor(getResources().getColor(R.color.Default_Gray));
                ic.setImageResource(TabDb.tabsIcons[i]);
            }
        }

        if (tabHost.getCurrentTab() == tabWidget.getChildCount() - 1) {
            showPersonRight();
        } else if (tabHost.getCurrentTab() == 0) {
            showNormalRight();
        }
        if (tabHost.getCurrentTab() == 1) {
            showShoppingMall();
        }
    }

    @Override
    public void onTabChanged(String str) {
        updateTab();
    }

    private View getTabView(int idx) {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_tab, null);

        TextView txtView = view.findViewById(R.id.tab_txt);
        txtView.setText(TabDb.tabsTxt[idx]);

        ImageView iconView = view.findViewById(R.id.imgIcon);
        iconView.setImageResource(TabDb.tabsIcons[idx]);

        return view;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    @Override
    public void onClick(View v) {
        if (v == searchBtn || v == searchShopBtn) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("searchType", this.searchType);
            startActivity(intent);
        } else if (v == cartBtn || v == cartPersonBtn || v == cartShopBtn) {
            JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(this);
            String accessToken = JsonUtils.getString(loginResult, "accessToken", null);
            if (StringUtils.isEmpty(accessToken)) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ShopCartActivity.class);
                startActivity(intent);
            }
        } else if (v == serviceBtn) {
            Intent intent = new Intent(this, InformationCenterActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onHeadClickListener() {
        if (AppSessionCache.getInstance().isLogin(this)) {
            Intent intent = new Intent(this, CollectionActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void showNormalRight() {
        hideLeftBtn();
        showHead();
        hideRightFullView();
        showRightCustomView(rightView);
        showLine();
    }

    private void showPersonRight() {
        hideHeadBtn();
        hideRightFullView();
        showLeftBtn(R.drawable.setting_icon_press, false);
        showRightCustomView(rightPersonView);
        hideLine();
    }

    private void showShoppingMall() {
        hideLeftBtn();
        hideHeadBtn();
        hideLine();
        hideTitle();
        hideRightCustomView();
        showRightFullView(rightShopMallView);
    }

    @Override
    protected void onLeftClickListener() {
        super.onLeftClickListener();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_logout)) {
            XGPushManager.delAccount(this, memberId);
            Log.w(Constants.LogTag, "+++ 退出登陆成功. account:" + memberId);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    setCurrentTab(0);
                }
            }, 500);

        } else if (notification.getKey().equals(EventBusNotification.event_bus_re_login)) {
            xgRegister();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LoginActivity.go(MainActivity.this);
                }
            });
        } else if (notification.getKey().equals(EventBusNotification.event_bus_login_success)) {
            xgRegister();
        } else if (notification.getKey().equals(EventBusNotification.event_bus_server_reject)) {
            String message = (String) notification.getValue();
            AlertManager.showErrorToast(this, message, false);
        } else if (notification.getKey().equals(EventBusNotification.event_bus_to_inspiration)) {
            selectPage = 1;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    EventBusNotification notification1 = new EventBusNotification(EventBusNotification.event_bus_change_space);
                    EventBus.getDefault().post(notification1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setCurrentTab(2);
                        }
                    }, 5);
                }
            }, 0);
        } else if (notification.getKey().equals(EventBusNotification.event_bus_to_scene)) {
            selectPage = 2;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    EventBusNotification notification1 = new EventBusNotification(EventBusNotification.event_bus_change_scene);
                    EventBus.getDefault().post(notification1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setCurrentTab(2);
                        }
                    }, 5);
                }
            }, 0);
        } else if (notification.getKey().equals(EventBusNotification.event_bus_to_plan)) {
            selectPage = 0;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    EventBusNotification notification1 = new EventBusNotification(EventBusNotification.event_bus_change_plan);
                    EventBus.getDefault().post(notification1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setCurrentTab(2);
                        }
                    }, 5);
                }
            }, 0);
        } else if (notification.getKey().equals(EventBusNotification.event_bus_person_center_onResume)) {
            getUnReadCount();
        }
    }

    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        x.image().clearMemCache();
        AppLog.i("mem 清理图片内存空间");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        x.image().clearMemCache();
        AppLog.i("mem low 清理图片内存空间");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getShoppingCartCount() {
        if (!AppSessionCache.getInstance().isLogin(this)) {
            return;
        }
        HttpRequest.get(AppConfig.SHOPPING_CAT_COUNT, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    int count = JsonUtils.getInt(response, "data", 0);
                    cartCountView.setText(count + "");
                    cartCountPersonView.setText(count + "");
                    cartCountShopView.setText(count + "");
                    if (count == 0) {
                        cartCountView.setVisibility(View.GONE);
                        cartCountPersonView.setVisibility(View.GONE);
                        cartCountShopView.setVisibility(View.GONE);
                    } else {
                        cartCountView.setVisibility(View.VISIBLE);
                        cartCountPersonView.setVisibility(View.VISIBLE);
                        cartCountShopView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
            }
        });
    }

    private static class HandlerExtension extends Handler {
        WeakReference<MainActivity> mActivity;

        HandlerExtension(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity theActivity = mActivity.get();
            if (theActivity == null) {
                theActivity = new MainActivity();
            }
            if (msg != null) {
                Log.d("TPush", msg.obj.toString());
            }
            // XGPushManager.registerCustomNotification(theActivity,
            // "BACKSTREET", "BOYS", System.currentTimeMillis() + 5000, 0);
        }
    }

    static class TabDb {
        private static final String[] tabsTxt = {"构巢", "商城", "发现", "我的"};
        private static final Integer[] tabsIcons = {R.mipmap.tab_home_normal, R.mipmap.tab_market_normal, R.mipmap.tab_photo_normal, R.mipmap.tab_me_normal};
        private static final Integer[] selectIds = {R.mipmap.tab_home_focus, R.mipmap.tab_market_focus, R.mipmap.tab_photo_focus, R.mipmap.tab_me_focus};
        private static final Class[] fragments = {NewHomeFragment.class, NewShoppingMallFragment.class, InspirationFragment.class, PersonalCenterFragment.class};
    }

    //腾讯信鸽注册
    private void xgRegister() {
        //判断是否登陆
        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(this);
        if (loginResult == null) {

            //注册小米appId，appKey
            XGPushConfig.setMiPushAppId(getApplicationContext(), "2882303761517756164");
            XGPushConfig.setMiPushAppKey(getApplicationContext(), "5791775654164");
            //打开第三方推送
            XGPushConfig.enableOtherPush(getApplicationContext(), true);
            /**
             *注册信鸽服务的接口
             *如果仅仅需要发推送消息调用这段代码即可
             */
            XGPushManager.registerPush(getApplicationContext(),
                    new XGIOperateCallback() {
                        @Override
                        public void onSuccess(Object data, int flag) {
                            Log.w(Constants.LogTag, "+++ register push sucess. token:" + data + ".flag" + flag);

                            m.obj = "+++ register push sucess. token:" + data;
                            m.sendToTarget();
                        }

                        @Override
                        public void onFail(Object data, int errCode, String msg) {
                            Log.w(Constants.LogTag,
                                    "+++ register push fail. token:" + data
                                            + ", errCode:" + errCode + ",msg:"
                                            + msg);
                            m.obj = "+++ register push fail. token:" + data
                                    + ", errCode:" + errCode + ",msg:" + msg;
                            m.sendToTarget();
                        }
                    });
        } else {
            memberId = JsonUtils.getString(loginResult, "memberId", "");
            XGPushManager.registerPush(getApplicationContext(), memberId, new XGIOperateCallback() {
                @Override
                public void onSuccess(Object o, int i) {
                    Log.w(Constants.LogTag, "+++ register push sucess. token:" + o + ".account:" + memberId + ".flag" + i);

                    m.obj = "+++ register push sucess. token:" + o + ".account:" + memberId;
                    m.sendToTarget();
                }

                @Override
                public void onFail(Object o, int i, String s) {
                    Log.w(Constants.LogTag,
                            "+++ register push fail. token:" + o
                                    + ".account:" + memberId + ", errCode:" + i + ",msg:"
                                    + s);
                    m.obj = "+++ register push fail. token:" + o
                            + ".account:" + memberId + ", errCode:" + i + ",msg:" + s;
                    m.sendToTarget();
                }
            });

        }

        // 获取token
        String token = XGPushConfig.getToken(this);
        pushToken(token);
    }

    private void getUnReadCount() {

        if (!AppSessionCache.getInstance().isLogin(this)) {
            return;
        }

        HttpRequest.get(AppConfig.UNREAD_COUNT, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject jsonObject = JsonUtils.getJsonObject(response, "data", null);
                    int sysUnreadCount = JsonUtils.getInt(jsonObject, "sysUnreadCount", 0);
                    int goodsUnreadCount = JsonUtils.getInt(jsonObject, "goodsUnreadCount", 0);
                    int subjectUnreadCount = JsonUtils.getInt(jsonObject, "subjectUnreadCount", 0);
                    int activityUnreadCount = JsonUtils.getInt(jsonObject, "activityUnreadCount", 0);

                    if (sysUnreadCount == 0 && goodsUnreadCount == 0 && subjectUnreadCount == 0 && activityUnreadCount == 0) {
                        haveUnRead = false;
                    } else {
                        haveUnRead = true;
                    }
                    haveUnReadMessage();
                }
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onComplete(boolean success, JSONObject response) {

            }
        });
    }

    private void haveUnReadMessage() {

        boolean serviceHaveUnRead = ZhichiUtils.haveUnRead(this);
        if (serviceHaveUnRead || haveUnRead) {
            noReadCountView.setVisibility(View.VISIBLE);
        } else {
            noReadCountView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onReceive(String content, int noReadNum) {
        getUnReadCount();
    }

    @Override
    protected void doDestroy() {
        try {
            if (zhichiReceiver != null) {
                unregisterReceiver(zhichiReceiver);
            }
            if (zhichiNotificationReceiver != null) {
                unregisterReceiver(zhichiNotificationReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void regReceiver() {
        IntentFilter filter = new IntentFilter();
        if (zhichiNotificationReceiver == null) {
            zhichiNotificationReceiver = new ZhichiNotificationReceiver();
        }
        filter.addAction(ZhiChiConstant.SOBOT_NOTIFICATION_CLICK);
        registerReceiver(zhichiNotificationReceiver, filter);

        if (zhichiReceiver == null) {
            zhichiReceiver = new ZhichiReceiver(this);
        }
        filter.addAction(ZhiChiConstant.sobot_unreadCountBrocast);
        registerReceiver(zhichiReceiver, filter);
    }

    /**
     * 设置通知自定义View，这样在下发通知时可以指定build_id。编号由开发者自己维护,build_id=0为默认设置
     *
     * @param context
     */
    private void initCustomPushNotificationBuilder(Context context) {
        XGBasicPushNotificationBuilder builder = new XGBasicPushNotificationBuilder() {
            @Override
            protected Notification a(Context context) {
                return super.a(context);
            }

            @Override
            protected void b(JSONObject jsonObject) {
                super.b(jsonObject);
            }

            @Override
            public Notification buildNotification(Context context) {
                return super.buildNotification(context);
            }
        };
        builder.setNotificationLargeIcon(0);
        builder.setLargeIcon(null);
        XGPushManager.setPushNotificationBuilder(this, 1, builder);
        XGPushManager.setDefaultNotificationBuilder(this, builder);
    }

    /**
     * 注册信鸽时，回传token
     *
     * @param token
     */
    private void pushToken(String token) {

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("type", "1");
        HttpRequest.head(AppConfig.TOKEN_PUSH, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
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

    private void saveLastLogTime() {

        if (!AppSessionCache.getInstance().isLogin(this)) {
            return;
        }

        HttpRequest.head(AppConfig.SAVE_LAST_LOG_TIME, null, null, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject response) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

        });
    }
}

