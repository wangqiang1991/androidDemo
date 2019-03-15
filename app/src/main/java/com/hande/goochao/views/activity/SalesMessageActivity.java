package com.hande.goochao.views.activity;

import android.content.Intent;
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
public class SalesMessageActivity extends ToolBarActivity {

    @ViewInject(R.id.refresh_sales_message_list)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.sales_message_load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.sales_message_noDataView)
    private NoDataView noDataView;
    @ViewInject(R.id.sales_message_loading_view)
    private LoadingView loadingView;

    @ViewInject(R.id.sales_message_list_view)
    private GoochaoListView listView;
    private SalesMessageAdapter salesMessageAdapter;

    private LayoutInflater inflater;
    private GlideRequests glide;
    private int kWidth;

    private JSONArray salesMessageResource;
    private JSONArray salesMessageArray;
    private int pageSize = 20;
    private int pageIndex = 1;
    private boolean firstLoad = true;

    //防止多次点击
    private boolean click = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_message);
        setTitle("活动通知");
        hideBackText();

        glide = GlideApp.with(this);
        inflater = LayoutInflater.from(this);
        kWidth = WindowUtils.getDeviceWidth(this);
        noDataView.setImageAndText(R.mipmap.no_message_view, "暂时没有消息通知");

        salesMessageAdapter = new SalesMessageAdapter();
        listView.setAdapter(salesMessageAdapter);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                pageIndex = 1;
                loadSalesMessage();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                loadSalesMessage();
            }
        });
        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadSalesMessage();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (click){
                    return;
                }
                click = true;
                JSONObject jsonObject = JsonUtils.getJsonItem(salesMessageArray,position,null);
                String messageId = JsonUtils.getString(jsonObject,"messageId","");
                recordRead(messageId);
                String activityUrl = JsonUtils.getString(jsonObject, "detail.url", "");
                String activityId = JsonUtils.getString(jsonObject, "detail.activityId", "");
                Intent intent = new Intent(SalesMessageActivity.this, SaleGiftActivity.class);
                if (activityUrl.indexOf("?") == -1){
                    intent.putExtra("url", activityUrl + "?activityId=" + activityId);
                }else {
                    intent.putExtra("url", activityUrl + "&activityId=" + activityId);
                }
                startActivity(intent);
            }
        });

        loadSalesMessage();
    }

    private void loadSalesMessage() {

        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", "" + pageIndex);
        params.put("pageSize", "" + pageSize);
        params.put("messageType", "3");
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
                    salesMessageArray = new JSONArray();
                }
                if (JsonUtils.getCode(response) == 0) {
                    salesMessageResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < salesMessageResource.length(); i++) {
                        try {
                            salesMessageArray.put(salesMessageResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (salesMessageArray.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        if (salesMessageResource.length() < pageSize) {
                            listView.setFooterViewVisibility(View.VISIBLE);
                            refreshLayout.setNoMoreData(true);
                            refreshLayout.setEnableLoadMore(false);
                        } else {
                            listView.setFooterViewVisibility(View.GONE);
                            refreshLayout.setNoMoreData(false);
                            refreshLayout.setEnableLoadMore(true);
                        }
                    }
                    salesMessageAdapter.notifyDataSetChanged();
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
        params.put("messageType", "3");

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

    class SalesMessageAdapter extends BaseAdapter {


        class ViewHolder {

            TextView salesDate;
            TextView salesTitle;
            ImageView salesCover;
            ZoomView salesZoom;
            TextView salesDesc;
            TextView chakanhuodongxiangqing;

            ViewHolder(View convertView) {
                salesDate = convertView.findViewById(R.id.sales_message_date);
                salesTitle = convertView.findViewById(R.id.sales_message_name);
                salesCover = convertView.findViewById(R.id.sales_message_cover);
                salesZoom = convertView.findViewById(R.id.sales_message_zoom_view);
                salesDesc = convertView.findViewById(R.id.sales_message_desc);
                chakanhuodongxiangqing = convertView.findViewById(R.id.txt_chakanhuodongxiangqing);
            }

        }

        @Override
        public int getCount() {
            return salesMessageArray == null ? 0 : salesMessageArray.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(salesMessageArray, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.sales_message_item, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            final JSONObject item = getItem(position);

            String salesNameStr = JsonUtils.getString(item,"messageTitle","");
            viewHolder.salesTitle.setText(salesNameStr);

            String salesDescStr = JsonUtils.getString(item, "messageDesc", "");
            viewHolder.salesDesc.setText(salesDescStr);

            String salesDateStr = JsonUtils.getString(item, "createdAt", "");
            viewHolder.salesDate.setText(DateUtils.timeStampToStr(Long.parseLong(salesDateStr), "yyyy-MM-dd HH:mm"));

            double itemWidth = kWidth * 325 / 375;
            double itemHeight = kWidth * 165 / 375;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.salesCover.getLayoutParams();
            params.width = (int) itemWidth;
            params.height = (int) itemHeight;
            viewHolder.salesCover.setLayoutParams(params);

            String cover = JsonUtils.getString(item, "messageCover", "");
            ImageUtils.loadImage(glide, ImageUtils.resize(cover, 1024, 520), viewHolder.salesCover, -1);

            WindowUtils.boldMethod(viewHolder.salesTitle);
            WindowUtils.boldMethod(viewHolder.salesDesc);
            WindowUtils.boldMethod(viewHolder.chakanhuodongxiangqing);

            viewHolder.salesZoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (click){
                        return;
                    }
                    click = true;
                    String messageId = JsonUtils.getString(item,"messageId","");
                    recordRead(messageId);
                    String activityUrl = JsonUtils.getString(item, "detail.url", "");
                    String activityId = JsonUtils.getString(item, "detail.activityId", "");
                    Intent intent = new Intent(SalesMessageActivity.this, SaleGiftActivity.class);
                    if (activityUrl.indexOf("?") == -1){
                        intent.putExtra("url", activityUrl + "?activityId=" + activityId);
                    }else {
                        intent.putExtra("url", activityUrl + "&activityId=" + activityId);
                    }
                    startActivity(intent);
                }
            });

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
