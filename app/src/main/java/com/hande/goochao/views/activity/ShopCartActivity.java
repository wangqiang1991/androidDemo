package com.hande.goochao.views.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.refresh.NoDataTwoLineView;
import com.hande.goochao.views.widget.NumberEdit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShopCartActivity extends ToolBarActivity implements View.OnClickListener {

    private JSONArray cartResource;
    private JSONArray cartList = new JSONArray();
    private String orderId;
    private List<Map<String, String>> items = new ArrayList<>();
    private List<Map<String, String>> editItems = new ArrayList<>();

    @ViewInject(R.id.shop_cart_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.load_fail_view_cart_list)
    private LoadFailView loadFailView;
    @ViewInject(R.id.noDataView)
    private NoDataTwoLineView noDataView;
    private CustomLoadingDialog loadingDialog;

    private boolean firstLoad = true;
    private boolean checked;
    private boolean select = false;
    private boolean editClick = false;

    private ShoppingCartAdapter shoppingCartAdapter;
    @ViewInject(R.id.cart_list)
    private ListView cartListView;

    private String price;
    private int count; // 被选中项
    private int stock; //库存
    private double priceItem = 0;
    private double allPrice = 0; //共计显示的总金额
    private double preferentialPrice = 0; // 折扣优惠的总金额
    private String styleId;
    private String newGoodsId;
    private String editStyleId;
    private String editGoodsId;
    private String cartId;
    private String cartIds = "";

    @ViewInject(R.id.pay_button)
    private Button payBt;
    @ViewInject(R.id.money)
    private TextView money; //共计显示的总金额视图
    @ViewInject(R.id.preferential_view)
    private TextView preferentialView; // 折扣优惠的总金额视图
    @ViewInject(R.id.select_all_view)
    private View selectAll;
    @ViewInject(R.id.select_image)
    private ImageView selectImage;
    @ViewInject(R.id.down_bar)
    private View downView;

    //判断商品状态类型布尔
    private boolean pulled; // 下架
    private boolean sellout; // 售罄
    private boolean noStorage; //  库存不足

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_cart);

        setTitle("购物车");
        setSubmitText("编辑");
        glide = GlideApp.with(this);
        noDataView.setImageAndText(R.mipmap.shop_cart_nodata, "购物车居然是空的", "快去选购啦");

        payBt.setOnClickListener(this);
        selectAll.setOnClickListener(this);

        shoppingCartAdapter = new ShoppingCartAdapter();
        cartListView.setAdapter(shoppingCartAdapter);

        loadingDialog = new CustomLoadingDialog(this);
        firstLoad = true;

        /**
         * 加载失败
         */
        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadFailView.setVisibility(View.GONE);
                cartListView.setVisibility(View.VISIBLE);
                loadCart();
            }
        });

        cartList = new JSONArray();
        cartResource = new JSONArray();
        loadCart();

        allSelectCheck();
        EventBus.getDefault().register(this);
    }

//    private void getE

    private void allSelectCheck() {
        for (int m = 0; m < cartList.length(); m++) {
            if (!JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, m, null), "checked", false)) {
                selectImage.setImageResource(R.mipmap.high_dp_default_icon);
                select = false;
                return;
            } else {
                int n = 0;
                n = n + 1;
                if (m == n - 1) {
                    select = true;
                    selectImage.setImageResource(R.mipmap.high_dp_select_icon);
                }
            }
        }
    }

    @Override
    protected void onSubmitClickLister() {
        if (!editClick) {
            setSubmitText("完成");
            money.setVisibility(View.GONE);
            preferentialView.setVisibility(View.GONE);

            for (int m = 0; m < cartList.length(); m++) {
                JsonUtils.put(JsonUtils.getJsonItem(cartList, m, null), "checked", false);
            }
            payBt.setText("删除（0）");
            select = false;
            selectImage.setImageResource(R.mipmap.high_dp_default_icon);
            editClick = true;
            shoppingCartAdapter.notifyDataSetChanged();
        } else {
            setSubmitText("编辑");
            loadingDialog.show();
            money.setVisibility(View.VISIBLE);
            if (preferentialPrice > 0) {
                preferentialView.setVisibility(View.VISIBLE);
            }

            for (int m = 0; m < cartList.length(); m++) {
                JsonUtils.put(JsonUtils.getJsonItem(cartList, m, null), "checked", false);
            }
            payBt.setText("结算（0）");
            money.setText("¥0");
            preferentialView.setVisibility(View.GONE);
            select = false;
            selectImage.setImageResource(R.mipmap.high_dp_default_icon);
            editClick = false;
            editCart();
        }
    }

    private void loadCart() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        } else {
            loadingDialog.show();
        }

        HttpRequest.get(AppConfig.NEW_SHOPPING_CART_LIST, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    cartResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < cartResource.length(); i++) {
                        try {
                            cartList.put(cartResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (cartList.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                        cartListView.setVisibility(View.GONE);
                        downView.setVisibility(View.GONE);
                        hideSubmitText();
                    } else {
                        noDataView.setVisibility(View.GONE);
                        cartListView.setVisibility(View.VISIBLE);
                        downView.setVisibility(View.VISIBLE);
                        if (editClick) {
                            setSubmitText("完成");
                        } else {
                            setSubmitText("编辑");
                        }
                    }
                    shoppingCartAdapter.notifyDataSetChanged();
                    firstLoad = false;
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
                AlertManager.showErrorToast(ShopCartActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }

    private void deleteOrBuy() {
        if (!editClick) {
            for (int i = 0; i < cartList.length(); i++) {
                if (JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, i, null), "checked", false)) {
                    newGoodsId = JsonUtils.getString(JsonUtils.getJsonItem(cartList, i, null), "goodsId", null);
                    styleId = JsonUtils.getString(JsonUtils.getJsonItem(cartList, i, null), "styleId", null);


                    Map<String, String> params = new HashMap<>();
                    params.put("goodsId", newGoodsId);
                    params.put("styleId", styleId);
                    items.add(params);
                }
            }
            if (items == null || items.isEmpty()) {
                AlertManager.toast(ShopCartActivity.this, "请选择你要结算的商品");
                return;
            }
            submitOrder();
        } else {
            for (int i = 0; i < cartList.length(); i++) {
                if (JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, i, null), "checked", false)) {
                    cartId = JsonUtils.getString(JsonUtils.getJsonItem(cartList, i, null), "cartId", null);
                    if (cartIds.equals("")) {
                        cartIds = cartId;
                    } else {
                        cartIds = cartIds + "," + cartId;
                    }
                }
            }
            if (cartIds.equals("")) {
                AlertManager.toast(ShopCartActivity.this, "请选择你要删除的商品");
                return;
            }
            ConfirmDialog alertDialog = new ConfirmDialog(ShopCartActivity.this, ConfirmDialog.ConfirmDialogType.ConfirmDialogType_Danger);
            alertDialog.setMsg("确认要删除该商品吗？")
                    .setLeftButtonText("取消")
                    .setRightButtonText("删除")
                    .setCallBack(new ConfirmDialog.CallBack() {
                        @Override
                        public void buttonClick(Dialog dialog, boolean leftClick) {
                            dialog.dismiss();
                            if (!leftClick) {
                                deleteCart();
                            }
                        }
                    });
            alertDialog.show();
        }
    }

    private void selectAllItem() {
        if (!select) {
            for (int n = 0; n < cartList.length(); n++) {

                pulled = JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, n, null), "pulled", false); // 下架
                sellout = JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, n, null), "sellout", false); // 售罄
                noStorage = JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, n, null), "noStorage", false); //  库存不足

                if (editClick) {
                    JsonUtils.put(JsonUtils.getJsonItem(cartList, n, null), "checked", true);
                } else {
                    if (!(pulled || sellout || noStorage)) {
                        JsonUtils.put(JsonUtils.getJsonItem(cartList, n, null), "checked", true);
                    } else {
                        JsonUtils.put(JsonUtils.getJsonItem(cartList, n, null), "checked", false);
                    }
                }
            }
            for (int i = 0; i < cartList.length(); i++) {
                pulled = JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, i, null), "pulled", false); // 下架
                sellout = JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, i, null), "sellout", false); // 售罄
                noStorage = JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, i, null), "noStorage", false); //  库存不足
                if (editClick) {
                    count++;
                } else {
                    if (!(pulled || sellout || noStorage)) {
                        count++;
                        int type = JsonUtils.getInt(JsonUtils.getJsonItem(cartList, i, null), "discountType", 0);
                        if (type == 3) {
                            double freeAmountMoney = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "coupon.toAmount", 0); //需要达到的目标金额
                            double freeMoney = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "coupon.amount", 0); //达标后优惠金额

                            double singlePrice = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "discountPrice", 0);
                            double itemCount = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "amount", 0);
                            priceItem = singlePrice * itemCount;
                            allPrice = allPrice + priceItem;
                            if (priceItem > freeAmountMoney) {
                                allPrice = allPrice - freeMoney;
                                preferentialPrice = preferentialPrice + freeMoney;
                            }
                        } else if (type == 2) {
                            double oldPrice = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "price", 0); // 原单价
                            double singlePrice = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "discountPrice", 0); // 折后单价
                            double itemCount = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "amount", 0);
                            priceItem = singlePrice * itemCount;
                            allPrice = allPrice + priceItem;
                            preferentialPrice = preferentialPrice + (oldPrice - singlePrice) * itemCount;
                        } else {
                            double singlePrice = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "discountPrice", 0);
                            double itemCount = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "amount", 0);
                            priceItem = singlePrice * itemCount;
                            allPrice = allPrice + priceItem;
                        }
                    }
                }
            }
            if (editClick) {
                payBt.setText("删除（" + count + "）");
            } else {
                payBt.setText("结算（" + count + "）");
            }
            double lookMoney = NumberUtils.decimalDouble(allPrice);
            double freeLookMoney = NumberUtils.decimalDouble(preferentialPrice);
            money.setText("¥" + PriceUtils.beautify(lookMoney));
            preferentialView.setText("已优惠:¥" + PriceUtils.beautify(freeLookMoney));
            if (preferentialPrice > 0) {
                preferentialView.setVisibility(View.VISIBLE);
            } else {
                preferentialView.setVisibility(View.GONE);
            }
            select = true;
            shoppingCartAdapter.notifyDataSetChanged();
            count = 0;
            allPrice = 0;
            preferentialPrice = 0;

        } else {
            for (int n = 0; n < cartList.length(); n++) {
                JsonUtils.put(JsonUtils.getJsonItem(cartList, n, null), "checked", false);
            }
            if (editClick) {
                payBt.setText("删除（0）");
            } else {
                payBt.setText("结算（0）");
            }
            money.setText("¥0");
            preferentialView.setVisibility(View.GONE);
            select = false;
            shoppingCartAdapter.notifyDataSetChanged();
            count = 0;
            allPrice = 0;
            preferentialPrice = 0;
        }
        if (select) {
            selectImage.setImageResource(R.mipmap.high_dp_select_icon);
        } else {
            selectImage.setImageResource(R.mipmap.high_dp_default_icon);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == payBt) {
            deleteOrBuy(); // 结算、删除
        } else if (v == selectAll) {
            selectAllItem(); // 全选
        }
    }

    class ShoppingCartAdapter extends BaseAdapter implements NumberEdit.OnValueChangeListener {

        @Override
        public void onChange(NumberEdit numberEdit, int value) {
            JSONObject item = (JSONObject) numberEdit.getTag();
            JsonUtils.put(item, "amount", value);
        }

        class ViewHolder {
            TextView txtTitleValue;
            TextView txtStyleValue;
            TextView txtPriceValue;
            TextView txtCountValue;
            TextView txtSubValue;
            View xView;
            NumberEdit editView;
            ImageView selectBt;
            View selectView;
            ImageView goodsImage;
            ImageView pulledImg;
            ImageView selloutImg;
            ImageView noStorageImg;
            TextView noStorageHint;
            ImageView garyMirrorImg;
            TextView productOldPrice;
            TextView saleOutPrice;

            ViewHolder(View view) {
                txtTitleValue = view.findViewById(R.id.goods_title);
                txtStyleValue = view.findViewById(R.id.style_id);
                txtPriceValue = view.findViewById(R.id.item_price);
                txtCountValue = view.findViewById(R.id.item_count);
                txtSubValue = view.findViewById(R.id.sub_id);
                xView = view.findViewById(R.id.X);
                editView = view.findViewById(R.id.edit_view);
                selectBt = view.findViewById(R.id.select_button);
                selectView = view.findViewById(R.id.select_view);
                goodsImage = view.findViewById(R.id.goods_image);
                pulledImg = view.findViewById(R.id.pulled_img);
                selloutImg = view.findViewById(R.id.sellout_img);
                noStorageImg = view.findViewById(R.id.noStorage_img);
                noStorageHint = view.findViewById(R.id.noStorage_hint);
                garyMirrorImg = view.findViewById(R.id.gary_mirror_view);
                productOldPrice = view.findViewById(R.id.old_price);
                saleOutPrice = view.findViewById(R.id.sale_out_price);
            }
        }

        @Override
        public int getCount() {
            return cartList == null ? 0 : cartList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(cartList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(ShopCartActivity.this).inflate(R.layout.activity_shopping_cart_item, viewGroup, false);
                view.setTag(new ViewHolder(view));
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            JSONObject item = getItem(i);

            pulled = JsonUtils.getBoolean(item, "pulled", false); // 下架
            sellout = JsonUtils.getBoolean(item, "sellout", false); // 售罄
            noStorage = JsonUtils.getBoolean(item, "noStorage", false); //  库存不足
            stock = JsonUtils.getInt(item, "count", 0);
            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(getItem(i), "discountTag", "");
            if (tagValue.equals("")) {
                holder.productOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(item, "price", 0);
                double priceValue = NumberUtils.decimalDouble(price);
                holder.txtPriceValue.setText("" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else {
                if (JsonUtils.getInt(getItem(i), "discountType", 0) == 2) {

                    holder.productOldPrice.setVisibility(View.VISIBLE);
                    holder.saleOutPrice.setVisibility(View.GONE);

                    double oldPrice = JsonUtils.getDouble(item, "price", 0);
                    double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                    holder.productOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                    holder.productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                    double newPrice = JsonUtils.getDouble(item, "discountPrice", 0);
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);
                    holder.txtPriceValue.setText("" + PriceUtils.beautify(newPriceValue));
                }             //折扣
                else {
                    holder.productOldPrice.setVisibility(View.GONE);
                    holder.saleOutPrice.setVisibility(View.VISIBLE);

                    double price = JsonUtils.getDouble(item, "price", 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    holder.txtPriceValue.setText("" + PriceUtils.beautify(priceValue));

                    holder.saleOutPrice.setText(tagValue);
                }             //满减
            }
            WindowUtils.boldMethod(holder.saleOutPrice);

            holder.editView.setTag(item);
            holder.editView.setOnValueChangeListener(this);

            if (pulled) {
                holder.pulledImg.setVisibility(View.VISIBLE);
            } else {
                if (sellout) {
                    holder.selloutImg.setVisibility(View.VISIBLE);
                } else {
                    if (noStorage) {
                        holder.noStorageImg.setVisibility(View.VISIBLE);
                        holder.noStorageHint.setVisibility(View.VISIBLE);
                        holder.noStorageHint.setText("库存仅剩" + stock + "件");
                    } else {
                        holder.selloutImg.setVisibility(View.GONE);
                        holder.pulledImg.setVisibility(View.GONE);
                        holder.noStorageImg.setVisibility(View.GONE);
                        holder.noStorageHint.setVisibility(View.GONE);
                    }
                }
            }

            holder.editView.setMinValue(1);
            holder.editView.setMaxValue(99);
            if (editClick) {
                holder.xView.setVisibility(View.GONE);
                holder.txtCountValue.setVisibility(View.GONE);
                if (pulled || sellout) {
                    holder.editView.setVisibility(View.GONE);
                } else {
                    holder.editView.setVisibility(View.VISIBLE);
                }
                holder.editView.setValue(JsonUtils.getInt(item, "amount", 0));
                view.setEnabled(false);
            } else {
                holder.xView.setVisibility(View.VISIBLE);
                holder.txtCountValue.setVisibility(View.VISIBLE);
                holder.editView.setVisibility(View.GONE);
                view.setEnabled(true);
            }
            holder.txtTitleValue.setText(JsonUtils.getString(item, "goodsTitle", ""));

            String styleName = JsonUtils.getString(item, "styleName", "");
            String subName = JsonUtils.getString(item, "subName", "");
            holder.txtStyleValue.setText(styleName);
            holder.txtSubValue.setText(subName);
            WindowUtils.boldMethod(holder.txtStyleValue);
            WindowUtils.boldMethod(holder.txtSubValue);
            holder.txtCountValue.setText(JsonUtils.getString(item, "amount", ""));

            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(item, "styleCover", "--"), 500, 500), holder.goodsImage, -1);

            holder.selectView.setTag(item);

            holder.selectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject item = (JSONObject) v.getTag();
                    pulled = JsonUtils.getBoolean(item, "pulled", false); // 下架
                    sellout = JsonUtils.getBoolean(item, "sellout", false); // 售罄
                    noStorage = JsonUtils.getBoolean(item, "noStorage", false); //  库存不足
                    if ((pulled || sellout || noStorage) && (!editClick)) {
                        return;
                    }
                    checked = JsonUtils.getBoolean(item, "checked", false);
                    JsonUtils.put(item, "checked", !checked);
                    for (int i = 0; i < cartList.length(); i++) {
                        if (JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, i, null), "checked", false)) {
                            count++;
                            int type = JsonUtils.getInt(JsonUtils.getJsonItem(cartList, i, null), "discountType", 0);
                            if (type == 3) {
                                double freeAmountMoney = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "coupon.toAmount", 0); //需要达到的目标金额
                                double freeMoney = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "coupon.amount", 0); //达标后优惠金额

                                double singlePrice = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "discountPrice", 0);
                                double itemCount = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "amount", 0);
                                priceItem = singlePrice * itemCount;
                                allPrice = allPrice + priceItem;
                                if (priceItem > freeAmountMoney) {
                                    allPrice = allPrice - freeMoney;
                                    preferentialPrice = preferentialPrice + freeMoney;
                                }
                            } else if (type == 2) {
                                double oldPrice = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "price", 0); // 原单价
                                double singlePrice = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "discountPrice", 0); // 折后单价
                                double itemCount = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "amount", 0);
                                priceItem = singlePrice * itemCount;
                                allPrice = allPrice + priceItem;
                                preferentialPrice = preferentialPrice + (oldPrice - singlePrice) * itemCount;
                            } else {
                                double singlePrice = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "discountPrice", 0);
                                double itemCount = JsonUtils.getDouble(JsonUtils.getJsonItem(cartList, i, null), "amount", 0);
                                priceItem = singlePrice * itemCount;
                                allPrice = allPrice + priceItem;
                            }
                        }
                    }
                    if (editClick) {
                        payBt.setText("删除（" + count + "）");
                    } else {
                        payBt.setText("结算（" + count + "）");
                    }
                    double lookMoney = NumberUtils.decimalDouble(allPrice);
                    double freeLookMoney = NumberUtils.decimalDouble(preferentialPrice);
                    money.setText("¥" + PriceUtils.beautify(lookMoney));
                    preferentialView.setText("已优惠:¥" + PriceUtils.beautify(freeLookMoney));
                    if (preferentialPrice > 0) {
                        preferentialView.setVisibility(View.VISIBLE);
                    } else {
                        preferentialView.setVisibility(View.GONE);
                    }
                    notifyDataSetChanged();
                    allSelectCheck();
                    count = 0;
                    allPrice = 0;
                    preferentialPrice = 0;
                }
            });
            checked = JsonUtils.getBoolean(JsonUtils.getJsonItem(cartList, i, null), "checked", false);
            if (checked) {
                holder.selectBt.setImageResource(R.mipmap.high_dp_select_icon);
            } else {
                holder.selectBt.setImageResource(R.mipmap.high_dp_default_icon);
            }

            if (pulled || sellout || noStorage) {
                if (editClick) {
                    holder.selectBt.setVisibility(View.VISIBLE);
                    holder.selectView.setEnabled(true);
                } else {
                    holder.selectBt.setVisibility(View.GONE);
                    holder.selectView.setEnabled(false);
                }
            } else {
                holder.selectBt.setVisibility(View.VISIBLE);
            }
            shoppingCartAdapter.notifyDataSetChanged();
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    {
                        String goodsId = JsonUtils.getString(JsonUtils.getJsonItem(cartList, i, null), "goodsId", "");
                        Intent intent = new Intent(ShopCartActivity.this, NewProductInformationActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("goodsId", goodsId);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
            });

            TextView textView = view.findViewById(R.id.icon);
            WindowUtils.boldMethod(textView);
            WindowUtils.boldMethod(holder.txtTitleValue);
            WindowUtils.boldMethod(holder.txtPriceValue);

            return view;
        }
    }

    private void submitOrder() {
        Map<String, Object> param = new HashMap<>();
        param.put("items", items);
        loadingDialog.show();
        HttpRequest.postJson(AppConfig.CREATE_ORDER, null, param, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
                items.clear();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    orderId = JsonUtils.getString(response, "data", "");

                    Intent intent = new Intent(ShopCartActivity.this, ConfirmOrderActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("orderId", orderId);
                    intent.putExtras(bundle);
                    startActivity(intent);

                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(ShopCartActivity.this, "服务器繁忙", false);
            }
        });
    }

    private void deleteCart() {

        loadingDialog.show();
        HttpRequest.delete(RestfulUrl.build(AppConfig.DELETE_CART, ":cartIds", cartIds), null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    cartIds = "";
                    AlertManager.showSuccessToast(ShopCartActivity.this, "删除成功", false);
                    cartList = new JSONArray();
                    loadCart();
                    selectImage.setImageResource(R.mipmap.high_dp_default_icon);
                    payBt.setText("删除（0）");
                    setSubmitText("完成");

                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(ShopCartActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(ShopCartActivity.this, "服务器繁忙", false);
            }
        });
    }

    private void editCart() {

        for (int i = 0; i < cartList.length(); i++) {
            editGoodsId = JsonUtils.getString(JsonUtils.getJsonItem(cartList, i, null), "goodsId", null);
            editStyleId = JsonUtils.getString(JsonUtils.getJsonItem(cartList, i, null), "styleId", null);
            String data = JsonUtils.getString(JsonUtils.getJsonItem(cartList, i, null), "amount", null);
            String cartId = JsonUtils.getString(JsonUtils.getJsonItem(cartList, i, null), "cartId", null);
            Map<String, String> editParams = new HashMap<>();
            editParams.put("goodsId", editGoodsId);
            editParams.put("styleId", editStyleId);
            editParams.put("cartId", cartId);
            editParams.put("amount", data);
            editItems.add(editParams);

        }

        HttpRequest.postJson(AppConfig.SHOPPING_CART_EDIT, null, new Gson().toJson(editItems), JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                if (!success || JsonUtils.getCode(response) != 0) {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    cartList = new JSONArray();
                    editItems.clear();
                    loadCart();
                    AlertManager.showSuccessToast(ShopCartActivity.this, "保存成功", false);
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(ShopCartActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(ShopCartActivity.this, "服务器繁忙", false);
            }
        });
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }


    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            cartListView.setVisibility(View.GONE);
        } else {
            AlertManager.showErrorInfo(ShopCartActivity.this);
        }
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_create_waitpay_order)) {
            cartResource = new JSONArray();
            cartList = new JSONArray();
            loadCart();
            priceItem = 0;
            allPrice = 0;
            preferentialPrice = 0;
            payBt.setText("结算（0）");
            money.setText("¥0");
            preferentialView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
