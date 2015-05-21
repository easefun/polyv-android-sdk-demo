1 集成配置
    jar：项目libs下所使用到的jar有polyvSDK.jar，commons-codec-1.5.jar，httpmine-4.3.5.jar，universal-image-loader.jar，httpcilent-android-4.3.3.jar，ijkmediaplayer.jar和ijkmediawidget.jar。其中polyvSDK已经使用到了ijkmediaplayer和ijkmeidawidget中，因此对于这2个jar只需要Add to Java Build Path即可，而universal-image-loader.jar是为了实现视频列表中图片的加载的。
   res：由于视图控件使用到了xml、图片等资源,因此需要把res文件拷贝到项目res下，所需要用到的文件如图1所示 

图1 drawable/drawable-hdpi

图2  values
另外，项目还需要Layout下的ijkMediaController.xml。而ijkMediaController.xml是新SDK所引用到的。
AndroidManifest.xml：
user-premission需声明的权限有
 <uses-permission android:name="android.permission.CAMERA" />
 <uses-permission android:name="android.permission.INTERNET" />
 <uses-permission android:name="android.permission.RECORD_AUDIO" />
 <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
注册一个Service
<service  
    android:name="com.easefun.polyvsdk.server.AndroidService"
></service>
2 模块说明
  在使用SDK时，需对key参数进行设置，代码调用:
    	PolyvSDKClient client = PolyvSDKClient.getInstance();
		client.setReadtoken(readtoken);
		client.setWritetoken(writetoken);
		client.setPrivatekey(privatekey);
		client.setUserId(userid);
		client.setSign(true);
		client.setDownloadDir(saveDir);//下载文件的目录
       client.startService(getApplicationContext());//启动服务





2.1 下载模块
加密视频下载m3u8到本地目录，ts目录为polyvclient.getDownloadDir/userid/，非加密视频下载MP4，目录为polyvclient.getDownloadDir。
初始化一个PolyvDownloader，两个构造参数分别是视频vid，码率bitRate(1-流畅  2-高清  3-超清)
PolyvDownloader  downloader = new PolyvDownloader(videoId, 1);
开启下载则调用downloader.start()
监听下载进度则
downloader.setPolyvDownloadProressListener(new PolyvDownloadProgressListener() {
					@Override
					public void onDownloadSuccess() {
						// TODO Auto-generated method stub
						Log.i(TAG, "下载完成");
					}
				@Override
					public void onDownload(long current, long total) {
						// TODO Auto-generated method stub
						Message msg = new Message();
						msg.what = DOWNLOAD;
						Bundle bundle = new Bundle();
						bundle.putLong("current", current);
						bundle.putLong("total", total);
						msg.setData(bundle);
						handler.sendMessage(msg);
					}
				});

删除视频：downloader.deleteVideo(String vid,int bitRate)
          downloader.deleteVideo(String vid)
清理下载目录：downloader.cleanDownloadDir()
2.2 视频播放模块
   视频播放调用了位于polyvSDK.jar包里的IjkVideoView类和IjkBaseMediaController类。而IjkBaseMediaController是一个抽象类，开者只需继承并实现里面相关方法即可自定义MediaController的布局。Demo有提供一个示例类MediaController参考实现方法。两者类的实现都引用到了ijkmeidaplayer.jar和ijkmeidawidget中的相关类。播放视频所在的Activity在AndroidManifest.xml中的配置示例如图5所示，其中设置onConfigChanges之后切屏会执行一次onConfigurationChanged，在这里面所做的事情是隐藏掉MediaController确保横屏显示在正确的位置。

图5 IjkVideoActivity的配置
小窗口模式：
     在小窗口模式下，小窗口以屏幕的宽度为基准，宽高比为4:3。
在布局里IjkVideoView的父布局使用RelativeLayout，IjkVideoView居中展示。如图4所示。

图4 IjkVideoView布局
  在Activity中，代码调用：
IjkVideoView videoview=(IjkVideoView)findViewById(R.id.videoview);
ProgressBar progressBar =(ProgressBar)findViewById(R.id.loadingprogress);
videoview.setMediaBufferingIndicator(progressBar); //在缓冲时出现的loading
mediaController = new MediaController(this,false);
mediaController.setAnchorView(videoview);
videoview.setMediaController(mediaController);
videoview.setVid(vid,1);
videoview.setOnPreparedListener(new OnPreparedListener() {
			
			@Override
			public void onPrepared(IMediaPlayer mp) {
				// TODO Auto-generated method stub
				//do something before video play
			}
		});
IjkVideoView 方法介绍：
--void setVid(String vid,int bitRate)
 设置播放视频的vid以及码率
--void setVid(String vid )
  相当于setVid(vid,1) 默认第一个码率
--void seVideoUri(Uri uri)
  播放视频的方法，setVideoUri(Uri.prase(uri/filepath)).
--int getLevel()
   获取码率的数量，当没调用setVid()就调用此方法返回0
--void switchLevel(int bitRate)
   切换码率，当没调用setVid()就调用此方法无效。
--void setMediaBufferingIndicator(View mediaBufferingIndicator)
  播放视频时设置缓冲Loading的View
--void setOnCompletionListener(OnCompletionListener l)
  设置播放完成时的监听器
-- void setOnSeekCompleteListener(OnSeekCompleteListener l)
  设置SeekBar拖动到末尾的监听器
-- void setOnInfoListener(OnInfoListener l)
  设置对播放视频返回信息的监听器
-- void setOnErrorListener(OnErrorListener l)
  设置对错误信息的监听器，本身已有所处理，外部不需要做处理
--void setVideoLayout(int  layout)
  设置Video的视频尺寸，layout对应的4个常量为     VIDEO_LAYOUT_ORIGIN，VIDEO_LAYOUT_SCALE，VIDEO_LAYOUT_STRETCH，VIDEO_LAYOUT_ZOOM
分别是原大小，比例放大，拉伸，缩小

MediaController方法介绍：（用户可以自定义这些方法）
-- void setOnBoardChangeListener(OnBoardChangeListener l)
  设置切屏监听器
-- void setOnVideoChangeListener(OnVideoChangeListener l)
  设置视频尺寸监听器，监听的是layout常量
-- void setOnShownListener(OnShownListener l)
  设置MediaController显示时的监听器
-- void setOnHiddenListener(OnHiddenListener l)
  设置MediaController隐藏时的监听器
横屏模式：
 默认横屏播放只需要在播放视频的Activity的android:screenOrientation="landscape" 即可
预览图模式：
将IjkVideoView替换成PreviewIjkVideoView，并在onPrepared()里面调用initPreview(vid)，并且videoview.pause()让视频停止播放，那么点击中央按钮才可以播放。
