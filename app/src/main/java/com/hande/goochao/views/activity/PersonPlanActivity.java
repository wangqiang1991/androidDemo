package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.BaseActivity;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.refresh.NoDataTwoLineView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/9/12.
 */
public class PersonPlanActivity extends ToolBarActivity {

    @ViewInject(R.id.collection_plan_list_view)
    private ListView planListView;
    private PlanListAdapter planListAdapter;

    @ViewInject(R.id.collection_plan_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.collection_plan_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.collection_plan_noDataView)
    private NoDataView noDataView;

    private JSONArray planResource;
    private JSONArray planList = new JSONArray();

    private boolean firstLoad = true;
    private boolean loaded = false;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_plan);
        hideBackText();
        setTitle("我领取&关注的软装方案");

        glide = GlideApp.with(this);

        planListAdapter = new PlanListAdapter();
        planListView.setAdapter(planListAdapter);
        planListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PersonPlanActivity.this, DesignPlanDetailActivity.class);
                String planId = JsonUtils.getString(JsonUtils.getJsonItem(planList, position, null), "planId", null);
                intent.putExtra("planId", planId);
                startActivity(intent);
            }
        });

        loaded = true;

        noDataView.setImageAndText(R.mipmap.no_data_plan, "你还没有关注软装方案哦");

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadPlan();
            }
        });
        loadPlan();

        EventBus.getDefault().register(this);
    }

    private void loadPlan() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        HttpRequest.get(AppConfig.COLLECTION_PLAN , null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    planResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < planResource.length(); i++) {
                        try {
                            planList.put(planResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    System.out.print(planList);
                    if (planResource.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                    }
                    planListAdapter.notifyDataSetChanged();
                    firstLoad = false;
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(PersonPlanActivity.this, "服务器繁忙，请稍后重试", false);
                showError();
            }
        });
    }

    class PlanListAdapter extends BaseAdapter {

        class ViewHolder {
            TextView txtTitleValue;
            TextView txtContentValue;
            ImageView imageView;

            ViewHolder(View view) {
                txtTitleValue = view.findViewById(R.id.plan_title);
                txtContentValue = view.findViewById(R.id.plan_content);
                imageView = view.findViewById(R.id.plan_image);
            }
        }

        @Override
        public int getCount() {
            return planList == null ? 0 : planList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(planList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(PersonPlanActivity.this).inflate(R.layout.person_plan_item, viewGroup, false);
                view.setTag(new ViewHolder(view));
            }

            ViewHolder holder = (ViewHolder) view.getTag();
            holder.txtTitleValue.setText(JsonUtils.getString(getItem(i), "planName", ""));
            WindowUtils.boldMethod(holder.txtTitleValue);

            String typeName = JsonUtils.getString(getItem(i), "houseTypeName", "");

            JSONObject styleOb = JsonUtils.getJsonObject(getItem(i),"style",null);
            String style = JsonUtils.getString(styleOb, "name", "");
            holder.txtContentValue.setText(typeName + " | " + style);
            WindowUtils.boldMethod(holder.txtContentValue);

            ImageUtils.loadImage(glide, JsonUtils.getString(getItem(i), "cover", ""), holder.imageView, R.mipmap.loadpicture);

            return view;
        }
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            AlertManager.showErrorInfo(this);
            loadingView.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_plan_delete)) {
            planResource = new JSONArray();
            planList = new JSONArray();
            loadPlan();
        }else if (notification.getKey().equals(EventBusNotification.event_bus_plan_get)) {
            planResource = new JSONArray();
            planList = new JSONArray();
            loadPlan();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
