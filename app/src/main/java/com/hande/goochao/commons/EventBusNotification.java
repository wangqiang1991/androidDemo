package com.hande.goochao.commons;

/**
 * Created by Wangem on 2017/7/25.
 */
public class EventBusNotification {


    /**
     * 微信授权成功
     */
    public static String event_bus_wx_auth_success = "event_bus_wx_auth_success";

    /**
     * 注册成功
     */
    public static String event_bus_register_success = "event_bus_register_success";

    /**
     * 注销登录
     */
    public static String event_bus_logout = "event_bus_logout";

    /**
     * 取消订单
     */
    public static String event_bus_cancel_order = "event_bus_cancel_order";

    /**
     * 支付成功后关闭页面
     */
    public static String event_pay_close = "event_pay_close";

    /**
     * 刷新订单数据
     */
    public static String event_bus_refresh_order_list = "event_bus_refresh_order_list";

    /**
     * 重新登录，token失效
     */
    public static String event_bus_re_login = "event_bus_re_login";

    /**
     * 低版本请求接口被服务器拒绝
     */
    public static String event_bus_server_reject = "event_bus_server_reject";

    /**
     * 发现页面空间选择风格通知
     */
    public static String event_bus_inspiration_style_space = "event_bus_inspiration_style_space";
    /**
     * 发现场景图页面选择风格通知
     */
    public static String event_bus_inspiration_style_scene = "event_bus_inspiration_style_scene";

    /**
     * 发现页面选择区域通知
     */
    public static String event_bus_inspiration_region = "event_bus_inspiration_region";

    /**
     * 发现页面选择类别通知
     */
    public static String event_bus_inspiration_kind = "event_bus_inspiration_kind";

    /**
     * 发现页面期刊选择分类通知
     */
    public static String event_bus_inspiration_article = "event_bus_inspiration_article";

    /**
     * 发现页面上滑通知
     */
    public static String event_bus_inspiration_up = "event_bus_inspiration_up";

    /**
     * 发现页面下滑通知
     */
    public static String event_bus_inspiration_down = "event_bus_inspiration_down";

    /**
     * 发现页面主TabLayout切换通知
     */
    public static String event_bus_inspiration_change = "event_bus_inspiration_change";

    /**
     *  首页空间搭配跳转至发现页面
     */
    public static String event_bus_to_inspiration = "event_bus_to_inspiration";

    /**
     *  首页积赞优选跳转至发现场景图页面
     */
    public static String event_bus_to_scene = "event_bus_to_scene";

    /**
     *  首页积赞优选跳转至发现场景图页面,当mainActivity切换tab延迟后执行
     */
    public static String event_bus_change_scene = "event_bus_change_scene";

    /**
     * 登陆成功
     */
    public static String event_bus_login_success = "event_bus_login_success";

    /**
     * 新人礼包领取成功(或者活动加入成功)—隐藏首页红包
     */
    public static String event_bus_hide_gift_icon = "event_bus_hide_gift_icon";

    /**
     * 首页活动banner中点击跳转至软装方案列表
     */
    public static String event_bus_banner_to_plan_list = "event_bus_banner_to_plan_list";


    /**
     * 首页跳转至软装方案列表
     */
    public static String event_bus_plan_list = "event_bus_plan_list";

    /**
     * 删除领取方案时通知
     */
    public static String event_bus_plan_delete = "event_bus_plan_delete";

    /**
     * 领取方案时通知
     */
    public static String event_bus_plan_get = "event_bus_plan_get";

    /**
     * 购物车已提交，生成了待支付订单
     */
    public static String event_bus_create_waitpay_order = "event_bus_create_waitpay_order";

    /**
     *  首页空间搭配跳转至发现空间页面,当mainActivity切换tab延迟后执行
     */
    public static String event_bus_change_space = "event_bus_change_space";

    /**
     *  首页软装案例跳转至发现软装方案页面
     */
    public static String event_bus_to_plan = "event_bus_to_plan";

    /**
     *  首页软装案例跳转至发现软装方案页面,当mainActivity切换tab延迟后执行
     */
    public static String event_bus_change_plan = "event_bus_change_plan";

    /**
     *  个人中心页面onResume
     */
    public static String event_bus_person_center_onResume = "event_bus_person_center_onResume";

    private String key;  //key
    private Object value; //value

    public EventBusNotification(String key) {
        this.key = key;
    }

    public EventBusNotification(String key,Object value) {
        this.key = key;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
