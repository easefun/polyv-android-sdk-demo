package com.easefun.polyvsdk.demo;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.easefun.polyvsdk.ijk.IjkBaseMediaController;
import com.easefun.polyvsdk.ijk.IjkVideoView;
import com.easefun.polyvsdk.ijk.OnPreparedListener;

import tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * 带预览图的VideoView
 */
public class PreviewIjkVideoView extends RelativeLayout implements
		View.OnClickListener {
	private static final String TAG = "PreviewIjkVideoView";
	private Context mContext;
	private LayoutInflater inflater;
	private IjkVideoView mIjkVideoView;
	private RelativeLayout background;
	private ProgressBar mProgressBar;
	private ImageButton btn_video;
	private View view;

	public PreviewIjkVideoView(Context context) {
		super(context, null);
	}

	public PreviewIjkVideoView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		mContext = context;
		inflater = LayoutInflater.from(mContext);
		view = inflater.inflate(getResourseIdByName("layout", "preview_videoview"), this, true);
		mIjkVideoView = (IjkVideoView) view.findViewById(getResourseIdByName("id", "ijkvideoview"));
		background = (RelativeLayout) view.findViewById(getResourseIdByName("id", "background"));
		mProgressBar = (ProgressBar) view.findViewById(getResourseIdByName("id", "progressbar"));
		btn_video = (ImageButton) view.findViewById(getResourseIdByName("id","btn_video"));
		btn_video.setOnClickListener(this);
		mIjkVideoView.setMediaBufferingIndicator(mProgressBar);
		this.setClickable(true);
		this.setFocusable(true);
	}

	public IjkVideoView getVideoView() {
		return mIjkVideoView;
	}

	public String getCurrentVideoId() {
		return mIjkVideoView.getCurrentVideoId();
	}

	public int getCurrentPosition() {
		return mIjkVideoView.getCurrentPosition();
	}

	public int getBufferPercentage() {
		return mIjkVideoView.getBufferPercentage();
	}

	public IjkMediaPlayer getMediaPlayer() {
		return mIjkVideoView.getMediaPlayer();
	}

	public boolean isPlaying() {
		return mIjkVideoView.isPlaying();
	}

	public void seekTo(long mesc) {
		mIjkVideoView.seekTo(mesc);
	}


	/**
	 * @param vid
	 */
	public void setVid(String vid) {
		mIjkVideoView.setVid(vid);
	}
	
	public void setVid(String vid,int bitRate){
		 mIjkVideoView.setVid(vid,bitRate);
	}

	/**
	 * @param uri
	 */
	public void setVideoUri(Uri uri) {
		mIjkVideoView.setVideoURI(uri);
	}

	/**
	 * @param path
	 */
	public void setVideoPath(String path) {
		mIjkVideoView.setVideoPath(path);
	}

	public void setVideoLayout(int layout) {
		mIjkVideoView.setVideoLayout(layout);
	}

	public void setMediaController(IjkBaseMediaController mediaController) {
		mIjkVideoView.setMediaController(mediaController);
	}

	public void setOnPreparedListener(OnPreparedListener l) {
		mIjkVideoView.setOnPreparedListener(l);
	}

	public void setOnCompletionListener(OnCompletionListener l) {
		mIjkVideoView.setOnCompletionListener(l);
	}

	public void setOnErrorListener(OnErrorListener l) {
		mIjkVideoView.setOnErrorListener(l);
	}

	public void setOnInfoListener(OnInfoListener l) {
		mIjkVideoView.setOnInfoListener(l);
	}

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
		mIjkVideoView.setOnBufferingUpdateListener(l);
	}

	public void start() {
		btn_video.setVisibility(View.GONE);
		mIjkVideoView.setBackgroundDrawable(null);// remove preview
		mIjkVideoView.start();
	}

	public void pause() {
		mIjkVideoView.pause();
	}

	public void stopPlayback() {
		mIjkVideoView.stopPlayback();
	}

	public void initPreview(String vid) {
		new LoadPreview().execute(vid);
	}

	class LoadPreview extends AsyncTask<String, String, String> {
		private BitmapDrawable drawable = null;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			mProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			DefaultHttpClient httpclient = new DefaultHttpClient();
			InputStream in = null;
			try {
				HttpGet httpget = new HttpGet(
						"http://v.polyv.net/uc/video/getImage?vid=" + params[0]);
				;
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				in = entity.getContent();
				Bitmap bitmap = BitmapFactory.decodeStream(in);
				drawable = new BitmapDrawable(bitmap);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return params[0];
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			btn_video.setVisibility(View.VISIBLE);
			mIjkVideoView.setBackgroundDrawable(drawable);
			mProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	public int getResourseIdByName(String className, String name) {
		Class r = null;
		int id = 0;
		try { // System.out.println(getPackageName() + ".R");
			r = Class.forName(mContext.getPackageName() + ".R");

			Class[] classes = r.getClasses();
			Class desireClass = null;

			for (int i = 0; i < classes.length; i++) {
				if (classes[i].getName().split("\\$")[1].equals(className)) {
					desireClass = classes[i];
					break;
				}
			}

			if (desireClass != null)
				id = desireClass.getField(name).getInt(desireClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return id;

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		start();
	}
}
