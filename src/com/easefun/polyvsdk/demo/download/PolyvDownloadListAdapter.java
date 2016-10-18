package com.easefun.polyvsdk.demo.download;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.easefun.polyvsdk.PolyvDownloadProgressListener;
import com.easefun.polyvsdk.PolyvDownloader;
import com.easefun.polyvsdk.PolyvDownloaderErrorReason;
import com.easefun.polyvsdk.PolyvDownloaderErrorReason.ErrorType;
import com.easefun.polyvsdk.PolyvDownloaderManager;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.Video;
import com.easefun.polyvsdk.demo.IjkVideoActicity;
import com.easefun.polyvsdk.demo.download.PolyvDLNotificationService.BindListener;

import android.Manifest;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
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

	private static final int NO_WRITE_PERMISSION = 12;
	private PolyvDLNotificationService notificationService;
	private ServiceConnection serconn;
	// 每个id的progress
	private SparseIntArray id_progress = new SparseIntArray();
	// 完成任务的key集合(key=vid+"_"+bit)
	private List<String> finishKeys;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.arg2 == NO_WRITE_PERMISSION) {
				Toast.makeText(context, "权限被拒绝，无法开始下载", 0).show();
				return;
			}
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
				long downloaded = msg.getData().getLong("count");
				long total = msg.getData().getLong("total");
				ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
				progressBar.setMax((int) total);
				progressBar.setProgress((int) downloaded);
				TextView tv = (TextView) view.findViewById(R.id.rate);
				tv.setText("" + downloaded * 100 / total);
				break;

			case SUCCESS:
				btn = (Button) view.findViewById(R.id.download);
				Toast.makeText(context, (position + 1) + "下载成功", Toast.LENGTH_SHORT).show();
				btn.setText("播放");
				break;

			case FAILURE:
				ErrorType errorType = (ErrorType) msg.obj;
				btn = (Button) view.findViewById(R.id.download);
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
				case VIDEO_LOAD_FAILURE:
					Toast.makeText(context, "第" + (position + 1) + "个任务Video加载失败，请重试", 0).show();
					break;
				case VIDEO_NULL:
					Toast.makeText(context, "第" + (position + 1) + "个任务video取得为null，请重试", 0).show();
					break;
				case DIR_SPACE_LACK:
					Toast.makeText(context, "第" + (position + 1) + "个任务存储空间不足，请清除存储空间重试", Toast.LENGTH_SHORT).show();
					break;
				case DOWNLOAD_DIR_IS_NUll:
					Toast.makeText(context, "第" + (position + 1) + "个任务下载文件夹未设置", Toast.LENGTH_SHORT).show();
					break;
				case HLS_15X_URL_ERROR:
					Toast.makeText(context, "第" + (position + 1) + "个任务1.5倍速播放地址错误", Toast.LENGTH_SHORT).show();
					break;
				case HLS_SPEED_TYPE_IS_NULL:
					Toast.makeText(context, "第" + (position + 1) + "个任务未设置视频播放速度，请设置", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}

				break;
			}
		};
	};

	public ServiceConnection getSerConn() {
		return serconn;
	}

	public PolyvDownloadListAdapter(Context context, LinkedList<PolyvDownloadInfo> data, ListView listView) {
		serconn = PolyvDLNotificationService.bindDownloadService(context, new BindListener() {

			@Override
			public void bindSuccess(PolyvDLNotificationService downloadService) {
				notificationService = downloadService;
			}
		});
		this.context = context;
		this.data = data;
		this.inflater = LayoutInflater.from(context);
		this.service = new PolyvDBservice(context);
		listener = new ArrayList<MyDownloadListener>();
		finishKeys = new ArrayList<String>();
		this.listView = listView;
		initDownloaders();
	}

	private class MyDownloadListener implements PolyvDownloadProgressListener {
		private int position;
		private PolyvDownloadInfo info;
		private long total;
		private int id;

		public MyDownloadListener(int position, PolyvDownloadInfo info) {
			this.position = position;
			this.info = info;
			this.id = PolyvDLNotificationService.getId(info.getVid(), info.getBitrate(), info.getSpeed());
		}

		public void setPosition(int position) {
			this.position = position;
		}

		@Override
		public void onDownloadSuccess() {
			addFinishKeyToList(info.getVid(), info.getBitrate(), info.getSpeed());
			if (notificationService != null)
				notificationService.updateFinishNF(id);
			Message msg = handler.obtainMessage();
			msg.arg1 = position;
			msg.what = SUCCESS;
			service.updatePercent(info, total, total);
			handler.sendMessage(msg);
		}

		@Override
		public void onDownloadFail(PolyvDownloaderErrorReason errorReason) {
			if (notificationService != null)
				notificationService.updateErrorNF(id, false);
			Message msg = handler.obtainMessage();
			msg.arg1 = position;
			msg.what = FAILURE;
			msg.obj = errorReason.getType();
			handler.sendMessage(msg);
		}

		@Override
		public void onDownload(long count, long total) {
			int progress = (int) (count * 100 / total);
			id_progress.put(id, progress);
			if (notificationService != null)
				notificationService.updateDownloadingNF(id, progress, false);
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
			downloader = PolyvDownloaderManager.getPolyvDownloader(_vid, info.getBitrate(),
					Video.HlsSpeedType.getHlsSpeedType(info.getSpeed()));
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
		// 初始化progress
		int id = PolyvDLNotificationService.getId(info.getVid(), info.getBitrate(), info.getSpeed());
		int progress = 0;
		if (total != 0)
			progress = (int) (percent * 100 / total);
		if (id_progress.get(id, -1) == -1)
			id_progress.put(id, progress);
		if (total != 0)
			holder.tv_rate.setText("" + percent * 100 / total);
		else
			holder.tv_rate.setText("" + 0);
		if (total != 0 && total == percent) {
			// 已经完成的任务，把其key放到集合中，当下载全部的时候可以不让其开始
			addFinishKeyToList(info.getVid(), info.getBitrate(), info.getSpeed());
			holder.btn_download.setText("播放");
		} else if (PolyvDownloaderManager.getPolyvDownloader(info.getVid(), info.getBitrate(),
				Video.HlsSpeedType.getHlsSpeedType(info.getSpeed())).isDownloading())
			holder.btn_download.setText("暂停");
		else
			holder.btn_download.setText("开始");
		holder.btn_download.setOnClickListener(
				new DownloadListener(info.getVid(), info.getSpeed(), info.getBitrate(), convertView, info.getTitle()));
		holder.btn_delete.setOnClickListener(new DeleteListener(info, position));
		return convertView;
	}

	// 把完成的任务加入到key集合中
	private void addFinishKeyToList(String vid, int bit, String speed) {
		String key = vid + "_" + bit + "_" + speed;
		if (!finishKeys.contains(key))
			finishKeys.add(key);
	}

	// 从集合中移除完成的任务key
	private void removeFinishKeyToList(String vid, int bit, String speed) {
		String key = vid + "_" + bit + "_" + speed;
		if (finishKeys.contains(key))
			finishKeys.remove(key);
	}

	public boolean downloadAllFile() {
		if (!hasPermission())
			return false;
		if (notificationService != null)
			notificationService.updateUnfinishedNF(data, finishKeys);
		PolyvDownloaderManager.startUnfinished(finishKeys);
		return true;
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

	// 判断是否有写入sd卡的权限
	private boolean hasPermission() {
		if (ContextCompat.checkSelfPermission(context,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			// 没有权限，无法开始下载
			Message msg = handler.obtainMessage();
			msg.arg2 = NO_WRITE_PERMISSION;
			handler.sendMessage(msg);
			return false;
		}
		return true;
	}

	class DownloadListener implements View.OnClickListener {
		private final String vid;
		private final int bitRate;
		private final String speed;
		private View view;
		// 视频的标题
		private String title;

		public DownloadListener(String vid, String speed, int bitRate, View view, String title) {
			this.vid = vid;
			this.speed = speed;
			this.bitRate = bitRate;
			this.view = view;
			this.title = title;
		}

		@Override
		public void onClick(View v) {
			if (!hasPermission())
				return;
			Button download = (Button) view.findViewById(R.id.download);
			int id = PolyvDLNotificationService.getId(vid, bitRate, speed);
			if (download.getText().equals("开始")) {
				((Button) v).setText("暂停");
				PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(vid, bitRate,
						Video.HlsSpeedType.getHlsSpeedType(speed));
				// 先执行
				if (notificationService != null) {
					notificationService.updateStartNF(id, vid, bitRate, speed, title, id_progress.get(id));
				}
				if (downloader != null) {
					downloader.start();
				}
			} else if (download.getText().equals("暂停")) {
				((Button) v).setText("开始");
				PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(vid, bitRate,
						Video.HlsSpeedType.getHlsSpeedType(speed));
				// 先执行
				if (notificationService != null) {
					notificationService.updatePauseNF(id);
				}
				if (downloader != null) {
					downloader.stop();
				}
			} else if (download.getText().equals("播放")) {
				IjkVideoActicity.intentTo(context, IjkVideoActicity.PlayMode.portrait, IjkVideoActicity.PlayType.vid,
						vid, true, Video.HlsSpeedType.getHlsSpeedType(speed), false);
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
			removeFinishKeyToList(info.getVid(), info.getBitrate(), info.getSpeed());
			PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(info.getVid(), info.getBitrate(),
					Video.HlsSpeedType.getHlsSpeedType(info.getSpeed()));
			PolyvDownloaderManager.clearPolyvDownload(info.getVid(), info.getBitrate());
			if (downloader != null) {
				downloader.deleteVideo(info.getVid(), info.getBitrate(),
						Video.HlsSpeedType.getHlsSpeedType(info.getSpeed()));
			}
			int id = PolyvDLNotificationService.getId(info.getVid(), info.getBitrate(), info.getSpeed());
			if (notificationService != null) {
				notificationService.updateDeleteNF(id);
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
		if (notificationService != null)
			notificationService.updateAllPauseNF(data);
		PolyvDownloaderManager.stopAll();
	}
}
