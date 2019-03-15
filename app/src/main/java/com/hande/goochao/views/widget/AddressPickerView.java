package com.hande.goochao.views.widget;


import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LMC on 2018/3/8.
 */

public class AddressPickerView extends RelativeLayout  {

    /**
     * 自加属性
     */

    // recyclerView 选中Item 的颜色
    private int defaultSelectedColor = Color.parseColor("#50AA00");
    // recyclerView 未选中Item 的颜色
    private int defaultUnSelectedColor = Color.parseColor("#262626");
    // 确定字体不可以点击时候的颜色
    private int defaultSureUnClickColor = Color.parseColor("#7F7F7F");
    // 确定字体可以点击时候的颜色
    private int defaultSureCanClickColor = Color.parseColor("#50AA00");

    private Context mContext;
    private int defaultTabCount = 3; //tab 的数量
    private TabLayout mTabLayout; // tabLayout
    private RecyclerView mRvList; // 显示数据的RecyclerView
    private String defaultProvince = "省份"; //显示在上面tab中的省份
    private String defaultCity = "城市"; //显示在上面tab中的城市
    private String defaultDistrict = "区县"; //显示在上面tab中的区县

    private List<YwpAddressBean.AddressItemBean> mRvData; // 用来在recyclerview显示的数据
    private AddressAdapter mAdapter;   // recyclerview 的 adapter

    private YwpAddressBean mYwpAddressBean; // 总数据
    private YwpAddressBean.AddressItemBean mSelectProvice; //选中 省份 bean
    private YwpAddressBean.AddressItemBean mSelectCity;//选中 城市  bean
    private YwpAddressBean.AddressItemBean mSelectDistrict;//选中 区县  bean
    private int mSelectProvicePosition = 0; //选中 省份 位置
    private int mSelectCityPosition = 0;//选中 城市  位置
    private int mSelectDistrictPosition = 0;//选中 区县  位置

    private OnAddressPickerSureListener mOnAddressPickerSureListener;
//    private TextView mTvSure; //确定

    public AddressPickerView(Context context) {
        super(context);
        init(context);
    }

    public AddressPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddressPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        mContext = context;
        mRvData = new ArrayList<>();
        // UI
        View rootView = inflate(mContext, R.layout.address_picker_view_new, this);
        // 确定
//        mTvSure = rootView.findViewById(R.id.tvSure);
//        mTvSure.setTextColor(defaultSureUnClickColor);
//        mTvSure.setOnClickListener(this);
        // tablayout初始化
        mTabLayout = (TabLayout) rootView.findViewById(R.id.tlTabLayout);
        mTabLayout.addTab(mTabLayout.newTab().setText(defaultProvince));
        mTabLayout.addTab(mTabLayout.newTab().setText(defaultCity));
        mTabLayout.addTab(mTabLayout.newTab().setText(defaultDistrict));

        mTabLayout.addOnTabSelectedListener(tabSelectedListener);

        // recyclerview adapter的绑定
        mRvList = (RecyclerView) rootView.findViewById(R.id.rvList);
        mRvList.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new AddressAdapter();
        mRvList.setAdapter(mAdapter);
        // 初始化默认的本地数据  也提供了方法接收外面数据
        mRvList.post(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });
    }


//    /**
//     * 初始化数据
//     * 拿assets下的json文件
//     */
//    private void initData() {
//        StringBuilder jsonSB = new StringBuilder();
//        try {
//            BufferedReader addressJsonStream = new BufferedReader(new InputStreamReader(mContext.getAssets().open("address.json")));
//            String line;
//            while ((line = addressJsonStream.readLine()) != null) {
//                jsonSB.append(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        // 将数据转换为对象
//        mYwpAddressBean = new Gson().fromJson(jsonSB.toString(), YwpAddressBean.class);
//        if (mYwpAddressBean != null) {
//            mRvData.clear();
//            mRvData.addAll(mYwpAddressBean.getProvince());
//            mAdapter.notifyDataSetChanged();
//        }
//    }

    /**
     * 初始化数据 省
     * 调用借口
     */
    private void initData(){

        HttpRequest.get(AppConfig.PROVINCES_LIST_GET, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
//                List<YwpAddressBean.AddressItemBean> provinces = new ArrayList<>();
                JSONObject data = JsonUtils.getJsonObject(response, "data", null);
                JSONArray dataList = JsonUtils.getJsonArray(data, "districts", null);
                for (int i = 0; i < dataList.length(); i++) {
                    JSONArray resource = JsonUtils.getJsonArray(JsonUtils.getJsonItem(dataList, i, null), "districts", null);
                    for (int m = 0; m < resource.length(); m++) {
                        YwpAddressBean.AddressItemBean itemBean = new YwpAddressBean.AddressItemBean();
                        itemBean.setN(JsonUtils.getString(JsonUtils.getJsonItem(resource, m, null), "name", ""));
                        itemBean.setI(JsonUtils.getString(JsonUtils.getJsonItem(resource, m, null), "adcode", ""));
                        mRvData.add(itemBean);
//                    provinces.add(itemBean);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
            }
        }) ;
    }

    /**
     * 动态加载属性 市
     */
    private void loadCity(final String adcode) {
        Map<String, String> params = new HashMap<>();
        params.put("adcode", adcode);
        HttpRequest.get(AppConfig.CITY_AND_COUNTY_LIST_GET, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
//                List<YwpAddressBean.AddressItemBean> cityList = new ArrayList<>();
                JSONObject object = JsonUtils.getJsonObject(response,"data",null);
                JSONArray newResource = JsonUtils.getJsonArray(object,"districts",null);
                for (int i = 0; i < newResource.length(); i++) {
                    JSONArray resource = JsonUtils.getJsonArray(JsonUtils.getJsonItem(newResource, i, null), "districts", null);
                    for (int n = 0; n < resource.length(); n++) {
                        YwpAddressBean.AddressItemBean itemBean = new YwpAddressBean.AddressItemBean();
                        itemBean.setN(JsonUtils.getString(JsonUtils.getJsonItem(resource, n, null), "name", ""));
                        itemBean.setI(JsonUtils.getString(JsonUtils.getJsonItem(resource, n, null), "adcode", ""));
                        itemBean.setP(adcode);
                        mRvData.add(itemBean);
//                    cityList.add(itemBean);
                    }

                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable ex) {

            }
        });
    }

    /**
     * 动态加载属性 行政区
     */
    private void loadCounty(final String adcode) {
        Map<String, String> params = new HashMap<>();
        params.put("adcode", adcode);
        HttpRequest.get(AppConfig.CITY_AND_COUNTY_LIST_GET, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
//                List<YwpAddressBean.AddressItemBean> countyList = new ArrayList<>();
                JSONObject object = JsonUtils.getJsonObject(response,"data",null);
                JSONArray newResource = JsonUtils.getJsonArray(object,"districts",null);
                    for(int k = 0; k < newResource.length(); k++){
                        JSONArray resources = JsonUtils.getJsonArray(JsonUtils.getJsonItem(newResource,k,null),"districts",null);
                    for (int h = 0; h < resources.length(); h++) {
                        YwpAddressBean.AddressItemBean itemBean = new YwpAddressBean.AddressItemBean();
                        itemBean.setN(JsonUtils.getString(JsonUtils.getJsonItem(resources, h, null), "name", ""));
                        itemBean.setI(JsonUtils.getString(JsonUtils.getJsonItem(resources, h, null), "adcode", ""));
                        itemBean.setP(adcode);
                        mRvData.add(itemBean);
//                    countyList.add(itemBean);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable ex) {

            }
        });
    }

    /**
     * 开放给外部传入数据
     * 暂时就用这个Bean模型，如果数据不一致就需要各自根据数据来生成这个bean了
     */
    public void initData(YwpAddressBean bean) {
        if (bean != null) {
            mSelectDistrict = null;
            mSelectCity = null;
            mSelectProvice = null;
            mTabLayout.getTabAt(0).select();

            mYwpAddressBean = bean;
            mRvData.clear();
            mRvData.addAll(mYwpAddressBean.getProvince());
            mAdapter.notifyDataSetChanged();

        }
    }


//    @Override
//    public void onClick(View v) {
//        int i = v.getId();
//        if (i == R.id.tvSure) {
//            sure();
//        }
//    }

    //点确定
//    private void sure() {
//        if (mSelectProvice != null &&
//                mSelectCity != null &&
//                mSelectDistrict != null) {
//            //   回调接口
//            if (mOnAddressPickerSureListener != null) {
//                mOnAddressPickerSureListener.onSureClick(mSelectProvice.getN() + " " + mSelectCity.getN() + " " + mSelectDistrict.getN() + " ",
//                        mSelectProvice.getI(), mSelectCity.getI(), mSelectDistrict.getI());
//            }
//        } else {
//            Toast.makeText(mContext, "地址还没有选完整哦", Toast.LENGTH_SHORT).show();
//        }
//
//    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mYwpAddressBean = null;
    }

    /**
     * TabLayout 切换事件
     */
    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mRvData.clear();
            switch (tab.getPosition()) {
                case 0:
//                    mRvData.addAll(mYwpAddressBean.getProvince());
                    initData();
                    mAdapter.notifyDataSetChanged();
                    // 滚动到这个位置
                    mRvList.smoothScrollToPosition(mSelectProvicePosition);
                    break;
                case 1:
                    // 点到城市的时候要判断有没有选择省份
                    if (mSelectProvice != null) {
//                        for (YwpAddressBean.AddressItemBean itemBean : mYwpAddressBean.getCity()) {
//                            if (itemBean.getP().equals(mSelectProvice.getI()))
//                                mRvData.add(itemBean);
//                        }
                        loadCity(mSelectProvice.getI());
                    } else {
                        Toast.makeText(mContext, "请您先选择省份", Toast.LENGTH_SHORT).show();
                    }
                    mAdapter.notifyDataSetChanged();
                    // 滚动到这个位置
                    mRvList.smoothScrollToPosition(mSelectCityPosition);
                    break;
                case 2:
                    // 点到区的时候要判断有没有选择省份与城市
                    if (mSelectProvice != null && mSelectCity != null) {
                        loadCounty(mSelectCity.getI());
                    } else {
                        Toast.makeText(mContext, "请您先选择省份与城市", Toast.LENGTH_SHORT).show();
                    }
                    mAdapter.notifyDataSetChanged();
                    // 滚动到这个位置
                    mRvList.smoothScrollToPosition(mSelectDistrictPosition);
                    break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };


    /**
     * 下面显示数据的adapter
     */
    class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_address_text, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final int tabSelectPosition = mTabLayout.getSelectedTabPosition();
            holder.mTitle.setText(mRvData.get(position).getN());
            holder.mTitle.setTextColor(defaultUnSelectedColor);
            // 设置选中效果的颜色
            switch (tabSelectPosition) {
                case 0:
                    if (mRvData.get(position) != null &&
                            mSelectProvice != null &&
                            mRvData.get(position).getI().equals(mSelectProvice.getI())) {
                        holder.mTitle.setTextColor(defaultSelectedColor);
                    }
                    break;
                case 1:
                    if (mRvData.get(position) != null &&
                            mSelectCity != null &&
                            mRvData.get(position).getI().equals(mSelectCity.getI())) {
                        holder.mTitle.setTextColor(defaultSelectedColor);
                    }
                    break;
                case 2:
                    if (mRvData.get(position) != null &&
                            mSelectDistrict != null &&
                            mRvData.get(position).getI().equals(mSelectDistrict.getI()) &&
                            mRvData.get(position).getN().equals(mSelectDistrict.getN())) {
                        holder.mTitle.setTextColor(defaultSelectedColor);
                    }
                    break;
            }
            // 设置点击之后的事件
            holder.mTitle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击 分类别
                    switch (tabSelectPosition) {
                        case 0:
                            mSelectProvice = mRvData.get(position);
                            // 清空后面两个的数据
                            mSelectCity = null;
                            mSelectDistrict = null;
                            mSelectCityPosition = 0;
                            mSelectDistrictPosition = 0;
                            mTabLayout.getTabAt(1).setText(defaultCity);
                            mTabLayout.getTabAt(2).setText(defaultDistrict);
                            // 设置这个对应的标题
                            mTabLayout.getTabAt(0).setText(mSelectProvice.getN());
                            // 跳到下一个选择
                            mTabLayout.getTabAt(1).select();
                            // 灰掉确定按钮
//                            mTvSure.setTextColor(defaultSureUnClickColor);
                            mSelectProvicePosition = position;
                            break;
                        case 1:
                            mSelectCity = mRvData.get(position);
                            // 清空后面一个的数据
                            mSelectDistrict = null;
                            mSelectDistrictPosition = 0;
                            mTabLayout.getTabAt(2).setText(defaultDistrict);
                            // 设置这个对应的标题
                            mTabLayout.getTabAt(1).setText(mSelectCity.getN());
                            // 跳到下一个选择
                            mTabLayout.getTabAt(2).select();
                            // 灰掉确定按钮
//                            mTvSure.setTextColor(defaultSureUnClickColor);
                            mSelectCityPosition = position;
                            break;
                        case 2:
                            mSelectDistrict = mRvData.get(position);
                            // 没了，选完了，这个时候可以点确定了
                            mTabLayout.getTabAt(2).setText(mSelectDistrict.getN());
                            notifyDataSetChanged();
                            // 确定按钮变亮
//                            mTvSure.setTextColor(defaultSureCanClickColor);
                            mSelectDistrictPosition = position;
                            if (mSelectProvice != null &&
                                    mSelectCity != null &&
                                    mSelectDistrict != null) {
                                //   回调接口
                                if (mOnAddressPickerSureListener != null) {
                                    mOnAddressPickerSureListener.onSureClick(mSelectProvice.getN() + " " + mSelectCity.getN() + " " + mSelectDistrict.getN() + " ",
                                            mSelectProvice.getI(), mSelectCity.getI(), mSelectDistrict.getI());
                                }
                            }
                            break;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mRvData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mTitle;

            ViewHolder(View itemView) {
                super(itemView);
                mTitle = (TextView) itemView.findViewById(R.id.itemTvTitle);
            }

        }
    }


    /**
     * 点第三个标签回调这个接口
     */
    public interface OnAddressPickerSureListener {
        void onSureClick(String address, String provinceCode, String cityCode, String districtCode);
    }

    public void setOnAddressPickerSure(OnAddressPickerSureListener listener) {
        this.mOnAddressPickerSureListener = listener;
    }

    /**
     * 返回select省项name
     */
    public String returnSelectProvince(){
        return mSelectProvice.getN();
    }

    /**
     * 返回select城市项name
     */
    public String returnSelectCity(){
        return  mSelectCity.getN();
    }

    /**
     * 返回select行政区域项name
     */
    public String returnSelectCounty(){
        return mSelectDistrict.getN();
    }

    /**
     * 返回select省项code
     */
    public String returnSelectProvinceCode(){
        return mSelectProvice.getI();
    }

    /**
     * 返回select城市项code
     */
    public String returnSelectCityCode(){
        return  mSelectCity.getI();
    }

    /**
     * 返回select行政区域项code
     */
    public String returnSelectCountyCode(){
        return mSelectDistrict.getI();
    }

    /**
     * 初始化选中省
     */
    public void setSelectProvince(String province){
        mSelectProvice.setN(province);
    }

    /**
     * 初始化选中市
     */
    public void setSelectCity(String city){
        mSelectProvice.setN(city);
    }

    /**
     * 初始化选中行政区域
     */
    public void setSelectCounty(String county){
        mSelectProvice.setN(county);
    }

    /**
     * 初始化选中省Code
     */
    public void setSelectProvinceCode(String provinceCode){
        mSelectProvice.setN(provinceCode);
    }

    /**
     * 初始化选中市Code
     */
    public void setSelectCityCode(String cityCode){
        mSelectProvice.setN(cityCode);
    }

    /**
     * 初始化选中行政区域Code
     */
    public void setSelectCountyCode(String countyCode){
        mSelectProvice.setN(countyCode);
    }

    /**
     * 判断是否选择省
     */
    public boolean booleanSelectProvince(){
        if (mSelectProvice == null){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 判断是否选择市
     */
    public boolean booleanSelectCity(){
        if ( mSelectCity == null){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 判断是否选择区、县
     */
    public boolean booleanSelectCounty(){
        if ( mSelectDistrict == null){
            return true;
        }
        else {
            return false;
        }
    }
}
