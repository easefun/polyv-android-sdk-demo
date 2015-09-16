package com.easefun.polyvsdk.demo;

import java.util.List;

import com.easefun.polyvsdk.PolyvDownloader;
import com.easefun.polyvsdk.PolyvDownloaderManager;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.RestVO;
import com.easefun.polyvsdk.Video;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class VideoAdapter extends BaseAdapter {
	private List<RestVO> videos;
	private Context context;
	private LayoutInflater inflater;
	private ViewHolder holder;
	private DisplayImageOptions options;
	private DBservice service;

	public VideoAdapter(Context context, List<RestVO> videos) {
		this.context = context;
		this.videos = videos;
		this.inflater = LayoutInflater.from(context);
		this.service = new DBservice(context);

		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.bg_loading) // 设置图片在下载期间显示的图片
				.showImageForEmptyUri(R.drawable.bg_loading)// 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.bg_loading) // 设置图片加载/解码过程中错误时候显示的图片
				.bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型//
				.cacheInMemory(true)// 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true)// 设置下载的图片是否缓存在SD卡中
				.displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();// 构建完成
	}

	@Override
	public int getCount() {
		return videos.size();
	}

	@Override
	public Object getItem(int position) {
		return videos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup group) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.view_video, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.imageview);
			holder.video_title = (TextView) convertView.findViewById(R.id.video_title);
			holder.video_duration = (TextView) convertView.findViewById(R.id.video_duration);
			holder.btn_download = (Button) convertView.findViewById(R.id.btn_download);
			holder.btn_play = (Button) convertView.findViewById(R.id.btn_play);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		RestVO restVO = videos.get(position);
		holder.video_title.setText(restVO.getTitle());
		holder.video_duration.setText(restVO.getDuration());
		holder.btn_download.setOnClickListener(new DownloadListener(restVO.getVid(), restVO.getTitle()));
		holder.btn_play.setOnClickListener(new PlayListener(restVO.getVid()));
		ImageLoader imageloader = ImageLoader.getInstance();
		imageloader.displayImage(restVO.getFirstImage(), holder.image, options, new AnimateFirstDisplayListener());
		return convertView;
	}

	class ViewHolder {
		ImageView image;
		TextView video_title;
		TextView video_duration;
		Button btn_play, btn_download;
	}

	class PlayListener implements View.OnClickListener {
		private String vid;

		public PlayListener(String vid) {
			this.vid = vid;
		}

		@Override
		public void onClick(View arg0) {
			Intent playUrl = new Intent(context, IjkVideoActicity.class);
			VideoListActivity activity = (VideoListActivity) context;
			playUrl.putExtra("vid", vid);
			activity.startActivityForResult(playUrl, 1);
		}
	}

	class DownloadListener implements View.OnClickListener {
		private String vid;
		private String title;

		public DownloadListener(String vid, String title) {
			this.vid = vid;
			this.title = title;
		}

		@Override
		public void onClick(View v) {
			// new VideoInfo().execute(vid);

			Video.loadVideo(vid, new Video.OnVideoLoaded() {
				public void onloaded(final Video v) {
					if (v == null) {
						return;
					}
					// 码率数
					int df_num = v.getDfNum();
					String[] items = null;
					if (df_num == 1) {
						items = new String[] { "流畅" };
					}
					if (df_num == 2) {
						items = new String[] { "流畅", "高清" };
					}
					if (df_num == 3) {
						items = new String[] { "流畅", "高清", "超清" };
					}

					// 数字2代表的是数组的下标
					final Builder selectDialog = new AlertDialog.Builder(context).setTitle("选择下载码率")
							.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int bitrate = which + 1;

							DownloadInfo downloadInfo = new DownloadInfo(vid, v.getDuration(), v.getFilesize(bitrate), bitrate);
							downloadInfo.setTitle(title);
							Log.i("videoAdapter", downloadInfo.toString());
							if (service != null && !service.isAdd(downloadInfo)) {
								service.addDownloadFile(downloadInfo);
								PolyvDownloader polyvDownloader = PolyvDownloaderManager.getPolyvDownloader(vid, bitrate);
								polyvDownloader.start();
							} else {
								((Activity) context).runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										// Auto-generated
										// method stub
										Toast.makeText(context, "下载任务已经增加到队列", 1).show();
									}
									
								});
							}
							
							dialog.dismiss();
						}
					});
					
					selectDialog.show().setCanceledOnTouchOutside(true);
				}
			});
		}
	}

	/*
	 * class VideoInfo extends AsyncTask<String,String,Video>{
	 * 
	 * @Override protected Video doInBackground(String... params) { // TODO
	 * Auto-generated method stub //Video.loadVideo(vid, listener); JSONArray
	 * jsonArray = SDKUtil.loadVideoInfo(params[0]);
	 * 
	 * try {
	 * 
	 * JSONObject jsonObject =jsonArray.getJSONObject(0); final String vid =
	 * jsonObject.getString("vid"); final String duration =
	 * jsonObject.getString("duration"); final int filesize =
	 * jsonObject.getInt("filesize1");
	 * 
	 * 
	 * 
	 * 
	 * 
	 * } catch (JSONException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * 
	 * return null; }
	 * 
	 * protected void onPostExecute(Video result) {
	 * 
	 * 
	 * super.onPostExecute(result);
	 * 
	 * } }
	 */
}
