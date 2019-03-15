package com.hande.goochao.views.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import com.hande.goochao.utils.DateUtils;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.widget.GoochaoListView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/11/6.
 */
public class RecommendsMessageActivity extends ToolBarActivity {

    @ViewInject(R.id.refresh_recommends_message_list)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.recommends_message_load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.recommends_message_noDataView)
    private NoDataView noDataView;
    @ViewInject(R.id.recommends_message_loading_view)
    private LoadingView loadingView;

    @ViewInject(R.id.recommends_message_list_view)
    private GoochaoListView listView;
    private RecommendsMessageAdapter recommendsMessageAdapter;

    private LayoutInflater inflater;
    private GlideRequests glide;
    private int kWidth;

    private JSONArray recommendsMessageResource;
    private JSONArray recommendsMessageArray;
    private int pageSize = 20;
    private int pageIndex = 1;
    private boolean firstLoad = true;

    //防止多次点击
    private boolean click = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommends_message);
        setTitle("构巢精选");
        hideBackText();

        glide = GlideApp.with(this);
        inflater = LayoutInflater.from(this);
        kWidth = WindowUtils.getDeviceWidth(this);
        noDataView.setImageAndText(R.mipmap.no_message_view, "暂时没有消息通知");

        recommendsMessageAdapter = new RecommendsMessageAdapter();
        listView.setAdapter(recommendsMessageAdapter);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                pageIndex = 1;
                loadRecommendsMessage();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                loadRecommendsMessage();
            }
        });
        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadRecommendsMessage();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (click){
                    return;
                }
                click = true;
                JSONObject item = JsonUtils.getJsonItem(recommendsMessageArray, position, null);
                int type = JsonUtils.getInt(item, "messageType", 0);
                if (type == 2) {
                    String subjectId = JsonUtils.getString(item, "objectId", "");
                    String messageId = JsonUtils.getString(item,"messageId","");
                    recordRead(messageId);
                    Intent intent = new Intent(RecommendsMessageActivity.this, MagazineDetailActivity.class);
                    intent.putExtra("subjectArticleId", subjectId);
                    startActivity(intent);
                } else if (type == 1) {
                    String goodsId = JsonUtils.getString(item ,"objectId","");
                    String messageId = JsonUtils.getString(item,"messageId","");
                    recordRead(messageId);
                    Intent intent = new Intent(RecommendsMessageActivity.this, NewProductInformationActivity.class);
                    intent.putExtra("goodsId", goodsId);
                    startActivity(intent);
                }
            }
        });

        loadRecommendsMessage();
    }

    private void loadRecommendsMessage() {

        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", "" + pageIndex);
        params.put("pageSize", "" + pageSize);
        params.put("messageType", "4");
        HttpRequest.get(AppConfig.GET_NOTICE_MESSAGE, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (pageIndex == 1) {
                    recommendsMessageArray = new JSONArray();
                }
                if (JsonUtils.getCode(response) == 0) {
                    recommendsMessageResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < recommendsMessageResource.length(); i++) {
                        try {
                            recommendsMessageArray.put(recommendsMessageResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (recommendsMessageArray.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        if (recommendsMessageResource.length() < pageSize) {
                            listView.setFooterViewVisibility(View.VISIBLE);
                            refreshLayout.setNoMoreData(true);
                            refreshLayout.setEnableLoadMore(false);
                        } else {
                            listView.setFooterViewVisibility(View.GONE);
                            refreshLayout.setNoMoreData(false);
                            refreshLayout.setEnableLoadMore(true);
                        }
                    }
                    recommendsMessageAdapter.notifyDataSetChanged();
                    firstLoad = false;
                    resetView();
                    pageIndex++;
                    clearUnRead();
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

    private void clearUnRead(){

        Map<String, String> params = new HashMap<>();
        params.put("messageType", "4");

        HttpRequest.get(AppConfig.CLEAR_UNREAD_MESSAGE, null, params, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject response) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

        });

    }

    private void recordRead(String messageId){

        HttpRequest.head(AppConfig.RECORD_READ + messageId, null, null, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject response) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

        });

    }

    class RecommendsMessageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return recommendsMessageArray == null ? 0 : recommendsMessageArray.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(recommendsMessageArray, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            int type = JsonUtils.getInt(JsonUtils.getJsonItem(recommendsMessageArray, position, null), "messageType", 0);
            //数据类型为期刊
            if (type == 2) {
                convertView = inflater.inflate(R.layout.recommends_article_item, parent, false);
                final JSONObject item = getItem(position);

                TextView articleDate = convertView.findViewById(R.id.recommends_article_message_date);
                TextView articleTitle = convertView.findViewById(R.id.article_message_name);
                ImageView articleCover = convertView.findViewById(R.id.article_message_cover);
                ZoomView articleZoom = convertView.findViewById(R.id.article_message_zoom_view);
                TextView articleDesc = convertView.findViewById(R.id.article_message_desc);

                String articleDateStr = JsonUtils.getString(item, "createdAt", "");
                articleDate.setText(DateUtils.timeStampToStr(Long.parseLong(articleDateStr), "yyyy-MM-dd HH:mm"));

                String articleTitleStr = JsonUtils.getString(item, "messageTitle", "");
                articleTitle.setText(articleTitleStr);

                String articleDescStr = JsonUtils.getString(item, "messageDesc", "");
                articleDesc.setText(articleDescStr);

                double itemWidth = kWidth * 325 / 375;
                double itemHeight = kWidth * 165 / 375;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) articleCover.getLayoutParams();
                params.width = (int) itemWidth;
                params.height = (int) itemHeight;
                articleCover.setLayoutParams(params);

                String articleCoverStr = JsonUtils.getString(item, "messageCover", "");
                ImageUtils.loadImage(glide, ImageUtils.resize(articleCoverStr, 1024, 520), articleCover, -1);

                articleZoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (click){
                            return;
                        }
                        click = true;
                        String subjectId = JsonUtils.getString(item, "objectId", "");
                        String messageId = JsonUtils.getString(item,"messageId","");
                        recordRead(messageId);
                        Intent intent = new Intent(RecommendsMessageActivity.this, MagazineDetailActivity.class);
                        intent.putExtra("subjectArticleId", subjectId);
                        startActivity(intent);
                    }
                });

                TextView yueduquanwen = convertView.findViewById(R.id.txt_yueduquanwen);
                WindowUtils.boldMethod(yueduquanwen);
                WindowUtils.boldMethod(articleTitle);
                WindowUtils.boldMethod(articleDesc);
            }
            //数据类型为商品
            else if (type == 1) {
                convertView = inflater.inflate(R.layout.recommends_goods_item, parent, false);
                final JSONObject item = getItem(position);

                TextView goodsDate = convertView.findViewById(R.id.goods_message_date);
                ImageView goodsCover = convertView.findViewById(R.id.goods_message_cover);
                TextView goodsTitle = convertView.findViewById(R.id.goods_message_name);
                TextView goodsDesc = convertView.findViewById(R.id.goods_message_desc);
                TextView goodsPrice = convertView.findViewById(R.id.goods_message_price);
                TextView goodsOldPrice = convertView.findViewById(R.id.goods_message_old_price);
                ZoomView goodsZoom = convertView.findViewById(R.id.goods_message_zoom_view);

                String goodsDateStr = JsonUtils.getString(item, "createdAt", "");
                goodsDate.setText(DateUtils.timeStampToStr(Long.parseLong(goodsDateStr), "yyyy-MM-dd HH:mm"));

                String goodsDescStr = JsonUtils.getString(item, "messageDesc", "");
                goodsDesc.setText(goodsDescStr);

                String goodsTitleStr = JsonUtils.getString(item, "messageTitle", "");
                goodsTitle.setText(goodsTitleStr);

                double itemWidth = kWidth * 80 / 375;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) goodsCover.getLayoutParams();
                params.width = (int) itemWidth;
                params.height = (int) itemWidth;
                goodsCover.setLayoutParams(params);

                String goodsCoverStr = JsonUtils.getString(item, "messageCover", "");
                ImageUtils.loadImage(glide, ImageUtils.resize(goodsCoverStr, 500, 500), goodsCover, -1);

                //判断商品类型（满减 折扣 原价）
                JSONObject goodsObject = JsonUtils.getJsonObject(item,"detail",null);
                String tagValue = JsonUtils.getString(goodsObject, "discountTag", "");
                if (tagValue.equals("")) {
                    goodsOldPrice.setVisibility(View.GONE);
                    double price = JsonUtils.getDouble(goodsObject, "minPrice", 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    goodsPrice.setText("¥" + PriceUtils.beautify(priceValue));
                }                //原价 无活动
                else {
                    if (JsonUtils.getInt(goodsObject, "discountType", 0) == 2) {

                        goodsOldPrice.setVisibility(View.VISIBLE);

                        double oldPrice = JsonUtils.getDouble(goodsObject, "minPrice", 0);
                        double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                        goodsOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                        goodsOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                        double newPrice = JsonUtils.getDouble(goodsObject, "discountPrice", 0);
                        double newPriceValue = NumberUtils.decimalDouble(newPrice);
                        goodsPrice.setText("¥" + PriceUtils.beautify(newPriceValue));
                    }             //折扣
                    else {
                        goodsOldPrice.setVisibility(View.GONE);
                        double price = JsonUtils.getDouble(goodsObject, "minPrice", 0);
                        double priceValue = NumberUtils.decimalDouble(price);
                        goodsPrice.setText("¥" + PriceUtils.beautify(priceValue));
                    }             //满减
                }

                goodsZoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (click){
                            return;
                        }
                        click = true;
                        String goodsId = JsonUtils.getString(item ,"objectId","");
                        String messageId = JsonUtils.getString(item,"messageId","");
                        recordRead(messageId);
                        Intent intent = new Intent(RecommendsMessageActivity.this, NewProductInformationActivity.class);
                        intent.putExtra("goodsId", goodsId);
                        startActivity(intent);
                    }
                });

                TextView chakanxiangqing = convertView.findViewById(R.id.txt_chakanxiangqing);
                WindowUtils.boldMethod(chakanxiangqing);

                TextView jingxuanhaohuotuijian = convertView.findViewById(R.id.txt_jingxuanhaohuotuijian);
                WindowUtils.boldMethod(jingxuanhaohuotuijian);
                WindowUtils.boldMethod(goodsTitle);
                WindowUtils.boldMethod(goodsDesc);
                WindowUtils.boldMethod(goodsPrice);

            }
            return convertView;
        }
    }

    @Override
    protected void onResume() {
        click = false;
        super.onResume();
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.GONE);
        }
    }
}
