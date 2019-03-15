package com.hande.goochao.views.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.ProductListActivity;
import com.hande.goochao.views.activity.SearchActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.widget.ChangeLineWidth;
import com.hande.goochao.views.widget.GoochaoGridView;
import com.hande.goochao.views.widget.GoochaoListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;


/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/11.
 */
@ContentView(R.layout.fragment_new_shopping_mall)
public class NewShoppingMallFragment extends BaseFragment {

    @ViewInject(R.id.new_shopping_mall_load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.new_shop_mall_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.first_category_list)
    private ListView firstCategoryListView;
    @ViewInject(R.id.second_category_list)
    private GoochaoGridView secondCategoryListView;
    @ViewInject(R.id.third_category_list)
    private GoochaoListView thirdCategoryListView;
    @ViewInject(R.id.space_view)
    private View spaceView;

    private LayoutInflater inflater;

    private View secondHeadView;
    private ImageView secondHeadCover;

    private boolean firstLoad = true;
    private boolean loaded = false;

    //防止多次加载
    private boolean loading = true;
    //防止多次点击
    private boolean click = false;

    private JSONArray categoryArray = new JSONArray();
    private JSONArray secondCategoryArray = new JSONArray();  // type为1的子类目数组
    private JSONArray thirdCategoryArray = new JSONArray();  // type为2的子类目数组
    private String firstId;

    private int windowX;

    private GlideRequests glide;

    private FirstCategoryAdapter firstCategoryAdapter;
    private SecondCategoryAdapter secondCategoryAdapter;
    private ThirdCategoryAdapter thirdCategoryAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).setSearchType(SearchActivity.SEARCH_GOODS);
        ((ToolBarActivity) getActivity()).showToolBar();

        if (!loaded) {

            bindSource();

            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadFailView.setReloading();
                    if (loading) {
                        loading = false;
                        loadAllCategory();
                    }
                }
            });

            if (loading) {
                loading = false;
                loadAllCategory();
            }
        }
    }

    private void bindSource(){
        glide = GlideApp.with(getActivity());
        inflater = LayoutInflater.from(getActivity());

        secondHeadView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_mall_second_head , secondCategoryListView ,false);
        secondCategoryListView.addHeaderView(secondHeadView);
        secondHeadCover = secondHeadView.findViewById(R.id.second_category_head_cover);

        windowX = WindowUtils.getDeviceWidth(getActivity());
        firstCategoryAdapter = new FirstCategoryAdapter();
        firstCategoryListView.setAdapter(firstCategoryAdapter);
        secondCategoryAdapter = new SecondCategoryAdapter();
        secondCategoryListView.setAdapter(secondCategoryAdapter);
        thirdCategoryAdapter = new ThirdCategoryAdapter();
        thirdCategoryListView.setAdapter(thirdCategoryAdapter);

        int imgWidth = windowX * 252 / 375;
        int imgHeight = imgWidth * 96 / 252;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imgWidth, imgHeight);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        secondHeadCover.setLayoutParams(layoutParams);
    }

    /**
     * 加载一级类目和二级类目
     */
    private void loadAllCategory() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        HttpRequest.get(AppConfig.FIRST_AND_SECOND_CATEGORY, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loading = true;
                loadingView.setVisibility(View.GONE);
                loadFailView.setFinishReload();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray dataArray = JsonUtils.getJsonArray(response, "data", new JSONArray());

                    for (int i = 0; i < dataArray.length(); i++) {
                        try {
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            JSONArray tempArray = new JSONArray();
                            JSONObject allObject = new JSONObject();
                            allObject.put("name", "全部");
                            tempArray.put(0, allObject);

                            JSONArray childrenArray = JsonUtils.getJsonArray(dataObject, "children", new JSONArray());

                            for (int j = 0; j < childrenArray.length(); j++) {
                                tempArray.put(childrenArray.getJSONObject(j));
                            }
                            if (i == 0){
                                firstId = JsonUtils.getString(dataObject,"categoryId","");
                                secondCategoryArray = tempArray;
                                dataObject.put("click",true);
                            }else {
                                dataObject.put("click",false);
                            }
                            dataObject.put("children", tempArray);
                            categoryArray.put(dataObject);
                            loadFailView.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    firstCategoryAdapter.notifyDataSetChanged();
                    secondCategoryAdapter.notifyDataSetChanged();
                    firstLoad = false;
                    loaded = true;
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(getContext(), "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.GONE);
            AlertManager.showErrorInfo(this.getActivity());
        }
    }

    @Override
    public void onResume() {
        click = false;
        super.onResume();
    }

    class FirstCategoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return categoryArray == null ? 0 : categoryArray.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(categoryArray, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position,View convertView, ViewGroup parent) {

            convertView = inflater.inflate(R.layout.fragment_new_shop_mall_first_category_item, parent, false);
            final TextView firstCategoryName = convertView.findViewById(R.id.first_category_name);
            View line = convertView.findViewById(R.id.bottom_line);

            final JSONObject item = getItem(position);
            String firstName = JsonUtils.getString(item, "name", "");
            firstCategoryName.setText(firstName);

            int imgWidth = ChangeLineWidth.getWidth(firstName , getActivity());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imgWidth, WindowUtils.dpToPixels(getActivity() , 2));
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            WindowUtils.setMargins(line,0,5 ,0,0);
            line.setLayoutParams(layoutParams);

            boolean click = JsonUtils.getBoolean(item , "click",false);
            if (click) {
                firstCategoryName.setTypeface(Typeface.DEFAULT_BOLD);
                line.setVisibility(View.VISIBLE);

                String cover = JsonUtils.getString(item ,"image","");
                ImageUtils.loadImage(glide,ImageUtils.resize(cover, 1024, 390), secondHeadCover, R.mipmap.loadpicture);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        for (int i = 0; i < categoryArray.length();i++){
                            JsonUtils.getJsonItem(categoryArray,i,null).put("click",false);
                        }
                        JsonUtils.getJsonItem(categoryArray,position,null).put("click",true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    int type = JsonUtils.getInt(item , "type" , 0);
                    if (type != 2){
                        secondCategoryListView.setVisibility(View.VISIBLE);
                        secondHeadCover.setVisibility(View.VISIBLE);
                        thirdCategoryListView.setVisibility(View.GONE);
                        spaceView.setVisibility(View.GONE);
                        secondCategoryArray = JsonUtils.getJsonArray(item ,"children" , null);
                        firstId = JsonUtils.getString(item, "categoryId","");
                        firstCategoryAdapter.notifyDataSetChanged();
                        secondCategoryAdapter.notifyDataSetChanged();
                    }else {
                        secondCategoryListView.setVisibility(View.GONE);
                        secondHeadCover.setVisibility(View.GONE);
                        thirdCategoryListView.setVisibility(View.VISIBLE);
                        spaceView.setVisibility(View.VISIBLE);
                        thirdCategoryArray = JsonUtils.getJsonArray(item ,"children" , null);
                        firstId = JsonUtils.getString(item, "categoryId","");
                        firstCategoryAdapter.notifyDataSetChanged();
                        thirdCategoryAdapter.notifyDataSetChanged();
                    }
                }
            });

            return convertView;
        }
    }

    class SecondCategoryAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return secondCategoryArray == null ? 0 : (secondCategoryArray.length() - 1) ;
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(secondCategoryArray , position ,null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = inflater.inflate(R.layout.fragment_new_shop_mall_second_category_item , parent , false);
            ImageView secondCover = convertView.findViewById(R.id.second_category_cover);
            TextView secondName = convertView.findViewById(R.id.second_category_name);
            ZoomView zoomView = convertView.findViewById(R.id.second_category_zoom_view);

            final JSONObject item = getItem(position + 1);

            String secondCoverValue = JsonUtils.getString(item , "image" , "");
            ImageUtils.loadImage(glide,ImageUtils.resize(secondCoverValue, 640, 640),secondCover,R.mipmap.loadpicture);

            String secondNameValue = JsonUtils.getString(item , "name" , "");
            secondName.setText(secondNameValue);

            zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (click){
                        return;
                    }
                    click = true;
                    Intent intent = new Intent(getActivity(), ProductListActivity.class);
                    intent.putExtra("categoryId", JsonUtils.getString(item, "categoryId", ""));
                    intent.putExtra("firstId", firstId);
                    intent.putExtra("categoryList", secondCategoryArray.toString());
                    intent.putExtra("all", "");
                    getActivity().startActivity(intent);
                }
            });

            secondHeadCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (click){
                        return;
                    }
                    click = true;
                    Intent intent = new Intent(getActivity(), ProductListActivity.class);
                    intent.putExtra("categoryId", JsonUtils.getString(item , "categoryId", ""));
                    intent.putExtra("all", "1");
                    intent.putExtra("firstId", firstId);
                    intent.putExtra("categoryList", secondCategoryArray.toString());
                    getActivity().startActivity(intent);
                }
            });

            return convertView;
        }
    }

    class ThirdCategoryAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return thirdCategoryArray == null? 0 : thirdCategoryArray.length() - 1;
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(thirdCategoryArray , position ,null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = inflater.inflate(R.layout.fragment_new_shop_mall_third_category_item,parent,false);

            final JSONObject thirdItem = getItem(position + 1);

            ImageView thirdCover = convertView.findViewById(R.id.third_category_cover);
            int imgWidth = windowX * 252 / 375;
            int imgHeight = imgWidth * 96 / 252;

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imgWidth, imgHeight);
            thirdCover.setLayoutParams(layoutParams);

            String thirdCoverValue = JsonUtils.getString(thirdItem , "image" , "");
            ImageUtils.loadImage(glide,ImageUtils.resize(thirdCoverValue, 1024, 390),thirdCover,R.mipmap.loadpicture);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (click){
                        return;
                    }
                    click = true;
                    Intent intent = new Intent(getActivity(), ProductListActivity.class);
                    intent.putExtra("categoryId", JsonUtils.getString(thirdItem, "categoryId", ""));
                    intent.putExtra("firstId", firstId);
                    intent.putExtra("categoryList", thirdCategoryArray.toString());
                    intent.putExtra("all", "");
                    getActivity().startActivity(intent);
                }
            });

            return convertView;
        }
    }
}
