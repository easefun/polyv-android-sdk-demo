package com.easefun.polyvsdk.demo.upload;

import java.io.File;
import java.util.List;

import com.easefun.polyvsdk.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.RemoteViews;

/**
 * 上传Service
 *
 */
public class PolyvULNotificationService extends Service {
	// 交互实例
	private NotificationBinder binder = new NotificationBinder();
	// notification管理者
	private NotificationManager notificationManager;
	// 前台服务的id
	private int foreid;
	// 常量
	private static final int XOR = 1234321;

	// 绑定的监听器
	public BindListener listener;

	public interface BindListener {
		public void bindSuccess(PolyvULNotificationService uploadService);
	}

	// 绑定下载服务并开始服务
	public static ServiceConnection bindUploadService(Context context, final BindListener listener) {
		Intent intent = new Intent(context, PolyvULNotificationService.class);
		ServiceConnection serconn = null;
		context.bindService(intent, serconn = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				NotificationInteraction interaction = (NotificationInteraction) service;
				if (listener != null)
					listener.bindSuccess(interaction.getNotificationService());
			}
		}, Context.BIND_AUTO_CREATE);
		context.startService(intent);
		return serconn;
	}

	// 获取id
	public static int getId(String filePath) {
		return new File(filePath).hashCode();
	}

	public interface NotificationInteraction {
		// 获取Notification服务
		public PolyvULNotificationService getNotificationService();
	}

	// 交互类
	class NotificationBinder extends Binder implements NotificationInteraction {
		@Override
		public PolyvULNotificationService getNotificationService() {
			return PolyvULNotificationService.this;
		}
	}

	// 记录下载器的vid和bit，在用id获取不到下载器的时候，将用vid和bit获取下载器(并记录id下载器)开始下载
	private SparseIntArray id_progress = new SparseIntArray();
	// 每个id的builder,只会初始化一次，因为删除的时候notification也会删除
	private SparseArray<Notification.Builder> id_builder = new SparseArray<Notification.Builder>();
	private SparseArray<RemoteViews> id_remote = new SparseArray<RemoteViews>();
	private SparseArray<Notification> id_notifi = new SparseArray<Notification>();
	// 每个id的notification按钮的状态
	private SparseBooleanArray id_status = new SparseBooleanArray();
	private SparseArray<String> id_title = new SparseArray<String>();
	// 该集合用于记录id是否已经下载成功，当下载成功的时候，再次开始将不会再更新，直到删除(true:完成,false:删除)
	// private SparseBooleanArray id_isfinish = new SparseBooleanArray();
	private static final boolean status_start = true;
	private static final boolean status_pause = false;

	// 初始化并设置前台Notification
	public void updateStartNF(int id, String filePath, String title, String desc, int progress) {
		if (id_title.get(id) == null)
			id_title.put(id, title);
		id_progress.put(id, progress);
		// 先清除该id的临时notification
		notificationManager.cancel(id ^ XOR);
		// 初始化remoteview和notification.builder
		initRVAndNB(id, title);
		// 设置前台Notification
		setForeNotification(id, filePath, title, desc, progress);
	}

	// 更新前台Notification
	private void setForeNotification(int id, String filePath, String title, String desc, int progress) {
		PolyvIdUploaderManager.addIdUploader(filePath, title, desc);
		// 初始化开始的notification
		updateDownloadingNF(id, progress, true);
		if (foreid == 0) {
			// 最先开始的id设置为前台id
			foreid = id;
			startForeground(foreid, id_notifi.get(foreid));
		}
	}

	private static final String STATUS_UPLOADING = "上传中";
	private static final String STATUS_PAUSED = "已暂停";
	private static final String STATUS_ERROR = "上传出错";
	private static final String STATUS_FINISH = "上传完成";

	// 初始化remoteview和Notification.Builder
	private void initRVAndNB(int id, String title) {
		if (id_builder.get(id) == null) {
			// remoteViews
			RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
			remoteViews.setImageViewResource(R.id.custom_icon, R.drawable.polyv_logo);
			remoteViews.setTextViewText(R.id.tv_title, title);
			remoteViews.setProgressBar(R.id.pb_pro, 100, 0, false);
			remoteViews.setTextViewText(R.id.tv_rate, 0 + "%");
			Notification.Builder builder = new Notification.Builder(this);

			// pendingIntent
			Intent resultIntent = new Intent(this, PolyvUploadListActivity.class);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// 要在该Activity的xml中配置其父Activity
			stackBuilder.addParentStack(PolyvUploadListActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			// 注意requestCode不要和别的重复
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(13, PendingIntent.FLAG_UPDATE_CURRENT);

			builder.setContent(remoteViews).setSmallIcon(R.drawable.polyv_logo).setContentIntent(resultPendingIntent);
			id_remote.put(id, remoteViews);
			id_builder.put(id, builder);
		}
	}

	// 更新前台id
	// return 是否还要更新该id的notification
	private boolean updateForeId(int id, String status) {
		if (foreid == id)
			if (PolyvIdUploaderManager.getAllIdUploader().size() >= 1) {
				for (int id_other : PolyvIdUploaderManager.getAllIdUploader().keySet()) {
					foreid = id_other;
					startForeground(foreid, id_notifi.get(id_other));
					break;
				}
			} else {
				foreid = 0;
				// 先用别的id更新一次(因为stopForeground(true)的foreid会被销毁)，避免在进程和服务都关闭的情况下更新不了
				switch (status) {
				case STATUS_FINISH:
					finishNotification(id ^ XOR, true);
					stopForeground(true);
					return false;
				case STATUS_ERROR:
					errorNotification(id ^ XOR, true);
					stopForeground(true);
					return false;
				}
				stopForeground(true);
			}
		return true;
	}

	// 更新删除文件的Notification
	public void updateDeleteNF(int id) {
		// 移除id下载器,不存在的时候即已移除，此时还是要移除Notification
		PolyvIdUploaderManager.removeIdUploader(id);
		updateForeId(id, "");
		id_status.put(id, status_pause);
		notificationManager.cancel(id ^ XOR);
		notificationManager.cancel(id);
	}

	// 更新下载完成的Notification
	public void updateFinishNF(int id) {
		// 先清除该id的临时notification
		notificationManager.cancel(id ^ XOR);
		PolyvIdUploaderManager.removeIdUploader(id);
		if (updateForeId(id, STATUS_FINISH))
			finishNotification(id, false);
	}

	// 下载完成的Notification
	private void finishNotification(int id, boolean isXOR) {
		if (isXOR)
			id = id ^ XOR;
		id_status.put(id, status_pause);
		RemoteViews remoteViews = id_remote.get(id);
		remoteViews.setTextViewText(R.id.tv_status, STATUS_FINISH);
		remoteViews.setViewVisibility(R.id.pb_pro, View.GONE);
		remoteViews.setViewVisibility(R.id.tv_rate, View.GONE);
		Notification notifi = id_builder.get(id).setTicker("上传完成:" + id_title.get(id)).setContent(remoteViews).build();
		if (isXOR)
			notificationManager.notify(id ^ XOR, notifi);
		else
			notificationManager.notify(id, notifi);
	}

	// 更新下载出错的Notification
	public void updateErrorNF(int id, boolean isReconn) {
		// 移除id下载器,不存在的时候即已移除，此时不作操作
		if (PolyvIdUploaderManager.getIdUploader(id) == null) {
			return;
		} else {
			// 不是重连的是否才移除
			if (!isReconn)
				PolyvIdUploaderManager.removeIdUploader(id);
			if (updateForeId(id, STATUS_ERROR))
				errorNotification(id, false);
		}
	}

	// 下载错误的Notification
	private void errorNotification(int id, boolean isXOR) {
		if (isXOR)
			id = id ^ XOR;
		id_status.put(id, status_pause);
		RemoteViews remoteViews = id_remote.get(id);
		remoteViews.setTextViewText(R.id.tv_status, STATUS_ERROR);
		remoteViews.setViewVisibility(R.id.pb_pro, View.GONE);
		remoteViews.setViewVisibility(R.id.tv_rate, View.GONE);
		Notification notifi = id_builder.get(id).setTicker("上传出错:" + id_title.get(id)).setContent(remoteViews).build();
		if (isXOR)
			notificationManager.notify(id ^ XOR, notifi);
		else
			notificationManager.notify(id, notifi);
	}

	// 更新下载中的Notification(由于会存在断网重连的情况，故这里要重新获取)
	public void updateDownloadingNF(int id, int progress, boolean isstart) {
		// 可以作其他操作，使notification不用频繁更新。
		// 相同的progress和%5!=0不更新，避免多次重复更新
		if ((id_status.get(id, false) && foreid != 0) && (id_progress.get(id) == progress || progress % 5 != 0))
			return;
		if (PolyvIdUploaderManager.getIdUploader(id) == null)
			// 防止已暂停/删除/完成的时候会被更新
			return;
		id_status.put(id, status_start);
		// 记录进度在服务中，当进程结束时以获取到
		id_progress.put(id, progress);
		RemoteViews remoteViews = id_remote.get(id);
		remoteViews.setTextViewText(R.id.tv_status, STATUS_UPLOADING);
		remoteViews.setProgressBar(R.id.pb_pro, 100, progress, false);
		remoteViews.setTextViewText(R.id.tv_rate, progress + "%");
		remoteViews.setViewVisibility(R.id.pb_pro, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.tv_rate, View.VISIBLE);
		Notification notifi = null;
		if (isstart)
			notifi = id_builder.get(id).setTicker("开始上传:" + id_title.get(id)).setContent(remoteViews).build();
		else
			notifi = id_builder.get(id).setTicker(null).setContent(remoteViews).build();
		notifi.flags = Notification.FLAG_ONGOING_EVENT;
		// 只有正在下载的任务才可以作为前台服务
		id_notifi.put(id, notifi);
		notificationManager.notify(id, notifi);
	}

	// 更新暂停的Notification
	public void updatePauseNF(int id) {
		// 移除id下载器,不存在的时候即已移除，此时不作操作
		if (PolyvIdUploaderManager.getIdUploader(id) == null) {
			return;
		} else {
			PolyvIdUploaderManager.removeIdUploader(id);
			if (updateForeId(id, STATUS_PAUSED))
				pauseNotification(id);
		}
	}

	// 暂停的Notification
	private void pauseNotification(int id) {
		id_status.put(id, status_pause);
		RemoteViews remoteViews = id_remote.get(id);
		remoteViews.setTextViewText(R.id.tv_status, STATUS_PAUSED);
		Notification notifi = id_builder.get(id).setTicker(null).setContent(remoteViews).build();
		notificationManager.notify(id, notifi);
	}

	// 更新所有暂停任务的Notification
	public void updateAllPauseNF(List<PolyvUploadInfo> infos) {
		for (PolyvUploadInfo info : infos) {
			int id = getId(info.getFilepath());
			updatePauseNF(id);
		}
	}

	// 更新所有开始任务的Notification
	public void updateAllStartNF(List<PolyvUploadInfo> infos) {
		for (PolyvUploadInfo info : infos) {
			int id = getId(info.getFilepath());
			int progress = 0;
			if (info.getTotal() != 0)
				progress = (int) (info.getPercent() * 100 / info.getTotal());
			updateStartNF(id, info.getFilepath(), info.getTitle(), info.getDesc(), progress);
		}
	}

	// 更新未完成任务的Notification
	public void updateUnfinishedNF(List<PolyvUploadInfo> infos, List<String> finishKey) {
		for (PolyvUploadInfo info : infos) {
			boolean flag = false;
			for (String key : finishKey) {
				if (key.equals(info.getFilepath())) {
					flag = true;
					break;
				}
			}
			if (flag)
				continue;
			int id = getId(info.getFilepath());
			int progress = 0;
			if (info.getTotal() != 0)
				progress = (int) (info.getPercent() * 100 / info.getTotal());
			updateStartNF(id, info.getFilepath(), info.getTitle(), info.getDesc(), progress);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 初始化
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
