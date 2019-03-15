package com.hande.goochao.views.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;

import org.w3c.dom.Text;

/**
 * Created by LMC on 2018/3/23.
 */

public class NumberEdit extends LinearLayout implements View.OnClickListener {

    private TextView addBt;
    private TextView reduceBt;
    private TextView numberView;

    private int value;
    private int minValue = Integer.MIN_VALUE;
    private int maxValue = Integer.MAX_VALUE;

    private OnValueChangeListener onValueChangeListener;

    public NumberEdit(Context context) {
        super(context);
        init();
    }

    public NumberEdit(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberEdit(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NumberEdit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_number_edit, this, true);

        addBt = findViewById(R.id.add_goods);
        reduceBt = findViewById(R.id.minus_goods);
        numberView = findViewById(R.id.goods_number);

        addBt.setOnClickListener(this);
        reduceBt.setOnClickListener(this);
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    @Override
    public void onClick(View v) {
        if (v == addBt) {
            int newV = value + 1;
            if (newV > maxValue) {
                newV = maxValue;
            }
            setValue(newV);
        } else {
            int newV = value - 1;
            if (newV < minValue) {
                newV = minValue;
            }
            setValue(newV);
        }
        if (onValueChangeListener != null) {
            onValueChangeListener.onChange(this,getValue());
        }
    }

    public void setValue(int value) {
        this.value = value;
        this.numberView.setText(value + "");
    }

    public int getValue() {
        return value;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public interface OnValueChangeListener {

        void onChange(NumberEdit numberEdit, int value);
    }
}
