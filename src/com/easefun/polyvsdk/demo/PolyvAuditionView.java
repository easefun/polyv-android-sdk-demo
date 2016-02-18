package com.easefun.polyvsdk.demo;

import java.io.IOException;
import java.util.ArrayList;

import com.easefun.polyvsdk.QuestionVO;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.SDKUtil;
import com.easefun.polyvsdk.ijk.IjkVideoView;

import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PolyvAuditionView extends RelativeLayout {

	private Context context = null;
	private IjkVideoView ijkVideoView = null;
	private TextView passBtn = null;
	private TextView title = null;
	private ImageButton playPauseBtn = null;
	private ProgressBar progressBar = null;
	private TextView progressTotalText = null;
	private MediaPlayer mediaPlayer = null;
	private PopupWindow popupWindow = null;
	private View anchorView = null;
	private QuestionVO questionVO = null;
	
	private static final int UPDATE_PROGRESS = 1;
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			long currentTime = mediaPlayer.getCurrentPosition();
			if (questionVO.getWrongTime() * 1000 <= currentTime) {
				ijkVideoView.answerQuestion(new ArrayList<Integer>(0));
				hide();
				return;
			}
			
			switch (msg.what) {
				case UPDATE_PROGRESS:
					setProgress();
					handler.removeMessages(UPDATE_PROGRESS);
					handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
					break;
			}
		}
	};
	
	public PolyvAuditionView(Context context) {
		super(context);
		this.context = context;
		init();
	}
	
	public void setIjkVideoView(IjkVideoView ijkVideoView) {
    	this.ijkVideoView = ijkVideoView;
    }
	
	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.audition, this);
		passBtn = (TextView) findViewById(R.id.audition_pass_btn);
		passBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ijkVideoView.skipQuestion();
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						hide();
					}
				});
			}
		});
		title = (TextView) findViewById(R.id.title);
		playPauseBtn = (ImageButton) findViewById(R.id.audition_play_pause);
		playPauseBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mediaPlayer.isPlaying()) {
					playPauseBtn.setImageResource(R.drawable.media_play);
					mediaPlayer.pause();
				} else {
					playPauseBtn.setImageResource(R.drawable.media_pause);
					mediaPlayer.start();
				}
			}
		});
		progressBar = (ProgressBar) findViewById(R.id.audition_progress);
		progressBar.setMax(1000);
		
		progressTotalText = (TextView) findViewById(R.id.audition_progress_total_text);
		progressTotalText.setText(String.format("%s/%s", SDKUtil.getVideoDisplayTime(0), SDKUtil.getVideoDisplayTime(0)));
		
		if (popupWindow == null) {
    		popupWindow = new PopupWindow(context);
    		popupWindow.setContentView(this);
    	}
	}
	
	private void setProgress() {
		long currentTime = mediaPlayer.getCurrentPosition();
		long durationTime = mediaPlayer.getDuration();
		float percentage = ((float) currentTime) / durationTime;
		int progress = (int) (percentage * progressBar.getMax());
		progressBar.setProgress(progress);
		progressTotalText.setText(String.format("%s/%s", SDKUtil.getVideoDisplayTime(currentTime), SDKUtil.getVideoDisplayTime(durationTime)));
	}
	
	/**
	 * 显示
	 * @param anchorView
	 * @param questionVO
	 */
	public void show(View anchorView, QuestionVO questionVO) {
		this.anchorView = anchorView;
		this.questionVO = questionVO;
		refresh();
	}
	
	/**
	 * 重新设置控件
	 */
	public void refresh() {
		int[] location = new int[2];
		anchorView.getLocationInWindow(location);
		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchorView.getWidth(), location[1] + anchorView.getHeight());
		popupWindow.setWidth(anchorView.getWidth());
		popupWindow.setHeight(anchorView.getHeight());
		popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, 0, anchorRect.top);
		
		if (questionVO.isSkip()) {
			passBtn.setVisibility(View.VISIBLE);
		}
		
		title.setText(questionVO.getQuestion());
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(context, Uri.parse(questionVO.getMp3url()));
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mediaPlayer.start();
		handler.removeMessages(UPDATE_PROGRESS);
		handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
	}
	
	/**
	 * 是否在显示中
	 * @return
	 */
	public boolean isShowing() {
		return this.getVisibility() == View.VISIBLE;
	}
	
	/**
	 * 隐藏
	 */
	public void hide() {
		mediaPlayer.release();
		handler.removeMessages(UPDATE_PROGRESS);
		popupWindow.dismiss();
	}
}
