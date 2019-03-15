package com.hande.goochao.views.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.bumptech.glide.Glide;
import com.hande.goochao.BuildConfig;
import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.JSONObjectCallback;
import com.hande.goochao.commons.http.Params;
import com.hande.goochao.commons.qiniu.UploadListener;
import com.hande.goochao.commons.qiniu.UploadService;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class SubmitOrderCommentsActivity extends ToolBarActivity implements View.OnClickListener, UploadListener {

    private static final int SELECT_IMAGE_CODE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.CAMERA"};
    private static final int REQUEST_CAMERA_CODE = 1;


    @ViewInject(R.id.grid_view)
    private GridView grid_view;
    @ViewInject(R.id.btnSubmit)
    private Button btnSubmit;
    @ViewInject(R.id.message_edit)
    private EditText messageEdit;
    @ViewInject(R.id.rating_bar)
    private RatingBar ratingBar;

    private CustomLoadingDialog loadingDialog;
    private boolean isUploading;

    private String relationId;
    private String goodsId;
    private String styleId;

    private UploadService uploadService = UploadService.getService("SubmitOrderCommentsActivity");

    private ImageAdapter adapter = new ImageAdapter();
    private ArrayList<String> mSelectPath = new ArrayList<>();
    private List<String> mArray ;
    private String[] mArrayImage;
    //上传返回的服务器图片地址
    private ArrayList<String> uploadBackUrls = new ArrayList<>();

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_order_comments);
        setTitle("评价晒单");
        glide = GlideApp.with(this);
        relationId = getIntent().getStringExtra("relationId");
        goodsId = getIntent().getStringExtra("goodsId");
        styleId = getIntent().getStringExtra("styleId");

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("数据上传中");

        grid_view.setAdapter(adapter);
        btnSubmit.setOnClickListener(this);
        uploadService.addCallback(this);

    }

    @Override
    public void onClick(View v) {
        if (v == btnSubmit) {
            doSubmit();
        } else if (StringUtils.equals("add", v.getTag() + "")) {
            if (mSelectPath != null && mSelectPath.size() == 3) {
                return;
            }
            doSelectImages();
        }
    }

    private void doSubmit() {
        if (isUploading) {
            return;
        }

        loadingDialog.show();
        isUploading = true;

        if (mSelectPath.size() == 0) {
            submitData();
        } else {
            uploadService.clearTask();
            uploadBackUrls.clear();
            for (String path : mSelectPath) {
                uploadService.addTask(path);
            }
            uploadService.startUpload();
        }
    }

    private void submitData() {
        int star = (int) ratingBar.getRating();
        String images;
        String content = messageEdit.getText().toString().trim();

        if (!uploadBackUrls.isEmpty()) {
            images = StringUtils.join(uploadBackUrls, ",");
        } else {
            images = null;
        }

        System.out.println(star + " " + images + " " + content);

        Map<String, Object> params = Params.buildForObj("orderGoodsId", relationId,
                "goodsId", goodsId,
                "styleId", styleId,
                "star", star + "",
                "images", images,
                "content", content);
        HttpRequest.postJson(AppConfig.SUBMIT_ORDER_COMMENTS, null, params, new JSONObjectCallback() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
                isUploading = false;
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(SubmitOrderCommentsActivity.this, "提交评价成功", false);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    AlertManager.showErrorToast(SubmitOrderCommentsActivity.this, "提交评价失败", false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(SubmitOrderCommentsActivity.this, "提交评价失败", false);
                AppLog.e("提交评价失败", ex);
            }
        });
    }

    private void doSelectImages() {
        int permission = ActivityCompat.checkSelfPermission(this,
                "android.permission.CAMERA");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有写的权限，去申请写的权限，会弹出对话框
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_CAMERA_CODE);
            return;
        }

        Intent intent = new Intent(this, MultiImageSelectorActivity.class);
        // 是否显示拍摄图片
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大可选择图片数量
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 3);
        // 选择模式
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        // 默认选择
        if (mSelectPath != null && mSelectPath.size() > 0) {
            intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
        }
        startActivityForResult(intent, SELECT_IMAGE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_CODE) {
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.CAMERA");
            if (permission == PackageManager.PERMISSION_GRANTED) {
                doSelectImages();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_CODE && resultCode == RESULT_OK) {
            mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void success(int index, String path) {
        uploadBackUrls.add(path);
    }

    @Override
    public void startUpload(int index) {
    }

    @Override
    public void complete() {
        submitData();
    }

    @Override
    public void error(int index) {
        loadingDialog.dismiss();
        uploadService.clearTask();
        uploadBackUrls.clear();
        isUploading = false;
        AlertManager.showErrorToast(this, "上传失败，请稍后再试", false);
    }

    class ImageAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return (mSelectPath == null ? 0 : mSelectPath.size()) + 1;
        }

        @Override
        public String getItem(int position) {
            return mSelectPath.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.add_image_item, parent, false);
                int size = (WindowUtils.getDeviceWidth(SubmitOrderCommentsActivity.this) - WindowUtils.dpToPixels(SubmitOrderCommentsActivity.this, 39)) / 4;
                GridView.LayoutParams params = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, size);
                convertView.setLayoutParams(params);
//            }

            ImageView imageView = convertView.findViewById(R.id.imageView);
            View layDelete = convertView.findViewById(R.id.layDelete);
            if (position == getCount() - 1) {
                imageView.setImageResource(R.mipmap.add_photo);
                convertView.setTag("add");
                layDelete.setVisibility(View.GONE);
            } else {
                String filePath = "file://" + getItem(position);
                ImageUtils.loadImage(glide, filePath, imageView, -1);

                convertView.setTag(position);
                layDelete.setVisibility(View.VISIBLE);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mArray = new ArrayList<>();
                        for (int i=0;i<mSelectPath.size();i++){
                            mArray.add(mSelectPath.get(i));
                        }
                        mArrayImage = new String[mArray.size()];
                        for (int i = 0; i < mArray.size(); i++) {
                            mArray.set(i, mArray.get(i) + AppConfig.IMAGE_COMPRESS);
                            mArrayImage[i] = mArray.get(i);
                        }

                        String currentSrc = mArray.get(position);
                        Intent intent = new Intent();
                        intent.setClass(SubmitOrderCommentsActivity.this, GalleryActivity.class);
                        intent.putExtra("isLocal",true );
                        intent.putExtra("currentSrc",currentSrc );
                        intent.putExtra("images", mArrayImage);
                        SubmitOrderCommentsActivity.this.startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });
            }
            if(getCount() == 4){
                if (position == getCount()-1){
                    imageView.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }
            convertView.setOnClickListener(SubmitOrderCommentsActivity.this);

            layDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectPath.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}
