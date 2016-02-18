package com.easefun.polyvsdk.demo;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easefun.polyvsdk.QuestionVO;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.ijk.IjkVideoView;
import com.easefun.polyvsdk.ijk.OnPreparedListener;
import com.easefun.polyvsdk.ijk.IjkVideoView.ErrorReason;

public class IjkVideoActicity extends Activity {
	private static final String TAG = "IjkVideoActicity";
	private IjkVideoView videoView = null;
	private PolyvQuestionView questionView = null;
	private PolyvAuditionView auditionView = null;
	private MediaController mediaController = null;
	private ProgressBar progressBar = null;
	int w = 0, h = 0, adjusted_h = 0;
	private RelativeLayout rl = null;
	private int stopPosition = 0;
	private String path;
	private String vid;
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.video_small2);
		// ------------------------------------------------------
    	Bundle e = getIntent().getExtras();
    	if (e != null) {
			path = e.getString("path");
			vid = e.getString("vid");
		}
		
    	Point point = new Point();
    	WindowManager wm = this.getWindowManager();
		wm.getDefaultDisplay().getSize(point);
		w = point.x;
		h = point.y;
		//小窗口的比例
		float ratio = (float) 4 / 3;
		adjusted_h = (int) Math.ceil((float) w / ratio);
		rl = (RelativeLayout) findViewById(R.id.rl);
		rl.setLayoutParams(new RelativeLayout.LayoutParams(w, adjusted_h));
		videoView = (IjkVideoView) findViewById(R.id.videoview);
		progressBar = (ProgressBar) findViewById(R.id.loadingprogress);
		//在缓冲时出现的loading
		videoView.setMediaBufferingIndicator(progressBar);
		videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_SCALE);
		videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(IMediaPlayer mp) {
				videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_SCALE);
				if (stopPosition > 0) {
					Log.d(TAG, "seek to stopPosition:" + stopPosition);
					videoView.seekTo(stopPosition);
				}
			}
		});
		
		videoView.setOnVideoStatusListener(new IjkVideoView.OnVideoStatusListener() {
			
			@Override
			public void onStatus(int status) {
				
			}
		});
		
		videoView.setOnVideoPlayErrorLisener(new IjkVideoView.OnVideoPlayErrorLisener() {
			
			@Override
			public boolean onVideoPlayError(ErrorReason errorReason) {
				return false;
			}
		});
		
		videoView.setOnQuestionOutListener(new IjkVideoView.OnQuestionOutListener() {
			
			@Override
			public void onOut(final QuestionVO questionVO) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						switch (questionVO.getType()) {
							case QuestionVO.TYPE_QUESTION:
								if (questionView == null) {
									questionView = new PolyvQuestionView(IjkVideoActicity.this);
									questionView.setIjkVideoView(videoView);
								}
								
								questionView.show(rl, questionVO);
								break;
								
							case QuestionVO.TYPE_AUDITION:
								if (auditionView == null) {
									auditionView = new PolyvAuditionView(IjkVideoActicity.this);
									auditionView.setIjkVideoView(videoView);
								}
								
								auditionView.show(rl, questionVO);
								break;
						}
					}
				});
			}
		});
		
		videoView.setOnQuestionAnswerTipsListener(new IjkVideoView.OnQuestionAnswerTipsListener() {
			
			@Override
			public void onTips(String msg) {
				questionView.showAnswerTips(msg);
			}
		});
		
		mediaController = new MediaController(this,false);
		mediaController.setIjkVideoView(videoView);
		mediaController.setAnchorView(videoView);
		videoView.setMediaController(mediaController);
		if(path != null && path.length() > 0){
			progressBar.setVisibility(View.GONE);
			videoView.setVideoPath(path);
		} else {
			videoView.setVid(vid);
		}
		
		//设置切屏事件
		mediaController.setOnBoardChangeListener(new MediaController.OnBoardChangeListener() {
			
			@Override
			public void onPortrait() {
				changeToLandscape();
			}
			
			@Override
			public void onLandscape() {
				changeToPortrait();
			}
		});
		
		// 设置视频尺寸 ，在横屏下效果较明显
		mediaController.setOnVideoChangeListener(new MediaController.OnVideoChangeListener() {
		
			@Override
			public void onVideoChange(final int layout) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						videoView.setVideoLayout(layout);
						switch (layout) {
							case IjkVideoView.VIDEO_LAYOUT_ORIGIN:
								Toast.makeText(IjkVideoActicity.this, "VIDEO_LAYOUT_ORIGIN", Toast.LENGTH_SHORT).show();
								break;
							case IjkVideoView.VIDEO_LAYOUT_SCALE:
								Toast.makeText(IjkVideoActicity.this, "VIDEO_LAYOUT_SCALE", Toast.LENGTH_SHORT).show();
								break;
							case IjkVideoView.VIDEO_LAYOUT_STRETCH:
								Toast.makeText(IjkVideoActicity.this, "VIDEO_LAYOUT_STRETCH", Toast.LENGTH_SHORT).show();
								break;
							case IjkVideoView.VIDEO_LAYOUT_ZOOM:
								Toast.makeText(IjkVideoActicity.this, "VIDEO_LAYOUT_ZOOM", Toast.LENGTH_SHORT).show();
								break;
						}
					}
				});
			}
		});
    }

	/**
	 * 切换到横屏
	 */
	public void changeToLandscape(){
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(h,w);
		rl.setLayoutParams(p);
		stopPosition = videoView.getCurrentPosition();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}
	
	/**
	 * 切换到竖屏
	 */
	public void changeToPortrait(){
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w,adjusted_h);
		rl.setLayoutParams(p);
		stopPosition = videoView.getCurrentPosition();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	// 配置文件设置congfigchange 切屏调用一次该方法，hide()之后再次show才会出现在正确位置
	@Override
	public void onConfigurationChanged(Configuration arg0) {
		super.onConfigurationChanged(arg0);
		videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_SCALE);
		mediaController.hide();
		
		if (questionView != null && questionView.isShowing()) {
			questionView.hide();
			questionView.refresh();
		} else if (auditionView != null && auditionView.isShowing()) {
			auditionView.hide();
			auditionView.refresh();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean value = mediaController.dispatchKeyEvent(event);
		if (value)
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (videoView != null) {
			videoView.stopPlayback();
			videoView.release(true);
		}
		
		if (questionView != null) {
			questionView.hide();
		}
		
		if (auditionView != null) {
			auditionView.hide();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (videoView != null) {
			videoView.stopPlayback();
			videoView.release(true);
		}
		
		if (questionView != null) {
			questionView.hide();
		}
		
		if (auditionView != null) {
			auditionView.hide();
		}
	};
}
