package com.hande.goochao.views.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.hande.goochao.BuildConfig;
import com.hande.goochao.GoochaoApplication;
import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.permissions.PermissionsManager;
import com.hande.goochao.commons.qiniu.UploadListener;
import com.hande.goochao.commons.qiniu.UploadService;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.ContextUtils;
import com.hande.goochao.utils.DateUtils;
import com.hande.goochao.utils.FileUtils;
import com.hande.goochao.utils.ImageResource;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CircleImageView;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.DateSelectorPicker;

import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by Wangem on 2018/2/10.
 */

public class PersonalInformationActivity extends ToolBarActivity implements View.OnClickListener, UploadListener {


    @ViewInject(R.id.layHead)
    private View layHead;
    @ViewInject(R.id.layNickName)
    private View layNickName;
    @ViewInject(R.id.laySex)
    private View laySex;
    @ViewInject(R.id.layBirthday)
    private View layBirthday;
    @ViewInject(R.id.headImg)
    private CircleImageView headImg;
    @ViewInject(R.id.txtName)
    private TextView txtName;
    @ViewInject(R.id.txtGender)
    private TextView txtGender;
    @ViewInject(R.id.txtBirthday)
    private TextView txtBirthday;

    private static final int MODIFY_HEAD = 1;       // 修改头像
    private static final int MODIFY_NAME = 2;       // 修改昵称
    private static final int MODIFY_GENDER = 3;     // 修改性别
    private static final int CLIP = 5;              // 裁剪图片

    private String selectedPath;
    private String uploadResultUrl;

    private CustomLoadingDialog loadingDialog;

    private UploadService uploadService = UploadService.getService("upload_head");

    private DateSelectorPicker selectorPicker;

    private GlideRequests glide;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);
        setTitle("个人信息");
        glide = GlideApp.with(this);
        loadingDialog = new CustomLoadingDialog(this);

        bindEvent();
        bindSource();

        uploadService.addCallback(this);
        selectorPicker = new DateSelectorPicker(this);
        selectorPicker.setListener(new DateSelectorPicker.DateSelectorPickerListener() {
            @Override
            public void onSelected(Date date) {
                String dateStr = DateUtils.format(date, "yyyy-MM-dd");
                loadingDialog = new CustomLoadingDialog(PersonalInformationActivity.this);
                loadingDialog.setLoadingText("生日修改中");
                loadingDialog.show();
                modifyMemberInfo("birthday", dateStr);
            }
        });


    }

    private void bindEvent() {
        layHead.setOnClickListener(this);
        layNickName.setOnClickListener(this);
        laySex.setOnClickListener(this);
        layBirthday.setOnClickListener(this);
    }

    private void bindSource() {
        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(this);
        String nickName = JsonUtils.getString(loginResult, "nickName", "--");
        txtName.setText(nickName);

        String headUrl = JsonUtils.getString(loginResult, "head", "");
        ImageUtils.loadImage(glide, ImageUtils.resize(headUrl,320,320), headImg, R.mipmap.loadpicture);

        int gender = JsonUtils.getInt(loginResult, "gender", 0);
        if (gender == 1) {
            txtGender.setText("男");
        } else if (gender == 2) {
            txtGender.setText("女");
        } else {
            txtGender.setText("--");
        }

        long birthday = JsonUtils.getLong(loginResult, "birthday", 0);
        if (birthday != 0) {
            String birthdayStr = DateUtils.timeStampToStr(birthday, "yyyy-MM-dd");
            txtBirthday.setText(birthdayStr);
        } else {
            txtBirthday.setText("--");
        }

    }

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.CAMERA"};
    private static final int REQUEST_CAMERA_CODE = 1;

    private void gotoSelectHeadImage() {
        Intent intent = new Intent(this, MultiImageSelectorActivity.class);
        // 是否显示拍摄图片
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大可选择图片数量
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
        // 选择模式
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        startActivityForResult(intent, MODIFY_HEAD);
    }

    private Uri tempUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MODIFY_HEAD && resultCode == RESULT_OK) {
            // 修改头像
            ArrayList<String> resultList = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            selectedPath = resultList.get(0);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                String packageName = GoochaoApplication.getApplication().getPackageName();
//                tempUri = FileProvider.getUriForFile(GoochaoApplication.getApplication(), packageName + ".FileProvider",
//                        new File(selectedPath));
//            } else {
//                tempUri = Uri.fromFile(new File(selectedPath));
//            }

            tempUri = FileUtils.getImageContentUri(this, new File(selectedPath));
            new ImageResource(this).openPhotoClip(tempUri, CLIP);
        } else if (requestCode == CLIP && resultCode == RESULT_OK) {
            try {
                loadingDialog = new CustomLoadingDialog(PersonalInformationActivity.this);
                loadingDialog.setLoadingText("头像上传中");
                loadingDialog.show();
                Bitmap headImage = new ImageResource(this).decodeUriAsBitmap(tempUri);
                String cachePath = saveFileToCache(headImage);
                selectedPath = cachePath;
                uploadService.clearTask();
                uploadService.addTask(cachePath);
                uploadService.startUpload();
            } catch (Exception ex) {
                ex.printStackTrace();
                loadingDialog.dismiss();
                AlertManager.showErrorToast(PersonalInformationActivity.this, "头像上传失败", false);
            }

        } else if (requestCode == MODIFY_NAME && resultCode == RESULT_OK) {
            String nickName = data.getStringExtra("inputValue");
            if (TextUtils.isEmpty(nickName)) {
                AlertManager.toast(this, "昵称不能为空");
                return;
            }if(nickName.length() > 16){
                AlertManager.toast(this, "昵称最大16位字符");
                return;
            }

            loadingDialog = new CustomLoadingDialog(PersonalInformationActivity.this);
            loadingDialog.setLoadingText("修改昵称中");
            loadingDialog.show();
            modifyMemberInfo("nickName", nickName);
        } else if (requestCode == MODIFY_GENDER && resultCode == RESULT_OK) {
            int genderValue = data.getIntExtra("genderValue", 1);
            loadingDialog = new CustomLoadingDialog(PersonalInformationActivity.this);
            loadingDialog.setLoadingText("修改性别中");
            loadingDialog.show();
            modifyMemberInfo("gender", genderValue + "");
        }
    }

    /**
     * 修改用户信息
     *
     * @param key
     * @param value
     */
    private void modifyMemberInfo(String key, String value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        HttpRequest.postJson(AppConfig.Modify_Member, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    JSONObject loginResult = JsonUtils.getJsonObject(response, "data", null);
                    AppSessionCache.getInstance().setLoginResult(loginResult, PersonalInformationActivity.this);
                    bindSource();
                    AlertManager.showSuccessToast(PersonalInformationActivity.this, "修改成功", false);
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(PersonalInformationActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(PersonalInformationActivity.this, "服务器繁忙", false);
            }
        });
    }

    /**
     * 保存文件
     *
     * @param bm
     * @throws IOException
     */
    public String saveFileToCache(Bitmap bm) throws IOException {
        String fileName = "upload_head.png";
        String path = ContextUtils.getCacheDir(AppConfig.IMAGE_CACHE_DIR);
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File myCaptureFile = new File(path + "/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        return myCaptureFile.getAbsolutePath();
    }


    @Override
    public void onClick(View v) {
        if (v == layHead) {
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.CAMERA");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_CAMERA_CODE);
                return;
            }

            gotoSelectHeadImage();
        } else if (v == layNickName) {
            gotoEditNickName();
        } else if (v == laySex) {
            gotoEditGender();
        } else if (v == layBirthday) {
            gotoEditBirthday();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_CODE) {
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.CAMERA");
            if (permission == PackageManager.PERMISSION_GRANTED) {
                gotoSelectHeadImage();
            }
        }


    }

    private void gotoEditNickName() {
        Intent intent = new Intent(this, SingleInputActivity.class);
        intent.putExtra("titleValue", "编辑昵称");
        intent.putExtra("leftValue", "取消");
        intent.putExtra("rightValue", "完成");
        intent.putExtra("placeHolder", "请输入昵称");
        intent.putExtra("inputValue", txtName.getText().toString());
        intent.putExtra("require" , true);
        intent.putExtra("requireTxt","昵称不能为空");
        startActivityForResult(intent, MODIFY_NAME);
    }

    private void gotoEditGender() {
        Intent intent = new Intent(this, SelectSexActivity.class);
        intent.putExtra("titleValue", "我的性别");
        intent.putExtra("leftValue", "取消");
        intent.putExtra("rightValue", "完成");
        intent.putExtra("genderValue", txtGender.getText().equals("男") ? 1 : 2);
        startActivityForResult(intent, MODIFY_GENDER);
    }

    private void gotoEditBirthday() {
        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(this);
        long birthday = JsonUtils.getLong(loginResult, "birthday", 0);
        selectorPicker.show();
        selectorPicker.setMaxDate((new Date()).getTime());
        selectorPicker.setDate(new Date(birthday));
    }


    @Override
    public void success(int index, String path) {
        uploadResultUrl = path;
    }

    @Override
    public void startUpload(int index) {

    }

    @Override
    public void complete() {
        if (!TextUtils.isEmpty(uploadResultUrl)) {
            modifyMemberInfo("head", uploadResultUrl);
        }
    }

    @Override
    public void error(int index) {
        loadingDialog.dismiss();
        AlertManager.showErrorToast(this, "上传头像失败", false);
    }


    @Override
    protected void onDestroy() {
        uploadService.removeCallback(this);
        super.onDestroy();
    }

    @Override
    public void finish() {
        uploadService.removeCallback(this);
        super.finish();
    }
}
