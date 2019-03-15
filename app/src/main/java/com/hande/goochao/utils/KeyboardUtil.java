package com.hande.goochao.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * 软键盘操作
 */
public class KeyboardUtil {

	public static InputMethodManager getInputMethodManager(Activity activity){
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm;
	}
	
	public static boolean isActive(Activity activity, View view){
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm.isActive(view);
//		return activity.getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
	}
	
	/**
	 * 隐藏软键盘
	 */
	public static void hidInput(Activity activity) {
		View view = activity.getCurrentFocus();
		if(view != null) {
			hidInput(activity, view);
		}
	}

	/**
	 * 隐藏软键盘
	 * @param activity
	 * @param view
	 */
	public static void hidInput(Activity activity, View view) {
		InputMethodManager imm = getInputMethodManager(activity);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	/**
	 * 显示键盘
	 * @param view
	 */
	public static void showInput(Activity activity, View view) {
		view.requestFocus();
		InputMethodManager imm = getInputMethodManager(activity);
		imm.showSoftInput(view, 0);
	}
	
}
