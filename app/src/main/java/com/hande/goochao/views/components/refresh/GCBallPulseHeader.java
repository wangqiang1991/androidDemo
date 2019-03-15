package com.hande.goochao.views.components.refresh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;

/**
 * Created by Wangem on 2018/1/31.
 */

public class GCBallPulseHeader extends GCBallPulseFooter implements RefreshHeader {

    public GCBallPulseHeader(@NonNull Context context) {
        super(context);
    }

    public GCBallPulseHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GCBallPulseHeader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
