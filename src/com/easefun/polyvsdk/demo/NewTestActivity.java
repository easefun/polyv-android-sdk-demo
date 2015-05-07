package com.easefun.polyvsdk.demo;

import java.io.File;

import org.json.JSONException;

import com.easefun.polyvsdk.DownloadProgressListener;
import com.easefun.polyvsdk.Downloader;
import com.easefun.polyvsdk.DownloadHelper;
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
    private String downloadId="testdownload";
    private String downloadSercetkey="f24c67d9bc0940b69ad8c0ebd6341730";

	private static String videoId="sl8da4jjbx0bbe98bc3edfd2307fdbde_s";    //没加密
	private ProgressDialog barProgressDialog;
	private Button btn_down,btn_downloadlist,btn_stop,btn_playUrl,btn_playUrlFull,btn_playLocal,btn_playLocalFull,btn_record,btn_upload,btn_videolist;
	private DownloadHelper downloadHelper;
	private Downloader downloader;
	File saveDir;
	private Handler handler;
	private static final int DOWNLOAD=1;
	private static final int UPLOAD=2;
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);	
    	btn_down = (Button)findViewById(R.id.download);
    	btn_downloadlist=(Button)findViewById(R.id.download_list);
    	btn_stop = (Button)findViewById(R.id.stop);
    	btn_playUrl=(Button)findViewById(R.id.onlinevideo_1);
    	btn_playUrlFull=(Button)findViewById(R.id.onlinevideo_2);
    	btn_playLocal=(Button)findViewById(R.id.localvideo_1);
    	btn_playLocalFull=(Button)findViewById(R.id.localvideo_2);
    	btn_record=(Button)findViewById(R.id.recordvideo);
    	btn_upload=(Button)findViewById(R.id.upload);
    	btn_videolist=(Button)findViewById(R.id.videolist);
    	barProgressDialog = new ProgressDialog(this);
		barProgressDialog.setTitle("正在下载 ...");
		barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
		barProgressDialog.setProgress(0);
		barProgressDialog.setMax(100);
		barProgressDialog.setCancelable(true);
		barProgressDialog.setCanceledOnTouchOutside(false);
		
		barProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
				if(downloader!=null) downloader.stop();
			}
		});
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case DOWNLOAD:
					long downloaded = msg.getData().getLong("downloaded");
					long total = msg.getData().getLong("total");
				
					long precent =downloaded*100/total;
					barProgressDialog.setProgress((int)precent);
					if(barProgressDialog.getProgress() == barProgressDialog.getMax()){
						if(downloader!=null) downloader.stop();
						Toast.makeText(getApplicationContext(), "下载成功", 1).show();
					}
					break;
				case UPLOAD:
					long offset = msg.getData().getLong("offset");
					long max = msg.getData().getLong("max");
				    Log.i("upload", offset+"-"+max);
					long precent2 =offset*100/max;
					barProgressDialog.setProgress((int)precent2);
					if(barProgressDialog.getProgress() == barProgressDialog.getMax()){
						Toast.makeText(getApplicationContext(), "上传成功", 1).show();
					}
					break;
				default:
					break;
				}
			}
		};
		 if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) )   {                               
	         saveDir = new File(Environment.getExternalStorageDirectory().getPath()+"/polyvdownload");
	         if(!saveDir.exists()) saveDir.mkdir();
	      }  
    	PolyvSDKClient client = PolyvSDKClient.getInstance();
		client.setReadtoken("nsJ7ZgQMN0-QsVkscukWt-qLfodxoDFm");
		client.setWritetoken("Y07Q4yopIVXN83n-MPoIlirBKmrMPJu0");
		client.setPrivatekey("DFZhoOnkQf");
		client.setUserId("sl8da4jjbx");
		client.setDownloadId(downloadId);
		client.setDownloadSecretKey(downloadSercetkey);
		client.setSign(true);
		client.setDownloadDir(saveDir);

		 
		downloadHelper = new DownloadHelper(this, videoId, 1);
		
		btn_down.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				downloader = downloadHelper.initDownloader("mp4");
				downloader.start();
				barProgressDialog.show();
				new DownloadTask().execute();
			}
		});
		
		btn_downloadlist.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent downloadlist = new Intent(NewTestActivity.this, DownloadListActivity.class);
				startActivity(downloadlist);
			}
		});
		btn_stop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(downloader!=null) downloader.stop();
			}
		});
		
		btn_playUrl .setOnClickListener(new View.OnClickListener() {
			
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
				Intent playUrlFull = new Intent(NewTestActivity.this,IjkFullVideoActivity.class);
				playUrlFull.putExtra("vid", videoId);
				startActivityForResult(playUrlFull, 1);
			}
		});
		btn_playLocal.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent playLocal = new Intent(NewTestActivity.this, IjkVideoActicity.class);
	    		playLocal.putExtra("path", SDKUtil.getDownloadFileByVid(videoId).getPath());
	    		playLocal.putExtra("vid", videoId);
	    		startActivityForResult(playLocal,1);
			}
		});
		btn_playLocalFull.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent playLocal = new Intent(NewTestActivity.this, IjkFullVideoActivity.class);
	    		
	    		playLocal.putExtra("path", SDKUtil.getDownloadFileByVid(videoId).getPath());
	    		playLocal.putExtra("vid", videoId);
	    		startActivityForResult(playLocal,1);
			}
		});
		btn_record.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent(NewTestActivity.this, RecordActivity.class);
				NewTestActivity.this.startActivity(myIntent);
			}
		});
		btn_upload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
	//			 TODO Auto-generated method stub
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
				Intent videolist = new Intent(NewTestActivity.this, VideoListActivity.class);
				startActivity(videolist);
			}
		});
    }
	
	class VideoUploadTask extends AsyncTask<String, Void, String> {

	    @Override protected String doInBackground(String... params) {
	        File path = new File(Environment.getExternalStorageDirectory(), "myRecording.mp4");
	        
	        try{
//	        	Video video = PolyvSDKClient.getInstance().upload(path.toString(), "我的标题", "tag","desc", 0);
	        	String videojson = PolyvSDKClient.getInstance().resumableUpload(path.toString(), "我的标题","desc", 0,new Progress() {
					
					@Override
					public void run(long offset, long max) {
						// TODO Auto-generated method stub
						Message msg = new Message();
						msg.what=UPLOAD;
						Bundle bundle  = new Bundle();
						bundle.putLong("offset", offset);
						bundle.putLong("max", max);
						msg.setData(bundle);
						handler.sendMessage(msg);
					}
				});
	        	return videojson;
	        }catch(Exception e){
	        	
	        }
	        return null;
	    }
	    @Override protected void onPostExecute(String result) {
//	    	 Log.d("VideoUploadTask", "video uploaded json " + result);
	    	try {
				Video video = SDKUtil.convertJsonToVideo(result);
			      Log.d("VideoUploadTask", "video uploaded vid: " + video.getVid());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	 }
	
	class DownloadTask extends AsyncTask<String,String,String>{
        
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			
			downloader.begin(downloadHelper.getVideoId(),new DownloadProgressListener() {
				
				@Override
				public void onDownloadSize(long downloaded, long total) {
					// TODO Auto-generated method stub
					Message msg = new Message();
					msg.what=DOWNLOAD;
					Bundle bundle  = new Bundle();
					bundle.putLong("downloaded", downloaded);
					bundle.putLong("total", total);
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			});
			return null;
		}
		
	}
}
