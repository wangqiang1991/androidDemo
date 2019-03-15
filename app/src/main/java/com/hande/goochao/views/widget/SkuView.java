package com.hande.goochao.views.widget;

import android.content.Context;
import android.media.Image;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * @author yanshen
 * @description 无
 * Created by yanshen on 2018/8/8.
 */
public class SkuView extends LinearLayout implements View.OnClickListener {

    private JSONObject data;
    private JSONObject selectedSku;

    @ViewInject(R.id.firstView)
    private FrameLayout firstStyleView;
    @ViewInject(R.id.secondView)
    private FrameLayout secondStyleView;
    @ViewInject(R.id.firstStyleItemNameTxt)
    private TextView firstStyleItemNameTxt;
    @ViewInject(R.id.secondStyleItemNameTxt)
    private TextView secondStyleItemNameTxt;

    private View selectedFirstView;
    private View selectedSecondView;

    private OnSkuChangeListener listener;

    private GlideRequests glide;

    public SkuView(Context context) {
        super(context);
        init();
    }

    public SkuView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SkuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SkuView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        glide = GlideApp.with(this);
        LayoutInflater.from(getContext()).inflate(R.layout.view_sku, this, true);
        x.view().inject(this);
    }

    public void setListener(OnSkuChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 详情赋值
     * @param data
     */
    public void setData(JSONObject data) {
        this.data = data;

        String firstStyleItemName = JsonUtils.getString(data, "firstStyleItemName", "-");
        String secondStyleItemName = JsonUtils.getString(data, "secondStyleItemName", "-");
        firstStyleItemNameTxt.setText(firstStyleItemName);
        secondStyleItemNameTxt.setText(secondStyleItemName);

        JSONArray firstStyleItems = JsonUtils.getJsonArray(data, "firstStyleItems", new JSONArray());
        boolean firstStyleEnabledCover = JsonUtils.getBoolean(data, "firstStyleEnabledCover", false);
        insertStyleView(firstStyleItems, firstStyleView, firstStyleEnabledCover);

        JSONArray secondStyleItems = JsonUtils.getJsonArray(data, "secondStyleItems", new JSONArray());
        boolean secondStyleEnabledCover = JsonUtils.getBoolean(data, "secondStyleEnabledCover", false);
        insertStyleView(secondStyleItems, secondStyleView, secondStyleEnabledCover);
    }

    /**
     * 查询规格项
     * @param styleItems
     * @param view
     * @param showCover
     */
    private void insertStyleView(JSONArray styleItems, FrameLayout view, boolean showCover) {

        int startX = 0;
        int startY = 0;
        int width = WindowUtils.getDeviceWidth(getContext()) - WindowUtils.dpToPixels(getContext(),40);//getMeasuredWidth();

        for (int i = 0; i < styleItems.length(); i++) {
            JSONObject item = JsonUtils.getJsonItem(styleItems, i, null);
            String cover = JsonUtils.getString(item, "cover", "");
            String name = JsonUtils.getString(item, "name", "");

            if (showCover) {
                // 图片+文字
                int size = (int) (40f / 375 * WindowUtils.getDeviceWidth(getContext()));
                LinearLayout styleView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_sku_image, null);
                styleView.setTag(item);
                styleView.setOnClickListener(this);
                ImageView imageView = styleView.findViewById(R.id.imageView);
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.width = size;
                params.height = size;
                ImageUtils.loadImage(glide, cover, imageView, -1);
                TextView textView = styleView.findViewById(R.id.textView);
                textView.getLayoutParams().width = size;
                textView.setText(name);
                view.addView(styleView);
                ((FrameLayout.LayoutParams) styleView.getLayoutParams()).leftMargin = startX;
                startX = startX + size + WindowUtils.dpToPixels(getContext(), 15);
            } else  {
                // 纯文字
                int size = width / 2 - WindowUtils.dpToPixels(getContext(), 7.5f) - 1;
                LinearLayout styleView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_sku_text, null);
                styleView.setTag(item);
                styleView.setOnClickListener(this);
                TextView textView = styleView.findViewById(R.id.textView);
                textView.getLayoutParams().width = size;
                textView.getLayoutParams().height = WindowUtils.dpToPixels(getContext(), 35);
                textView.setText(name);
                view.addView(styleView);
                ((FrameLayout.LayoutParams) styleView.getLayoutParams()).leftMargin =  startX;
                ((FrameLayout.LayoutParams) styleView.getLayoutParams()).topMargin =  startY;
                if ((i + 1) % 2 != 0) {
                    startX = size + WindowUtils.dpToPixels(getContext(), 15);
                } else  {
                    startX = 0;
                    startY = startY + WindowUtils.dpToPixels(getContext(), 45);
                }
            }
        }

    }

    /**
     * 规格1被选择
     * @param view
     */
    private void onFirstSelected(View view) {
        if (selectedFirstView == view) {
            return;
        }

        boolean firstShowCover = JsonUtils.getBoolean(data, "firstStyleEnabledCover", false);
        onSelected(view, selectedFirstView, firstShowCover);
        selectedFirstView = view;
        loopDisabledSecondView(JsonUtils.getString((JSONObject) view.getTag(), "name", ""));
        dispatchChangeEvent();
    }


    /**
     * 规格二被选择
     * @param view
     */
    private void onSecondSelected(View view) {
        if (selectedSecondView == view) {
            return;
        }
        boolean secondShowCover = JsonUtils.getBoolean(data, "secondStyleEnabledCover", false);
        onSelected(view, selectedSecondView, secondShowCover);
        selectedSecondView = view;
        dispatchChangeEvent();
    }

    private void onSelected(View view, View selectedView, boolean showCover) {
        if (selectedView != null) {
            setUnSelectedStatus(selectedView, showCover);
        }
        setSelectedStatus(view, showCover);
    }

    private void setSelectedStatus(View view, boolean showCover) {
        if (showCover) {
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setBackgroundResource(R.drawable.view_new_product_select);
        } else {
            TextView textView = view.findViewById(R.id.textView);
            textView.setBackgroundResource(R.drawable.view_new_product_text_select);
            textView.setTextColor(getResources().getColor(R.color.black_button_selected));
        }
        view.setEnabled(true);
    }

    private void setUnSelectedStatus(View view, boolean showCover) {
        if (showCover) {
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setBackgroundResource(R.drawable.view_border);
        } else {
            TextView textView = view.findViewById(R.id.textView);
            textView.setBackgroundResource(R.drawable.view_border);
            textView.setTextColor(getResources().getColor(R.color.black_button_selected));
        }
        view.setEnabled(true);
    }

    /**
     * 循环设置规格二是否可选
     * @param name
     */
    private void loopDisabledSecondView(String name) {
        JSONArray secondStyleItems = JsonUtils.getJsonArray(data, "secondStyleItems", new JSONArray());
        boolean secondShowCover = JsonUtils.getBoolean(data, "secondStyleEnabledCover", false);
        for (int i = 0; i < secondStyleItems.length(); i++) {
            JSONObject item = JsonUtils.getJsonItem(secondStyleItems, i, null);
            String subName = JsonUtils.getString(item, "name", "");
            boolean exists = existsSKU(name, subName);
            View view = secondStyleView.getChildAt(i);
            if (exists) {
                if (view != selectedSecondView) {
                    setUnSelectedStatus(view, secondShowCover);
                }
            } else {
                setDisabledStatus(view, secondShowCover);
                if (view == selectedSecondView) {
                    selectedSecondView = null;
                    int index = findSkuIndexByName(JsonUtils.getString((JSONObject) selectedFirstView.getTag(), "name", ""));
                    if (index != -1) {
                        onSecondSelected(secondStyleView.getChildAt(index));
                    }
                }
            }
        }
    }

    /**
     * 触发选择事件
     */
    private void dispatchChangeEvent() {
        // 得到sku
        if (selectedFirstView != null && selectedSecondView != null) {
            String name = JsonUtils.getString((JSONObject) selectedFirstView.getTag(), "name", "");
            String subName = JsonUtils.getString((JSONObject) selectedSecondView.getTag(), "name", "");
            JSONArray styles = JsonUtils.getJsonArray(data, "styles", new JSONArray());
            for (int i = 0; i < styles.length(); i++) {
                JSONObject item = JsonUtils.getJsonItem(styles, i, null);
                if (name.equals(JsonUtils.getString(item, "name", ""))
                    && subName.equals(JsonUtils.getString(item, "subName", ""))) {
                    selectedSku = item;
                    if (this.listener != null) {
                        this.listener.onChange(item);
                    }
                }
            }
        }
    }

    // 按规格1的Name查找第一个存在的SKU索引
    private int findSkuIndexByName(String name) {
        JSONArray styles = JsonUtils.getJsonArray(data, "styles", new JSONArray());
        String subName = null;
        // 先找到第一个符合条件的SKU中的subName
        for (int i = 0; i < styles.length(); i++) {
            JSONObject item = JsonUtils.getJsonItem(styles, i, null);
            if (name.equals(JsonUtils.getString(item, "name", ""))) {
                subName = JsonUtils.getString(item, "subName", "");
                break;
            }
        }

        // 如果找到了再用subName去找符合条件的规格2索引并返回
        if (!TextUtils.isEmpty(subName)) {
            JSONArray secondStyleItems = JsonUtils.getJsonArray(data, "secondStyleItems", new JSONArray());
            for (int i = 0; i < secondStyleItems.length(); i++) {
                JSONObject item = JsonUtils.getJsonItem(secondStyleItems, i, null);
                if (subName.equals(JsonUtils.getString(item, "name", ""))) {
                    return i;
                }
            }
        }

        return -1;
    }

    private void setDisabledStatus(View view, boolean showCover) {
        view.setEnabled(false);
        if (showCover) {
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setBackgroundResource(R.mipmap.dotted_line_square);
        } else {
            TextView textView = view.findViewById(R.id.textView);
            textView.setBackgroundResource(R.mipmap.dotted_line_tall);
            textView.setTextColor(getResources().getColor(R.color.gray_add));
        }
    }

    private boolean existsSKU(String name, String subName) {
        JSONArray styles = JsonUtils.getJsonArray(data, "styles", new JSONArray());
        for (int i = 0; i < styles.length(); i++) {
            JSONObject item = JsonUtils.getJsonItem(styles, i, null);
            if (name.equals(JsonUtils.getString(item, "name", ""))
                    && subName.equals(JsonUtils.getString(item, "subName", ""))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置默认选中sku
      * @param selectedSku
     */
    public void setSelectedSku(JSONObject selectedSku) {
        if (selectedSku == null) {
            return;
        }
        this.selectedSku = selectedSku;

        JSONArray firstStyleItems = JsonUtils.getJsonArray(data, "firstStyleItems", new JSONArray());
        JSONArray secondStyleItems = JsonUtils.getJsonArray(data, "secondStyleItems", new JSONArray());

        String name = JsonUtils.getString(selectedSku, "name", "");
        String subName = JsonUtils.getString(selectedSku, "subName", "");

        int firstIndex = 0;
        int secondIndex = 0;

        for (int i = 0; i < firstStyleItems.length(); i++) {
            JSONObject item =  JsonUtils.getJsonItem(firstStyleItems, i, null);
            if (name.equals(JsonUtils.getString(item, "name", ""))) {
                firstIndex = i;
                break;
            }
        }

        for (int i = 0; i < secondStyleItems.length(); i++) {
            JSONObject item =  JsonUtils.getJsonItem(secondStyleItems, i, null);
            if (subName.equals(JsonUtils.getString(item, "name", ""))) {
                secondIndex = i;
                break;
            }
        }

        onFirstSelected(firstStyleView.getChildAt(firstIndex));
        onSecondSelected(secondStyleView.getChildAt(secondIndex));
    }

    public JSONObject getSelectedSku() {
        return selectedSku;
    }

    @Override
    public void onClick(View v) {
        if (v.getParent() == firstStyleView) {
            // 规格1被点击
            onFirstSelected(v);
        } else {
            // 规格2被点击
            onSecondSelected(v);
        }
    }

    public interface OnSkuChangeListener {
        void onChange(JSONObject sku);
    }
}
