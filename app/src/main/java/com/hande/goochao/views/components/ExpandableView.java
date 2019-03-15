package com.hande.goochao.views.components;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.hande.goochao.R;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * @author yanshen
 * @description 无
 * Created by yanshen on 2018/9/17.
 */
public class ExpandableView extends ScrollView {

    @ViewInject(R.id.contentLayout)
    private LinearLayout contentLayout;
    private ExpandableViewAdapter adapter;

    private View expandableItemView;

    public ExpandableView(Context context) {
        super(context);
        initView();
    }

    public ExpandableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ExpandableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public ExpandableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_expandable_view, this, true);
        x.view().inject(this);
    }

    public void setAdapter(ExpandableViewAdapter adapter) {
        if (this.adapter != null) {
            throw new RuntimeException("不能重复初始化adapter");
        }
        this.adapter = adapter;
        this.adapter.context = getContext();
        this.adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                contentLayout.removeAllViews();
                render(ExpandableView.this.adapter);
            }
        });
        render(this.adapter);
    }

    private void render(ExpandableViewAdapter adapter) {
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            View itemView = adapter.getView(i, null, contentLayout);

            contentLayout.addView(itemView);

            View headerView = itemView.findViewById(R.id.header_view);
            headerView.setTag(true);
            final View contentView = itemView.findViewById(R.id.content_view);
            ViewGroup.LayoutParams params = contentView.getLayoutParams();
            params.height = 1;
            contentView.setVisibility(View.GONE);

            headerView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean collapse = (boolean) v.getTag();
                    if (collapse) {
                        if (expandableItemView != null) {
                            expandableItemView.findViewById(R.id.header_view).setTag(true);
                            collapse(expandableItemView.findViewById(R.id.content_view));
                        }
                        expand(contentView);
                        expandableItemView = (View) v.getParent();
                    } else {
                        collapse(contentView);
                    }
                    v.setTag(!collapse);
                }
            });
        }
    }

    private void collapse(final View contentView) {
        contentView.clearAnimation();
        final int initialHeight = contentView.getMeasuredHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1) {
                    contentView.setVisibility(View.GONE);
                } else {
                    contentView.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                }
                contentView.requestLayout();
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(300);
        contentView.startAnimation(animation);
        contentView.requestLayout();
    }

    private void expand(final View contentView) {

        // 是否展开的最后一个视图
        final boolean last = contentLayout.getChildAt(contentLayout.getChildCount() - 1) == contentView.getParent();

        contentView.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = contentView.getMeasuredHeight();
        contentView.getLayoutParams().height = 1;
        contentView.setVisibility(VISIBLE);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                contentView.getLayoutParams().height = (interpolatedTime == 1) ? targetHeight : (int) (targetHeight * interpolatedTime);
                contentView.requestLayout();
                if (last) {
                    fullScroll(ScrollView.FOCUS_DOWN);
                }
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(300);
        contentView.startAnimation(animation);
    }

    public static abstract class ExpandableViewAdapter extends BaseAdapter {

        private Context context;

        @Override
        public final View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_expandable_item_view, parent, false);
            View headerView = getHeaderView(position);
            View contentView = getContentView(position);
            ((LinearLayout) view.findViewById(R.id.header_view)).addView(headerView);
            ((LinearLayout) view.findViewById(R.id.content_view)).addView(contentView);
            return view;
        }

        public abstract View getHeaderView(int position);
        public abstract View getContentView(int position);
    }

}
