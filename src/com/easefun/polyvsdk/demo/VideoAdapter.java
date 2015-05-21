package com.easefun.polyvsdk.demo;
 
import java.util.ArrayList;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.SDKUtil;
import com.easefun.polyvsdk.Video;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
    private ArrayList<Video> videos;
    private Context context;
    private LayoutInflater inflater;
    private ViewHolder holder;
    private DisplayImageOptions options;
    private DBservice service;
    public VideoAdapter(Context context,ArrayList<Video> videos){
    	this.context=context;
    	this.videos=videos;
    	this.inflater=LayoutInflater.from(context);
    	this.service=new DBservice(context);

    	options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.bg_loading) // 设置图片在下载期间显示的图片
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
		// TODO Auto-generated method stub
		return videos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return videos.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup group) {
		// TODO Auto-generated method stub
		if(convertView==null){
			convertView=inflater.inflate(R.layout.view_video, null);
			holder=new ViewHolder();
			holder.image=(ImageView) convertView.findViewById(R.id.imageview);
			holder.video_title=(TextView)convertView.findViewById(R.id.video_title);
			holder.video_duration=(TextView)convertView.findViewById(R.id.video_duration);
			holder.btn_download=(Button)convertView.findViewById(R.id.btn_download);
			holder.btn_play=(Button)convertView.findViewById(R.id.btn_play);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
		holder.video_title.setText(videos.get(position).getTitle());
		holder.video_duration.setText(videos.get(position).getDuration());
		holder.btn_download.setOnClickListener(new DownloadListener(videos.get(position).getVid()));
		holder.btn_play.setOnClickListener(new PlayListener());
		ImageLoader imageloader = ImageLoader.getInstance();
		imageloader.displayImage(videos.get(position).getFirst_image(),holder.image ,options, new AnimateFirstDisplayListener());
		return convertView;
	}
    
	class ViewHolder{
		ImageView image;
		TextView video_title;
		TextView video_duration;
		Button btn_play,btn_download;
	}
	
	class PlayListener implements View.OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	class DownloadListener implements View.OnClickListener{
        private String vid;
        public DownloadListener(String vid){
        	this.vid=vid;
        }
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			new VideoInfo().execute(vid);
		}
		
	}
	
	class VideoInfo extends AsyncTask<String,String,String>{
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			DownloadInfo downloadInfo=null;
			JSONArray jsonArray = SDKUtil.loadVideoInfo(params[0]);
			String vid = null;
			String duration = null;
			int filesize = 0;
			try {
				JSONObject jsonObject =jsonArray.getJSONObject(0);
				vid = jsonObject.getString("vid");
				duration = jsonObject.getString("duration");
				filesize = jsonObject.getInt("filesize1");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 downloadInfo = new DownloadInfo(vid, duration, filesize);
			 Log.i("videoAdapter", downloadInfo.toString());
			 if(service!=null&&!service.isAdd(downloadInfo)){
				 service.addDownloadFile(downloadInfo);
			 }else{
				 ((Activity) context).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						 Toast.makeText(context, "this video has been added !!", 1).show();
					}
				});
				
			 }
			return null;
		}
	}
}
