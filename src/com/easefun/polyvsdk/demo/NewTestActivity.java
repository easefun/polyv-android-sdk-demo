package com.easefun.polyvsdk.demo;

import java.io.File;

import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.demo.RecordActivity;
import com.easefun.polyvsdk.demo.IjkVideoActicity;
import com.easefun.polyvsdk.net.PolyvUploadManager;
import com.easefun.polyvsdk.net.Progress;
import com.easefun.polyvsdk.net.Success;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class NewTestActivity extends Activity {
	// sl8da4jjbx684cdae6bf17b1b70a8354_s 非加密
	// sl8da4jjbx80cb8878980c1626c51923_s 加密
	private static String videoId = "sl8da4jjbx80cb8878980c1626c51923_s";
	private static String TAG="NewTestActivity";
	private ProgressDialog barProgressDialog;
	private Button btn_downloadlist, btn_playUrl, btn_playUrlFull,
		btn_record, btn_upload, btn_videolist;
//	private Button btn_playLocal, btn_playLocalFull;
	File saveDir;
	
	private static final int PROGRESS = 1;
	private static final int SUCCESS = 2;
	
	// handler
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case PROGRESS:
					long offset = msg.getData().getLong("offset");
					long max = msg.getData().getLong("max");
					Log.i(TAG, offset + "-" + max);
					long precent2 = offset * 100 / max;
					barProgressDialog.setProgress((int) precent2);
					break;
					
				case SUCCESS:
					barProgressDialog.setTitle("上传成功");
					barProgressDialog.setProgress(100);
					barProgressDialog.setCancelable(true);
					barProgressDialog.setCanceledOnTouchOutside(true);
					Toast.makeText(getApplicationContext(), "上传成功", Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};

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
		barProgressDialog = new ProgressDialog(this);
		barProgressDialog.setTitle("正在上传 ...");
		barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		barProgressDialog.setProgress(0);
		barProgressDialog.setMax(100);
		barProgressDialog.setCancelable(false);
		barProgressDialog.setCanceledOnTouchOutside(false);

		btn_downloadlist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent downloadlist = new Intent(NewTestActivity.this, DownloadListActivity.class);
				startActivity(downloadlist);
			}
		});

		btn_playUrl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent playUrl = new Intent(NewTestActivity.this, IjkVideoActicity.class);
				playUrl.putExtra("vid", videoId);
				startActivityForResult(playUrl, 1);
			}
		});

		btn_playUrlFull.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent playUrlFull = new Intent(NewTestActivity.this, IjkFullVideoActivity.class);
				playUrlFull.putExtra("vid", videoId);
				startActivityForResult(playUrlFull, 1);
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
				barProgressDialog.setTitle("正在上传 ...");
				barProgressDialog.setCancelable(false);
				barProgressDialog.setCanceledOnTouchOutside(false);
				barProgressDialog.show();
				barProgressDialog.setProgress(0);
				VideoUploadTask uploadTask = new VideoUploadTask();
				uploadTask.execute();
			}
		});
		
		btn_videolist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent videolist = new Intent(NewTestActivity.this, VideoListActivity.class);
				startActivity(videolist);
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		barProgressDialog.dismiss();
	}

	class VideoUploadTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			File path = new File(Environment.getExternalStorageDirectory(), "myRecording.mp4");
			String videojson = PolyvUploadManager.getInstance().upload(path.getAbsolutePath(), "我的标题", "desc", 0,
					new Progress() {
				
						@Override
						public void run(long offset, long max) {
							Bundle bundle = new Bundle();
							bundle.putLong("offset", offset);
							bundle.putLong("max", max);
							
							Message msg = new Message();
							msg.what = PROGRESS;
							msg.setData(bundle);
							
							handler.sendMessage(msg);
						}
					}, new Success() {
						
						@Override
						public void run() {
							Message msg = new Message();
							msg.what = SUCCESS;
							handler.sendMessage(msg);
						}
					});
			
			return videojson;
		}

		@Override
		protected void onPostExecute(String result) {
			
		}
	}

}
