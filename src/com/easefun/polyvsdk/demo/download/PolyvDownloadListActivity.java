 package com.easefun.polyvsdk.demo.download;

import java.util.LinkedList;

import com.easefun.polyvsdk.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class PolyvDownloadListActivity extends Activity {
	private static final String TAG = "PolyvDownloadListActivity";
	private ListView list;
	private LinkedList<PolyvDownloadInfo> infos;
	private PolyvDBservice service;
	private PolyvDownloadListAdapter adapter;
	private Button btn_downloadall;
	private TextView emptyView;
	private boolean isStop = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloadlist);
		list = (ListView) findViewById(R.id.list);
		emptyView = (TextView) findViewById(R.id.emptyView);
		list.setEmptyView(emptyView);
		btn_downloadall = (Button) findViewById(R.id.download_all);
		initData();
		btn_downloadall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isStop) {
					((Button) v).setText("暂停全部");
					adapter.downloadAllFile();
					adapter.updateAllButton(true);
					isStop = !isStop;
				} else {
					((Button) v).setText("下载全部");
					adapter.stopAll();
					adapter.updateAllButton(false);
					isStop = !isStop;
				}
			}
		});
	}

	private void initData() {
		service = new PolyvDBservice(this);
		infos = service.getDownloadFiles();
		adapter = new PolyvDownloadListAdapter(this, infos,list);
		list.setAdapter(adapter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(adapter.getSerConn());
	}
}
