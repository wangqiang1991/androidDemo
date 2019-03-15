package com.hande.goochao.views.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.Params;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.KeyboardUtil;
import com.hande.goochao.views.base.BaseActivity;
import com.hande.goochao.views.base.PayCloseNoToolBarActivity;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.ClearEditText;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.widget.SearchGoodsView;
import com.hande.goochao.views.widget.SearchPictureView;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends PayCloseNoToolBarActivity implements View.OnClickListener, ClearEditText.TextOnChangeListener, View.OnKeyListener, TagFlowLayout.OnTagClickListener, AdapterView.OnItemClickListener {

    /**
     * 搜索所有
     */
    public static final int SEARCH_ALL = 0;
    /**
     * 搜索商品
     */
    public static final int SEARCH_GOODS = 1;
    /**
     * 搜索图片
     */
    public static final int SEARCH_PICTURE = 2;

    @ViewInject(R.id.close_btn)
    private TextView closeBtn;
    @ViewInject(R.id.goods_list_view)
    private ListView goodsListView;
    @ViewInject(R.id.picture_list_view)
    private ListView pictureListView;
    @ViewInject(R.id.search_history_layout)
    private TagFlowLayout searchHistoryLayout;
    @ViewInject(R.id.hot_search_layout)
    private TagFlowLayout hotSearchLayout;
    @ViewInject(R.id.history_layout)
    private View historyLayout;
    @ViewInject(R.id.hot_layout)
    private View hotLayout;
    @ViewInject(R.id.goods_line_view)
    private View goodsLineView;
    @ViewInject(R.id.picture_line_view)
    private View pictureLineView;
    @ViewInject(R.id.search_edit)
    private ClearEditText searchEdit;
    @ViewInject(R.id.history_del_btn)
    private View historyDelBtn;
    @ViewInject(R.id.search_result_view)
    private View searchResultView;
    @ViewInject(R.id.search_goods_view)
    private SearchGoodsView searchGoodsView;
    @ViewInject(R.id.search_picture_view)
    private SearchPictureView searchPictureView;
    @ViewInject(R.id.search_scroll_view)
    private View searchScrollView;

    private List<String> goodsList;
    private List<String> pictureList;
    private List<String> historyTags;
    private List<String> hotTags;

    private String searchKeyword;
    private boolean inputSearch;

    private int searchType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchType = getIntent().getIntExtra("searchType", SEARCH_ALL);
        if (searchType < SEARCH_ALL || searchType > SEARCH_PICTURE) {
            AlertManager.showErrorToast(this, "参数无效", false);
            return;
        }

        goodsList = new ArrayList<>();
        pictureList = new ArrayList<>();
        historyTags = new ArrayList<>();
        hotTags = new ArrayList<>();

        if (searchType == SEARCH_GOODS){
            searchEdit.setHint("搜索商品");
        }else if (searchType == SEARCH_PICTURE){
            searchEdit.setHint("搜索图片");
        }else if (searchType == SEARCH_ALL){
            searchEdit.setHint("搜索商品、图片");
        }

        searchEdit.setTextOnChangeListener(this);
        searchEdit.setOnKeyListener(this);
        searchEdit.setOnClickListener(this);
        closeBtn.setOnClickListener(this);

        goodsListView.setAdapter(new SearchResultAdapter(goodsList));
        pictureListView.setAdapter(new SearchResultAdapter(pictureList));
        goodsListView.setOnItemClickListener(this);
        pictureListView.setOnItemClickListener(this);

        searchHistoryLayout.setAdapter(new SearchTagAdapter(historyTags));
        hotSearchLayout.setAdapter(new SearchTagAdapter(hotTags));

        searchHistoryLayout.setOnTagClickListener(this);
        hotSearchLayout.setOnTagClickListener(this);

        historyDelBtn.setOnClickListener(this);

        refreshKeywordResultView();
        refreshHistoryKeywordsView();
        refreshHotKeywordsView();
        loadHotKeywords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchPictureView.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == closeBtn) {
            finish();
        } else if (v == historyDelBtn) {
            doClearSearchHistory();
        } else if (v == searchEdit) {
            showSearchView();
        }
    }

    private void showSearchView() {
        searchScrollView.setVisibility(View.VISIBLE);
        searchResultView.setVisibility(View.GONE);
        searchEdit.setCursorVisible(true);
    }

    private void showGoodsSearchResultView() {
        searchScrollView.setVisibility(View.GONE);
        searchResultView.setVisibility(View.VISIBLE);
        searchGoodsView.setVisibility(View.VISIBLE);
        searchPictureView.setVisibility(View.GONE);
    }

    private void showPictureSearchResultView() {
        searchScrollView.setVisibility(View.GONE);
        searchResultView.setVisibility(View.VISIBLE);
        searchGoodsView.setVisibility(View.GONE);
        searchPictureView.setVisibility(View.VISIBLE);
    }

    private void doClearSearchHistory() {

        ConfirmDialog alertDialog = new ConfirmDialog(SearchActivity.this, ConfirmDialog.ConfirmDialogType.ConfirmDialogType_Normal);
        alertDialog.setMsg("是否确认清除搜索历史？")
                .setLeftButtonText("取消")
                .setRightButtonText("清除")
                .setCallBack(new ConfirmDialog.CallBack() {
                    @Override
                    public void buttonClick(Dialog dialog, boolean leftClick) {
                        dialog.dismiss();
                        if (!leftClick) {
                            AppSessionCache.getInstance().remove(AppConst.HISTORY_KEYWORDS);
                            refreshHistoryKeywordsView();
                        }
                    }
                });
        alertDialog.show();
    }

    private void refreshHistoryKeywordsView() {
        List<String> keywords = AppSessionCache.getInstance().get(AppConst.HISTORY_KEYWORDS);
        if (keywords == null || keywords.isEmpty()) {
            historyLayout.setVisibility(View.GONE);
            return;
        }
        historyTags.clear();
        historyTags.addAll(keywords);
        searchHistoryLayout.getAdapter().notifyDataChanged();
        historyLayout.setVisibility(View.VISIBLE);
    }

    private void refreshHotKeywordsView() {
        List<String> keywords = AppSessionCache.getInstance().get(AppConst.HOT_KEYWORDS);
        if (keywords == null || keywords.isEmpty()) {
            hotLayout.setVisibility(View.GONE);
            return;
        }
        hotTags.clear();
        hotTags.addAll(keywords);
        hotSearchLayout.getAdapter().notifyDataChanged();
        hotLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onChange(ClearEditText editText, Editable editable) {
        searchKeyword = editable.toString();
        inputSearch = true;
        searchHandler.removeMessages(1);
        searchHandler.sendEmptyMessageDelayed(1, 500);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            if (searchType == SEARCH_ALL) {
                doSearch(SEARCH_GOODS);
            } else {
                doSearch(searchType);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTagClick(View view, int position, FlowLayout parent) {
        String keyword;
        if (parent == searchHistoryLayout) {
            keyword = historyTags.get(position);
        } else {
            keyword = hotTags.get(position);
        }

        inputSearch = false;
        searchKeyword = keyword;
        searchEdit.disabledTextChange();
        searchEdit.setText(searchKeyword);
        searchEdit.setSelection(searchKeyword.length());
        doSearchKeyword();
        searchEdit.enabledTextChange();
        return false;
    }

    private void refreshKeywordResultView() {
        if (goodsList.isEmpty()) {
            goodsListView.setVisibility(View.GONE);
            goodsLineView.setVisibility(View.GONE);
        } else {
            goodsListView.setVisibility(View.VISIBLE);
            goodsLineView.setVisibility(View.VISIBLE);
        }

        if (pictureList.isEmpty()) {
            pictureListView.setVisibility(View.GONE);
            pictureLineView.setVisibility(View.GONE);
        } else {
            pictureListView.setVisibility(View.VISIBLE);
            pictureLineView.setVisibility(View.VISIBLE);
        }

        ((BaseAdapter) goodsListView.getAdapter()).notifyDataSetChanged();
        ((BaseAdapter) pictureListView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            // head view skip handle
            return;
        }
        if (parent == goodsListView) {
            searchKeyword = goodsList.get(position);
            searchEdit.setText(searchKeyword);
            searchEdit.setSelection(searchKeyword.length());
            doSearch(SEARCH_GOODS);
        } else {
            searchKeyword = pictureList.get(position);
            searchEdit.setText(searchKeyword);
            searchEdit.setSelection(searchKeyword.length());
            doSearch(SEARCH_PICTURE);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler searchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            doSearchKeyword();
        }
    };

    private void loadHotKeywords() {
        HttpRequest.get(AppConfig.HOT_KEYWORDS, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray hotKeywordsArr = JsonUtils.getJsonArray(response, "data", null);
                    List<String> hotKeywords = new ArrayList<>();
                    for (int i = 0; i < hotKeywordsArr.length(); i++) {
                        JSONObject item = JsonUtils.getJsonItem(hotKeywordsArr, i, null);
                        hotKeywords.add(JsonUtils.getString(item, "keyword", ""));
                    }
                    AppSessionCache.getInstance().put(AppConst.HOT_KEYWORDS, hotKeywords);
                    refreshHotKeywordsView();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("加载热词失败", ex);
            }
        });
    }

    private void doSearchKeyword() {
        if (StringUtils.isEmpty(searchKeyword)) {
            goodsList.clear();
            pictureList.clear();
            refreshKeywordResultView();
            return;
        }

        Map<String, String> params = Params.buildForStr("keyword", searchKeyword);
        HttpRequest.get(AppConfig.SEARCH_KEYWORDS, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) != 0) {
                    return;
                }
                String resultKeyword = JsonUtils.getResponseMessage(response);
                if (!StringUtils.equals(searchKeyword, resultKeyword)) {
                    return;
                }
                JSONArray result = JsonUtils.getJsonArray(response, "data", null);

                goodsList.clear();
                pictureList.clear();

                for (int i = 0; i < result.length(); i++) {
                    JSONObject item = JsonUtils.getJsonItem(result, i, null);
                    String keyword = JsonUtils.getString(item, "keyword", "");
                    int type = JsonUtils.getInt(item, "type", 1);
                    if (searchType != SEARCH_ALL && type != searchType) {
                        continue;
                    }
                    if (type == 1) {
                        goodsList.add(keyword);
                    } else if (type == 2) {
                        pictureList.add(keyword);
                    }
                }

                if (goodsList.size() > 0) {
                    goodsList.add(0, "商品");
                }

                if (pictureList.size() > 0) {
                    pictureList.add(0, "图片");
                }

                if (searchType == SEARCH_PICTURE) {
                    if ((pictureList == null || pictureList.size() == 0)) {
                        if (!inputSearch) {
                            // 当点击变迁搜索关键词返回为0的时候，则自动跳转搜索商品或图片
                            onKey(searchEdit, KeyEvent.KEYCODE_ENTER, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                        }
                        return;
                    }
                } else if (searchType == SEARCH_GOODS) {
                    if ((goodsList == null || goodsList.size() == 0)) {
                        if (!inputSearch) {
                            // 当点击变迁搜索关键词返回为0的时候，则自动跳转搜索商品或图片
                            onKey(searchEdit, KeyEvent.KEYCODE_ENTER, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                        }
                        return;
                    }
                } else {
                    if ((result == null || result.length() == 0)) {
                        if (!inputSearch) {
                            // 当点击变迁搜索关键词返回为0的时候，则自动跳转搜索商品或图片
                            onKey(searchEdit, KeyEvent.KEYCODE_ENTER, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                        }
                        return;
                    }
                }


                refreshKeywordResultView();
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("搜索关键词失败", ex);
            }
        });
    }

    private void doSearch(int searchType) {
        if (StringUtils.isEmpty(searchKeyword)) {
            return;
        }

        KeyboardUtil.hidInput(this, searchEdit);
        searchEdit.clearFocus();
        searchEdit.setCursorVisible(false);

        List<String> historyKeywords = AppSessionCache.getInstance().get(AppConst.HISTORY_KEYWORDS);
        if (historyKeywords == null) {
            historyKeywords = new ArrayList<>();
        }
        for (String historyKeyword : historyKeywords) {
            if (StringUtils.equals(searchKeyword, historyKeyword)) {
                historyKeywords.remove(historyKeyword);
                break;
            }
        }

        historyKeywords.add(0, searchKeyword);

        if (historyKeywords.size() > 5) {
            historyKeywords.removeAll(historyKeywords.subList(5, historyKeywords.size()));
        }

        AppSessionCache.getInstance().put(AppConst.HISTORY_KEYWORDS, historyKeywords);
        refreshHistoryKeywordsView();

        // 进入搜索
        if (searchType == SEARCH_GOODS) {
            showGoodsSearchResultView();
            searchGoodsView.search(searchKeyword);
        } else {
            showPictureSearchResultView();
            searchPictureView.search(searchKeyword);
        }
    }

    class SearchResultAdapter extends BaseAdapter {

        private List<String> data;

        public SearchResultAdapter(List<String> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data != null ? data.size() : 0;
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_search_result_item, parent, false);
            }
            String keyword = getItem(position);
            TextView headText = convertView.findViewById(R.id.head_text);
            TextView resultText = convertView.findViewById(R.id.result_text);
            if (position == 0) {
                headText.setText(keyword);
                headText.setVisibility(View.VISIBLE);
                resultText.setVisibility(View.GONE);
            } else {
                resultText.setText(keyword);
                headText.setVisibility(View.GONE);
                resultText.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }

    class SearchTagAdapter extends TagAdapter<String> {

        public SearchTagAdapter(List<String> datas) {
            super(datas);
        }

        @Override
        public View getView(FlowLayout parent, int position, String tag) {
            View view = LayoutInflater.from(SearchActivity.this).inflate(R.layout.layout_search_tag, parent, false);
            TextView tv = view.findViewById(R.id.tag_text);
            tv.setText(tag);
            return view;
        }
    }
}
