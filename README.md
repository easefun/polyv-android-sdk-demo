polyv-android-sdk-demo
======================

主要演示polyv视频下载，本地播放，网络播放，视频拍摄和上传功能

配置
--
下载本案例，在eclipse创建android项目，选择"android project from existing code"

额外需要的包有

	commons-codec-1.5.jar
	httpclient-android-4.3.3.jar
	httpmime-4.3.5.jar
	polyvSDK.jar
	universal-image-loader-1.9.3-SNAPSHOT.jar
	


描述
--
演示窗体入口`NewTestActivity`

      先初始化PolyvSDKClient，设置token
    	PolyvSDKClient client = PolyvSDKClient.getInstance();
		client.setReadtoken("nsJ7ZgQMN0-QsVkscukWt-qLfodxoDFm");
		client.setWritetoken("Y07Q4yopIVXN83n-MPoIlirBKmrMPJu0");
		client.setPrivatekey("DFZhoOnkQf");
		client.setUserId("sl8da4jjbx");
		client.setDownloadId(downloadId);
		client.setDownloadSecretKey(downloadSercetkey);
		client.setSign(true);
		client.setDownloadDir(saveDir);
		
		
其中downloadId,downloadSecretkey要向客服申请获取。


如何用小窗口播放在线视频
--

使用SmallVideoDemoActivity类做演示，调用方法：

```java
Intent playUrl = new Intent(NewTestActivity.this,SmallVideoDemoActivity.class);
playUrl.putExtra("vid", videoId);
startActivityForResult(playUrl, 1);

```
  
如何全屏播放在线视频
--

使用FullVideoDemoActivity类做演示，调用方法：

```objective-c
Intent playUrlFull = new Intent(NewTestActivity.this,FullVideoDemoActivity.class);
playUrlFull.putExtra("vid", videoId);
startActivityForResult(playUrlFull, 1);
```

使用PolyvVideoView构建播放器
--

```java
import com.easefun.polyvsdk.view.PolyvOnPreparedListener;
import com.easefun.polyvsdk.view.PolyvVideoView;
import com.easefun.polyvsdk.view.PopupMediaController;

...
videoview=(PolyvVideoView) findViewById(R.id.videoview);
mediaController = new PopupMediaController(SmallVideoDemoActivity.this,videoview);
mediaController.setFullscreenListener(new FullscreenListener());
videoview.setMediaController(mediaController);
videoview.setViewSize(w, ah);
videoview.setVideoId("");
				
				

```
