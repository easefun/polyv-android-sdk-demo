package com.easefun.polyvsdk.demo;

import java.io.File;

import com.easefun.polyvsdk.PolyvDevMountInfo;
import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.SDKUtil;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Application;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public class MyApplication extends Application {

	private static final String TAG = MyApplication.class.getSimpleName();
	//aeskey,iv 值修改请参考https://github.com/easefun/polyv-android-sdk-demo/wiki/10.%E5%85%B3%E4%BA%8E-SDK%E5%8A%A0%E5%AF%86%E4%B8%B2-%E4%B8%8E-%E7%94%A8%E6%88%B7%E9%85%8D%E7%BD%AE%E4%BF%A1%E6%81%AF%E5%8A%A0%E5%AF%86%E4%BC%A0%E8%BE%93
	private String aeskey = "VXtlHmwfS2oYm0CZ";
	private String iv = "2u9gDPKdX6GyQJKU";
	public MyApplication() {
		
	}
	
	@Override
	public void onTerminate(){
		super.onTerminate();
		PolyvSDKClient client = PolyvSDKClient.getInstance();
		client.stopService(getApplicationContext(), PolyvDemoService.class);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "polyvSDK/Cache");
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.memoryCacheExtraOptions(480, 800)
				.threadPoolSize(2)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				// .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
				// You can pass your own memory cache implementation/
				.memoryCache(new WeakMemoryCache()).memoryCacheSize(2 * 1024 * 1024)
				.diskCacheSize(50 * 1024 * 1024)
				// .discCacheFileNameGenerator(new Md5FileNameGenerator())//
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.diskCacheFileCount(100)
				.diskCache(new UnlimitedDiscCache(cacheDir))
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				.imageDownloader(new BaseImageDownloader(getApplicationContext(), 5 * 1000, 30 * 1000))
				.writeDebugLogs() // Remove for release app
				.build();

		// Initialize ImageLoader with configuration
		ImageLoader.getInstance().init(config);
		initPolyvCilent();
	}
	
	public void initPolyvCilent() {
		//网络方式取得SDK加密串，（推荐）
//		new LoadConfigTask().execute();
		PolyvSDKClient client = PolyvSDKClient.getInstance();
		//设置SDK加密串
//		client.setConfig("你的SDK加密串", aeskey, iv);
		client.setConfig("iPGXfu3KLEOeCW4KXzkWGl1UYgrJP7hRxUfsJGldI6DEWJpYfhaXvMA+32YIYqAOocWd051v5XUAU17LoVlgZCSEVNkx11g7CxYadcFPYPozslnQhFjkxzzjOt7lUPsW", aeskey, iv);
		//初始化数据库服务
		client.initDatabaseService(this);
		//启动服务
		client.startService(getApplicationContext(), PolyvDemoService.class);
		//启动Bugly
//		client.initCrashReport(getApplicationContext());
		//启动Bugly后，在学员登录时设置学员id
//		client.crashReportSetUserId(userId);
		//获取SD卡信息
		PolyvDevMountInfo.getInstance().init(this, new PolyvDevMountInfo.OnLoadCallback() {
			
			@Override
			public void callback() {
				if (PolyvDevMountInfo.getInstance().isSDCardAvaiable() == false) {
					// TODO 没有可用的SD卡
					Log.e(TAG, "没有sd卡");
					return;
				}
				
				long remainedSpareInMB = 100; 
				if (PolyvDevMountInfo.getInstance().getSDCardAvailSpace() * 1024 < remainedSpareInMB) {
					// TODO 可用剩余磁盘大小
					Log.e(TAG, String.format("磁盘空间不足%d MB", remainedSpareInMB));
				}
				
				StringBuilder dirPath = new StringBuilder();
				dirPath.append(PolyvDevMountInfo.getInstance().getSDCardPath()).append(File.separator).append("polyvdownload");
				File saveDir = new File(dirPath.toString());
				if (saveDir.exists() == false) {
					saveDir.mkdir();
				}
				
				//如果生成不了文件夹，可能是外部SD卡需要写入特定目录/storage/sdcard1/Android/data/包名/
				if (saveDir.exists() == false) {
					dirPath.delete(0, dirPath.length());
					dirPath.append(PolyvDevMountInfo.getInstance().getSDCardPath()).append(File.separator).append("Android").append(File.separator).append("data")
						.append(File.separator).append(getPackageName()).append(File.separator).append("polyvdownload");
					saveDir = new File(dirPath.toString());
					getExternalFilesDir(null); // 生成包名目录
					saveDir.mkdirs();
				}
				
				if (saveDir.exists() == false) {
					// TODO 没有任何可写的SD卡
					Log.e(TAG, "没有SD卡可供保存文件，不能使用下载功能");
					return;
				}
				
				//设置下载文件保存目录
				PolyvSDKClient.getInstance().setDownloadDir(saveDir);
			}
		});
	}
	
	private class LoadConfigTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String config = SDKUtil.getUrl2String("http://demo.polyv.net/demo/appkey.php", false);
			if (TextUtils.isEmpty(config)) {
				try {
					throw new Exception("没有取到数据");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return config;
		}
		
		@Override
		protected void onPostExecute(String config) {
			PolyvSDKClient client = PolyvSDKClient.getInstance();
			client.setConfig(config, aeskey, iv);
		}
	}

}
