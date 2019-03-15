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

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
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
public class SystemMessageActivity extends ToolBarActivity {

    @ViewInject(R.id.refresh_system_message_list)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.system_message_load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.system_message_noDataView)
    private NoDataView noDataView;
    @ViewInject(R.id.system_message_loading_view)
    private LoadingView loadingView;

    @ViewInject(R.id.system_message_list_view)
    private GoochaoListView listView;
    private SystemMessageAdapter systemMessageAdapter;

    private LayoutInflater inflater;
    private GlideRequests glide;
    private int kWidth;

    private JSONArray systemMessageResource;
    private JSONArray systemMessageArray;
    private int pageSize = 20;
    private int pageIndex = 1;
    private boolean firstLoad = true;

    //防止多次点击
    private boolean click = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_message);
        setTitle("系统消息");
        hideBackText();

        glide = GlideApp.with(this);
        inflater = LayoutInflater.from(this);
        kWidth = WindowUtils.getDeviceWidth(this);
        noDataView.setImageAndText(R.mipmap.no_message_view, "暂时没有消息通知");

        systemMessageAdapter = new SystemMessageAdapter();
        listView.setAdapter(systemMessageAdapter);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                pageIndex = 1;
                loadSystemMessage();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                loadSystemMessage();
            }
        });
        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadSystemMessage();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (click){
                    return;
                }
                click = true;
                JSONObject item = JsonUtils.getJsonItem(systemMessageArray, position, null);

                String params = JsonUtils.getString(item, "params", "");
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(params);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int messageType = JsonUtils.getInt(jsonObject, "messageType", 0);
                if (messageType >= 3 && messageType <= 8) {
                    String relationId = JsonUtils.getString(item, "detail.relationId", "");
                    Intent intent = new Intent(SystemMessageActivity.this, AfterSaleDetailActivity.class);
                    intent.putExtra("relationId", relationId);
                    startActivity(intent);
                } else if (messageType == 1) {
                    Intent intent = new Intent(SystemMessageActivity.this, MyOrderActivity.class);
                    intent.putExtra("selectedIndex", 2);
                    startActivity(intent);
                } else if (messageType == 2) {
                    Intent intent = new Intent(SystemMessageActivity.this, MyOrderActivity.class);
                    intent.putExtra("selectedIndex", 3);
                    startActivity(intent);
                }
            }
        });

        loadSystemMessage();
    }

    private void loadSystemMessage() {

        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", "" + pageIndex);
        params.put("pageSize", "" + pageSize);
        HttpRequest.get(AppConfig.GET_SYSTEM_MESSAGE, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (pageIndex == 1) {
                    systemMessageArray = new JSONArray();
                }
                if (JsonUtils.getCode(response) == 0) {
                    systemMessageResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < systemMessageResource.length(); i++) {
                        try {
                            systemMessageArray.put(systemMessageResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (systemMessageArray.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        if (systemMessageResource.length() < pageSize) {
                            listView.setFooterViewVisibility(View.VISIBLE);
                            refreshLayout.setNoMoreData(true);
                            refreshLayout.setEnableLoadMore(false);
                        } else {
                            listView.setFooterViewVisibility(View.GONE);
                            refreshLayout.setNoMoreData(false);
                            refreshLayout.setEnableLoadMore(true);
                        }
                    }
                    systemMessageAdapter.notifyDataSetChanged();
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
        params.put("messageType", "5");

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

    class SystemMessageAdapter extends BaseAdapter {


        class ViewHolder {

            TextView systemDate;
            ImageView systemCover;
            ZoomView systemZoom;
            TextView systemDesc;
            TextView dingdanzhuangtaitixing;

            ViewHolder(View convertView) {
                systemDate = convertView.findViewById(R.id.system_message_date);
                systemCover = convertView.findViewById(R.id.system_message_cover);
                systemZoom = convertView.findViewById(R.id.system_message_zoom_view);
                systemDesc = convertView.findViewById(R.id.system_message_desc);
                dingdanzhuangtaitixing = convertView.findViewById(R.id.txt_dingdanzhuangtaitixing);
            }

        }

        @Override
        public int getCount() {
            return systemMessageArray == null ? 0 : systemMessageArray.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(systemMessageArray, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.system_message_item, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            final JSONObject item = getItem(position);

            String systemDescStr = JsonUtils.getString(item, "content", "");
            viewHolder.systemDesc.setText(systemDescStr);

            String systemDateStr = JsonUtils.getString(item, "createdAt", "");
            viewHolder.systemDate.setText(DateUtils.timeStampToStr(Long.parseLong(systemDateStr), "yyyy-MM-dd HH:mm"));

            double itemWidth = kWidth * 80 / 375;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.systemCover.getLayoutParams();
            params.width = (int) itemWidth;
            params.height = (int) itemWidth;
            viewHolder.systemCover.setLayoutParams(params);

            String cover = JsonUtils.getString(item, "detail.cover", "");
            ImageUtils.loadImage(glide, ImageUtils.resize(cover, 500, 500), viewHolder.systemCover, -1);

            viewHolder.systemZoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (click){
                        return;
                    }
                    click = true;
                    String params = JsonUtils.getString(item, "params", "");
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(params);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int messageType = JsonUtils.getInt(jsonObject, "messageType", 0);
                    if (messageType >= 3 && messageType <= 8) {
                        String relationId = JsonUtils.getString(item, "detail.relationId", "");
                        Intent intent = new Intent(SystemMessageActivity.this, AfterSaleDetailActivity.class);
                        intent.putExtra("relationId", relationId);
                        startActivity(intent);
                    } else if (messageType == 1) {
                        Intent intent = new Intent(SystemMessageActivity.this, MyOrderActivity.class);
                        intent.putExtra("selectedIndex", 2);
                        startActivity(intent);
                    } else if (messageType == 2) {
                        Intent intent = new Intent(SystemMessageActivity.this, MyOrderActivity.class);
                        intent.putExtra("selectedIndex", 3);
                        startActivity(intent);
                    }
                }
            });
            WindowUtils.boldMethod(viewHolder.dingdanzhuangtaitixing);

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
