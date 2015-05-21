package com.easefun.polyvsdk.demo;
 
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import com.easefun.polyvsdk.DownloadHelper;
import com.easefun.polyvsdk.DownloadProgressListener;
import com.easefun.polyvsdk.Downloader;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.SDKUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ListAdapter extends BaseAdapter {
	private static final String TAG="DownloadList";
	private LinkedList<DownloadInfo> data;
	private Context context;
	private LayoutInflater inflater;
	private DownloadHelper downloadHelper;
	private Downloader downloader;
	private File downloadedFile;

	private static ViewHolder holder;
	private static final int REFRESH_PROGRESS = 1;
	private LinkedList<ProgressBar> barlist;
	private LinkedList<TextView> rlist;
	private LinkedList<Boolean> flags;
	private LinkedList<String> vids;
	private ArrayList<Downloader> downloaders;
	private LinkedList<Button> butlist;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_PROGRESS:
				long downloaded = msg.getData().getLong("downloaded");
				long total = msg.getData().getLong("total");
				long precent = downloaded * 100 / total;
				int position = msg.arg1;
				ProgressBar progressBar = barlist.get(position);
				TextView tv = rlist.get(position);
				progressBar.setProgress((int) precent);
				tv.setText("" + precent);
				if (progressBar.getProgress() == progressBar.getMax()) {
//					if (downloader != null)
//						downloader.stop();
					Button btn = butlist.get(position);
					btn.setEnabled(true);
					Toast.makeText(context, "下载成功", 1).show();
				}
				break;

			default:
				break;
			}
		};
	};

	public ListAdapter(Context context, LinkedList<DownloadInfo> data) {
		this.context = context;
		this.data = data;
		this.inflater = LayoutInflater.from(context);
		this.barlist = new LinkedList<ProgressBar>();
		this.rlist = new LinkedList<TextView>();
		this.butlist=new LinkedList<Button>();
		this.flags = new LinkedList<Boolean>();
		this.vids = new LinkedList<String>();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup group) {
		// TODO Auto-generated method stub

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.view_item, null);
			holder = new ViewHolder();
			holder.tv_vid = (TextView) convertView.findViewById(R.id.tv_vid);
			holder.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);
			holder.tv_filesize = (TextView) convertView.findViewById(R.id.tv_filesize);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
			barlist.addLast(holder.progressBar);
			holder.btn_download = (Button) convertView.findViewById(R.id.download);
			holder.tv_rate = (TextView) convertView.findViewById(R.id.rate);
			rlist.addLast(holder.tv_rate);
			butlist.addLast(holder.btn_download);
			convertView.setTag(holder);
			flags.addLast(new Boolean(false));
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String vid = data.get(position).getVid();
		vids.addLast(vid);
		String duration = data.get(position).getDuration();
		int filesize = data.get(position).getFilesize();
		downloadedFile = SDKUtil.getDownloadFileByVid(vid);
		holder.tv_vid.setText(vid + ".mp4");
		holder.tv_duration.setText(duration);
		holder.tv_filesize.setText("" + filesize);
		holder.progressBar.setTag("" + position);
		holder.progressBar.setMax(100);
		if (downloadedFile.exists()) {
			float downloaded = ((float) downloadedFile.length()/ (float) filesize * 100);
			holder.progressBar.setProgress((int) Math.round(downloaded));
			holder.tv_rate.setText("" + (int) Math.round(downloaded));
		}
		holder.btn_download.setOnClickListener(new DownloadListener(data.get(
				position).getVid(), position));
		return convertView;
	}

	public void downloadAllFile() {
		Log.i("ListAdapter", vids.toString());
		// 实例化N个downloader，令flags 全true,for()启动下载
		downloaders = new ArrayList<Downloader>();

		for (int i = 0; i < vids.size(); i++) {
			downloadHelper = new DownloadHelper(context, vids.get(i), 1);
			downloader = downloadHelper.initDownloader("mp4");
			downloaders.add(downloader);
		}
		for (int j = 0; j < vids.size(); j++) {
			final int p = j;
			new AsyncTask<String, String, String>() {

				@Override
				protected String doInBackground(String... arg0) {
					// TODO Auto-generated method stub
					downloaders.get(p).begin(vids.get(p),
							new DownloadProgressListener() {

								@Override
								public void onDownloadSize(long downloaded,
										long total) {
									// TODO Auto-generated method stub
									Message msg = new Message();
									msg.what = REFRESH_PROGRESS;
									msg.arg1 = p;
									Bundle bundle = new Bundle();
									bundle.putLong("downloaded", downloaded);
									bundle.putLong("total", total);
									msg.setData(bundle);
									handler.sendMessage(msg);
								}

								@Override
								public void onDownloadSuccess() {
									// TODO Auto-generated method stub
									//下载完成调用
								}
							});
					return null;
				}

			}.execute();
		}
	}
    
	public void updateAllButton(boolean isStop){
	   if(isStop){
		   for(int i=0;i<butlist.size();i++){
			   butlist.get(i).setText("暂停");
			   flags.set(i, !isStop);
		   }
	   }else{
		   for(int i=0;i<butlist.size();i++){
			   butlist.get(i).setText("开始");
			   flags.set(i, !isStop);
		   }
	   }
	}
	
	private class ViewHolder {
		TextView tv_vid, tv_duration, tv_filesize, tv_rate;
		ProgressBar progressBar;
		Button btn_download;
	}

	class DownloadListener implements View.OnClickListener {
		private String vid;
		int p;

		public DownloadListener(String vid, int p) {
			this.vid = vid;
			this.p = p;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			boolean isStop = flags.get(p);
			if (!isStop) {
				Log.i(TAG, "start download - position "+p);
				((TextView) v).setText("暂停");
				downloadHelper = new DownloadHelper(context, vid, 1);
				downloader = downloadHelper.initDownloader("mp4");
				downloader.start();
				new DownloadTask(p).execute(vid);
				flags.set(p, !isStop);
			} else {
				Log.i(TAG, "stop download - position "+p);
				((TextView) v).setText("开始");
				if (downloader != null) {
					downloader.stop();
				}
				flags.set(p, !isStop);
			}
		}

	}

	class DownloadTask extends AsyncTask<String, String, String> {
		private int position;

		public DownloadTask(int p) {
			// TODO Auto-generated constructor stub
			this.position = p;
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			downloader.begin(params[0], new DownloadProgressListener() {
				@Override
				public void onDownloadSize(long downloaded, long total) {
					// TODO Auto-generated method stub
					Message msg = new Message();
					msg.what = REFRESH_PROGRESS;
					msg.arg1 = position;
					Bundle bundle = new Bundle();
					bundle.putLong("downloaded", downloaded);
					bundle.putLong("total", total);
					msg.setData(bundle);
					handler.sendMessage(msg);
				}

				@Override
				public void onDownloadSuccess() {
					// TODO Auto-generated method stub
					//下载完成后调用
				}
			});
			return null;
		}

	}

	public void stopAll() {
		// TODO Auto-generated method stub
		if(downloaders!=null){
	   for(int i=0;i<downloaders.size();i++){
		   if(downloaders.get(i)!=null){
			   downloaders.get(i).stop();
		   }
	   }
		}
	}
}
