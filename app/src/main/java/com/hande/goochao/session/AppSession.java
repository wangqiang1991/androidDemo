package com.hande.goochao.session;

import android.util.SparseArray;

public class AppSession {
	

	private SparseArray storage;

	private volatile static AppSession instance;

	/**
	 * 懒汉加载，使用双重校验锁定
	 * @return
	 */
	public static AppSession getInstance(){
		if(instance == null){
			synchronized (AppSession.class){
				if(instance == null) {
					instance = new AppSession();
				}
			}
		}
		return instance;
	}

	public AppSession() {
		this.storage = new SparseArray();
	}

	public void put(int key, Object value){
		storage.put(key, value);
	}

	public <T> T get(int key){
		return (T)storage.get(key);
	}

	public void remove(int key){
		storage.remove(key);
	}

	public boolean has(int key){
		int index = storage.indexOfKey(key);
		return index >= 0;
	}
	
}
