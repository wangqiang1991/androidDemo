package com.hande.goochao.views.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.andexert.expandablelayout.library.ExpandableLayoutItem;

/**
 * @author yanshen
 * @description 无
 * Created by yanshen on 2018/8/13.
 */
public class MyExpandableLayoutItem extends ExpandableLayoutItem {

    private Boolean isAnimationRunning = false;
    private Boolean isOpened = false;
    private Integer duration;
    private FrameLayout contentLayout;
    private FrameLayout headerLayout;
    private Boolean closeByUser = true;

    private boolean isLast;
    private MyExpandableLayoutListView myExpandableLayoutListView;
    private int pos;

    public MyExpandableLayoutItem(Context context) {
        super(context);
        init(context, null);
    }

    public MyExpandableLayoutItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyExpandableLayoutItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs)
    {
        final View rootView = View.inflate(context, com.andexert.expandablelayout.library.R.layout.view_expandable, this);
        headerLayout = (FrameLayout) rootView.findViewById(com.andexert.expandablelayout.library.R.id.view_expandable_headerlayout);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, com.andexert.expandablelayout.library.R.styleable.ExpandableLayout);
        final int headerID = typedArray.getResourceId(com.andexert.expandablelayout.library.R.styleable.ExpandableLayout_el_headerLayout, -1);
        final int contentID = typedArray.getResourceId(com.andexert.expandablelayout.library.R.styleable.ExpandableLayout_el_contentLayout, -1);
        contentLayout = (FrameLayout) rootView.findViewById(com.andexert.expandablelayout.library.R.id.view_expandable_contentLayout);

        if (headerID == -1 || contentID == -1)
            throw new IllegalArgumentException("HeaderLayout and ContentLayout cannot be null!");

        if (isInEditMode())
            return;

        duration = typedArray.getInt(com.andexert.expandablelayout.library.R.styleable.ExpandableLayout_el_duration, getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
        final View headerView = View.inflate(context, headerID, null);
        headerView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        headerLayout.addView(headerView);
        setTag(ExpandableLayoutItem.class.getName());
        final View contentView = View.inflate(context, contentID, null);
        contentView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        contentLayout.addView(contentView);
        contentLayout.setVisibility(GONE);

        headerLayout.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (isOpened() && event.getAction() == MotionEvent.ACTION_UP)
                {
                    hide();
                    closeByUser = true;
                    AdapterView.OnItemClickListener onItemClickListener = myExpandableLayoutListView.getOnItemClickListenerOverride();
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(myExpandableLayoutListView, MyExpandableLayoutItem.this, pos, 0);
                    }
                }

                return isOpened() && event.getAction() == MotionEvent.ACTION_DOWN;
            }
        });

    }

    private void expand(final View v)
    {
        isOpened = true;
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 0;
        v.setVisibility(VISIBLE);

        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t)
            {
                v.getLayoutParams().height = (interpolatedTime == 1) ? LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
                if (isLast) {
                    myExpandableLayoutListView.smoothScrollToPositionFromTop(pos, 0);
                    myExpandableLayoutListView.setSelection(myExpandableLayoutListView.getBottom());
                }
            }


            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(duration);
        v.startAnimation(animation);
    }

    private void collapse(final View v)
    {
        isOpened = false;
        final int initialHeight = v.getMeasuredHeight();
        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1)
                {
                    v.setVisibility(View.GONE);
                    isOpened = false;
                }
                else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(duration);
        v.startAnimation(animation);
    }

    public void hideNow()
    {
        contentLayout.getLayoutParams().height = 0;
        contentLayout.invalidate();
        contentLayout.setVisibility(View.GONE);
        isOpened = false;
    }

    public void showNow()
    {
        if (!this.isOpened())
        {
            contentLayout.setVisibility(VISIBLE);
            this.isOpened = true;
            contentLayout.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            contentLayout.invalidate();
        }
    }

    public Boolean isOpened()
    {
        return isOpened;
    }

    public void show()
    {
        if (!isAnimationRunning)
        {
            expand(contentLayout);
            isAnimationRunning = true;
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    isAnimationRunning = false;
                }
            }, duration);
        }
    }

    public FrameLayout getHeaderLayout()
    {
        return headerLayout;
    }

    public FrameLayout getContentLayout()
    {
        return contentLayout;
    }

    public void hide()
    {
        if (!isAnimationRunning)
        {
            collapse(contentLayout);
            isAnimationRunning = true;
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    isAnimationRunning = false;
                }
            }, duration);
        }
        closeByUser = false;
    }

    public Boolean getCloseByUser()
    {
        return closeByUser;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public void setMyExpandableLayoutListView(MyExpandableLayoutListView myExpandableLayoutListView) {
        this.myExpandableLayoutListView = myExpandableLayoutListView;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
