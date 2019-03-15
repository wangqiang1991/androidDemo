package com.hande.goochao.utils;

import android.widget.ImageView;


import org.xutils.image.ImageOptions;

/**
 * Created by Lmc on 2018/2/11.
 */

public class ImageOptionsUtil {

    /**
     * 设置图片加载
     * @param loadingMipmapId  加载时图片
     * @return
     */
    public static ImageOptions getImageOptions(int loadingMipmapId) {

        ImageOptions imageOptions = new ImageOptions.Builder()
                .setIgnoreGif(false)//是否忽略gif图。false表示不忽略。不写这句，默认是true
                .setImageScaleType(ImageView.ScaleType.FIT_XY)
                .setPlaceholderScaleType(ImageView.ScaleType.FIT_XY)
                .setLoadingDrawableId(loadingMipmapId)
//                .setSize(-1, -1)
                .setUseMemCache(true)
                .build();

        return imageOptions;
    }

    /**
     * 设置图片加载(center_crop)
     * @param loadingMipmapId  加载时图片
     * @return
     */
    public static ImageOptions getImageOptionsCenter(int loadingMipmapId) {

        ImageOptions imageOptions = new ImageOptions.Builder()
                .setIgnoreGif(false)//是否忽略gif图。false表示不忽略。不写这句，默认是true
                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .setPlaceholderScaleType(ImageView.ScaleType.CENTER_CROP)
                .setLoadingDrawableId(loadingMipmapId)
//                .setSize(-1, -1)
                .setUseMemCache(true)
                .build();

        return imageOptions;
    }

    /**
     * 设置图片加载
     * @return
     */
    public static ImageOptions getImageOptions() {

        ImageOptions imageOptions = new ImageOptions.Builder()
                .setIgnoreGif(false)//是否忽略gif图。false表示不忽略。不写这句，默认是true
                .setImageScaleType(ImageView.ScaleType.FIT_XY)
                .setPlaceholderScaleType(ImageView.ScaleType.FIT_XY)
                .setUseMemCache(true)
//                .setSize(-1, -1)
                .build();

        return imageOptions;
    }

    /**
     * 设置图片加载加载大图
     * @return
     */
    public static ImageOptions getBigImageOptions(int loadingMipmapId) {

        ImageOptions imageOptions = new ImageOptions.Builder()
                .setIgnoreGif(false)//是否忽略gif图。false表示不忽略。不写这句，默认是true
//                .setSize(-1,-1)
                .setImageScaleType(ImageView.ScaleType.FIT_XY)
                .setPlaceholderScaleType(ImageView.ScaleType.FIT_XY)
                .setLoadingDrawableId(loadingMipmapId)
                .setUseMemCache(true)
                .build();

        return imageOptions;
    }
}
