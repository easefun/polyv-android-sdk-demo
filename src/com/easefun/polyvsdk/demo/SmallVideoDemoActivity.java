//package com.easefun.polyvsdk.demo;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//
//import android.app.Activity;
//import android.content.Intent;
//import android.content.pm.ActivityInfo;
//import android.content.res.Configuration;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnBufferingUpdateListener;
//import android.media.MediaPlayer.OnInfoListener;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnTouchListener;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.RelativeLayout;
//import android.widget.Toast;
//
//import com.easefun.polyvsdk.R;
//import com.easefun.polyvsdk.SDKUtil;
//import com.easefun.polyvsdk.view.PolyvOnPreparedListener;
//import com.easefun.polyvsdk.view.PolyvVideoView;
//import com.easefun.polyvsdk.view.PopupMediaController;
//
//public class SmallVideoDemoActivity extends Activity implements OnTouchListener {
//	private RelativeLayout rl;
//	private RelativeLayout botlayout;
//	private PolyvVideoView videoview;
//	private PopupMediaController mediaController;
//	private Button play, stop, swtich,download;
//	private WindowManager wm;
//	private int w;
//	private int h;
//	private long stopPosition;
//	private String vid = "";
//	private String path = "";
//	private static final int APP_EXIT = -1;
//	private static final int VIDEO_PREPARE = 0;
//	private static final int VIDEO_PLAY = 1;
//	private static final int VIDEO_PREPARED=2;
//	private boolean isExit = false;
//	private boolean isLocal = false;
//	private boolean goon = false;
//	private boolean isFullscreen = false;
//	float ratio;
//	private int videoWidth,videoHeight;
//	private int ah;
//	private boolean p=false;
//	private DBservice service;
//	private MyPreparededListener preparedListener;
////	private String[] vids = { "sl8da4jjbx54b5291b44960754b59c5b_s","sl8da4jjbxa77bfb0b7c1aead9758315_s","sl8da4jjbx0bbe98bc3edfd2307fdbde_s","sl8da4jjbx54b5291b44960754b59c5b_s" };
//	private String[] vids = {"sl8da4jjbx0b29a76e0e02a96ca965b7_s","sl8da4jjbx42794ee20033179e1ab0a8_s" };
//    
//	private Handler handler = new Handler() {
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case VIDEO_PREPARE:
//				videoview=(PolyvVideoView) findViewById(R.id.videoview);
//				mediaController = new PopupMediaController(SmallVideoDemoActivity.this,videoview);
//				mediaController.setFullscreenListener(new FullscreenListener());
//				videoview.setMediaController(mediaController);
//				if(!isLocal){
////				   videoview.setVideoIds(vids);  //设置vids的数组实现多个视频的播放
//					videoview.setVideoId("sl8da4jjbx0b29a76e0e02a96ca965b7_s");
//					videoview.setPlayMode(PolyvVideoView.MODE_MANUAL);//手动播放
//				}else{
//					videoview.setLocalVideo(vid);
//				}
//				videoview.setViewSize(w, ah);
//				
//				preparedListener= new MyPreparededListener(path,vid);
//				videoview.setOnPreparedListener(preparedListener);
//				videoview.setOnInfoListener(new MyOnInfoListener());
//				videoview.setOnBufferingUpdateListener(new MyOnBufferingUpdateListener());
//				videoview.setEnabled(false);
//			
//				break;
//			case APP_EXIT:
//				isExit = false;
//				break;
//			case VIDEO_PREPARED:
//			   
//				break;
//			default:
//				break;
//			}
//		};
//	};
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		setContentView(R.layout.video_small);
//		Bundle e = getIntent().getExtras();
//		if (e != null) {
//			path = e.getString("path");
//			vid = e.getString("vid");
//		}
//		if (path != null && path.length() > 0) {
//			isLocal = true;
//		}
//		service=new DBservice(this);
//		wm = this.getWindowManager();
//		w = wm.getDefaultDisplay().getWidth();
//		h = wm.getDefaultDisplay().getHeight();
//		//小窗口的比例
//		ratio=(float)4/3;
//		 ah= (int)Math.ceil((float)w/ratio);
//		botlayout = (RelativeLayout) findViewById(R.id.botlayout);
//		play = (Button) findViewById(R.id.play);
//		play.setOnClickListener(new PlayListener());
//		stop = (Button) findViewById(R.id.stop);
//		stop.setOnClickListener(new StopListener());
//		swtich = (Button) findViewById(R.id.swtich);
//		download=(Button)findViewById(R.id.download);
//		download.setOnClickListener(new DownloadListener());
//		rl = (RelativeLayout) findViewById(R.id.rl);
//		rl.setLayoutParams(new RelativeLayout.LayoutParams(w, ah));
//		handler.sendEmptyMessageDelayed(VIDEO_PREPARE, 100);
//	}
//
//	class MyPreparededListener extends PolyvOnPreparedListener {
//        
//		public MyPreparededListener(String path, String videoId) {
//			super(path,videoId);
//		}
//
//		@Override
//		public void onPrepared(MediaPlayer mp) {
//			// TODO Auto-generated method stub
//			super.onPrepared(mp);
//			videoview.setVisibility(View.VISIBLE);
//			Toast.makeText(SmallVideoDemoActivity.this, "onprepared", Toast.LENGTH_SHORT).show();
//			videoWidth=mp.getVideoWidth();	
//			videoHeight=mp.getVideoHeight();
//			videoview.setVideoSize(videoWidth, videoHeight);
//			mediaController.show(3000);
//			if(isLocal){
//				videoview.start(); 
//			}
//		}
//	}
//   
//	class MyOnInfoListener implements OnInfoListener {
//       //701-858-702-859
//		public MyOnInfoListener() {
//		// TODO Auto-generated constructor stub
//    	  
//	}
//		@Override
//		public boolean onInfo(MediaPlayer mp, int what, int extra) {
//			// TODO Auto-generated method stub
//			Toast.makeText(SmallVideoDemoActivity.this, " ON INFO "+what+","+extra, Toast.LENGTH_SHORT).show();
//			switch (what) {
//			case MediaPlayer.MEDIA_INFO_BUFFERING_START: //缓冲开始 
//				
//				if (videoview.isPlaying()) {
//					 videoview.pause();
//				      videoview.setLoadingBarVisibility(View.VISIBLE);
//				      }
//				break;
//			 case MediaPlayer.MEDIA_INFO_BUFFERING_END: // 缓冲结束
//				 
//				  videoview.start();
//				  videoview.setLoadingBarVisibility(View.GONE);
//			      break;
//			 case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING: 
//				//当设备无法播放视频时将出现该提示信息  
//		        //比如视频太复杂或者码率过高  
//				 break;
//			default:
//				break;
//			}
//			return true;
//		}
//		
//	}
//	
//	class MyOnBufferingUpdateListener implements OnBufferingUpdateListener{
//        public MyOnBufferingUpdateListener(){
//        	
//        }
//		@Override
//		public void onBufferingUpdate(MediaPlayer mp, int precent) {
//			// TODO Auto-generated method stub
////			 Toast.makeText(SmallVideoDemoActivity.this, "OnBufferingUpdateListener->" +precent, Toast.LENGTH_SHORT).show();
//			
//		}
//		
//	}
//	
//	class PlayListener implements View.OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			
//			Toast.makeText(SmallVideoDemoActivity.this, "播放视频", 1).show();
//			if (!isLocal) {
//				videoview.setEnabled(true);
//				videoview.start();
//				
//			} else {
//				videoview.setLocalVideo(vid);
//				videoview.start();
//			}
//		}
//	}
//	
//	class StopListener implements View.OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			videoview.pause();
//		}
//
//	}
//    
//	class SwitchListener implements View.OnClickListener{
//
//		@Override
//		public void onClick(View arg0) {
//			// TODO Auto-generated method stub
//		}
//		
//	}
//	
//	class DownloadListener implements View.OnClickListener{
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			//get video info
//			String currentvid= videoview.getCurrentVideoId();
//			new VideoInfo().execute(currentvid);
//		}
//		
//	}
//	
//	class FullscreenListener implements View.OnClickListener {
//		@Override
//		public void onClick(View arg0) {
//			// TODO Auto-generated method stub
//			if (!isFullscreen) {
//				//横屏
//				changeToLandscape();
//			} else {
//			   //竖屏
//				changeToPortrait();
//			}
//		}
//
//	}
//  
//	/**
//	 *  切换到横屏
//	 */
//	public void changeToLandscape(){
//		videoview.setViewSize(h, w);
//		videoview.setVideoSize(videoWidth, videoHeight);
//		Toast.makeText(getApplicationContext(), "view w:"+w+", h:"+h+" video size "+videoWidth+","+"height"+videoHeight, 1).show();
//		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(h,w);
//		rl.setLayoutParams(p);
//		stopPosition = videoview.getCurrentPosition();
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		botlayout.setVisibility(View.GONE);
//		isFullscreen = !isFullscreen;
//	}
//	
//	/**
//	 * 切换到竖屏
//	 */
//	public void changeToPortrait(){
//		videoview.setViewSize(w, ah);
//		videoview.setVideoSize(videoWidth, videoHeight);
//		Toast.makeText(getApplicationContext(), "view w:"+w+", h:"+ah+" video size "+videoWidth+","+"height"+videoHeight, 1).show();
//		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w,ah);
//		rl.setLayoutParams(p);
//		stopPosition = videoview.getCurrentPosition();
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		botlayout.setVisibility(View.VISIBLE);
//		isFullscreen = !isFullscreen;
//	}
//	@Override
//	public boolean onTouch(View arg0, MotionEvent arg1) {
//		// TODO Auto-generated method stub
//		if (!mediaController.isShowing()) {
//			mediaController.show(0);
//		} else {
//			mediaController.hide();
//		}
//		return true;
//	}
//
//	public void exit() {
//		if (isExit) {
//			// 退到主屏幕
//			Intent intent = new Intent(Intent.ACTION_MAIN);
//			intent.addCategory(Intent.CATEGORY_HOME);
//			startActivity(intent);
//			System.exit(0);
//		} else {
//			isExit = true;
//			Toast.makeText(SmallVideoDemoActivity.this, "再按一次退出APP", Toast.LENGTH_SHORT).show();
//			handler.sendEmptyMessageDelayed(APP_EXIT, 3000);// 3秒后发送消息
//		}
//	}
//    
//	class VideoInfo extends AsyncTask<String,String,String>{
//		@Override
//		protected String doInBackground(String... params) {
//			// TODO Auto-generated method stub
//			DownloadInfo downloadInfo=null;
//			JSONArray jsonArray = SDKUtil.loadVideoInfo(params[0]);
//			String vid = null;
//			String duration = null;
//			int filesize = 0;
//			try {
//				JSONObject jsonObject =jsonArray.getJSONObject(0);
//				vid = jsonObject.getString("vid");
//				duration = jsonObject.getString("duration");
//				filesize = jsonObject.getInt("filesize1");
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			 downloadInfo = new DownloadInfo(vid, duration, filesize);
//			 if(service!=null&&!service.isAdd(downloadInfo)){
//				 service.addDownloadFile(downloadInfo);
//			 }else{
//				 runOnUiThread(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						 Toast.makeText(SmallVideoDemoActivity.this, "this video has been added !!", 1).show();
//					}
//				});
//				
//			 }
//			return null;
//		}
//	}
//	
//	@Override
//	public void onPause() {
//		super.onPause();
//		stopPosition = videoview.getCurrentPosition();
//		videoview.pause();
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		if(videoview!=null){
//		    
//			 
//		     videoview.start();
//		     videoview.seekTo((int)stopPosition);
//		}
//	}
//
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		// TODO Auto-generated method stub
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
////			if(isFullscreen) { //先退出横屏
////				changeToPortrait();
////				return false;
////			}
////			exit();
////			return false;
////		} else {
////			return super.onKeyDown(keyCode, event);
//			if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){
//			    onBackPressed();
//			}
//		}
//		return super.onKeyDown(keyCode, event);
//	}
//
//	@Override
//	public void onBackPressed() {
//		Intent intent = new Intent();
//		setResult(RESULT_OK, intent);
//		finish();
//	}
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		// TODO Auto-generated method stub
//		super.onConfigurationChanged(newConfig);
//		//切屏dismiss
//		mediaController.dismiss();
//	}
//}
