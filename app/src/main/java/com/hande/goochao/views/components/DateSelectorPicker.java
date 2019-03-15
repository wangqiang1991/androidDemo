package com.hande.goochao.views.components;

import android.app.Activity;
import android.os.ParcelUuid;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.utils.OSUtils;
import com.hande.goochao.utils.WindowUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by yanshen on 2016/2/29.
 */
public class DateSelectorPicker implements View.OnClickListener, DatePicker
        .OnDateChangedListener {

    private View dateSelectorPicker;
    private PopupWindow mPopupWindow;
    private DateSelectorPickerListener mListener;
    private Activity mContext;
    private DatePicker date_picker;

    private TextView doneBtn;
    private TextView cancelBtn;

    public DateSelectorPicker(Activity context) {
        this.mContext = context;
    }

    public void setListener(DateSelectorPickerListener listener) {
        this.mListener = listener;
    }

    public void show() {
        if (dateSelectorPicker == null) {
            dateSelectorPicker = LayoutInflater.from(mContext).inflate(R.layout.view_date_selector, null);
            date_picker = (DatePicker) dateSelectorPicker.findViewById(R.id.date_picker);
            if (OSUtils.isHuawei()) {
                int width = WindowUtils.getDeviceWidth(mContext);
                int height = WindowUtils.getDeviceHeight(mContext);
                mPopupWindow = new PopupWindow(dateSelectorPicker, width,
                        (height / 9) * 4, true);
            } else {
                mPopupWindow = new PopupWindow(dateSelectorPicker, RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT, true);
            }

            // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
            mPopupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(android.R.color
                    .transparent));
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setAnimationStyle(R.style.popwindow_anim_style);

            init();
        }

        if (mPopupWindow.isShowing()) {
            return;
        }

        final Window window = mContext.getWindow();

        WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 1f;
        window.setAttributes(params);

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });

        // 通过绝对位置显示
        mPopupWindow.showAtLocation(window.getDecorView(), Gravity.BOTTOM, 0, 0);
//        DatePicker.this.setMinDate(new Date());
    }

    private void init() {
        doneBtn = (TextView) dateSelectorPicker.findViewById(R.id.done_btn);
        cancelBtn = (TextView) dateSelectorPicker.findViewById(R.id.cancel_btn);
        doneBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

//        datePicker.init(startNow.getYear() - 1900, startNow.getMonth(), startNow.getDate(), this);
    }

    public void setDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        date_picker.init(year, month, day, this);
    }

    @Override
    public void onClick(View v) {
        mPopupWindow.dismiss();
        if (mListener != null && v == doneBtn) {
            mListener.onSelected(new Date(date_picker.getYear() - 1900, date_picker.getMonth(), date_picker.getDayOfMonth()));
        }
    }


    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    }


    public interface DateSelectorPickerListener {

        /**
         * 选择日期后
         *
         * @param date
         */
        void onSelected(Date date);

    }

    public void setMaxDate(long maxDate) {
        date_picker.setMaxDate(maxDate);
    }
}
