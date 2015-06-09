package com.easefun.polyvsdk.demo;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.ijk.IjkMediaController;
import com.easefun.polyvsdk.ijk.IjkVideoView;
import com.easefun.polyvsdk.ijk.OnPreparedListener;
import com.easefun.polyvsdk.ijk.PolyvOnPreparedListener;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class IjkFullVideoActivity extends Activity {
	private IjkVideoView videoview;
	private MediaController mediaController;
	private ProgressBar progressBar;
	private String path;
	private String vid;

	@Override
	public void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.video_full2);
		Bundle e = getIntent().getExtras();
		if (e != null) {
			path = e.getString("path");
			vid = e.getString("vid");
		}
		
		progressBar = (ProgressBar) findViewById(R.id.loadingprogress);
		videoview = (IjkVideoView) findViewById(R.id.videoview);
		mediaController = new MediaController(this, false);
		videoview.setMediaController(mediaController);
		videoview.setMediaBufferingIndicator(progressBar);
		if (path != null && path.length() > 0) {
			progressBar.setVisibility(View.GONE);
			videoview.setVideoURI(Uri.parse(path));
			
		} else {
			videoview.setVid(vid, 1);
		}
		videoview.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(IMediaPlayer mp) {
				// TODO Auto-generated method stub

			}
		});
		
				// 设置视频尺寸 ，在横屏下效果较明显
			   mediaController.setOnVideoChangeListener(new MediaController.OnVideoChangeListener() {
				
				@Override
				public void onVideoChange(int layout) {
					// TODO Auto-generated method stub
					videoview.setVideoLayout(layout);
					switch (layout) {
					case IjkVideoView.VIDEO_LAYOUT_ORIGIN:
						Toast.makeText(IjkFullVideoActivity.this, "VIDEO_LAYOUT_ORIGIN", 1).show();
						break;
		            case IjkVideoView.VIDEO_LAYOUT_SCALE:
		            	Toast.makeText(IjkFullVideoActivity.this, "VIDEO_LAYOUT_SCALE", 1).show();
						break;
		            case IjkVideoView.VIDEO_LAYOUT_STRETCH:
		            	Toast.makeText(IjkFullVideoActivity.this, "VIDEO_LAYOUT_STRETCH", 1).show();
			            break;
		             case IjkVideoView.VIDEO_LAYOUT_ZOOM:
		            	 Toast.makeText(IjkFullVideoActivity.this, "VIDEO_LAYOUT_ZOOM", 1).show();
			            break;
			            }
				}
			});
	}
}
