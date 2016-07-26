package com.easefun.polyvsdk.demo.upload;

import java.util.LinkedList;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.upload.PolyvUploader;
import com.easefun.polyvsdk.upload.PolyvUploaderManager;

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

public class PolyvUploadListAdapter extends BaseAdapter {
	private static final String TAG = "PolyvUploadListAdapter";

	private static final int REFRESH_PROGRESS = 1;
	private static final int SUCCESS = 2;
	private static final int FAILURE = 3;

	private LinkedList<PolyvUploadInfo> data;
	private Context context;
	private LayoutInflater inflater;
	private PolyvUDBService service;
	private ViewHolder holder;

	private PolyvUploader uploader;

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
				btn = (Button) view.findViewById(R.id.upload);
				if (!btn.getText().equals("完成")) {
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
				btn = (Button) view.findViewById(R.id.upload);
				if (!btn.getText().equals("完成")) {
					Toast.makeText(context, (position + 1) + "上传成功", Toast.LENGTH_SHORT).show();
					btn.setText("完成");
				}
				break;

			case FAILURE:
				int errorType = msg.getData().getInt("error");
				btn = (Button) view.findViewById(R.id.upload);
				if (!btn.getText().equals("完成")) {
					btn.setText("开始");

					switch (errorType) {
					case PolyvUploader.FFILE:
						Toast.makeText(context, "第" + (position + 1) + "个任务文件不存在，或者大小为0", 0).show();
						break;
					case PolyvUploader.FVIDEO:
						Toast.makeText(context, "第" + (position + 1) + "个任务不是支持上传的视频格式", 0).show();
						break;
					case PolyvUploader.NETEXCEPTION:
						Toast.makeText(context, "第" + (position + 1) + "个任务网络异常，请重试", 0).show();
						break;
					case PolyvUploader.RECONNECT:
						Toast.makeText(context, "第" + (position + 1) + "个任务网络异常，正在等待重新连接", 0).show();
						break;
					case PolyvUploader.OUTTIME:
						Toast.makeText(context, "第" + (position + 1) + "个任务连接超时，请重试", 0).show();
						break;
					}

				}
				break;
			}
		};
	};

	public PolyvUploadListAdapter(Context context, LinkedList<PolyvUploadInfo> data, ListView listView) {
		this.context = context;
		this.data = data;
		this.inflater = LayoutInflater.from(context);
		this.service = new PolyvUDBService(context);
		this.listView = listView;
		initDownloaders();
	}

	private class MyUploadListener implements PolyvUploader.UploadListener {
		private int position;
		private PolyvUploadInfo info;

		public MyUploadListener(int position, PolyvUploadInfo info) {
			this.position = position;
			this.info = info;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		@Override
		public void fail(int category) {
			Message msg = handler.obtainMessage();
			msg.arg1 = position;
			msg.what = FAILURE;
			Bundle bundle = new Bundle();
			bundle.putInt("error", category);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}

		@Override
		public void upCount(long count, long total) {
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

		@Override
		public void success(long total, String vid) {
			Message msg = handler.obtainMessage();
			msg.arg1 = position;
			msg.what = SUCCESS;
			service.updatePercent(info, total, total);
			handler.sendMessage(msg);
		}
	}

	private void initDownloaders() {
		for (int i = 0; i < data.size(); i++) {
			final PolyvUploadInfo info = data.get(i);
			final int p = i;
			uploader = PolyvUploaderManager.getPolyvUploader(info.getFilepath(), info.getTitle(), info.getDesc());
			MyUploadListener uploadListener = new MyUploadListener(p, info);
			uploader.setUploadListener(uploadListener);
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
			convertView = inflater.inflate(R.layout.view_item_upload, null);
			holder = new ViewHolder();
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
			holder.tv_filesize = (TextView) convertView.findViewById(R.id.tv_filesize);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
			holder.btn_upload = (Button) convertView.findViewById(R.id.upload);
			holder.btn_delete = (Button) convertView.findViewById(R.id.delete);
			holder.tv_rate = (TextView) convertView.findViewById(R.id.rate);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		data = service.getUploadFiles();
		final PolyvUploadInfo info = data.get(position);
		String desc = info.getDesc();
		String title = info.getTitle();
		String filePath = info.getFilepath();
		long filesize = info.getFilesize();
		long percent = info.getPercent();
		long total = info.getTotal();
		holder.tv_title.setText(info.getTitle());
		holder.tv_desc.setText(desc);
		holder.tv_filesize.setText("" + filesize);
		holder.progressBar.setTag("" + position);
		holder.progressBar.setMax((int) total);
		holder.progressBar.setProgress((int) percent);
		if (total != 0)
			holder.tv_rate.setText("" + percent * 100 / total);
		else
			holder.tv_rate.setText("" + 0);
		if (total != 0 && total == percent) {
			holder.btn_upload.setText("完成");
		} else if (PolyvUploaderManager.getPolyvUploader(filePath, title, desc).isUploading())
			holder.btn_upload.setText("暂停");
		else
			holder.btn_upload.setText("开始");
		holder.btn_upload.setOnClickListener(new UploadListener(filePath, title, desc, convertView));
		holder.btn_delete.setOnClickListener(new DeleteListener(info, position));
		return convertView;
	}

	public void uploadAllFile() {
		PolyvUploaderManager.startAll();
	}

	public void uploadAllFile(Context context) {
		PolyvUploaderManager.startAll(context);
	}

	public void updateAllButton(boolean isStop) {
		for (int i = 0; i < listView.getChildCount(); i++) {
			Button down = (Button) listView.getChildAt(i).findViewById(R.id.upload);
			if (!down.getText().equals("完成")) {
				if (isStop)
					down.setText("暂停");
				else
					down.setText("开始");
			}
		}
	}

	private class ViewHolder {
		TextView tv_title, tv_desc, tv_filesize, tv_rate;
		ProgressBar progressBar;
		Button btn_upload, btn_delete;
	}

	class UploadListener implements View.OnClickListener {
		private final String filePath;
		private final String desc;
		private final String title;
		private View view;

		public UploadListener(String filePath, String title, String desc, View view) {
			this.filePath = filePath;
			this.title = title;
			this.desc = desc;
			this.view = view;
		}

		@Override
		public void onClick(View v) {
			Button upload = (Button) view.findViewById(R.id.upload);
			if (upload.getText().equals("开始")) {
				((Button) v).setText("暂停");
				PolyvUploader uploader = PolyvUploaderManager.getPolyvUploader(filePath, title, filePath);
				if (upload != null)
					uploader.start();
			} else if (upload.getText().equals("暂停")) {
				((Button) v).setText("开始");
				PolyvUploader uploader = PolyvUploaderManager.getPolyvUploader(filePath, title, filePath);
				if (uploader != null) {
					uploader.pause();
				}
			}
		}

	}

	class DeleteListener implements View.OnClickListener {
		private PolyvUploadInfo info;
		private int position;

		public DeleteListener(PolyvUploadInfo info, int position) {
			this.info = info;
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			PolyvUploader uploader = PolyvUploaderManager.getPolyvUploader(info.getFilepath(), info.getTitle(),
					info.getDesc());
			PolyvUploaderManager.removePolyvUpload(info.getFilepath());
			if (uploader != null) {
				uploader.pause();
			}
			service.deleteUploadFile(info);
			data.remove(position);
			initDownloaders();
			notifyDataSetChanged();
		}
	}

	public void stopAll() {
		PolyvUploaderManager.stopAll();
	}
}
