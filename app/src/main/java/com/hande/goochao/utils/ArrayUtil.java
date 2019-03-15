package com.hande.goochao.utils;

import java.util.Collection;


/**
 * 数组或数据集合连接公用类
 * @author YShen
 * @version 1.0
 */
public class ArrayUtil {

	// Suppresses default constructor, ensuring non-instantiability.
	private ArrayUtil(){
		
	}
	/**
	 * 数组连接方法
	 * @param array 目标数组
	 * @param sep 分隔符
	 * @return
	 */
	public static String join(Object[] array, String sep)
	{
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<array.length; i++) {
			if(i == array.length-1)
				sep = "";
			sb.append(array[i]).append(sep);
		}
		return sb.toString();
	}
	
	/**
	 * 集合连接方法
	 * @param collection
	 * @param sep
	 * @return
	 */
	public static String join(Collection<?> collection, String sep)
	{
		if(null == collection || collection.size() == 0)
			return "";
		return join(collection.toArray(), sep);
	}
	
}
