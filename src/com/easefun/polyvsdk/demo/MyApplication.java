package com.easefun.polyvsdk.demo;

import java.io.File;

import com.easefun.polyvsdk.PolyvSDKClient;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;

public class MyApplication extends Application {
	private File saveDir;

	public MyApplication() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onTerminate(){
		super.onTerminate();
		PolyvSDKClient client = PolyvSDKClient.getInstance();
		client.stopService(getApplicationContext(), PolyvDemoService.class);
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		File cacheDir = StorageUtils.getOwnCacheDirectory(
				getApplicationContext(), "polyvSDK/Cache");
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.memoryCacheExtraOptions(480, 800)
				.threadPoolSize(2)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				// .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 *
				// 1024)) // You can pass your own memory cache implementation/
				.memoryCache(new WeakMemoryCache())
				.memoryCacheSize(2 * 1024 * 1024)
				.discCacheSize(50 * 1024 * 1024)
				// .discCacheFileNameGenerator(new Md5FileNameGenerator())//
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCacheFileCount(100)
				.discCache(new UnlimitedDiscCache(cacheDir))
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				.imageDownloader(
						new BaseImageDownloader(getApplicationContext(),
								5 * 1000, 30 * 1000)) // connectTimeout (5 s),
														// readTimeout (30
														// s)��ʱʱ��
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.

		// Initialize ImageLoader with configuration
		ImageLoader.getInstance().init(config);
		initPolyvCilent();

	}
	
	public void initPolyvCilent() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			saveDir = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/polyvdownload");
			if (!saveDir.exists())
				saveDir.mkdir();
		}

		PolyvSDKClient client = PolyvSDKClient.getInstance();
		
		client.setReadtoken("nsJ7ZgQMN0-QsVkscukWt-qLfodxoDFm");
		client.setWritetoken("Y07Q4yopIVXN83n-MPoIlirBKmrMPJu0");
		client.setPrivatekey("DFZhoOnkQf");
		client.setUserId("sl8da4jjbx");
		
		client.setDownloadDir(saveDir);
		client.initDatabaseService(this);
		client.startService(getApplicationContext(), PolyvDemoService.class);

	}

	class AndroidConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub

		}
	}

}
