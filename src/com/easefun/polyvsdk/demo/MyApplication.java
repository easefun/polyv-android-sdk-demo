package com.easefun.polyvsdk.demo;

import java.io.File;

import com.easefun.polyvsdk.PolyvDevMountInfo;
import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.SDKUtil;
import com.easefun.polyvsdk.server.AndroidService;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

public class MyApplication extends Application {

	private static final String TAG = MyApplication.class.getSimpleName();
	//加密秘钥和加密向量，在后台->设置->API接口中获取，用于解密SDK加密串
	//值修改请参考https://github.com/easefun/polyv-android-sdk-demo/wiki/10.%E5%85%B3%E4%BA%8E-SDK%E5%8A%A0%E5%AF%86%E4%B8%B2-%E4%B8%8E-%E7%94%A8%E6%88%B7%E9%85%8D%E7%BD%AE%E4%BF%A1%E6%81%AF%E5%8A%A0%E5%AF%86%E4%BC%A0%E8%BE%93
	/** 加密秘钥 */
	private String aeskey = "VXtlHmwfS2oYm0CZ";
	/** 加密向量 */
	private String iv = "2u9gDPKdX6GyQJKU";
	
	private ServiceStartErrorBroadcastReceiver serviceStartErrorBroadcastReceiver = null;
	
	public MyApplication() {
		
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
		//OPPO手机自动熄屏一段时间后，会启用系统自带的电量优化管理，禁止一切自启动的APP（用户设置的自启动白名单除外）。
		//如果startService异常，就会发送消息上来提醒异常了
		//如不需要额外处理，也可不接收此信息
		IntentFilter statusIntentFilter = new IntentFilter(AndroidService.SERVICE_START_ERROR_BROADCAST_ACTION);
		statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		serviceStartErrorBroadcastReceiver = new ServiceStartErrorBroadcastReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(serviceStartErrorBroadcastReceiver, statusIntentFilter);
		
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
					// TODO 没有可用的存储设备
					Log.e(TAG, "没有可用的存储设备");
					return;
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

	private class ServiceStartErrorBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getStringExtra("msg");
			Log.e(TAG, msg);
		}
	}
}
