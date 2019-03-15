package com.hande.goochao.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.views.components.AlertManager;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Wangenmao on 2018/3/9.
 */

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry);

        //注意：
        //第三方开发者如果使用透明界面来实现WXEntryActivity，需要判断handleIntent的返回值
        //如果返回值为false，则说明入参不合法未被SDK处理，应finish当前透明界面，避免外部通过传递非法参数的Intent导致停留在透明界面，引起用户的疑惑
        api = WXAPIFactory.createWXAPI(this, AppConfig.WX_APPID,true);
        try {
            boolean success = api.handleIntent(getIntent(), this);
            if (!success) {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
        AppLog.i("onReq......");
        finish();
    }

    @Override
    public void onResp(BaseResp resp) {
        AppLog.i("onResp......");

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (resp instanceof SendAuth.Resp) {  //微信登录授权
                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_wx_auth_success,resp);
                    EventBus.getDefault().post(notification);
                }else {
                    AlertManager.showSuccessToast(WXEntryActivity.this, "分享收藏成功", false);
                }
                finish();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                AlertManager.toast(this,"当前操作被取消");
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                AlertManager.toast(this,"当前操作被拒绝");
                finish();
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                AlertManager.toast(this,"不支持当前操作");
                finish();
                break;
            default:
                AlertManager.toast(this,"未知操作");
                finish();
                break;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }
}
