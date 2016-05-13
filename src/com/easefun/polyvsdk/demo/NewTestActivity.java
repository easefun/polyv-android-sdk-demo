package com.easefun.polyvsdk.demo;

import java.io.File;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.demo.RecordActivity;
import com.easefun.polyvsdk.demo.download.PolyvDownloadListActivity;
import com.easefun.polyvsdk.demo.upload.PolyvUploadListActivity;
import com.easefun.polyvsdk.demo.IjkVideoActicity;
import com.easefun.polyvsdk.server.AndroidService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;

public class NewTestActivity extends Activity {
	// sl8da4jjbx684cdae6bf17b1b70a8354_s 非加密
	// sl8da4jjbx80cb8878980c1626c51923_s 加密
	private static String videoId = "sl8da4jjbx80cb8878980c1626c51923_s";
	private static String TAG = "NewTestActivity";
	private MyBroadcastReceiver myBroadcastReceiver = null;
	private Button btn_downloadlist, btn_playUrl, btn_playUrlFull,
		btn_record, btn_upload, btn_videolist;
//	private Button btn_playLocal, btn_playLocalFull;
	File saveDir;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		btn_downloadlist = (Button) findViewById(R.id.download_list);
		btn_playUrl = (Button) findViewById(R.id.onlinevideo_1);
		btn_playUrlFull = (Button) findViewById(R.id.onlinevideo_2);
//		btn_playLocal = (Button) findViewById(R.id.localvideo_1);
//		btn_playLocalFull = (Button) findViewById(R.id.localvideo_2);
		btn_record = (Button) findViewById(R.id.recordvideo);
		btn_upload = (Button) findViewById(R.id.upload);
		btn_videolist = (Button) findViewById(R.id.videolist);
		btn_downloadlist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent downloadlist = new Intent(NewTestActivity.this, PolyvDownloadListActivity.class);
				startActivity(downloadlist);
			}
		});

		btn_playUrl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				IjkVideoActicity.intentTo(NewTestActivity.this, IjkVideoActicity.PlayMode.portrait, IjkVideoActicity.PlayType.vid, videoId, false);
			}
		});

		btn_playUrlFull.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				IjkVideoActicity.intentTo(NewTestActivity.this, IjkVideoActicity.PlayMode.landScape, IjkVideoActicity.PlayType.vid, videoId, false);
			}
		});
		
//		btn_playLocal.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				int range = videoId.indexOf("_");
//				String vpid = videoId.substring(0, range);
//				int bitRate = 1;
//				File mp4File = new File(PolyvSDKClient.getInstance().getDownloadDir(), vpid + "_" + bitRate + ".mp4");
//
//				if (mp4File.exists()) {
//					Intent playLocal = new Intent(NewTestActivity.this, IjkVideoActicity.class);
//					playLocal.putExtra("path", mp4File.getPath());
//					playLocal.putExtra("vid", videoId);
//					startActivityForResult(playLocal, 1);
//				} else {
//					Toast.makeText(NewTestActivity.this, "视频文件不存在,请先行下载", Toast.LENGTH_LONG).show();
//					;
//				}
//			}
//		});
//		
//		btn_playLocalFull.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				int range = videoId.indexOf("_");
//				String vpid = videoId.substring(0, range);
//				int bitRate = 1;
//				File mp4File = new File(PolyvSDKClient.getInstance().getDownloadDir(), vpid + "_" + bitRate + ".mp4");
//				if (mp4File.exists()) {
//					Intent playLocal = new Intent(NewTestActivity.this, IjkFullVideoActivity.class);
//					playLocal.putExtra("path", mp4File.getPath());
//					playLocal.putExtra("vid", videoId);
//					startActivityForResult(playLocal, 1);
//				} else {
//					Toast.makeText(NewTestActivity.this, "视频文件不存在,请先行下载", Toast.LENGTH_LONG).show();
//					;
//				}
//			}
//		});
		
		btn_record.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent myIntent = new Intent(NewTestActivity.this, RecordActivity.class);
				NewTestActivity.this.startActivity(myIntent);
			}
		});
		
		btn_upload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent=new Intent();
				intent.setClass(NewTestActivity.this, PolyvUploadListActivity.class);
				startActivity(intent);
			}
		});
		
		btn_videolist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent videolist = new Intent(NewTestActivity.this, VideoListActivity.class);
				startActivity(videolist);
			}
		});
		
		//如果httpd service 启动失败，就会发送消息上来提醒失败了
		IntentFilter statusIntentFilter = new IntentFilter(AndroidService.SERVICE_ERROR_BROADCAST_ACTION);
		statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		myBroadcastReceiver = new MyBroadcastReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, statusIntentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myBroadcastReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcastReceiver);
			myBroadcastReceiver = null;
		}
	}

	/**
	 * 本地服务的错误消息广播接收，如果本地服务启动失败，就需要接收此广播并给用户弹出提示
	 * @author TanQu
	 */
	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int count = intent.getIntExtra("count", 0);
			AlertDialog.Builder builder = new AlertDialog.Builder(NewTestActivity.this);
			builder.setTitle("提示");
			builder.setMessage(String.format("%d次重试都没有成功开启server,请截图联系客服", count));
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			});
			
			builder.setCancelable(false);
			builder.show();
		}
	}
}
