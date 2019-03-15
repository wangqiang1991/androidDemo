package com.hande.goochao.views.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.CustomerServiceUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.fragments.HelpProblemFragment;
import com.hande.goochao.views.widget.tablayout.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

public class HelpCenterActivity extends ToolBarActivity {

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    @ViewInject(R.id.help_viewPager)
    private ViewPager mViewPager;
    @ViewInject(R.id.help_tabs)
    private TabLayout mTabLayout;
    @ViewInject(R.id.function_feedback)
    private View functionFeedback;
    @ViewInject(R.id.call_us)
    private View callUs;
    @ViewInject(R.id.customer_service)
    private LinearLayout customerService;
    @ViewInject(R.id.help_center_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.load_fail_problem_view)
    private LoadFailView loadFailView;

    private static final int FEEDBACK_CODE = 1;
    private boolean firstLoad = true;

    private List<JSONObject> categoryList = new ArrayList<>();
    private JSONArray titlesData = new JSONArray();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);
        setTitle("帮助中心");

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        functionFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HelpCenterActivity.this, SnitchActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("titleValue", "功能反馈");
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
            }
        });

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadFailView.setVisibility(View.GONE);
                loadTitle();
            }

        });

        customerService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(HelpCenterActivity.this);
                CustomerServiceUtils.CustomerServiceModel model = new CustomerServiceUtils.CustomerServiceModel();
                model.setUname(JsonUtils.getString(loginResult, "nickName", null));
                model.setRealname(model.getUname());
                model.setFace(JsonUtils.getString(loginResult, "head", null));
                CustomerServiceUtils.goService(model, HelpCenterActivity.this);
            }
        });

        callUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConfirmDialog alertDialog = new ConfirmDialog(HelpCenterActivity.this, ConfirmDialog.ConfirmDialogType.ConfirmDialogType_Warning);
                alertDialog.setMsg("（028）85678699")
                        .setLeftButtonText("取消")
                        .setRightButtonText("呼叫")
                        .setCallBack(new ConfirmDialog.CallBack() {
                            @Override
                            public void buttonClick(Dialog dialog, boolean leftClick) {
                                dialog.dismiss();
                                if (!leftClick) {
                                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:（028）85678699"));
                                    if (ActivityCompat.checkSelfPermission(HelpCenterActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                        if (ActivityCompat.shouldShowRequestPermissionRationale(HelpCenterActivity.this,
                                                Manifest.permission.CALL_PHONE)) {
                                            Toast.makeText(HelpCenterActivity.this, "请于授权后返回拨打！", Toast.LENGTH_LONG).show();

                                            Intent newIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            newIntent.setData(uri);
                                            startActivity(newIntent);
                                        }else{
                                            ActivityCompat.requestPermissions(HelpCenterActivity.this,
                                                    new String[]{Manifest.permission.CALL_PHONE},
                                                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
                                        }
                                    }else {
                                        startActivity(intent);
                                    }
                                }
                            }
                        });
                alertDialog.show();
            }
        });

        hideLine();
        loadTitle();
    }

    private void loadTitle(){

        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }

        HttpRequest.get(AppConfig.HELP_CENTER , null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    titlesData = JsonUtils.getJsonArray(response, "data", null);
                    initViewPager();
                    firstLoad = false;
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            AlertManager.showErrorInfo(this);
        }
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void initViewPager() {
        List<Fragment> fragments = new ArrayList<>();

        for (int i = 0; i < titlesData.length(); i++) {
            categoryList.add(JsonUtils.getJsonItem(titlesData, i, null));
            fragments.add(new HelpProblemFragment(JsonUtils.getJsonArray(JsonUtils.getJsonItem(titlesData, i, null), "serviceVos", null)));
        }

        HelpFragmentAdapter mFragmentAdapter =
                new HelpFragmentAdapter(getSupportFragmentManager(), fragments ,categoryList);

        //给ViewPager设置适配器
        mViewPager.setAdapter(mFragmentAdapter);
        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
    }

    class HelpFragmentAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;
        private List<JSONObject> mTitles;

        public HelpFragmentAdapter(FragmentManager fm, List<Fragment> fragments,List<JSONObject> titles) {
            super(fm);
            mFragments = fragments;
            mTitles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
           return JsonUtils.getString(mTitles.get(position), "name", "");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FEEDBACK_CODE && resultCode == HelpCenterActivity.RESULT_OK) {
            AlertManager.showSuccessToast(HelpCenterActivity.this,"提交成功", false);
        }
    }
}
