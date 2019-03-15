/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hande.goochao.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.hande.goochao.GoochaoApplication;
import com.hande.goochao.views.components.AlertManager;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 图片资源
 * Created by yanshen on 2016/4/7.
 */
public class ImageResource {

    private Activity mContext;

    public ImageResource(Activity context) {
        this.mContext = context;
    }

    /**
     * 调用相册
     *
     * @param requestCode
     */
    public void openPhotoSel(int requestCode) {
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        /* 使用Intent.ACTION_GET_CONTENT这个Action */
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mContext.startActivityForResult(Intent.createChooser(intent, "请选择上传头像"), requestCode);
    }

    /**
     * 调用图片剪辑程序
     *
     * @param uri
     * @param requestCode
     */
    public void openPhotoClip(Uri uri, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("output", uri);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);  // 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);  // 输出图片大小
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);  // 去黑边
        intent.putExtra("scaleUpIfNeeded", true);  // 去黑边
        intent.putExtra("return-data", true);
        mContext.startActivityForResult(intent, requestCode);

    }


    /**
     * 通过URI获取Bitmap
     * @param uri
     * @return
     */
    public Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }


    /**
     * 调用相机
     *
     * @param requestCode
     */
    public void openCamera(Uri uri, int requestCode) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))// 判断是否有SD卡
        {
            AlertManager.toast(mContext, "没有SD卡");
            return;
        }
        // 调用照相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        // 设置保存文件路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        mContext.startActivityForResult(intent, requestCode);
    }
}
