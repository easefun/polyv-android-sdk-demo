polyv-android-sdk-demo
======================

主要演示polyv视频下载，本地播放，网络播放，视频拍摄和上传功能

配置
--
下载本案例，在eclipse创建android项目，选择"android project from existing code"

额外需要的包有

	httpclient-android-4.3.3.jar
	httpmime-4.3.5.jar


描述
--
      先初始化PolyvSDKClient，设置token
      	PolyvSDKClient client = PolyvSDKClient.getInstance();
	client.setReadtoken("nsJ7ZgQMN0-QsVkscukWt-qLfodxoDFm");
	client.setWritetoken("Y07Q4yopIVXN83n-MPoIlirBKmrMPJu0");
		
参考MainActivity按钮事件演示代码

<img src="https://cloud.githubusercontent.com/assets/3022663/4606614/4593a3e2-5227-11e4-8108-e1ef286ca087.png" alt="" style="width: 200px;"/>


  
