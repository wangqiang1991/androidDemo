package com.hande.goochao.commons.views.gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.views.components.AlertManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GalleryActivity extends Activity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private ViewPager photoViewPager;
    private ImageView delete_btn;
    private TextView pageCode;
    private String currentSrc = "";
    private int currentIndex = 0;
    private String[] srcArray;
    private boolean isLocal = true;
    private boolean useDelete;
    private boolean isLongClicked = false;
    private boolean isFullSize;
    private boolean head;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        glide = GlideApp.with(this);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        isLocal = getIntent().getBooleanExtra("isLocal", true);
        useDelete = getIntent().getBooleanExtra("delete", false);
        isFullSize = getIntent().getBooleanExtra("isFullSize", false);
        head = getIntent().getBooleanExtra("head", false);
        init();
    }

    private void init() {
        currentSrc = getIntent().getStringExtra("currentSrc");
        srcArray = getIntent().getStringArrayExtra("images");

        initViewPager(srcArray);

        updatePageCode(currentIndex);

        delete_btn = (ImageView) findViewById(R.id.delete_btn);
        if (useDelete) {
            delete_btn.setVisibility(View.VISIBLE);
            delete_btn.setOnClickListener(this);
        }
    }

    private void updatePageCode(int index) {
        pageCode.setText((index + 1) + "/" + srcArray.length);
    }

    /**
     * 关闭界面和释放资源
     */
    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("images", srcArray);
        setResult(RESULT_OK, intent);
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initViewPager(String[] srcArray) {
        for (int i = 0; i < srcArray.length; i++) {
            if (currentSrc.equals(srcArray[i])) {
                currentIndex = i;
            }

        }
        setContentView(R.layout.activity_gallery);
        photoViewPager = (ViewPager) findViewById(R.id.hack_view);
        photoViewPager.setAdapter(new SamplePagerAdapter(srcArray));
        photoViewPager.setCurrentItem(currentIndex);
        photoViewPager.addOnPageChangeListener(this);
        photoViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
        photoViewPager.setBackgroundColor(getResources().getColor(R.color.BLACK));
        pageCode = (TextView) findViewById(R.id.page_code);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        updatePageCode(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        AlertManager.show(this, "确定要删除图片?", "确实", "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCurrentImage();
            }
        }, null);
    }

    private void deleteCurrentImage() {
        int index = photoViewPager.getCurrentItem();
        //发送通知通知界面刷新
//		EventBusNotification notification = new EventBusNotification(EventBusNotification.EVENTBUS_DELETE_PHOTO);
//		notification.setValue(index);
//		EventBus.getDefault().post(notification);
        finish();
    }


    class SamplePagerAdapter extends PagerAdapter {

        private String[] data;

        public SamplePagerAdapter(String[] data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(GalleryActivity.this).inflate(R.layout.gallery_item, null);
            final PhotoView photoView = (PhotoView) view.findViewById(R.id.imgView);
            // 设置默认图片
            photoView.setImageResource(android.R.color.darker_gray);
            container.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            final int size = isFullSize ? 2000 : 1000;
            String thumbnail = isFullSize ? AppConfig.IMAGE_FULL_MAX : AppConfig.IMAGE_MAX;
            if (isLocal) {
                String filePath = "file://" + data[position];
                GlideApp.with(GalleryActivity.this)
                        .load(filePath)
                        .into(photoView);
            } else {
                final View loading = view.findViewById(R.id.loading);
                loading.setVisibility(View.VISIBLE);
                String imageUrl = data[position];
                if (!head) {
                    imageUrl = data[position].replace(AppConfig.IMAGE_COMPRESS, "").replaceAll("\\\\", "/")
                            + thumbnail;
                }

                ImageUtils.loadImage(glide,imageUrl, photoView, R.mipmap.loadpicture, new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        loading.setVisibility(View.GONE);
                        photoView.setImageDrawable(resource);
                    }
                });
            }
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isLongClicked) {
                        finish();
                    }
                }
            });

            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    isLongClicked = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                    builder.setItems(new String[]{"保存图片至本地", "取消"},
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                                        final String fileName = format.format(new Date()) + ".png";
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                saveMyBitmap(fileName, Environment.getExternalStorageDirectory() + AppConfig.CACHE_DIR + "/images/", drawableToBitmap(photoView.getDrawable()));
                                            }
                                        }).start();
                                    }
                                    isLongClicked = false;
                                }
                            });
                    builder.setCancelable(true);
                    builder.show();
                    return false;
                }
            });
            return view;
        }
    }

    /**
     * 功能：保存图片到本地
     *
     * @param fileName
     * @param bitmap
     */
    private void saveMyBitmap(String fileName, String filePath, Bitmap bitmap) {
        File f = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            f = new File(dir.getAbsolutePath(), fileName);
            f.createNewFile();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //通知媒体库刷新
        try {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + f.getPath())));
            MediaScannerConnection.scanFile(GalleryActivity.this, new String[]{f.getPath()}, null, null);
            refreshAlbum(f.getPath(), bitmap.getWidth(), bitmap.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertManager.toast(GalleryActivity.this, "保存成功");
            }
        });
        bitmap.recycle();

    }

    private void refreshAlbum(String filePath, int width, int height) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.ImageColumns.TITLE, "title");
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "filename.jpg");
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        values.put(MediaStore.Images.ImageColumns.DATA, filePath);
        values.put(MediaStore.Images.ImageColumns.WIDTH, width);
        values.put(MediaStore.Images.ImageColumns.HEIGHT, height);
        try {
            Uri uri = GalleryActivity.this.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Log.e("jileniao.net", "Failed to insert MediaStore");
            } else {
                GalleryActivity.this.sendBroadcast(new Intent(
                        "com.android.camera.NEW_PICTURE", uri));
            }
        } catch (Exception e) {
            Log.e("jileniao.net", "Failed to write MediaStore", e);
        }
    }

    /**
     * 功能：将Drawable对象转为Bitmap对象
     *
     * @param drawable
     * @return
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;

    }

}
