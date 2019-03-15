package com.hande.goochao.views.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.utils.WindowUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LMC on 2018/3/27.
 */

public class GuideActivity extends Activity{

    //管理圆点的
    private List<View>dots;
    private int oldPosition = 0;// 记录上一次点的位置
    private int currentItem; // 当前页面
    private View pointView;

    private ViewPager viewPager;//需要ViewPager
    private PagerAdapter mAdapter;//需要PagerAdapter适配器
    private List<View> mViews = new ArrayList<>();//准备数据源
    private Button bt_home;//在ViewPager的最后一个页面设置一个按钮，用于点击跳转到MainActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        if (WindowUtils.hasNavigationBar(this)) {
            WindowUtils.hideBottomUIMenu(this);
        }

        initView();//初始化view

        dots = new ArrayList<>();
        dots.add(findViewById(R.id.dot_1));
        dots.add(findViewById(R.id.dot_2));
        dots.add(findViewById(R.id.dot_3));
        dots.add(findViewById(R.id.dot_4));

        //因为开始页面不响应页面滑动，所以先把开始页面设置为选中背景setBackgroundResource不可以
        for(int i = 0; i < 4 ; i++){
            ImageView iv1=new ImageView(this);
            iv1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
            iv1.setImageResource(R.drawable.guide_point_normal);


        }
        dots.get(0).setBackgroundResource(R.drawable.guide_point_select);

        //addOnPageChangeListener替换掉setOnPageChangeListener
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //当前页面
            @Override
            public void onPageSelected(int position) {
                //遍历当前页改变背景其他背景设为常规
                dots.get(oldPosition).setBackgroundResource(R.drawable.guide_point_normal);
                dots.get(position).setBackgroundResource(R.drawable.guide_point_select);

                oldPosition = position;
                currentItem = position;

                pointView = findViewById(R.id.point_view);
                if (position == 3){
                    pointView.setVisibility(View.GONE);
                }else {
                    pointView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private void initView() {
        viewPager = findViewById(R.id.view_pager);

        LayoutInflater inflater = LayoutInflater.from(this);//将每个xml文件转化为View
        View guideOne = inflater.inflate(R.layout.activity_guide_item1, null);//每个xml中就放置一个imageView
        View guideTwo = inflater.inflate(R.layout.activity_guide_item2,null);
        View guideThree = inflater.inflate(R.layout.activity_guide_item3,null);
        View guideFour = inflater.inflate(R.layout.activity_guide_item4,null);

        mViews.add(guideOne);//将view加入到list中
        mViews.add(guideTwo);
        mViews.add(guideThree);
        mViews.add(guideFour);

        mAdapter = new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = mViews.get(position);//初始化适配器，将view加到container中
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View view=mViews.get(position);
                container.removeView(view);//将view从container中移除
            }

            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;//判断当前的view是我们需要的对象
            }
        };

        viewPager.setAdapter(mAdapter);

        bt_home = guideFour.findViewById(R.id.to_Main);
        bt_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Intent intent = new Intent(GuideActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }catch (Exception ex){
                    System.out.print(ex);
                }
            }
        });
    }
}
