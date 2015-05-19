package com.easefun.polyvsdk.demo;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.SDKUtil;
import com.easefun.polyvsdk.view.MyVideoView;
import com.easefun.polyvsdk.view.PolyvOnPreparedListener;
import com.easefun.polyvsdk.view.PolyvVideoView;
import com.easefun.polyvsdk.view.PopupMediaController;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class FullVideoDemoActivity extends Activity implements OnTouchListener {
	private RelativeLayout rl;
	private RelativeLayout botlayout;
	private PolyvVideoView videoview;
	private PopupMediaController mediaController;
	private WindowManager wm;
	private int w;
	private int h;
	private int stopPosition;
	private String vid = "";
	private String path = "";
	private static final int APP_EXIT = -1;
	private static final int VIDEO_PREPARE = 0;
	private static final int VIDEO_PLAY = 1;
	private static final int VIDEO_PREPARED=2;
	private boolean isExit = false;
	private boolean isLocal = false;
	private boolean goon = false;
	private boolean isFullscreen = false;
	float ratio;
	private int videoWidth,videoHeight;
	private int ah;
	private boolean p=false;
	private MyPreparededListener preparedListener;
	private String[] vids = { "sl8da4jjbxa77bfb0b7c1aead9758315_s","sl8da4jjbx0bbe98bc3edfd2307fdbde_s","sl8da4jjbx54b5291b44960754b59c5b_s" };
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case VIDEO_PREPARE:
				videoview = (PolyvVideoView) findViewById(R.id.videoview);
				mediaController = new PopupMediaController(FullVideoDemoActivity.this,videoview);
				mediaController.setFullscreenListener(new FullscreenListener());
			
				videoview.setMediaController(mediaController);
				if(!isLocal){
				   
				   videoview.setVideoId("sl8da4jjbxed639c727dbe1b71e73d98_s");		    
				   videoview.setPlayMode(PolyvVideoView.MODE_AUTO);//自动播放
				}else{
					videoview.setLocalVideo(vid);
				}
				videoview.setViewSize(w, h);
				preparedListener= new MyPreparededListener(path,vid);
				videoview.setOnPreparedListener(preparedListener);
			    
				break;
			case APP_EXIT:
				isExit = false;
				break;
			case VIDEO_PREPARED:
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.video_small);
		Bundle e = getIntent().getExtras();
		if (e != null) {
			path = e.getString("path");
			vid = e.getString("vid");
		}
		if (path != null && path.length() > 0) {
			isLocal = true;
		}
		wm = this.getWindowManager();
		w = wm.getDefaultDisplay().getWidth();
		h = wm.getDefaultDisplay().getHeight();
		
		botlayout = (RelativeLayout) findViewById(R.id.botlayout);
		rl = (RelativeLayout) findViewById(R.id.rl);
		rl.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
		handler.sendEmptyMessageDelayed(VIDEO_PREPARE, 100);
	}

	class MyPreparededListener extends PolyvOnPreparedListener {
        
		public MyPreparededListener(String path, String videoId) {
			super(path,videoId);
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			super.onPrepared(mp);
			videoWidth=mp.getVideoWidth();	
			videoHeight=mp.getVideoHeight();
			videoview.setVideoSize(videoWidth, videoHeight);
			mediaController.show(3000);
            if(isLocal){
			   videoview.start();
            }
		}
	}

	
	class FullscreenListener implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			   finish();
		}

	}
  
	/**
	 *  切换到横屏
	 */
	public void changeToLandscape(){
		videoview.setViewSize(h, w);
		videoview.setVideoSize(videoWidth, videoHeight);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(h,w);
		rl.setLayoutParams(p);
		stopPosition = videoview.getCurrentPosition();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		botlayout.setVisibility(View.GONE);
		isFullscreen = !isFullscreen;
	}
	
	/**
	 * 切换到竖屏
	 */
	public void changeToPortrait(){
		videoview.setViewSize(w, ah);
		videoview.setVideoSize(videoWidth, videoHeight);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w,ah);
		rl.setLayoutParams(p);
		stopPosition = videoview.getCurrentPosition();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		botlayout.setVisibility(View.VISIBLE);
		isFullscreen = !isFullscreen;
	}
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		if (!mediaController.isShowing()) {
			mediaController.show(0);
		} else {
			mediaController.hide();
		}
		return true;
	}

	public void exit() {
		if (isExit) {
			// 退到主屏幕
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			System.exit(0);
		} else {
			isExit = true;
			Toast.makeText(FullVideoDemoActivity.this, "再按一次退出APP", Toast.LENGTH_SHORT).show();
			handler.sendEmptyMessageDelayed(APP_EXIT, 3000);// 3秒后发送消息
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		stopPosition = videoview.getCurrentPosition();
		videoview.pause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if(videoview!=null){
		videoview.seekTo(stopPosition);
		videoview.start();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		onBackPressed();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		//切屏dismiss
		mediaController.dismiss();
	}
}
