package com.easefun.polyvsdk.demo;

import java.util.LinkedList;
import java.util.List;

import com.easefun.polyvsdk.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DownloadListActivity extends Activity {
	private static final String TAG="DownloadList";
	private ListView list;
	private LinkedList<DownloadInfo> infos;
	private DBservice service;
	private ListAdapter adapter;
	private Button btn_downloadall;
	private TextView emptyView;
	private boolean isStop=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloadlist);
		list = (ListView)findViewById(R.id.list);
		emptyView=(TextView)findViewById(R.id.emptyView);
		list.setEmptyView(emptyView);
		btn_downloadall=(Button)findViewById(R.id.download_all);
		initData();
		btn_downloadall.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(!isStop){
					((Button)v).setText("暂停全部");
					adapter.downloadAllFile();
					adapter.updateAllButton(true);
					isStop=!isStop;
				}else{
					((Button)v).setText("开始全部");
					adapter.stopAll();
					adapter.updateAllButton(false);
					isStop=!isStop;
				}
			}
		});
		
	}
	@Override
	public void finish() {
		if(adapter!=null){
			Log.i("downloader","stop all");
			adapter.stopAll();
		}
		super.finish();
	}
	private void initData() {
		service=new DBservice(this);
		infos = service.getDownloadFiles();
		Log.i(TAG, "download list->"+infos.toString());
		adapter=new ListAdapter(this, infos);
		list.setAdapter(adapter);
	}
}
