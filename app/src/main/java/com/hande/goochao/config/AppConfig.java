package com.hande.goochao.config;

import com.hande.goochao.BuildConfig;

/**
 * Created by Wangem on 2017/7/21.
 */
public class AppConfig {

    // <editor-fold desc="常量配置">
    /**
     * 功能：查看图片最大尺寸
     */
    public static final String IMAGE_MAX = "?imageView2/0/w/1000/h/1000|imageslim";

    /**
     * 功能：查看图片最大尺寸
     */
    public static final String IMAGE_FULL_MAX = "?imageView2/0/w/2000/h/2000|imageslim";

    /**
     * 功能:七牛图片查看缩略图
     */
    public static final String IMAGE_COMPRESS = "?imageslim";

    /**
     * AES加密解密密匙
     */
    public static final String AES_Secret_Key = "nLAX5preDkav04Gd".toLowerCase();




    // <editor-fold desc="微信配置">
    /**
     * 微信AppID以及AppSecret
     */
    public static final String WX_APPID = BuildConfig.APP_ID;
    public static final String WX_APPSECRET = BuildConfig.APP_SECRET;

    // </editor-fold>
    // </editor-fold>

    // <editor-fold desc="接口URL配置">

    public static final String RESTFUL_DOMAIN = BuildConfig.REST_DOMAIN;

    // <editor-fold desc="分享URL">
    public static final String SHARE_DOMAIN = RESTFUL_DOMAIN;
    public static final String SHARE_SHARE_URL = SHARE_DOMAIN + "/newscene.html?sceneId=:sceneId";
    public static final String GOODS_SHARE_URL = SHARE_DOMAIN + "/product_information.html?goodsid=:goodsId";
    public static final String PLAN_SHARE_URL = SHARE_DOMAIN + "/plan_detail.html?planId=:planId";
    public static final String SUBJECT_ARTICLE_SHARE_URL = SHARE_DOMAIN + "/article.html?subjectArticleId=:articleId";
    public static final String SPACE_SHARE_URL = SHARE_DOMAIN + "/shopwindowdetail2.html?spaceId=:spaceId";
    public static final String HELP_CENTER_URL = SHARE_DOMAIN + "/help.html";


    public static final String Share_App_Url = SHARE_DOMAIN + "/static/html/downLoad.html";
    public static final String Share_Logo_Url = "http://appsource.goochao.com/share_logo.png";
    public static final String User_Protocol_Url = SHARE_DOMAIN + "/static/html/userAgreement.html";
    public static final String About_Us_Url = SHARE_DOMAIN + "/static/html/aboutUs.html";

    // </editor-fold>

    /**
     * 分类列表查询
     */
    public static final String CATEGORY_LIST = RESTFUL_DOMAIN + "/app/api/category";
    /**
     * 首页数据查询
     */
    public static final String HOME_TOP = RESTFUL_DOMAIN + "/app/api/home";
    /**
     * 推荐商品查询
     */
    public static final String RECOMMEND_PRODUCT = RESTFUL_DOMAIN + "/app/api/goods/recommend";

    /**
     * 相似商品查询
     */
    public static final String SIMILARITY_PRODUCT = RESTFUL_DOMAIN + "/app/api/goods/similarity";

    /**
     * 商品详情查询
     */
    public static final String PRODUCT_INFORMATION = RESTFUL_DOMAIN + "/app/api/goods/:goodsId";

    /**
     * 商品评论查询
     */
    public static final String PRODUCT_COMMENTS = RESTFUL_DOMAIN + "/app/api/order-comments/:goodsId";

    /**
     * 商品列表查询
     */
    public static final String PRODUCT_LIST = RESTFUL_DOMAIN + "/app/api/goods";

    /**
     * 首页推荐查询
     */
    public static final String HOME_RECOMMENDS = RESTFUL_DOMAIN + "/app/api/home/recommends";

    /**
     * 图片获取显示
     */
    public static final String PICTURE_GET = RESTFUL_DOMAIN + "/app/api/picture";

    /**
     * 图片界面检索“全部”弹窗
     */
    public static final String QUERY_INFORMATION_ALL = RESTFUL_DOMAIN + "/app/api/picture/query_all_information";

    /**
     * 图片界面检索“区域”弹窗
     */
    public static final String QUERY_INFORMATION_REGION = RESTFUL_DOMAIN + "/app/api/picture/areas";

    /**
     * 图片界面检索“风格”弹窗
     */
    public static final String QUERY_INFORMATION_STYLE = RESTFUL_DOMAIN + "/app/api/picture/styles";

    /**
     * 设计师界面查询猜你喜欢
     */
    public static final String LIKE_GET = RESTFUL_DOMAIN + "/app/api/picture/like";

    /**
     * 查询设计师作品
     */
    public static final String DESIGN_GET = RESTFUL_DOMAIN + "/app/api/picture/more";

    /**
     * 设计师详情查询
     */
    public static final String DESIGNER_GET = RESTFUL_DOMAIN + "/app/api/picture/designer/detail/";

    /**
     * 文章列表查询
     */
    public static final String MAGAZINE_LIST = RESTFUL_DOMAIN + "/app/api/subject";

    /**
     * 家居场景列表查询 空间列表
     */
    public static final String FURNITURE_LIST = RESTFUL_DOMAIN + "/app/api/space";

    /**
     * 空间描述详情查询
     */
    public static final String SPACE_CONTENT = RESTFUL_DOMAIN + "/app/api/space/article/:spaceId";

    /**
     * 空间详情
     */
    public static final String SPACE_DETAIL = RESTFUL_DOMAIN + "/app/api/space/:spaceId";

    /**
     * banner详情
     */
    public static final String BANNER_DETAIL = RESTFUL_DOMAIN + "/app/api/banner/:bannerId";

    /**
     * 文章
     */
    public static final String MAGAZINE_DETAIL = RESTFUL_DOMAIN + "/app/api/subject/:subjectArticleId";

    /**
     * 文章评论查询
     */
    public static final String MAGAZINE_COMMENT = RESTFUL_DOMAIN + "/app/api/subject/comments/:subjectArticleId";

    /**
     * 点赞商品详情查询
     */
    public static final String EXCELLENT_GOODS_DETAIL = RESTFUL_DOMAIN + "/app/api/praise-goods/";

    /**
     * 富文本渲染接口
     */
    public static final String ARTICLE_CONTENT = RESTFUL_DOMAIN + "/editor/show.html?articleId=";

    /**
     * 缓存目录地址
     */
    public static final String CACHE_DIR = "/goochao/cache";

    /**
     * 序列化缓存目录地址
     */
    public static final String OBJECT_CACHE_DIR = "/obj";

    /**
     * 图片缓存目录地址
     */
    public static final String IMAGE_CACHE_DIR = CACHE_DIR + "/image";


    /**
     * 收货地址列表查询
     */
    public static final String ADDRESS_LIST = RESTFUL_DOMAIN + "/app/api/address";

    /**
     * 省份列表查询
     */
    public static final String PROVINCES_LIST_GET = RESTFUL_DOMAIN + "/app/api/region/provinces";

    /**
     * 城市/区县列表查询
     */
    public static final String CITY_AND_COUNTY_LIST_GET = RESTFUL_DOMAIN + "/app/api/region/city";

    /**
     * 默认收货地址选择，是否使用
     */
    public static final String ADDRESS_USE_POST = RESTFUL_DOMAIN + "/app/api/address/use/";

    /**
     * 登录
     */
    public static final String Login = RESTFUL_DOMAIN + "/app/api/auth/login/v2";

    /**
     * 微信登录
     */
    public static final String Wx_Login = RESTFUL_DOMAIN + "/app/api/auth/wx-login";

    /**
     * 获取验证码
     */
    public static final String GET_CODE = RESTFUL_DOMAIN + "/app/api/verification/code/register";

    /**
     * 注册接口
     */
    public static final String Register = RESTFUL_DOMAIN + "/app/api/member/register/v2";

    /**
     * 删除收货地址
     */
    public static final String DELETE_ADDRESS = RESTFUL_DOMAIN + "/app/api/address/delete/";

    /**
     * 购物车数量查询
     */
    public static final String SHOPPING_CAT_COUNT = RESTFUL_DOMAIN + "/app/api/shopping-cart/count";
    /**
     * 收藏商品
     */
    public static final String COLLECT_GOODS = RESTFUL_DOMAIN + "/app/api/goods/collection";
    /**
     * 领取全部优惠券
     */
    public static final String GET_COUPONS = RESTFUL_DOMAIN + "/app/api/coupon/batch/:goodsId";

    /**
     * 点赞商品点赞提交
     */
    public static final String PRAISE_GOODS_COMMENT = RESTFUL_DOMAIN + "/app/api/praise-goods/comment";

    /**
     * 打小报告
     */
    public static final String FEED_BACK = RESTFUL_DOMAIN + "/app/api/feed-back";

    /**
     * 我的优惠券
     */
    public static final String COUPONS_LIST = RESTFUL_DOMAIN + "/app/api/coupon";

    /**
     * 获取七牛上传token
     */
    public static final String GET_QINIU_TOKEN = RESTFUL_DOMAIN + "/app/api/uploader/token";

    /**
     * 修改个人信息
     */
    public static final String Modify_Member = RESTFUL_DOMAIN + "/app/api/member/modify";

    /**
     * 我喜欢的
     */
    public static final String LIKES_LIST = RESTFUL_DOMAIN + "/app/api/praise-goods/find-by-comment";

    /**
     * 获取各个状态的订单数量
     */
    public static final String Order_Count = RESTFUL_DOMAIN + "/app/api/order/count";

    /**
     * 获取查询待支付的订单列表
     */
    public static final String Pending_Pay_Order_List = RESTFUL_DOMAIN + "/app/api/order/list/non-payment";

    /**
     * 加入购物车
     */
    public static final String ADD_SHOPPING_CAT = RESTFUL_DOMAIN + "/app/api/shopping-cart";

    /**
     * 立即购买
     */
    public static final String BUY_NOW = RESTFUL_DOMAIN + "/app/api/order";
    /**
     * 场景图收藏
     */
    public static final String SCENE_COLLECT = RESTFUL_DOMAIN + "/app/api/scene/collection";

    /**
     * 查询订单列表 查询所有(type == null） 待发货(type == 2) 待收货(type == 3) 已完成(type == 5)
     */
    public static final String Order_List = RESTFUL_DOMAIN + "/app/api/order/list";

    /**
     * 查询订单详情
     */
    public static final String ORDER_DETAIL = RESTFUL_DOMAIN + "/app/api/order/:orderId";

    /**
     * 查询订单详情(待支付订单详情)
     */
    public static final String PAY_ORDER_DETAIL = RESTFUL_DOMAIN + "/app/api/order/repay/:orderId";

    /**
     * 文章收藏
     */
    public static final String MAGAZINE_COLLECT = RESTFUL_DOMAIN + "/app/api/subject/collection";

    /**
     * 文章发布评论
     */
    public static final String PUBLISH_COMMENT = RESTFUL_DOMAIN + "/app/api/articles/comment";

    /**
     * 收藏商品查询
     */
    public static final String COLLECTION_GOODS = RESTFUL_DOMAIN + "/app/api/goods/collection";

    /**
     * 收藏图片查询
     */
    public static final String COLLECTION_PICTURE = RESTFUL_DOMAIN + "/app/api/scene/collection";

    /**
     * 收藏期刊查询
     */
    public static final String COLLECTION_ARTICLE = RESTFUL_DOMAIN + "/app/api/subject/collection";

    /**
     * 订单优惠券列表
     */
    public static final String ORDER_COUPON_LIST = RESTFUL_DOMAIN + "/app/api/coupon/mapping/order";

    /**
     * 获取使用优惠券后订单金额以及运费金额
     */
    public static final String COUPON_MONEY = RESTFUL_DOMAIN + "/app/api/order/coupon/money/:orderId";

    /**
     * 确认订单
     */
    public static final String CONFIRM_ORDER = RESTFUL_DOMAIN + "/app/api/order/confirm";

    /**
     * 删除收藏商品
     */
    public static final String DELETE_COLLECTION_GOODS = RESTFUL_DOMAIN + "/app/api/goods/collection/by-goods/";

    /**
     * 删除收藏期刊
     */
    public static final String DELETE_COLLECTION_ARTICLE = RESTFUL_DOMAIN + "/app/api/subject/collection/by-subject/";

    /**
     * 删除收藏场景图
     */
    public static final String DELETE_COLLECTION_PICTURE = RESTFUL_DOMAIN + "/app/api/scene/collection/by-scene/";
    /**
     * 查询待评价列表
     */
    public static final String Pending_Comments = RESTFUL_DOMAIN + "/app/api/order/list/no-comment";

    /**
     * 查询已评价列表
     */
    public static final String Finished_Comments = RESTFUL_DOMAIN + "/app/api/order/list/comment";


    /**
     * 查询售后订单列表
     */
    public static final String After_Sale_List = RESTFUL_DOMAIN + "/app/api/order/after-sale";

    /**
     * 取消订单
     */
    public static final String Cancel_Order = RESTFUL_DOMAIN + "/app/api/order/cancel/:orderId";

    /**
     * 微信预下单
     */
    public static final String WECHAT_PREPAY = RESTFUL_DOMAIN + "/app/api/pay/order/wx/prepay/:orderNumber";

    /**
     * 获取调起支付宝支付的参数
     */
    public static final String ALIPAY_PREPAY_PARAMS = RESTFUL_DOMAIN + "/app/api/alipay/order/alipay/prepay-params/:orderNumber";

    /**
     * 帮助中心
     */
    public static final String HELP_CENTER = RESTFUL_DOMAIN + "/app/api/service";

    /**
     * 购物车列表查询
     */
    public static final String SHOPPING_CART_LIST = RESTFUL_DOMAIN + "/app/api/shopping-cart";

    /**
     * 订单创建
     */
    public static final String CREATE_ORDER = RESTFUL_DOMAIN + "/app/api/order";

    /**
     * 删除购物车商品
     */
    public static final String DELETE_CART = RESTFUL_DOMAIN + "/app/api/shopping-cart/:cartIds";

    /**
     * 购物车编辑
     */
    public static final String SHOPPING_CART_EDIT = RESTFUL_DOMAIN + "/app/api/shopping-cart/saveAll";

    /**
     * 根据订单编号查询订单详情
     */
    public static final String Order_Detail_By_OrderNumber = RESTFUL_DOMAIN + "/app/api/order/detail/:orderNumber";


    /**
     * 查询热门搜索热词
     */
    public static final String HOT_KEYWORDS = RESTFUL_DOMAIN + "/app/api/hot-keywords";

    /**
     * 关键词搜索
     */
    public static final String SEARCH_KEYWORDS = RESTFUL_DOMAIN + "/app/api/keyword/search";

    /**
     * 售后详情
     */
    public static final String AfterSale_Info = RESTFUL_DOMAIN + "/app/api/work/:relationId";

    /**
     * 搜索商品
     */
    public static final String SEARCH_GOODS = RESTFUL_DOMAIN + "/app/api/goods/search";

    /**
     * 场景图详情查询
     */
    public static final String SCENE_DETAIL = RESTFUL_DOMAIN + "/app/api/picture/detail/:sceneId";

    /**
     * 文章里面领取优惠券
     */
    public static final String GET_COUPON = RESTFUL_DOMAIN + "/app/api/coupon/:couponId";

    /**
     * 搜索图片
     */
    public static final String SEARCH_PICTURE = RESTFUL_DOMAIN + "/app/api/picture/search";

    /**
     * 物流信息
     */
    public static final String Express_Detail = RESTFUL_DOMAIN + "/app/api/order/express/track/:orderNumber";

    /**
     * 客服地址
     */
    public static final String SERVICE_URL = "https://www.sobot.com/chat/h5/index.html?sysNum=2c71dc6beb1a4afd94381adb33357251";

    /**
     * 确认收货
     */
    public static final String Goods_Receive = RESTFUL_DOMAIN + "/app/api/order/receive/:relationId";

    /**
     * 提交订单评价
     */
    public static final String SUBMIT_ORDER_COMMENTS = RESTFUL_DOMAIN + "/app/api/order-comments";

    /**
     * 用户通过旧密码修改密码
     */
    public static final String MODIFY_PASSWORD = RESTFUL_DOMAIN + "/app/api/member/modify-password/v2";

    /**
     * 售后申请
     */
    public static final String Apply_AfterSale = RESTFUL_DOMAIN + "/app/api/work/apply";


    /**
     * 获取验证码(找回密码使用)
     */
    public static final String GET_CODE_RestPwd = RESTFUL_DOMAIN + "/app/api/verification/code/reset-password";

    /**
     * 根据手机号和验证码重置密码
     */
    public static final String Reset_Password = RESTFUL_DOMAIN + "/app/api/member/reset-password/v2";

    /**
     * 订单评价详情
     */
    public static final String ORDER_COMMENTS_DETAIL = RESTFUL_DOMAIN + "/app/api/order-comments/detail/:orderGoodsId";

    /**
     * 验证token是否可用
     */
    public static final String VALIDATE_TOKEN = RESTFUL_DOMAIN + "/app/api/auth/refresh";

    /**
     * 检查更新
     */
    public static final String UPGRADE_CHECK = RESTFUL_DOMAIN + "/upgrade/check/android";

    /**
     * 领取单个优惠券
     */
    public static final String GET_SINGLE_COUPONS = RESTFUL_DOMAIN + "/app/api/coupon/";

    /**
     * 取消默认收货地址
     */
    public static final String ADDRESS_USE_CANCEL = RESTFUL_DOMAIN + "/app/api/address/cancel/";

    /**
     * 查询用户详细信息
     */
    public static final String GET_USER_INFORMATION = RESTFUL_DOMAIN + "/app/api/member/details/";

    /**
     * 新商品详情查询
     */
    public static final String NEW_PRODUCT_INFORMATION = RESTFUL_DOMAIN + "/app/api/goods/:goodsId/v2";

    /**
     * 新首页数据查询
     */
    public static final String NEW_HOME_PAGE = RESTFUL_DOMAIN + "/app/api/home/v2";

    /**
     * 获取商品一级分类和二级分类列表
     */
    public static final String FIRST_AND_SECOND_CATEGORY = RESTFUL_DOMAIN + "/app/api/category/all";

    /**
     * 查询专题文章分类
     */
    public static final String ARTICLE_KIND_GET = RESTFUL_DOMAIN + "/app/api/subject/types";

    /**
     * 查询商品评论条数
     */
    public static final String GET_GOODS_COMMENT_COUNT = RESTFUL_DOMAIN + "/app/api/order-comments/count/:goodsId";

    /**
     * 收货地址详情查询
     */
    public static final String ADDRESS_DETAIL = RESTFUL_DOMAIN + "/app/api/address/";

    /**
     * 确认订单界面，确定运费
     */
    public static final String FREIGHT_GET = RESTFUL_DOMAIN + "/app/api/order/express/money/";

    /**
     * 购物车列表查询(新版)
     */
    public static final String NEW_SHOPPING_CART_LIST = RESTFUL_DOMAIN + "/app/api/shopping-cart/v2";

    /**
     * 活动板块浏览次数+1接口
     */
    public static final String ACTIVITY_READ = RESTFUL_DOMAIN + "/app/api/banner/view/";

    /**
     * 软装方案列表查询
     */
    public static final String DESIGN_PLAN_LIST = RESTFUL_DOMAIN + "/app/api/plan/v2";

    /**
     * 软装方案详情查询
     */
    public static final String DESIGN_PLAN_DETAIL = RESTFUL_DOMAIN + "/app/api/plan/";

    /**
     * 批量加入购物车
     */
    public static final String ADD_TO_CART_LIST = RESTFUL_DOMAIN + "/app/api/shopping-cart/batch-join";

    /**
     * 领取方案
     */
    public static final String COLLECT_PLAN = RESTFUL_DOMAIN + "/app/api/plan-receive/receive";

    /**
     * 首页获取活动相关地址
     */
    public static final String GET_ACTIVITY = RESTFUL_DOMAIN + "/app/api/home/activity-info";

    /**
     * 已领取方案列表
     */
    public static final String COLLECTION_PLAN = RESTFUL_DOMAIN + "/app/api/plan-receive/receive/plans";

    /**
     * 删除已领取软装方案
     */
    public static final String DELETE_COLLECTION_PLAN = RESTFUL_DOMAIN + "/app/api/plan-receive/receive/plans/";

    /**
     * 是否已经领取活动方案
     */
    public static final String CHECK_GET_PLAN = RESTFUL_DOMAIN + "/app/api/plan-receive/receive-record";

    /**
     * 字典查询（查询软装方案搜索条件词列表）1.户型类型 2.价格区间
     */
    public static final String SEARCH_KEY_WORDS = RESTFUL_DOMAIN + "/app/api/dict/";

    /**
     * 发送推送通知，用户点击后，请求后台，点击数+1
     */
    public static final String MESSAGE_UP = RESTFUL_DOMAIN + "/app/api/notice-message/click/";

    /**
     * 推送token上报
     */
    public static final String TOKEN_PUSH = RESTFUL_DOMAIN + "/app/api/push-token";

    /**
     * 记录最后一次使用
     */
    public static final String SAVE_LAST_LOG_TIME = RESTFUL_DOMAIN + "/app/api/member/track";

    /**
     * 获取未读消息数量
     */
    public static final String UNREAD_COUNT = RESTFUL_DOMAIN + "/app/api/message-center/unread";

    /**
     * 查询最新消息
     */
    public static final String LAST_MESSAGE = RESTFUL_DOMAIN + "/app/api/message-center/last-message";

    /**
     * 分页查询历史系统消息
     */
    public static final String GET_SYSTEM_MESSAGE = RESTFUL_DOMAIN + "/app/api/message-center/sys-message";

    /**
     * 分页查询历史推广消息 (1商品 2期刊 3活动)
     */
    public static final String GET_NOTICE_MESSAGE = RESTFUL_DOMAIN + "/app/api/message-center/notice-message";

    /**
     * 清除消息未读数量
     */
    public static final String CLEAR_UNREAD_MESSAGE = RESTFUL_DOMAIN + "/app/api/message-center/unread/clear";

    /**
     * 楼盘列表
     */
    public static final String GET_PREMISES = RESTFUL_DOMAIN + "/app/api/house";

    /**
     * 消息浏览次数+1
     */
    public static final String RECORD_READ = RESTFUL_DOMAIN + "/app/api/message-center/view/";
    // </editor-fold>
}