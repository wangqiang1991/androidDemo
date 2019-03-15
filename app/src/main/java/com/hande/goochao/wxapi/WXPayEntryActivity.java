package com.hande.goochao.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hande.goochao.R;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.views.activity.PaySuccessActivity;
import com.hande.goochao.views.components.AlertManager;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    
    private IWXAPI api;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        
        api = WXAPIFactory.createWXAPI(this, AppConfig.WX_APPID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp resp) {
        AppLog.i("onPayFinish, errCode = " + resp.errCode);
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    AlertManager.showSuccessToast(this, "支付成功", false);
                    Intent intent = new Intent(WXPayEntryActivity.this, PaySuccessActivity.class);
                    startActivity(intent);
                    AppLog.e("支付成功");
                    break;
                case BaseResp.ErrCode.ERR_COMM:
                    AlertManager.showErrorToast(this, "支付失败", false);
                    AppLog.e("支付失败");
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    AlertManager.showErrorToast(this, "支付被取消", false);
                    AppLog.e("支付被取消");
                    break;
            }
        }

        finish();
    }
}