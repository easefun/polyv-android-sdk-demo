package com.easefun.polyvsdk.demo;

import java.util.ArrayList;

import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.Video;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

public class VideoListActivity extends Activity {
    private static final String TAG="VideoList";
	private ArrayList<Video> videos;
	private VideoAdapter adapter;
	private ListView list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videolist);
		videos =new ArrayList<Video>();
		list=(ListView)findViewById(R.id.videolist);
		new LoadVideoList().execute();
	}
	
	class LoadVideoList extends AsyncTask<String,String,String>{
     
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			videos = PolyvSDKClient.getInstance().getVideoList(1, 10);
			
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			adapter=new VideoAdapter(VideoListActivity.this, videos);
			list.setAdapter(adapter);
			String a = videos.toString();
			Log.i(TAG,a);
		}
	}
}
