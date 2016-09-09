package com.easefun.polyvsdk.demo;

import java.util.List;

import org.json.JSONException;

import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.RestVO;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class VideoListActivity extends Activity {
	
    private static final String TAG = "VideoList";
	private ListView list;
	private VideoAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videolist);
		list = (ListView)findViewById(R.id.videolist);
		new LoadVideoList().execute();
	}
	
	class LoadVideoList extends AsyncTask<String, String, List<RestVO>> {

		@Override
		protected List<RestVO> doInBackground(String... arg0) {
			try {
				return PolyvSDKClient.getInstance().getVideoList(1, 10);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(List<RestVO> result) {
			super.onPostExecute(result);
			if (result == null) return;
			adapter = new VideoAdapter(VideoListActivity.this, result);
			list.setAdapter(adapter);
			String a = result.toString();
			Log.i(TAG, a);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(adapter!=null)
			unbindService(adapter.getSerConn());
	}
}
