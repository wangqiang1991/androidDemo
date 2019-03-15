package com.hande.goochao.views.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.WebViewActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

@ContentView(R.layout.fragment_problem)
public class HelpProblemFragment extends BaseFragment{

    @ViewInject(R.id.problem_list)
    private ListView itemListView;
    private boolean loaded = false;
    private JSONArray serviceVos;

    private JSONArray data = new JSONArray();

    private ProblemListAdapter listAdapter;

    @SuppressLint("ValidFragment")
    public HelpProblemFragment(JSONArray serviceVos) {
        this.serviceVos = serviceVos;
    }

    public HelpProblemFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!loaded){
            listAdapter = new ProblemListAdapter();
            itemListView.setAdapter(listAdapter);
            loaded = true;
            listAdapter.notifyDataSetChanged();
        }
    }

    class ProblemListAdapter extends BaseAdapter {

        class ViewHolder {
            TextView txtTitleValue;

            ViewHolder(View view) {
                txtTitleValue = view.findViewById(R.id.item_name);
            }
        }

        @Override
        public int getCount() {
            return data == null ? 0 : serviceVos.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(serviceVos, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_help_center_item, viewGroup, false);
                view.setTag(new ViewHolder(view));
            }
            ViewHolder holder = (ViewHolder) view.getTag();

            final String detailId = JsonUtils.getString(getItem(i),"detailId","");
            final String title = JsonUtils.getString(getItem(i),"title","");

            holder.txtTitleValue.setText(title);
            holder.txtTitleValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = AppConfig.ARTICLE_CONTENT + detailId;
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra("title",title);
                    intent.putExtra("url",url);
                    startActivity(intent);
                }
            });

            return view;
        }
    }
}
