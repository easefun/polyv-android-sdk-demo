package com.easefun.polyvsdk.demo.download;

import java.util.ArrayList;
import java.util.LinkedList;

import com.easefun.polyvsdk.PolyvDownloadProgressListener;
import com.easefun.polyvsdk.PolyvDownloader;
import com.easefun.polyvsdk.PolyvDownloaderErrorReason;
import com.easefun.polyvsdk.PolyvDownloaderErrorReason.ErrorType;
import com.easefun.polyvsdk.PolyvDownloaderManager;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.demo.IjkVideoActicity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PolyvDownloadListAdapter extends BaseAdapter {
	private static final String TAG = "PolyvDownloadListAdapter";

	private static final int REFRESH_PROGRESS = 1;
	private static final int SUCCESS = 2;
	private static final int FAILURE = 3;

	private LinkedList<PolyvDownloadInfo> data;
	private ArrayList<MyDownloadListener> listener;
	private Context context;
	private LayoutInflater inflater;
	private PolyvDBservice service;
	private ViewHolder holder;

	private PolyvDownloader downloader;

	private ListView listView;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Button btn = null;
			int position = (int) msg.arg1;
			int offset = position - listView.getFirstVisiblePosition();
			int endset = position - listView.getLastVisiblePosition();
			if (offset < 0 || endset > 0)
				return;
			View view = (View) listView.getChildAt(offset);
			switch (msg.what) {
			case REFRESH_PROGRESS:
				btn = (Button) view.findViewById(R.id.download);
				if (!btn.getText().equals("播放")) {
					long downloaded = msg.getData().getLong("count");
					long total = msg.getData().getLong("total");
					ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
					progressBar.setMax((int) total);
					progressBar.setProgress((int) downloaded);
					TextView tv = (TextView) view.findViewById(R.id.rate);
					tv.setText("" + downloaded * 100 / total);
				}
				break;

			case SUCCESS:
				btn = (Button) view.findViewById(R.id.download);
				if (!btn.getText().equals("播放")) {
					Toast.makeText(context, (position + 1) + "下载成功", Toast.LENGTH_SHORT).show();
					btn.setText("播放");
				}
				break;

			case FAILURE:
				ErrorType errorType = (ErrorType) msg.obj;
				btn = (Button) view.findViewById(R.id.download);
				if (!btn.getText().equals("播放")) {
					btn.setText("开始");

					switch (errorType) {
					case VID_IS_NULL:
						Toast.makeText(context, "第" + (position + 1) + "个任务vid错误，请重试", 0).show();
						break;
					case NOT_PERMISSION:
						Toast.makeText(context, "第" + (position + 1) + "个任务没有权限访问视频,请重试", 0).show();
						break;
					case RUNTIME_EXCEPTION:
						Toast.makeText(context, "第" + (position + 1) + "个任务运行时异常，请重试", 0).show();
						break;
					case VIDEO_STATUS_ERROR:
						Toast.makeText(context, "第" + (position + 1) + "个任务视频状态错误，请重试", 0).show();
						break;
					case M3U8_NOT_DATA:
						Toast.makeText(context, "第" + (position + 1) + "个任务m3u8没有数据，请重试", 0).show();
						break;
					case QUESTION_NOT_DATA:
						Toast.makeText(context, "第" + (position + 1) + "个任务问答没有数据，请重试", 0).show();
						break;
					case MULTIMEDIA_LIST_EMPTY:
						Toast.makeText(context, "第" + (position + 1) + "个任务ts下载列表为空，请重试", 0).show();
						break;
					case CAN_NOT_MKDIR:
						Toast.makeText(context, "第" + (position + 1) + "个任务不能创建文件夹，请重试", 0).show();
						break;
					case DOWNLOAD_TS_ERROR:
						Toast.makeText(context, "第" + (position + 1) + "个任务下载ts错误，请重试", 0).show();
						break;
					case MULTIMEDIA_EMPTY:
						Toast.makeText(context, "第" + (position + 1) + "个任务mp4下载地址为空，请重试", 0).show();
						break;
					case NOT_CREATE_DIR:
						Toast.makeText(context, "第" + (position + 1) + "个任务不能创建目录，请重试", 0).show();
						break;
					}

				}
				break;
			}
		};
	};

	public PolyvDownloadListAdapter(Context context, LinkedList<PolyvDownloadInfo> data, ListView listView) {
		this.context = context;
		this.data = data;
		this.inflater = LayoutInflater.from(context);
		this.service = new PolyvDBservice(context);
		listener = new ArrayList<MyDownloadListener>();
		this.listView = listView;
		initDownloaders();
	}

	private class MyDownloadListener implements PolyvDownloadProgressListener {
		private int position;
		private PolyvDownloadInfo info;
		private long total;

		public MyDownloadListener(int position, PolyvDownloadInfo info) {
			this.position = position;
			this.info = info;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		@Override
		public void onDownloadSuccess() {
			Message msg = handler.obtainMessage();
			msg.arg1 = position;
			msg.what = SUCCESS;
			service.updatePercent(info, total, total);
			handler.sendMessage(msg);
		}

		@Override
		public void onDownloadFail(PolyvDownloaderErrorReason errorReason) {
			Message msg = handler.obtainMessage();
			msg.arg1 = position;
			msg.what = FAILURE;
			msg.obj = errorReason.getType();
			handler.sendMessage(msg);
		}

		@Override
		public void onDownload(long count, long total) {
			this.total = total;
			Message msg = handler.obtainMessage();
			msg.arg1 = position;
			Bundle bundle = new Bundle();
			bundle.putLong("count", count);
			bundle.putLong("total", total);
			msg.setData(bundle);
			msg.what = REFRESH_PROGRESS;
			service.updatePercent(info, count, total);
			handler.sendMessage(msg);
		}
	}

	private void initDownloaders() {
		for (int i = 0; i < data.size(); i++) {
			final PolyvDownloadInfo info = data.get(i);
			final String _vid = info.getVid();
			final int p = i;
			downloader = PolyvDownloaderManager.getPolyvDownloader(_vid, info.getBitrate());
			MyDownloadListener downloadListener = new MyDownloadListener(p, info);
			listener.add(downloadListener);
			downloader.setPolyvDownloadProressListener(downloadListener);
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
		final int i = position;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.view_item, null);
			holder = new ViewHolder();
			holder.tv_vid = (TextView) convertView.findViewById(R.id.tv_vid);
			holder.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);
			holder.tv_filesize = (TextView) convertView.findViewById(R.id.tv_filesize);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
			holder.btn_download = (Button) convertView.findViewById(R.id.download);
			holder.btn_delete = (Button) convertView.findViewById(R.id.delete);
			holder.tv_rate = (TextView) convertView.findViewById(R.id.rate);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		data = service.getDownloadFiles();
		final PolyvDownloadInfo info = data.get(position);
		String duration = info.getDuration();
		long filesize = info.getFilesize();
		long percent = info.getPercent();
		long total = info.getTotal();
		holder.tv_vid.setText(info.getTitle());
		holder.tv_duration.setText(duration);
		holder.tv_filesize.setText("" + filesize);
		holder.progressBar.setTag("" + position);
		holder.progressBar.setMax((int) total);
		holder.progressBar.setProgress((int) percent);
		if (total != 0)
			holder.tv_rate.setText("" + percent * 100 / total);
		else
			holder.tv_rate.setText("" + 0);
		if (total != 0 && total == percent) {
			holder.btn_download.setText("播放");
		} else if (PolyvDownloaderManager.getPolyvDownloader(info.getVid(), info.getBitrate()).isDownloading())
			holder.btn_download.setText("暂停");
		else
			holder.btn_download.setText("开始");
		holder.btn_download.setOnClickListener(new DownloadListener(info.getVid(), info.getBitrate(), convertView));
		holder.btn_delete.setOnClickListener(new DeleteListener(info, position));
		return convertView;
	}

	public void downloadAllFile() {
		PolyvDownloaderManager.startAll();
	}

	public void updateAllButton(boolean isStop) {
		for (int i = 0; i < listView.getChildCount(); i++) {
			Button down = (Button) listView.getChildAt(i).findViewById(R.id.download);
			if (!down.getText().equals("播放")) {
				if (isStop)
					down.setText("暂停");
				else
					down.setText("开始");
			}
		}
	}

	private class ViewHolder {
		TextView tv_vid, tv_duration, tv_filesize, tv_rate;
		ProgressBar progressBar;
		Button btn_download, btn_delete, btn_start, btn_pause;
	}

	class DownloadListener implements View.OnClickListener {
		private final String vid;
		private final int bitRate;
		private View view;

		public DownloadListener(String vid, int bitRate, View view) {
			this.vid = vid;
			this.bitRate = bitRate;
			this.view = view;
		}

		@Override
		public void onClick(View v) {
			Button download = (Button) view.findViewById(R.id.download);
			if (download.getText().equals("开始")) {
				((Button) v).setText("暂停");
				PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(vid, bitRate);
				if (downloader != null) {
					downloader.start();
				}

			} else if (download.getText().equals("暂停")) {
				((Button) v).setText("开始");
				PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(vid, bitRate);
				if (downloader != null) {
					downloader.stop();
				}
			} else if (download.getText().equals("播放")) {
				IjkVideoActicity.intentTo(context, IjkVideoActicity.PlayMode.portrait, IjkVideoActicity.PlayType.vid,
						vid, false);
			}
		}

	}

	class DeleteListener implements View.OnClickListener {
		private PolyvDownloadInfo info;
		private int position;

		public DeleteListener(PolyvDownloadInfo info, int position) {
			this.info = info;
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(info.getVid(), info.getBitrate());
			PolyvDownloaderManager.clearPolyvDownload(info.getVid(), info.getBitrate());
			if (downloader != null) {
				downloader.deleteVideo(info.getVid(), info.getBitrate());
			}
			service.deleteDownloadFile(info);
			data.remove(position);
			listener.remove(position);
			for (int i = 0; i < listener.size(); i++) {
				listener.get(i).setPosition(i);
			}
			notifyDataSetChanged();
		}
	}

	public void stopAll() {
		PolyvDownloaderManager.stopAll();
	}
}
