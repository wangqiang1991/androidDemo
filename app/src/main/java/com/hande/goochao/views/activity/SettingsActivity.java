package com.hande.goochao.views.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hande.goochao.BuildConfig;
import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ContextUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.UpgradeCheckUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.ArticleWebView;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.WebViewActivity;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;

/**
 * Created by Wangenmao on 2018/3/13.
 */

public class SettingsActivity extends ToolBarActivity implements View.OnClickListener {

    @ViewInject(R.id.layFeedBack)
    private View layFeedBack;
    @ViewInject(R.id.layClearCache)
    private View layClearCache;
    @ViewInject(R.id.layProtocol)
    private View layProtocol;
    @ViewInject(R.id.layAboutUs)
    private View layAboutUs;
    @ViewInject(R.id.layLogout)
    private View layLogout;

    @ViewInject(R.id.modify_password)
    private View layModify;

    @ViewInject(R.id.version_text)
    private TextView versionText;
    @ViewInject(R.id.check_version_btn)
    private View checkVersionBtn;
    @ViewInject(R.id.login_view)
    private TextView login;

    private String phone;

    private static final int FEEDBACK_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settins);
        setTitle("设置");
        showLine(R.color.Line_DarkGray_Color);
        WindowUtils.boldMethod(login);

        /**
         * 检验是否可以获取到用户注册手机号
         */
        phone = JsonUtils.getString(AppSessionCache.getInstance().getLoginResult(this),"phone","");
        if ( phone.equals("")){
            layModify.setVisibility(View.GONE);
        }

        layFeedBack.setOnClickListener(this);
        layClearCache.setOnClickListener(this);
        layProtocol.setOnClickListener(this);
        layAboutUs.setOnClickListener(this);
        layLogout.setOnClickListener(this);
        layModify.setOnClickListener(this);
        checkVersionBtn.setOnClickListener(this);

        versionText.setText("版本：" + BuildConfig.VERSION_NAME);
    }


    @Override
    public void onClick(View v) {
        if (v == layFeedBack) {
            gotoFeedBack();
        } else if (v == layClearCache) {
            clearCache();
        } else if (v == layProtocol) {
            gotoUserProtocol();
        } else if (v == layAboutUs) {
            gotoAboutUs();
        } else if (v == layLogout) {
            logout();
        } else if (v == layModify) {
            Intent intent = new Intent(this,ModifyPasswordActivity.class);
            startActivity(intent);
        } else if (v == checkVersionBtn) {
            checkUpgrade();
        }
    }

    private void checkUpgrade() {
        UpgradeCheckUtils.check(new UpgradeCheckUtils.UpgradeCheckListener() {
            @Override
            public void onUpgrade(boolean force, String versionName, int versionCode, String upgradeContent, final String downloadUrl) {
                String cancelText = "下次再说";
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("发现新版本" + versionName);
                builder.setMessage(upgradeContent);
                builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        openDownloadForBrowse(downloadUrl);
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.setNegativeButton(cancelText, null);
                builder.show();
            }

            @Override
            public void onCheckFail(Throwable ex) {
                AlertManager.showErrorToast(SettingsActivity.this, "检查新版本失败", false);
                AppLog.e("检查更新失败", ex);
            }

            @Override
            public void onLastVersion() {
                AlertManager.toast(SettingsActivity.this, "已经是最新版本");
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

    private void clearCache() {

        CustomLoadingDialog loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("缓存清理中");
        loadingDialog.show();


        // 清空xUtils图片缓存
        x.image().clearCacheFiles();

        // 清空AppSessionCache缓存
        File cache = new File(ContextUtils.getCacheDir(AppConfig.CACHE_DIR));
        if (cache.exists()) {
            delete(cache);
        }

        loadingDialog.dismiss();
        AlertManager.showSuccessToast(this, "缓存清理成功", false);
    }

    private void gotoUserProtocol() {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("title", "用户协议");
        intent.putExtra("url", AppConfig.User_Protocol_Url);
        startActivity(intent);
    }

    private void gotoAboutUs() {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("title", "关于我们");
        intent.putExtra("url", AppConfig.About_Us_Url);
        startActivity(intent);
    }

    private void gotoFeedBack() {
        Intent intent = new Intent(this, SnitchActivity.class);
        intent.putExtra("titleValue", "功能反馈");
        startActivityForResult(intent,1);
    }

    private void logout() {

        ConfirmDialog alertDialog = new ConfirmDialog(SettingsActivity.this, ConfirmDialog.ConfirmDialogType.ConfirmDialogType_Warning);
        alertDialog.setMsg("您是否确认退出当前登录账号?")
                .setLeftButtonText("取消")
                .setRightButtonText("退出")
                .setCallBack(new ConfirmDialog.CallBack() {
                    @Override
                    public void buttonClick(Dialog dialog, boolean leftClick) {
                        dialog.dismiss();
                        if (!leftClick) {
                            // 注销登录
                            AppSessionCache.getInstance().setLoginResult(null, SettingsActivity.this);
                            finish();
                        }
                    }
                });
        alertDialog.show();
    }

    /**
     * 功能：递归删除文件夹下面所有文件
     * @param file
     */
    private void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FEEDBACK_CODE && resultCode == SettingsActivity.RESULT_OK) {
            AlertManager.showSuccessToast(SettingsActivity.this,"提交成功", false);
        }
    }
}
