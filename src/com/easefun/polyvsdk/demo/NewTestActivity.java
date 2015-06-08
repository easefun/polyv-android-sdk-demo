package com.easefun.polyvsdk.demo;

import java.io.File;

import org.json.JSONException;
import com.easefun.polyvsdk.DownloadProgressListener;
import com.easefun.polyvsdk.PolyvDownloadProgressListener;
import com.easefun.polyvsdk.PolyvDownloader;
import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.SDKUtil;
import com.easefun.polyvsdk.demo.RecordActivity;
import com.easefun.polyvsdk.demo.IjkVideoActicity;
import com.easefun.polyvsdk.net.Progress;
import com.easefun.polyvsdk.Video;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
	// 不再需要以下参数..
	// private String downloadId="testdownload";
	// private String downloadSercetkey="f24c67d9bc0940b69ad8c0ebd6341730";
	private PolyvDownloader downloader;
	// sl8da4jjbx684cdae6bf17b1b70a8354_s 非加密
	// sl8da4jjbx80cb8878980c1626c51923_s 加密
	private static String videoId = "sl8da4jjbx0ec6ae62ea9c8f5d5fb0a0_s";
	private static String TAG="NewTestActivity";
	private ProgressDialog barProgressDialog;
	private Button btn_down, btn_downloadlist, btn_del,btn_playUrl, btn_playUrlFull,
			btn_playLocal, btn_playLocalFull, btn_record, btn_upload,
			btn_videolist;
	File saveDir;
	private static final int DOWNLOAD = 1;
	private static final int UPLOAD = 2;
	// handler
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case DOWNLOAD:
				long downloaded = msg.getData().getLong("current");
				long total = msg.getData().getLong("total");
				long precent = downloaded * 100 / total;
				barProgressDialog.setProgress((int) precent);
				if (barProgressDialog.getProgress() == barProgressDialog
						.getMax()) {
					if (downloader != null)
						downloader.stop();
					Toast.makeText(getApplicationContext(), "下载成功", 1).show();
				}
				break;
			case UPLOAD:
				long offset = msg.getData().getLong("offset");
				long max = msg.getData().getLong("max");
				Log.i("upload", offset + "-" + max);
				long precent2 = offset * 100 / max;
				barProgressDialog.setProgress((int) precent2);
				if (barProgressDialog.getProgress() == barProgressDialog
						.getMax()) {
					Toast.makeText(getApplicationContext(), "上传成功", 1).show();
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		btn_down = (Button) findViewById(R.id.download);
		btn_downloadlist = (Button) findViewById(R.id.download_list);
		btn_del=(Button)findViewById(R.id.del);
		btn_playUrl = (Button) findViewById(R.id.onlinevideo_1);
		btn_playUrlFull = (Button) findViewById(R.id.onlinevideo_2);
		btn_playLocal = (Button) findViewById(R.id.localvideo_1);
		btn_playLocalFull = (Button) findViewById(R.id.localvideo_2);
		btn_record = (Button) findViewById(R.id.recordvideo);
		btn_upload = (Button) findViewById(R.id.upload);
		btn_videolist = (Button) findViewById(R.id.videolist);
		barProgressDialog = new ProgressDialog(this);
		barProgressDialog.setTitle("正在下载 ...");
		barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
		barProgressDialog.setProgress(0);
		barProgressDialog.setMax(100);
		barProgressDialog.setCancelable(true);
		barProgressDialog.setCanceledOnTouchOutside(false);

		barProgressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {
						// TODO Auto-generated method stub
						downloader.stop();
					}
				});
		// 加密视频下载m3u8到本地目录，ts目录为polyvclient.getDownloadDir/userid/
		// 非加密视频 下载MP4，目录为polyvclient.getDownloadDir
		// 视频 vid ，码率
		downloader = new PolyvDownloader(videoId, 1);
		downloader.setPolyvDownloadProressListener(new PolyvDownloadProgressListener() {
					@Override
					public void onDownloadSuccess() {
						// TODO Auto-generated method stub
						Message msg = new Message();
						msg.what = DOWNLOAD;
						Bundle bundle = new Bundle();
						bundle.putLong("current", 1);
						bundle.putLong("total", 1);
						msg.setData(bundle);
						handler.sendMessage(msg);
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

					@Override
					public void onDownloadFail(String error) {
						// TODO Auto-generated method stub
						Log.i(TAG, "下载失败"+error);
					}
				});
		btn_down.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				barProgressDialog.show();
				downloader.start();
				// 跳转测试类
				// Intent intent = new Intent(NewTestActivity.this, TestDownloadActivity.class);
				// startActivity(intent);
			}
		});
		
		btn_del.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(downloader!=null){
					downloader.deleteVideo(videoId, 1);
//					downloader.cleanDownloadDir();
				}
			}
		});

		btn_downloadlist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent downloadlist = new Intent(NewTestActivity.this,DownloadListActivity.class);
				startActivity(downloadlist);
			}
		});

		btn_playUrl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent playUrl = new Intent(NewTestActivity.this,IjkVideoActicity.class);
				playUrl.putExtra("vid", videoId);
				startActivityForResult(playUrl, 1);
			}
		});

		btn_playUrlFull.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent playUrlFull = new Intent(NewTestActivity.this,
						IjkFullVideoActivity.class);
				playUrlFull.putExtra("vid", videoId);
				startActivityForResult(playUrlFull, 1);
			}
		});
		btn_playLocal.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				File mp4File = new File(PolyvSDKClient.getInstance().getDownloadDir(),
						"3.mp4");
				if (mp4File.exists()) {
					Intent playLocal = new Intent(NewTestActivity.this,IjkVideoActicity.class);
					playLocal.putExtra("path",mp4File.getPath());
					playLocal.putExtra("vid", videoId);
					startActivityForResult(playLocal, 1);
				}else{
					Toast.makeText(NewTestActivity.this, "视频文件不存在,请先行下载",Toast.LENGTH_LONG );
				}
			}
		});
		btn_playLocalFull.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				File mp4File = new File(PolyvSDKClient.getInstance().getDownloadDir(),
						"3.mp4");
				if (mp4File.exists()) {
					Intent playLocal = new Intent(NewTestActivity.this,IjkFullVideoActivity.class);
					playLocal.putExtra("path",mp4File.getPath());
					playLocal.putExtra("vid", videoId);
					startActivityForResult(playLocal, 1);
				}else{
					Toast.makeText(NewTestActivity.this, "视频文件不存在,请先行下载",Toast.LENGTH_LONG );
				}
			}
		});
		btn_record.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent(NewTestActivity.this,
						RecordActivity.class);
				NewTestActivity.this.startActivity(myIntent);
			}
		});
		btn_upload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				barProgressDialog.setTitle("正在上传 ...");
				barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
				barProgressDialog.setProgress(0);
				barProgressDialog.setMax(100);
				barProgressDialog.setCancelable(true);
				barProgressDialog.setCanceledOnTouchOutside(false);
				barProgressDialog.show();
				VideoUploadTask uploadTask = new VideoUploadTask();
				uploadTask.execute();

			}
		});
		btn_videolist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent videolist = new Intent(NewTestActivity.this,
						VideoListActivity.class);
				startActivity(videolist);
			}
		});
	}

	class VideoUploadTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			File path = new File(Environment.getExternalStorageDirectory(),"myRecording.mp4");
			try {
				// Video video =PolyvSDKClient.getInstance().upload(path.toString(), "我的标题", "tag","desc", 0);
				String videojson = PolyvSDKClient.getInstance()
						.resumableUpload(path.toString(), "我的标题", "desc", 0,
								new Progress() {

									@Override
									public void run(long offset, long max) {
										// TODO Auto-generated method stub
										Message msg = new Message();
										msg.what = UPLOAD;
										Bundle bundle = new Bundle();
										bundle.putLong("offset", offset);
										bundle.putLong("max", max);
										msg.setData(bundle);
										handler.sendMessage(msg);
									}
								});
				return videojson;
			} catch (Exception e) {

			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				Video video = SDKUtil.convertJsonToVideo(result);
				Log.d("VideoUploadTask","video uploaded vid: " + video.getVid());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
