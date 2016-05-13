package com.easefun.polyvsdk.demo;

import java.io.File;

import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.SDKUtil;
import com.easefun.polyvsdk.Video;
import com.easefun.polyvsdk.demo.AnimateFirstDisplayListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

/**
 * 预览图视图
 * @author TanQu 2016-3-3
 */
public class PolyvPlayerFirstStartView extends RelativeLayout {
	private static final String TAG = PolyvPlayerFirstStartView.class.getSimpleName();
	private Context mContext = null;
	private PopupWindow popupWindow = null;
	private ImageView mFirstStartImage = null;
	private ImageButton mFirstStartBtn = null;
	private Callback mCallback = null;
	private DisplayImageOptions mOptions = null;
	private View mAnchorView = null;
	private String mVid = "";

    public PolyvPlayerFirstStartView(Context context) {
        super(context);
        mContext = context;
        initViews();
    }
    
    public PolyvPlayerFirstStartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }
    
    public PolyvPlayerFirstStartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initViews();
    }
    
    private void initViews() {
    	LayoutInflater.from(getContext()).inflate(R.layout.polyv_player_first_start_view, this);
    	mFirstStartImage = (ImageView) findViewById(R.id.first_start_image);
    	mFirstStartBtn = (ImageButton) findViewById(R.id.first_start_btn);
    	mFirstStartBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.onClickStart();
				}
			}
		});
    	
    	if (mOptions == null) {
    		mOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.bg_loading) // 设置图片在下载期间显示的图片
    				.showImageForEmptyUri(R.drawable.bg_loading)// 设置图片Uri为空或是错误的时候显示的图片
    				.showImageOnFail(R.drawable.bg_loading) // 设置图片加载/解码过程中错误时候显示的图片
    				.bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
    				.cacheInMemory(true)// 设置下载的图片是否缓存在内存中
    				.cacheOnDisk(true)// 设置下载的图片是否缓存在SD卡中
    				.displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
    				.imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();// 构建完成
		}
    	
    	if (popupWindow == null) {
    		popupWindow = new PopupWindow(mContext);
    		popupWindow.setContentView(this);
    	}
    }
    
    /**
     * 设置图片并显示
     * @param anchorView
     * @param vid
     */
    public void show(View anchorView, String vid) {
    	mAnchorView = anchorView;
    	mVid = vid;
    	refresh();
    }
    
    /**
	 * 重新设置控件
	 */
	public void refresh() {
		int[] location = new int[2];
		mAnchorView.getLocationInWindow(location);
		Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchorView.getWidth(), location[1] + mAnchorView.getHeight());
		popupWindow.setWidth(mAnchorView.getWidth());
		popupWindow.setHeight(mAnchorView.getHeight());
		popupWindow.showAtLocation(mAnchorView, Gravity.NO_GRAVITY, 0, anchorRect.top);
		Video.loadVideo(mVid, new Video.OnVideoLoaded() {
			
			@Override
			public void onloaded(Video v) {
				if (v == null) return;
				File dir = PolyvSDKClient.getInstance().getVideoDownloadExtraResourceDir(mVid);
				String fileName = v.getFirstImage().substring(v.getFirstImage().lastIndexOf("/"));
				File file = new File(dir, fileName);
				if (file.exists()) {
					//TODO 在本地已经有图片的情况下，应该设置使用本地路径的图片
//					ImageLoader.getInstance().displayImage(file.getAbsolutePath(), mFirstStartImage, mOptions, new AnimateFirstDisplayListener());
					Log.d(TAG, "有本地图片，请完善本地图片显示逻辑");
				} else {
					ImageLoader.getInstance().displayImage(v.getFirstImage(), mFirstStartImage, mOptions, new AnimateFirstDisplayListener());
				}
			}
		});
	}
    
	/**
	 * 是否在显示中
	 * @return
	 */
	public boolean isShowing() {
		return popupWindow.isShowing();
	}
	
    /**
     * 隐藏
     */
    public void hide() {
    	Drawable drawable = mFirstStartImage.getDrawable();
    	if (drawable != null && drawable instanceof BitmapDrawable) {
    		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
    		if (bitmap != null && bitmap.isRecycled() == false) {
    			bitmap.recycle();
    			bitmap = null;
    		}
    	}
    	
    	mFirstStartImage.setImageBitmap(null);
    	popupWindow.dismiss();
    	System.gc();
    }
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	super.onTouchEvent(event);
		return true;
	}
    
    public void setCallback(Callback callback) {
    	mCallback = callback;
    }
    
    public interface Callback {
    	public void onClickStart();
    }
}
