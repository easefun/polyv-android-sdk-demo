package com.easefun.polyvsdk.demo;


import tv.danmaku.ijk.media.player.IMediaPlayer;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.ijk.IjkMediaController;
import com.easefun.polyvsdk.ijk.IjkVideoView;
import com.easefun.polyvsdk.ijk.OnPreparedListener;
import com.easefun.polyvsdk.ijk.PolyvOnPreparedListener;

import android.app.Activity;
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
	private boolean encrypt = false;

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
		if (path != null && path.length() > 0) {
			encrypt = true;
		}
		progressBar = (ProgressBar) findViewById(R.id.loadingprogress);
		videoview = (IjkVideoView) findViewById(R.id.videoview);
		mediaController = new MediaController(this, false);
		videoview.setMediaController(mediaController);
		videoview.setMediaBufferingIndicator(progressBar);
		if (!encrypt) {
			videoview.setVid(vid, 1);
		} else {

		}
		videoview.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(IMediaPlayer mp) {
				// TODO Auto-generated method stub

			}
		});

	}
}
