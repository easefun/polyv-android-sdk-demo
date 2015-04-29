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
import android.widget.Toast;

public class IjkFullVideoActivity extends Activity{
      private IjkVideoView videoview;
      private IjkMediaController mediaController;
      private String path;
      private String vid;
      private boolean isLocal=false;
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
			isLocal = true;
		}
		videoview =(IjkVideoView)findViewById(R.id.videoview);
    	mediaController = new IjkMediaController(this,false);
    	videoview.setMediaController(mediaController);
    	if(!isLocal){
    		videoview.setVideoId("sl8da4jjbx2262724b8a5132bd6103b2_s");
    	}else{
    			videoview.setLocalVideo(vid);
    	}
   
    	videoview.setOnPreparedListener(new MyListener(path,vid));
    	//设置切屏事件- do nothing 
    	mediaController.setOnBoardChangeListener(new IjkMediaController.OnBoardChangeListener() {
    				
    				@Override
    				public void onPortrait() {
    					// TODO Auto-generated method stub
    					
    				}
    				
    				@Override
    				public void onLandscape() {
    					// TODO Auto-generated method stub
    					
    				}
    			});
    			// 设置视频尺寸 ，在横屏下效果较明显
    		   mediaController.setOnVideoChangeListener(new IjkMediaController.OnVideoChangeListener() {
    			
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
      class MyListener extends PolyvOnPreparedListener{

  		public MyListener(String path, String videoId) {
  			super(path, videoId);
  		
  		}
      	 @Override
      	public void onPrepared(IMediaPlayer mp) {
      		// TODO Auto-generated method stub
      		super.onPrepared(mp);
      	}
       }
}
