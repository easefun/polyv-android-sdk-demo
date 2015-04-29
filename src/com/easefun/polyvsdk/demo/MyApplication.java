package com.easefun.polyvsdk.demo;

import java.io.File;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;


import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MyApplication extends Application {

	
	public MyApplication() {
		// TODO Auto-generated constructor stub
	}
	
	@Override 
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
				File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "polyvSDK/Cache");
				// This configuration tuning is custom. You can tune every option, you may tune some of them, 
				// or you can create default configuration by
				//  ImageLoaderConfiguration.createDefault(this);
				// method.
				ImageLoaderConfiguration config = new ImageLoaderConfiguration  
					    .Builder(getApplicationContext())  
					    .memoryCacheExtraOptions(480, 800) 
					     .threadPoolSize(2)
					     .threadPriority(Thread.NORM_PRIORITY - 2)
					    .denyCacheImageMultipleSizesInMemory()  
//					    .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/  
					    .memoryCache(new WeakMemoryCache())
					    .memoryCacheSize(2 * 1024 * 1024)    
					    .discCacheSize(50 * 1024 * 1024)    
//					    .discCacheFileNameGenerator(new Md5FileNameGenerator())//
					    .tasksProcessingOrder(QueueProcessingType.LIFO)  
					    .discCacheFileCount(100) 
					    .discCache(new UnlimitedDiscCache(cacheDir))
					    .defaultDisplayImageOptions(DisplayImageOptions.createSimple())  
					    .imageDownloader(new BaseImageDownloader(getApplicationContext(), 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)��ʱʱ��  
					    .writeDebugLogs() // Remove for release app  
					    .build();
					    // Initialize ImageLoader with configuration.  
				
				//Initialize ImageLoader with configuration
				ImageLoader.getInstance().init(config);
	}

}
