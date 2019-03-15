package com.hande.goochao.views.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.MD5Utils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;

import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LMC on 2018/3/23.
 */

public class ModifyPasswordActivity extends ToolBarActivity {

    @ViewInject(R.id.old_password)
    private EditText oldPassWordEdit;
    @ViewInject(R.id.new_password)
    private EditText newPassWordEdit;
    @ViewInject(R.id.save_button)
    private Button saveBt;

    private String phone;

    private String editOldWord;
    private String editNewWord;
    private int editOldWordNum;
    private int editNewWordNum;

    //防止多次点击
    private boolean click = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);
        setTitle("修改密码");

        oldPassWordEdit.setBackgroundResource(R.color.WHITE);
        newPassWordEdit.setBackgroundResource(R.color.WHITE);

        /**
         * 获取用户旧密码以及登录、注册手机号
         */
        phone = JsonUtils.getString(AppSessionCache.getInstance().getLoginResult(this),"phone","");

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( click ){
                    return;
                }else {
                    /**
                     * 用户输入新、旧密码字符串，输入字符个数
                     */
                    editOldWord = oldPassWordEdit.getText().toString();
                    editNewWord = newPassWordEdit.getText().toString();
                    editOldWordNum = oldPassWordEdit.length();
                    editNewWordNum = newPassWordEdit.length();

                    if (TextUtils.isEmpty(editOldWord)) {
                        AlertManager.showErrorToast(ModifyPasswordActivity.this, "旧密码不能为空", false);
                        return;
                    }
                    if (editNewWordNum > 20){
                        AlertManager.showErrorToast(ModifyPasswordActivity.this, "密码最大长度20位", false);
                        return;
                    }
                    if (6 > editOldWordNum || 6 > editNewWordNum) {
                        AlertManager.showErrorToast(ModifyPasswordActivity.this, "密码至少6位", false);
                        return;
                    }
                    if (editNewWord.equals(editOldWord)) {
                        AlertManager.showErrorToast(ModifyPasswordActivity.this, "新旧密码不能相同", false);
                        return;
                    }
                    else {
                        saveWord();
                    }
                }
            }
        });
    }

    private void saveWord(){
        Map<String,String> params = new HashMap<>();
        params.put("phone" , phone);
        params.put("newPassword" , MD5Utils.md5Password(editNewWord));
        params.put("oldPassword" , MD5Utils.md5Password(editOldWord));

        click = true;

        HttpRequest.post(AppConfig.MODIFY_PASSWORD , null, params , JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if(JsonUtils.getCode(response) == 0){
                    click = true;
                    AlertManager.showSuccessToast(ModifyPasswordActivity.this , "修改密码成功", false);
                    JSONObject loginResult = JsonUtils.getJsonObject(response, "data", null);
                    AppSessionCache.getInstance().setLoginResult(loginResult, ModifyPasswordActivity.this);
                    finish();
                }else {
                    click = false;
                    AlertManager.showErrorToast(ModifyPasswordActivity.this, "旧密码错误", false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                click = false;
                AppLog.e("err", ex);
                AlertManager.showErrorToast(ModifyPasswordActivity.this, "服务器繁忙", false);
            }
        });
    }
}
