package com.hande.goochao.commons.views.gallery;

import android.graphics.RectF;
import android.widget.ImageView;

/**
 * Created by Wangem on 2018/3/6.
 */

public interface OnPhotoViewChangeListener {

    void onChange(ImageView imageView, float scale, RectF displayRect);

}
