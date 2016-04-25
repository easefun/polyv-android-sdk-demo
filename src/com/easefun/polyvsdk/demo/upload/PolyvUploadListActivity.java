package com.easefun.polyvsdk.demo.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.upload.PolyvMTUploadVideo;
import com.easefun.polyvsdk.upload.PolyvMTUploadVideo.UploadListener;
import com.easefun.polyvsdk.upload.PolyvMThreadUploadManager;
import com.easefun.polyvsdk.upload.PolyvUDBService;
import com.easefun.polyvsdk.upload.PolyvUploadInfo;
import com.easefun.polyvsdk.util.ByteTool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PolyvUploadListActivity extends Activity implements OnClickListener {
	private boolean isStop = false;
	private boolean finTag = false;
	private boolean sendBroadcast = false;
	private static int count;
	private Button bt_select, bt_uploadall, bt_query;
	private String filePath;
	private LayoutParams params;
	private LayoutParams params1;
	private LayoutParams params2;
	private LayoutParams params3;
	private LinearLayout ll_downlist;
	private LinkedList<ViewGroup> rlitem;
	private PolyvUDBService service;
	private LinkedList<PolyvUploadInfo> infos;
	// 上传状态
	private static final int REFRESH_PROGRESS = 1;
	private static final int SUCCESS = 2;
	private static final int FAILURE = 3;
	private static final int PAUSE = 4;
	private static final int START = 5;
	private Boolean status;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			int p = (Integer) msg.obj;
			RelativeLayout rl = (RelativeLayout) rlitem.get(p);
			ProgressBar pro_pro = null;
			TextView tv_rate = null;
			Button bt_down = null;
			TextView tv_status = null;
			switch (msg.what) {
			case REFRESH_PROGRESS:
				long count = msg.getData().getLong("count");
				long total = msg.getData().getLong("total");
				pro_pro = (ProgressBar) rl.findViewById(4);
				tv_rate = (TextView) rl.findViewById(7);
				tv_status = (TextView) rl.findViewById(9);
				bt_down = (Button) rl.findViewById(6);

				pro_pro.setMax((int) total);
				pro_pro.setProgress((int) count);
				if (total != 0)
					tv_rate.setText("进度:" + count * 100 / total + "%");
				if (!bt_down.getText().equals("完成")) {
					tv_status.setText("第" + (p + 1) + "个任务正在" + "上传中...");
					bt_down.setText("暂停");
				}
				break;
			case PAUSE:
				bt_down = (Button) rl.findViewById(6);
				tv_status = (TextView) rl.findViewById(9);
				status = msg.getData().getBoolean("status");
				if (!bt_down.getText().equals("完成")) {
					if (status == true) {
						bt_down.setText("开始");
						tv_status.setText("第" + (p + 1) + "个任务已" + "暂停");
					} else {
						bt_down.setText("暂停中");
						tv_status.setText("第" + (p + 1) + "个任务正在" + "暂停中...");
					}
				}
				break;
			case START:
				bt_down = (Button) rl.findViewById(6);
				tv_status = (TextView) rl.findViewById(9);
				boolean startStatus = msg.getData().getBoolean("startStatus");
				if (!bt_down.getText().equals("完成")) {
					if (startStatus == false) {
						// bt_down.setText("初始化中");
						tv_status.setText("正在初始化数据...");
					} else {
						bt_down.setText("暂停");
						tv_status.setText("第" + (p + 1) + "个任务正在" + "上传中...");
					}
				}
				break;

			case SUCCESS:
				bt_down = (Button) rl.findViewById(6);
				tv_status = (TextView) rl.findViewById(9);
				if (!bt_down.getText().equals("完成"))
					Toast.makeText(PolyvUploadListActivity.this, (p + 1) + "上传成功", 0).show();
				bt_down.setText("完成");
				bt_down.setEnabled(false);
				tv_status.setText("上传完成");
				break;

			case FAILURE:
				int error = msg.getData().getInt("error");
				bt_down = (Button) rl.findViewById(6);
				tv_status = (TextView) rl.findViewById(9);
				if (!bt_down.getText().equals("完成")) {
					bt_down.setText("开始");
					tv_status.setText("第" + (p + 1) + "个任务已" + "暂停");

					switch (error) {
					case PolyvMTUploadVideo.FFILE:
						Toast.makeText(PolyvUploadListActivity.this, (p + 1) + "文件不存在，或者大小为0", 0).show();
						break;
					case PolyvMTUploadVideo.FVIDEO:
						Toast.makeText(PolyvUploadListActivity.this, (p + 1) + "不是支持上传的视频格式", 0).show();
						break;
					case PolyvMTUploadVideo.NETEXCEPTION:
						Toast.makeText(PolyvUploadListActivity.this, (p + 1) + "网络异常，请重试", 0).show();
						break;
					case PolyvMTUploadVideo.RECONNECT:
						Toast.makeText(PolyvUploadListActivity.this, (p + 1) + "网络异常，正在等待重新连接", 0).show();
						break;
					case PolyvMTUploadVideo.OUTTIME:
						Toast.makeText(PolyvUploadListActivity.this, (p + 1) + "连接超时，请重试", 0).show();
						break;
					}
				}
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_uploadlist);

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.startsWith("video/")) {
				handleSendVideo(intent);
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("video/") || type.startsWith("*/")) {
				handleSendMultiVideo(intent);
			}
		}

		initView();
		initViewListener();
	}

	// 获取视频的地址并添加到上传列表中
	private void handle(Uri... uris) {
		if (service == null)
			service = new PolyvUDBService(this);
		for (int i = 0; i < uris.length; i++) {
			// 在图册中上传
			if (uris[i].toString().startsWith("content")) {
				String[] filePathColumn = { MediaStore.Video.Media.DATA };
				Cursor cursor = getContentResolver().query(uris[i], filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				filePath = cursor.getString(columnIndex);
				cursor.close();
			} else {
				// 在文件中选择
				filePath = uris[i].getPath().substring(uris[i].getPath().indexOf("/") + 1);
			}
			count++;
			File file = new File(filePath);
			String fileName=file.getName();
			PolyvUploadInfo uploadInfo = new PolyvUploadInfo(filePath,fileName.substring(0,fileName.lastIndexOf(".")), "测试" + count);
			uploadInfo.setFilesize(file.length());
			uploadInfo.setFilepath(filePath);
			if (service != null && !service.isAdd(uploadInfo)) {
				service.addDownloadFile(uploadInfo);
				PolyvMThreadUploadManager.getPolyvUploader(filePath, fileName.substring(0,fileName.lastIndexOf(".")) , "测试" + count);

			} else {
				((Activity) PolyvUploadListActivity.this).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(PolyvUploadListActivity.this, "上传任务已经增加到队列", 0).show();
					}
				});
			}
		}
	}

	private void handleSendVideo(Intent intent) {
		Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (uri != null) {
			handle(uri);
		}
	}

	private void handleSendMultiVideo(Intent intent) {
		ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (uris != null) {
			Uri[] uriarr = new Uri[uris.size()];
			handle(uris.toArray(uriarr));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		rlitem.clear();
		ll_downlist.removeAllViews();
		initData();
	}

	// 初始化view
	private void initView() {
		rlitem = new LinkedList<ViewGroup>();
		bt_select = (Button) findViewById(R.id.bt_select);
		bt_uploadall = (Button) findViewById(R.id.upload_all);
		ll_downlist = (LinearLayout) findViewById(R.id.ll_uplist);
		// bt_query = (Button) findViewById(R.id.bt_query);
		params = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
		params1 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params3 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	// 初始化view的事件
	private void initViewListener() {
		bt_select.setOnClickListener(this);
		// bt_query.setOnClickListener(this);
		bt_uploadall.setOnClickListener(this);
	}

	// 初始化数据
	private void initData() {
		service = new PolyvUDBService(this);
		infos = service.getDownloadFiles();
		if (infos.size() == 0) {
			if (ll_downlist.getChildCount() == 0) {
				TextView tv_empty = new TextView(this);
				tv_empty.setText("暂无上传任务");
				LayoutParams lp = new LayoutParams(params3);
				tv_empty.setLayoutParams(lp);
				tv_empty.setGravity(android.view.Gravity.CENTER);
				ll_downlist.addView(tv_empty);
			}
		} else {
			for (int i = 0; i < infos.size(); i++) {
				// 获取上传信息
				final PolyvUploadInfo info = infos.get(i);
				final int p = i;
				// 设置监听
				final PolyvMTUploadVideo mtuv = PolyvMThreadUploadManager.getPolyvUploader(info.getFilepath(), info.getTitle(),
						info.getDesc());
				mtuv.setUploadListener(new UploadListener() {

					@Override
					public void success(long total, String vid) {
						Message msg = handler.obtainMessage();
						msg.obj = p;
						msg.what = SUCCESS;
						service.updatePercent(info, total, total, 1);
						handler.sendMessage(msg);
					}

					@Override
					public void pause(boolean status) {
						Message msg = handler.obtainMessage();
						msg.obj = p;
						msg.what = PAUSE;
						Bundle bundle = new Bundle();
						bundle.putBoolean("status", status);
						msg.setData(bundle);
						handler.sendMessage(msg);
					}

					@Override
					public void fail(int category) {
						Message msg = handler.obtainMessage();
						msg.obj = p;
						msg.what = FAILURE;
						Bundle bundle = new Bundle();
						bundle.putInt("error", category);
						msg.setData(bundle);
						handler.sendMessage(msg);
					}

					@Override
					public void upCount(long count, long total) {
						Message msg = handler.obtainMessage();
						msg.obj = p;
						Bundle bundle = new Bundle();
						bundle.putLong("count", count);
						bundle.putLong("total", total);
						msg.setData(bundle);
						msg.what = REFRESH_PROGRESS;
						service.updatePercent(info, count, total, 0);
						handler.sendMessage(msg);
					}

					@Override
					public void start(boolean status) {
						Message msg = handler.obtainMessage();
						msg.what = START;
						msg.obj = p;
						Bundle bundle = new Bundle();
						bundle.putBoolean("startStatus", status);
						msg.setData(bundle);
						handler.sendMessage(msg);
					}
				});

				// 设置view
				RelativeLayout rl_downlist = new RelativeLayout(this);
				TextView tv_title = new TextView(this);
				tv_title.setId(1);
				TextView tv_duration = new TextView(this);
				tv_duration.setId(2);
				TextView tv_filesize = new TextView(this);
				tv_filesize.setId(3);
				ProgressBar pro_pro = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
				pro_pro.setId(4);
				TextView tv_rate = new TextView(this);
				tv_rate.setId(7);
				Button bt_del = new Button(this);
				bt_del.setId(5);
				Button bt_down = new Button(this);
				bt_down.setId(6);
				TextView tv_status = new TextView(this);
				tv_status.setId(9);
				View v = new View(this);
				v.setId(8);

				RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(params1);
				tv_title.setLayoutParams(lp1);
				tv_title.setText(info.getTitle());
				tv_title.setTextColor(Color.BLACK);
				rl_downlist.addView(tv_title);

				RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(params1);
				lp2.addRule(RelativeLayout.ALIGN_LEFT, 1);
				lp2.addRule(RelativeLayout.BELOW, 1);
				tv_duration.setLayoutParams(lp2);
				tv_duration.setText("描述:" + info.getDesc());
				tv_duration.setTextColor(Color.BLACK);
				rl_downlist.addView(tv_duration);

				RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(params1);
				lp3.addRule(RelativeLayout.RIGHT_OF, 2);
				lp3.addRule(RelativeLayout.BELOW, 1);
				tv_filesize.setLayoutParams(lp3);
				ByteTool.setByte(info.getFilesize());
				tv_filesize.setText("  大小:" + ByteTool.getMB() + "M");
				tv_filesize.setTextColor(Color.BLACK);
				rl_downlist.addView(tv_filesize);

				RelativeLayout.LayoutParams lp4 = new RelativeLayout.LayoutParams(params2);
				lp4.addRule(RelativeLayout.BELOW, 3);
				pro_pro.setLayoutParams(lp4);
				pro_pro.setMax((int) info.getTotal());
				if (info.getIsuping() == 1)
					pro_pro.setProgress((int) info.getTotal());
				else
					pro_pro.setProgress((int) info.getPercent());
				rl_downlist.addView(pro_pro);

				RelativeLayout.LayoutParams lp5 = new RelativeLayout.LayoutParams(params1);
				lp5.addRule(RelativeLayout.BELOW, 1);
				lp5.addRule(RelativeLayout.ALIGN_RIGHT, 4);
				tv_rate.setLayoutParams(lp5);
				tv_rate.setTextColor(Color.BLACK);
				if (info.getTotal() == 0)
					tv_rate.setText("进度:" + 0 + "%");
				else
					tv_rate.setText("进度:" + info.getPercent() * 100 / info.getTotal() + "%");
				rl_downlist.addView(tv_rate);

				RelativeLayout.LayoutParams lp6 = new RelativeLayout.LayoutParams(params1);
				lp6.addRule(RelativeLayout.BELOW, 4);
				lp6.addRule(RelativeLayout.LEFT_OF, 6);
				bt_del.setLayoutParams(lp6);
				bt_del.setTextColor(Color.BLACK);
				bt_del.setText("删除");
				rl_downlist.addView(bt_del);

				RelativeLayout.LayoutParams lp7 = new RelativeLayout.LayoutParams(params1);
				lp7.addRule(RelativeLayout.BELOW, 4);
				lp7.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				bt_down.setLayoutParams(lp7);
				bt_down.setTextColor(Color.BLACK);
				if (info.getTotal() != 0 && info.getPercent() == info.getTotal()) {
					bt_down.setText("完成");
					bt_down.setEnabled(false);
				} else
					bt_down.setText("开始");
				rl_downlist.addView(bt_down);

				RelativeLayout.LayoutParams lp8 = new RelativeLayout.LayoutParams(params);
				lp8.addRule(RelativeLayout.BELOW, 6);
				v.setLayoutParams(lp8);
				v.setBackgroundColor(Color.BLACK);
				rl_downlist.addView(v);

				RelativeLayout.LayoutParams lp9 = new RelativeLayout.LayoutParams(params1);
				lp9.addRule(RelativeLayout.ABOVE, 8);
				tv_status.setLayoutParams(lp9);
				tv_status.setBackgroundColor(Color.BLACK);
				tv_status.setTextColor(Color.WHITE);
				if (info.getTotal() != 0 && info.getPercent() == info.getTotal())
					tv_status.setText("上传完成");
				else
					tv_status.setText("第" + (p + 1) + "个任务已" + "暂停");
				rl_downlist.addView(tv_status);

				bt_del.setOnClickListener(new DelClickListener(i));
				bt_down.setOnClickListener(new StartClickListener(i));
				ll_downlist.addView(rl_downlist);
				rlitem.addLast(rl_downlist);
			}
		}
	}

	class StartClickListener implements View.OnClickListener {
		private int position;

		public StartClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			PolyvUploadInfo info = null;
			if (position < infos.size()) {
				info = infos.get(position);
				final PolyvMTUploadVideo mtuv = PolyvMThreadUploadManager.getPolyvUploader(info.getFilepath(), info.getTitle(),
						info.getDesc());
				// 先获取view
				RelativeLayout rl = (RelativeLayout) rlitem.get(position);
				Button bt_down = (Button) rl.findViewById(6);
				if (bt_down.getText().equals("开始")) {
					mtuv.start();
				} else if (bt_down.getText().equals("暂停")) {
					mtuv.pause();
				}
			} else {
				Toast.makeText(PolyvUploadListActivity.this, "操作失败，请重试", 0).show();
				PolyvUploadListActivity.this.finish();
			}
		}

	}

	class DelClickListener implements View.OnClickListener {
		private int position;

		public DelClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			Builder dialog = new AlertDialog.Builder(PolyvUploadListActivity.this);
			dialog.setTitle("提示").setMessage("确认删除?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PolyvUploadInfo info = null;
					if (position < infos.size()) {
						info = infos.get(position);
						service.deleteDownloadFile(info);
						PolyvMTUploadVideo mtut = PolyvMThreadUploadManager.getPolyvUploader(info.getFilepath(),
								info.getTitle(), info.getDesc());
						if (mtut != null) {
//							mtut.stop();
							PolyvMThreadUploadManager.removePolyvDownload(info.getFilepath());
						}
						// 先移除view
						RelativeLayout rl = (RelativeLayout) rlitem.get(position);
						ll_downlist.removeView(rl);
						if (ll_downlist.getChildCount() == 0) {
							TextView tv_empty = new TextView(PolyvUploadListActivity.this);
							tv_empty.setText("暂无上传任务");
							LayoutParams lp = new LayoutParams(params3);
							tv_empty.setLayoutParams(lp);
							tv_empty.setGravity(android.view.Gravity.CENTER);
							ll_downlist.addView(tv_empty);
						}
					} else {
						Toast.makeText(PolyvUploadListActivity.this, "操作失败，请重试", 0).show();
						PolyvUploadListActivity.this.finish();
					}
				}
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			dialog.show();

		}

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.upload_all:
			if (infos.size() == 0)
				return;
			infos = service.getDownloadFiles();
			for (int i = 0; i < infos.size(); i++) {
				PolyvUploadInfo uploadInfo = infos.get(i);
				int suping = uploadInfo.getIsuping();
				if (suping == 0) {
					finTag = true;
				}
			}
			if (finTag == false) {
				return;
			}
			finTag = false;
			if (!isStop) {
				PolyvMThreadUploadManager.startAll();
				((Button) v).setText("暂停全部");
				isStop = !isStop;
			} else {
				PolyvMThreadUploadManager.stopAll();
				((Button) v).setText("开始全部");
				isStop = !isStop;
			}
			break;
		// case R.id.bt_query:
		// infos = service.getDownloadFiles();
		// break;
		case R.id.bt_select:
			if (sendBroadcast == false) {
				// 发送sd卡就绪广播
//				Intent intent1 = new Intent();
//				intent1.setAction(Intent.ACTION_MEDIA_MOUNTED);
//				intent1.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
//				sendBroadcast(intent1);
				
				// 4.4限制系统使用sd卡，才可获取发送sd卡的广播。
				//4.4及以后的系统是不允许发送上面的广播，原因是你可能只增加可一个文件，然后就进行全盘扫描，这样很耗电，因此只有系统才能发送这个广播
				Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.parse("file://" + Environment.getExternalStorageDirectory()));
				sendBroadcast(intent1);
				sendBroadcast = true;
			}
			// 打开图册，选择MP4的视频
			Intent intent = new Intent(Intent.ACTION_PICK, null);
			intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/mp4");
			startActivityForResult(intent, 1);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK && data != null) {
				// 获取文件路径
				Uri uri = data.getData();
				handle(uri);
			} else {
				Toast.makeText(PolyvUploadListActivity.this, "视频获取失败", 0).show();
			}
			break;
		}
	}
}
