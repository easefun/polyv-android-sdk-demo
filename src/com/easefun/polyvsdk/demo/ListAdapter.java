package com.easefun.polyvsdk.demo;

import java.util.LinkedList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyvsdk.PolyvDownloadProgressListener;
import com.easefun.polyvsdk.PolyvDownloader;
import com.easefun.polyvsdk.PolyvDownloaderErrorReason;
import com.easefun.polyvsdk.PolyvDownloaderManager;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.SDKUtil;

public class ListAdapter extends BaseAdapter {
	private static final String TAG = "DownloadList";
	
	private static final int REFRESH_PROGRESS = 1;
	private static final int SUCCESS = 2;
	private static final int FAILURE = 3;
	
	private LinkedList<DownloadInfo> data = null;
	private Context context = null;
	private LayoutInflater inflater = null;
	private DBservice dbService = null;
	private static ViewHolder holder = null;
	private LinkedList<ProgressBar> barlist = null;
	private LinkedList<TextView> rlist = null;
	private LinkedList<Boolean> flags = null;
	private LinkedList<Button> btnlist = null;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Button btn = null;
			int position = msg.arg1;
			switch (msg.what) {
				case REFRESH_PROGRESS:
					long downloaded = msg.getData().getLong("downloaded");
					long total = msg.getData().getLong("total");
					long precent = downloaded * 100 / total;
					if (position < barlist.size()) {
						ProgressBar progressBar = barlist.get(position);
						progressBar.setProgress((int) precent);
						TextView tv = rlist.get(position);
						tv.setText("" + precent);
					}
					
					break;
					
				case SUCCESS:
					Log.i(TAG, "下载完成");
					Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show();
					if (position < btnlist.size()) {
						btn = btnlist.get(position);
						btn.setEnabled(true);
						btn.setText("开始");
						flags.set(position, false);
					}
					
					break;
					
				case FAILURE:
					String error = msg.getData().getString("error");
					Log.i(TAG, "下载失败:" + error);
					Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
					if (position < btnlist.size()) {
						btn = btnlist.get(position);
						btn.setEnabled(true);
						btn.setText("开始");
						flags.set(position, false);
					}
					
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
		this.btnlist = new LinkedList<Button>();
		this.flags = new LinkedList<Boolean>();
		this.dbService = new DBservice(context);
		initDownloaders();
	}
	
	private void initDownloaders() {
		for (int i = 0; i < data.size(); i++) {
			final DownloadInfo info = data.get(i);
			final String _vid = info.getVid();
			final int p = i;
			PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(_vid, info.getBitrate());
			downloader.setPolyvDownloadProressListener(new PolyvDownloadProgressListener() {
				public void onDownload(long downloaded, long total) {
					Log.i("download", "downloading:" + _vid + " - " + downloaded + "/" + total);
					Message msg = new Message();
					msg.what = REFRESH_PROGRESS;
					msg.arg1 = p;
					
					Bundle bundle = new Bundle();
					bundle.putLong("downloaded", downloaded);
					bundle.putLong("total", total);
					
					msg.setData(bundle);
					handler.sendMessage(msg);
					
					long percent = downloaded * 100 / total;
					dbService.updatePercent(info, (int) percent);
				}

				public void onDownloadSuccess() {
					Message msg = new Message();
					msg.what = SUCCESS;
					msg.arg1 = p;
					handler.sendMessage(msg);
				}

				public void onDownloadFail(PolyvDownloaderErrorReason errorReason) {
					Message msg = new Message();
					msg.what = FAILURE;
					msg.arg1 = p;
					
					Bundle bundle = new Bundle();
					bundle.putString("error", SDKUtil.getExceptionFullMessage(errorReason.getCause(), -1));
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			});
		}
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup group) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.view_item, null);
			holder = new ViewHolder();
			holder.tv_vid = (TextView) convertView.findViewById(R.id.tv_vid);
			holder.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);
			holder.tv_filesize = (TextView) convertView.findViewById(R.id.tv_filesize);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
			barlist.addLast(holder.progressBar);
			holder.btn_download = (Button) convertView.findViewById(R.id.download);
			btnlist.addLast(holder.btn_download);
			holder.btn_delete = (Button) convertView.findViewById(R.id.delete);
			holder.tv_rate = (TextView) convertView.findViewById(R.id.rate);
			rlist.addLast(holder.tv_rate);
			convertView.setTag(holder);
			flags.addLast(new Boolean(false));
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String duration = data.get(position).getDuration();
		long filesize = data.get(position).getFilesize();
		int percent = data.get(position).getPercent();
		holder.tv_vid.setText(data.get(position).getTitle());
		holder.tv_duration.setText(duration);
		holder.tv_filesize.setText("" + filesize);
		holder.progressBar.setTag("" + position);
		holder.progressBar.setMax(100);
		holder.progressBar.setProgress(percent);
		holder.tv_rate.setText("" + percent);
		holder.btn_download.setOnClickListener(
				new DownloadListener(data.get(position).getVid(), data.get(position).getBitrate(), position));
		holder.btn_delete.setOnClickListener(new DeleteListener(data.get(position), position));
		return convertView;
	}

	public void downloadAllFile() {
		PolyvDownloaderManager.startAll();
	}

	public void updateAllButton(boolean isStop) {
		if (isStop) {
			for (int i = 0; i < btnlist.size(); i++) {
				btnlist.get(i).setText("暂停");
				flags.set(i, !isStop);
			}
		} else {
			for (int i = 0; i < btnlist.size(); i++) {
				btnlist.get(i).setText("开始");
				flags.set(i, !isStop);
			}
		}
	}

	private class ViewHolder {
		TextView tv_vid, tv_duration, tv_filesize, tv_rate;
		ProgressBar progressBar;
		Button btn_download, btn_delete;
	}

	class DownloadListener implements View.OnClickListener {
		private final String vid;
		private final int bitRate;
		private final int p;

		public DownloadListener(String vid, int bitRate, int p) {
			this.vid = vid;
			this.bitRate = bitRate;
			this.p = p;
		}

		@Override
		public void onClick(View v) {
			boolean isStop = flags.get(p);
			if (!isStop) {
				Log.i(TAG, "start download - position " + p);
				((TextView) v).setText("暂停");
				PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(vid, bitRate);
				if (downloader != null) {
					downloader.start();
				}
				
				flags.set(p, !isStop);
			} else {
				Log.i(TAG, "stop download - position " + p);
				((TextView) v).setText("开始");
				PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(vid, bitRate);
				if (downloader != null) {
					downloader.stop();
				}
				
				flags.set(p, !isStop);
			}
		}

	}

	class DeleteListener implements View.OnClickListener {
		private DownloadInfo info;
		int p;

		public DeleteListener(DownloadInfo info, int p) {
			this.info = info;
			this.p = p;
		}

		@Override
		public void onClick(View v) {
			PolyvDownloader downloader = PolyvDownloaderManager.clearPolyvDownload(info.getVid(), info.getBitrate());
			if (downloader != null) {
				downloader.deleteVideo(info.getVid(), info.getBitrate());
				dbService.deleteDownloadFile(info);
				data.remove(p);
				flags.remove(p);
				notifyDataSetChanged();
			}
		}
	}

	public void stopAll() {
		PolyvDownloaderManager.stopAll();
	}
}
