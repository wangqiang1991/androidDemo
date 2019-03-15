package com.hande.goochao.views.base;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.views.components.CircleImageView;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.Calendar;


/**
 * 支持ToolBar的Activity
 * Created by yanshen on 2018/1/8.
 */
@SuppressWarnings("ResourceType")
public abstract class ToolBarActivity extends BaseActivity {

    private static int[] ATTRS = {
            R.attr.windowActionBarOverlay,
            R.attr.actionBarSize
    };

    private ToolBarHelper mToolBarHelper;
    public Toolbar toolbar;
    public View userView;

    @ViewInject(R.id.back_btn)
    private LinearLayout backBtnLayout;

    @ViewInject(R.id.back_text)
    private TextView backText;

    @ViewInject(R.id.head_btn_layout)
    private LinearLayout headBtnLayout;
    @ViewInject(R.id.head_btn)
    private CircleImageView headBtn;

    @ViewInject(R.id.left_btn_layout)
    private LinearLayout leftBtnLayout;
    @ViewInject(R.id.left_btn)
    private CircleImageView leftBtn;

    @ViewInject(R.id.left_custom_view)
    private LinearLayout leftCustomLayout;

    @ViewInject(R.id.title)
    private TextView mTitle;

    @ViewInject(R.id.right_img_btn)
    private CircleImageView rightImgBtn;

    @ViewInject(R.id.right_custom_view)
    private LinearLayout rightCustomLayout;

    @ViewInject(R.id.right_full_view)
    private LinearLayout rightFullLayout;

    @ViewInject(R.id.line_view)
    private View lineView;

    @ViewInject(R.id.appbar)
    private AppBarLayout appbar;

    @ViewInject(R.id.layClose)
    private View layClose;

    @ViewInject(R.id.txtClose)
    private TextView txtClose;

    @ViewInject(R.id.laySubmit)
    private View laySubmit;

    @ViewInject(R.id.txtSubmit)
    private TextView txtSubmit;

    private GlideRequests glide;

    private View.OnClickListener clickListener = new View.OnClickListener() {

        public static final int MIN_CLICK_DELAY_TIME = 600;
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            // 防止有人点击过快,这里一次点击需要600毫秒的冷却时间
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;
                if (v == backBtnLayout) {
                    onBackListener();
                } else if (v == rightImgBtn) {
                    onRightClickListener();
                } else if (v == leftBtn) {
                    onLeftClickListener();
                } else if (v == headBtn) {
                    onHeadClickListener();
                } else if (v == layClose) {
                    onCloseClickLister();
                } else if (v == laySubmit) {
                    onSubmitClickLister();
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        glide = GlideApp.with(this);
        mToolBarHelper = new ToolBarHelper(this, layoutResID);
        toolbar = mToolBarHelper.getToolBar();
        userView = mToolBarHelper.getUserView();
        setContentView(mToolBarHelper.getContentView());
        // 自定义的一些操作
        setSupportActionBar(toolbar);
        // 自定义的一些操作
        onCreateCustomToolBar(toolbar);

        backBtnLayout.setOnClickListener(clickListener);
        leftBtn.setOnClickListener(clickListener);
        rightImgBtn.setOnClickListener(clickListener);
        headBtn.setOnClickListener(clickListener);
        layClose.setOnClickListener(clickListener);
        laySubmit.setOnClickListener(clickListener);

        if (!(this instanceof MainActivity)) {
            showBack();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
        TextPaint paint = mTitle.getPaint();
        paint.setFakeBoldText(true);
    }

    public void hideTitle(){
        mTitle.setVisibility(View.GONE);
    }

    public void showTitle(){
        mTitle.setVisibility(View.VISIBLE);
    }

    protected void showBack() {
        backBtnLayout.setVisibility(View.VISIBLE);
    }

    protected void hideBack() {
        backBtnLayout.setVisibility(View.GONE);
    }

    protected void hideBackText() {
        backText.setVisibility(View.GONE);
    }

    protected void showHeadBtn(int resourceId) {
        this.showHeadBtn(resourceId, false);
    }

    protected void showHeadBtn(int resourceId, boolean circle) {
        headBtn.setDisableCircle(!circle);
        headBtnLayout.setVisibility(View.VISIBLE);
        headBtn.setImageResource(resourceId);
    }

    protected void showHeadBtn(String imageUrl) {
        this.showHeadBtn(imageUrl, false);
    }

    protected void showHeadBtn(String imageUrl, boolean circle) {
        headBtn.setDisableCircle(!circle);
        headBtnLayout.setVisibility(View.VISIBLE);
//        ImageUtils.loadImage(glide, imageUrl, headBtn, R.drawable.border);
        x.image().bind(headBtn,imageUrl);
    }

    public void hideHeadBtn() {
        headBtnLayout.setVisibility(View.GONE);
    }

    protected void showLeftBtn(int resourceId) {
        this.showLeftBtn(resourceId, false);
    }

    protected void showLeftBtn(int resourceId, boolean circle) {
        leftBtn.setDisableCircle(!circle);
        leftBtnLayout.setVisibility(View.VISIBLE);
        leftBtn.setImageResource(resourceId);
    }

    protected void showLeftBtn(Drawable drawable, boolean circle){
        leftBtn.setDisableCircle(!circle);
        leftBtnLayout.setVisibility(View.VISIBLE);
        leftBtn.setBackground(drawable);
    }

    protected void showLeftBtn(String imageUrl) {
        this.showLeftBtn(imageUrl, false);
    }

    protected void showLeftBtn(String imageUrl, boolean circle) {
        leftBtn.setDisableCircle(!circle);
        leftBtnLayout.setVisibility(View.VISIBLE);
        ImageUtils.loadImage(glide, imageUrl, leftBtn, R.drawable.border);
    }

    protected void hideLeftBtn() {
        leftBtnLayout.setVisibility(View.GONE);
    }

    protected void showLeftCustomView(int layoutId) {
        leftCustomLayout.removeAllViews();
        leftCustomLayout.setVisibility(View.VISIBLE);
        LayoutInflater.from(this).inflate(layoutId, leftCustomLayout, true);
    }

    protected void showLeftCustomView(View layoutView) {
        leftCustomLayout.removeAllViews();
        leftCustomLayout.setVisibility(View.VISIBLE);
        leftCustomLayout.addView(layoutView);
    }

    protected void hideLeftCustomView() {
        leftCustomLayout.setVisibility(View.GONE);
    }

    protected void showRightBtn(int resourceId) {
        this.showRightBtn(resourceId, false);
    }

    protected void showRightBtn(Drawable drawable) {
        this.showRightBtn(drawable, false);
    }

    protected void showRightBtn(int resourceId, boolean circle) {
        rightImgBtn.setDisableCircle(!circle);
        rightImgBtn.setVisibility(View.VISIBLE);
        rightImgBtn.setImageResource(resourceId);
    }

    protected void showRightBtn(String imageUrl) {
        this.showRightBtn(imageUrl, false);
    }

    protected void showRightBtn(String imageUrl, boolean circle) {
        rightImgBtn.setDisableCircle(!circle);
        rightImgBtn.setVisibility(View.VISIBLE);
        ImageUtils.loadImage(glide, imageUrl, rightImgBtn, R.mipmap.loadpicture);
    }

    protected void showRightBtn(Drawable drawable, boolean circle) {
        rightImgBtn.setDisableCircle(!circle);
        rightImgBtn.setVisibility(View.VISIBLE);
        rightImgBtn.setBackground(drawable);
    }

    protected void hideRightBtn() {
        rightImgBtn.setVisibility(View.GONE);
    }

    protected void showRightCustomView(int layoutId) {
        rightCustomLayout.removeAllViews();
        rightCustomLayout.setVisibility(View.VISIBLE);
        LayoutInflater.from(this).inflate(layoutId, rightCustomLayout, true);
    }

    protected void showRightCustomView(View layoutView) {
        rightCustomLayout.removeAllViews();
        rightCustomLayout.setVisibility(View.VISIBLE);
        rightCustomLayout.addView(layoutView);
    }

    protected void hideRightCustomView() {
        rightCustomLayout.setVisibility(View.GONE);
    }

    protected void showRightFullView(int layoutId) {
        rightFullLayout.removeAllViews();
        rightFullLayout.setVisibility(View.VISIBLE);
        LayoutInflater.from(this).inflate(layoutId, rightCustomLayout, true);
    }

    protected void showRightFullView(View layoutView) {
        rightFullLayout.removeAllViews();
        rightFullLayout.setVisibility(View.VISIBLE);
        rightFullLayout.addView(layoutView);
    }

    protected void hideRightFullView() {
        rightFullLayout.setVisibility(View.GONE);
    }

    protected void showLine() {
        lineView.setVisibility(View.VISIBLE);
    }

    protected void showLine(int color) {
        lineView.setVisibility(View.VISIBLE);
        lineView.setBackgroundColor(this.getResources().getColor(color));
    }


    public void hideLine() {
        lineView.setVisibility(View.GONE);
    }

    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setContentInsetsRelative(0, 0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 返回点击
     */
    protected void onBackListener() {
        // subclass impl
        finish();
    }

    /**
     * 右侧按钮点击
     */
    protected void onRightClickListener() {
        // subclass impl
    }

    protected void onLeftClickListener() {
        // subclass impl
    }

    protected void onHeadClickListener() {
        // subclass impl
    }

    protected void onCloseClickLister() {
        // subclass impl
    }

    protected void onSubmitClickLister() {
        // subclass impl
    }


    public void hiddenToolBar() {
        appbar.setVisibility(View.GONE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        userView.setLayoutParams(params);
    }

    public void showToolBar() {
        appbar.setVisibility(View.VISIBLE);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TypedArray typedArray = this.getTheme().obtainStyledAttributes(ATTRS);
        /*获取主题中定义的悬浮标志*/
        boolean overly = typedArray.getBoolean(0, false);
        /*获取主题中定义的toolbar的高度*/
        int toolBarSize = (int) typedArray.getDimension(1, (int) this.getResources().getDimension(R.dimen.abc_action_bar_default_height_material));
        typedArray.recycle();
        /*如果是悬浮状态，则不需要设置间距*/
        params.topMargin = overly ? 0 : toolBarSize;
        userView.setLayoutParams(params);
    }

    @Override
    protected void onResume() {
//        MobclickAgent.onResume(this);
        super.onResume();
        if (appbar != null) {
            appbar.setElevation(0f);
        }
    }

    @Override
    protected void onPause() {
//        MobclickAgent.onPause(this);
        super.onPause();
    }

    protected void setCloseText(String text) {
        layClose.setVisibility(View.VISIBLE);
        txtClose.setText(text);
    }

    protected void setSubmitText(String text) {
        laySubmit.setVisibility(View.VISIBLE);
        txtSubmit.setText(text);
    }

    protected void hideSubmitText() {
        laySubmit.setVisibility(View.GONE);
    }

}