package com.hande.goochao.views.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.utils.WindowUtils;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/24.
 */
public class ConfirmDialog extends Dialog {

    private LinearLayout lLayout_bg;
    private TextView txtMsg;
    private Button btnLeft;
    private Button btnRight;

    public interface CallBack {
        void buttonClick(Dialog dialog, boolean leftClick);
    }

    public enum ConfirmDialogType {
        ConfirmDialogType_Normal,       // 默认类型
        ConfirmDialogType_Warning,      // 警告类型
        ConfirmDialogType_Danger        // 危险或者删除类型
    }


    public ConfirmDialog(Context context, ConfirmDialogType type) {
        this(context, R.style.ConfirmDialog);
        builder(type);
    }

    public ConfirmDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ConfirmDialog builder(ConfirmDialogType type) {
        setContentView(R.layout.view_alertdialog);
        lLayout_bg = findViewById(R.id.lLayout_bg);
        txtMsg = findViewById(R.id.txt_msg);
        btnLeft = findViewById(R.id.btn_neg);
        btnRight = findViewById(R.id.btn_pos);
        if (type == ConfirmDialogType.ConfirmDialogType_Normal) {
            btnLeft.setTextColor(getContext().getResources().getColor(R.color.gray_add));
            btnRight.setTextColor(getContext().getResources().getColor(R.color.actionsheet_blue));
        } else if (type == ConfirmDialogType.ConfirmDialogType_Warning) {
            btnLeft.setTextColor(getContext().getResources().getColor(R.color.actionsheet_gray));
            btnRight.setTextColor(getContext().getResources().getColor(R.color.gray_add));
        } else if (type == ConfirmDialogType.ConfirmDialogType_Danger) {
            btnLeft.setTextColor(getContext().getResources().getColor(R.color.actionsheet_gray));
            btnRight.setTextColor(getContext().getResources().getColor(R.color.actionsheet_delete));
        }

        WindowUtils.boldMethod(btnLeft);
        WindowUtils.boldMethod(btnRight);

        int width = (int) (WindowUtils.getDeviceWidth(getContext()) * 0.7f);
        lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT));
        return this;
    }

    public ConfirmDialog setMsg(String msg) {
        txtMsg.setText(msg);
        WindowUtils.boldMethod(txtMsg);
        return this;
    }

    public ConfirmDialog setLeftButtonText(String text) {
        btnLeft.setText(text);
        return this;
    }

    public ConfirmDialog setRightButtonText(String text) {
        btnRight.setText(text);
        return this;
    }

    public ConfirmDialog setCallBack(final CallBack callBack) {
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null)
                    callBack.buttonClick(ConfirmDialog.this, false);
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null)
                    callBack.buttonClick(ConfirmDialog.this, true);
            }
        });
        return this;
    }

}
