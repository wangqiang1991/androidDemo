package com.hande.goochao.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.hande.goochao.views.components.ExpandableView;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;

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
@ContentView(R.layout.fragment_new_shopping_mall_v2)
public class NewShoppingMallFragmentV2 extends BaseFragment {

    @ViewInject(R.id.new_shopping_mall_load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.new_shop_mall_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.expandable_view)
    private ExpandableView expandableView;

    private boolean loaded = false;

    private JSONArray categoryArray = new JSONArray();

    private GlideRequests glide;

    private MallAdapter adapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).setSearchType(SearchActivity.SEARCH_GOODS);
        ((ToolBarActivity) getActivity()).showToolBar();

        if (!loaded) {
            loaded = true;

            glide = GlideApp.with(getActivity());
            adapter = new MallAdapter();
            expandableView.setAdapter(adapter);

            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadFailView.setReloading();
                    loadAllCategory();
                }
            });

            loadAllCategory();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((ToolBarActivity) getActivity()).showToolBar();
            ((ToolBarActivity) getActivity()).hideTitle();
        }
    }

    /**
     * 加载一级类目和二级类目
     */
    private void loadAllCategory() {
        loadingView.setVisibility(View.VISIBLE);
        HttpRequest.get(AppConfig.FIRST_AND_SECOND_CATEGORY, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
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

                            for (int j = 0;j < childrenArray.length(); j++) {
                                tempArray.put(childrenArray.getJSONObject(j));
                            }
                            dataObject.put("children", tempArray);
                            categoryArray.put(dataObject);
                            loadFailView.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    adapter.notifyDataSetChanged();
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
        loadFailView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
    }

    class MallAdapter extends ExpandableView.ExpandableViewAdapter {

        @Override
        public View getHeaderView(int position) {
            int imgWidth = WindowUtils.getDeviceWidth(getContext()) - WindowUtils.dpToPixels(getContext(), 60);
            int imgHeight = imgWidth * 120 / 315;

            ImageView imageView = new ImageView(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imgWidth, imgHeight);
            layoutParams.setMargins(WindowUtils.dpToPixels(getContext(), 30), 0, 0, 0);
            imageView.setLayoutParams(layoutParams);

            JSONObject item = getItem(position);
            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(item, "image", ""), 1024, 390), imageView, R.mipmap.loadpicture);
            return imageView;
        }

        @Override
        public View getContentView(final int position) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(0, WindowUtils.dpToPixels(getContext(), 15), 0, 0);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);

            JSONObject item = getItem(position);
            final JSONArray childrenArray = JsonUtils.getJsonArray(item, "children", new JSONArray());

            for (int i = 0; i < childrenArray.length(); i++) {
                View view = inflater.inflate(R.layout.new_shopping_mall_second_item, null);
                TextView textView = view.findViewById(R.id.second_category_name);
                textView.setText(JsonUtils.getString(JsonUtils.getJsonItem(childrenArray, i, null), "name", ""));
                WindowUtils.boldMethod(textView);
                textView.setTag(i);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = (int) v.getTag();
                        if (index == 0) {
                            Intent intent = new Intent(getActivity(), ProductListActivity.class);
                            intent.putExtra("categoryId", JsonUtils.getString(getItem(position), "categoryId", ""));
                            intent.putExtra("all", "1");
                            intent.putExtra("firstId", JsonUtils.getString(getItem(position), "categoryId", ""));
                            intent.putExtra("categoryList", childrenArray.toString());
                            getActivity().startActivity(intent);
                        } else {
                            JSONArray secondList = JsonUtils.getJsonArray(getItem(position), "children", null);
                            Intent intent = new Intent(getActivity(), ProductListActivity.class);
                            intent.putExtra("categoryId", JsonUtils.getString(JsonUtils.getJsonItem(secondList, index, null), "categoryId", ""));
                            intent.putExtra("firstId", JsonUtils.getString(getItem(position), "categoryId", ""));
                            intent.putExtra("categoryList", childrenArray.toString());
                            intent.putExtra("all", "");
                            getActivity().startActivity(intent);
                        }
                    }
                });
                linearLayout.addView(view);
            }

            return linearLayout;
        }

        @Override
        public int getCount() {
            return categoryArray.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(categoryArray, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
