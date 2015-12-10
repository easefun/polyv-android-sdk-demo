package com.easefun.polyvsdk.demo;

import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.easefun.polyvsdk.BitRateEnum;
import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.ijk.IjkBaseMediaController;
import com.easefun.polyvsdk.ijk.IjkVideoView;

public class MediaController extends IjkBaseMediaController {
	private static final String TAG = "MediaController";
	private static final int URL_LOADER = 0;
	private MediaPlayerControl mPlayer;
	private Context mContext;
	private PopupWindow mWindow;
	private int mAnimStyle;
	private View mAnchor;
	private View mRoot;
	private ProgressBar mProgress;
	private TextView mEndTime, mCurrentTime;
	private IjkVideoView ijkVideoView;

	private String mTitle;
	private long mDuration;
	private boolean mShowing;
	private boolean mDragging;
	private boolean mInstantSeeking = true;
	private static final int sDefaultTimeout = 3000;
	private static final int FADE_OUT = 1;
	private static final int SHOW_PROGRESS = 2;
	private boolean mFromXml = false;
	private ImageButton mPauseButton;
	private ImageButton mFfwdButton;
	private ImageButton mRewButton;
	private ImageButton mNextButton;
	private ImageButton mPreButton;
	private AudioManager mAM;
	private boolean isUsePreNext = false;
	private OnBoardChangeListener onBoardChangeListener;
	private OnVideoChangeListener onVideoChangeListener;
	// private ImageButton btn_back;
	private ImageButton btn_boardChange;
	private ImageButton btn_videoChange;
	private Button selectBitrate = null;
	private LinearLayout bitrateLinearLayout = null;
	private SparseArray<Button> bitRateBtnArray = null;
	private OnPreNextListener onPreNextListener;

	public MediaController(Context context, AttributeSet attrs) {
		super(context, attrs);
		mRoot = this;
		mFromXml = true;
		initController(context);
	}

	/**
	 * 当你不需要实现上一集下一集按钮时，设置isUsePreNext 为false，需要时设为true
	 * 并实现setPreNextListener()方法
	 * 
	 * @param context
	 * @param isUsePreNext
	 * @param isUseReffwd
	 */
	public MediaController(Context context, boolean isUsePreNext) {
		super(context);
		if (!mFromXml && initController(context))
			initFloatingWindow();
		this.isUsePreNext = isUsePreNext;
	}

	private boolean initController(Context context) {
		mContext = context;
		mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		return true;
	}

	@Override
	public void onFinishInflate() {
		if (mRoot != null)
			initControllerView(mRoot);
	}

	private void initFloatingWindow() {
		mWindow = new PopupWindow(mContext);
		mWindow.setFocusable(false);
		mWindow.setTouchable(true);
		mWindow.setBackgroundDrawable(null);
		mWindow.setOutsideTouchable(true);
		mAnimStyle = android.R.style.Animation;
	}

	/**
	 * Set the view that acts as the anchor for the control view. This can for
	 * example be a VideoView, or your Activity's main view.
	 * 
	 * @param view
	 *            The view to which to anchor the controller when it is visible.
	 */
	@Override
	public void setAnchorView(View view) {
		mAnchor = view;
		if (!mFromXml) {
			removeAllViews();
			mRoot = makeControllerView();
			mWindow.setContentView(mRoot);
			mWindow.setWidth(mAnchor.getWidth());
			mWindow.setHeight(mAnchor.getHeight());
		}
		initControllerView(mRoot);
	}

	@Override
	protected View makeControllerView() {

		mRoot = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.ijkmedia_controller, this);
		return mRoot;
	}

	@Override
	protected void initControllerView(View v) {
		// mPauseButton = (ImageButton)
		// v.findViewById(R.id.mediacontroller_play_pause);
		mPauseButton = (ImageButton) v.findViewById(getResourseIdByName("id", "mediacontroller_play_pause"));
		if (mPauseButton != null) {
			mPauseButton.requestFocus();
			mPauseButton.setOnClickListener(mPauseListener);
		}
		// mFfwdButton =(ImageButton)v.findViewById(R.id.ffwd);
		mFfwdButton = (ImageButton) v.findViewById(getResourseIdByName("id", "ffwd"));
		mFfwdButton.setOnClickListener(mFfwdListener);

		// mRewButton=(ImageButton)v.findViewById(R.id.rew);
		mRewButton = (ImageButton) v.findViewById(getResourseIdByName("id", "rew"));
		mRewButton.setOnClickListener(mRewListener);
		// btn_boardChange=(ImageButton)v.findViewById(R.id.landscape);
		btn_boardChange = (ImageButton) v.findViewById(getResourseIdByName("id", "landscape"));
		btn_boardChange.setOnClickListener(mBoardListener);
		// btn_videoChange=(ImageButton)v.findViewById(R.id.videochange);
		btn_videoChange = (ImageButton) v.findViewById(getResourseIdByName("id", "videochange"));
		btn_videoChange.setTag("0");
		btn_videoChange.setOnClickListener(mVideoListener);

		// mPreButton=(ImageButton)v.findViewById(getResourseIdByName("id",
		// "prev"));
		// mNextButton=(ImageButton)v.findViewById(getResourseIdByName("id",
		// "next"));
		mPreButton = (ImageButton) v.findViewById(R.id.prev);
		mNextButton = (ImageButton) v.findViewById(R.id.next);
		if (isUsePreNext) {
			mPreButton.setVisibility(View.VISIBLE);
			mNextButton.setVisibility(View.VISIBLE);
		}
		mPreButton.setOnClickListener(mPreListener);
		mNextButton.setOnClickListener(mNextListener);
		mProgress = (ProgressBar) v.findViewById(R.id.mediacontroller_seekbar);
		// mProgress = (ProgressBar) v.findViewById(getResourseIdByName("id",
		// "mediacontroller_seekbar"));
		if (mProgress != null) {
			if (mProgress instanceof SeekBar) {
				SeekBar seeker = (SeekBar) mProgress;
				seeker.setOnSeekBarChangeListener(mSeekListener);
				seeker.setClickable(false);
				seeker.setThumbOffset(1);
			}
			mProgress.setMax(1000);
		}

		mEndTime = (TextView) v.findViewById(R.id.mediacontroller_time_total);
		// mEndTime = (TextView) v.findViewById(getResourseIdByName("id",
		// "mediacontroller_time_total"));
		mCurrentTime = (TextView) v.findViewById(R.id.mediacontroller_time_current);
		// mCurrentTime = (TextView) v.findViewById(getResourseIdByName("id",
		// "mediacontroller_time_current"));
		
		// 码率选择功能涉及的控件
		selectBitrate = (Button) mRoot.findViewById(R.id.select_bitrate);
		selectBitrate.setOnClickListener(mSelectBitRate);
		bitrateLinearLayout = (LinearLayout) mRoot.findViewById(R.id.bitrate_linear_layout);
		
		bitRateBtnArray = new SparseArray<Button>();
		Button liuchangBtn = (Button) mRoot.findViewById(R.id.liuchang);
		liuchangBtn.setText(BitRateEnum.liuChang.getName());
		bitRateBtnArray.append(BitRateEnum.liuChang.getNum(), liuchangBtn);
		
        Button gaoqingBtn = (Button) mRoot.findViewById(R.id.gaoqing);
        gaoqingBtn.setText(BitRateEnum.gaoQing.getName());
        bitRateBtnArray.append(BitRateEnum.gaoQing.getNum(), gaoqingBtn);
        
        Button chaoqingBtn = (Button) mRoot.findViewById(R.id.chaoqing);
        chaoqingBtn.setText(BitRateEnum.chaoQing.getName());
        bitRateBtnArray.append(BitRateEnum.chaoQing.getNum(), chaoqingBtn);
	}

	public void setOnBoardChangeListener(OnBoardChangeListener l) {
		onBoardChangeListener = l;
	}

	public void setOnVideoChangeListener(OnVideoChangeListener l) {
		onVideoChangeListener = l;
	}

	public void setOnPreNextListener(OnPreNextListener l) {
		onPreNextListener = l;
	}

	public interface OnBoardChangeListener {
		public void onLandscape();

		public void onPortrait();
	}

	public interface OnVideoChangeListener {
		public void onVideoChange(int layout);
	}

	public interface OnPreNextListener {
		public void onPreviou();

		public void onNext();
	}

	private boolean isScreenPortrait() {
		return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	private View.OnClickListener mPreListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (onPreNextListener != null)
				onPreNextListener.onPreviou();
		}
	};

	private View.OnClickListener mNextListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (onPreNextListener != null)
				onPreNextListener.onNext();
		}
	};
	private View.OnClickListener mVideoListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag().equals("0")) {
				v.setTag("1");
				if (onVideoChangeListener != null)
					onVideoChangeListener.onVideoChange(0);
			} else if (v.getTag().equals("1")) {
				v.setTag("2");
				if (onVideoChangeListener != null)
					onVideoChangeListener.onVideoChange(1);
			} else if (v.getTag().equals("2")) {
				v.setTag("3");
				if (onVideoChangeListener != null)
					onVideoChangeListener.onVideoChange(2);
			} else if (v.getTag().equals("3")) {
				v.setTag("0");
				if (onVideoChangeListener != null)
					onVideoChangeListener.onVideoChange(3);
			}
		}
	};
	private View.OnClickListener mBoardListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isScreenPortrait()) {//
				if (onBoardChangeListener != null)
					onBoardChangeListener.onPortrait();
			} else {
				if (onBoardChangeListener != null)
					onBoardChangeListener.onLandscape();
			}

		}
	};

	private View.OnClickListener mFfwdListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v("MediaController", "into the mFfw button");
			int pos = mPlayer.getCurrentPosition();
			pos += 5000; // milliseconds
			mPlayer.seekTo(pos);
			setProgress();

			show(sDefaultTimeout);
		}
	};
	private View.OnClickListener mRewListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v("MediaController", "into the mRew button");
			int pos = mPlayer.getCurrentPosition();
			pos -= 5000; // milliseconds
			mPlayer.seekTo(pos);
			setProgress();
			show(sDefaultTimeout);
		}
	};
	
	private View.OnClickListener mSelectBitRate = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (bitrateLinearLayout.getVisibility() == View.INVISIBLE) {
				bitrateLinearLayout.setVisibility(View.VISIBLE);
			} else {
				bitrateLinearLayout.setVisibility(View.INVISIBLE);
			}
		}
	};

	@Override
	public void setMediaPlayer(MediaPlayerControl player) {
		mPlayer = player;
		updatePausePlay();
	}

	public void setInstantSeeking(boolean seekWhenDragging) {
		mInstantSeeking = seekWhenDragging;
	}

	@Override
	public void show() {
		show(sDefaultTimeout);
	}

	private void disableUnsupportedButtons() {
		try {
			if (mPauseButton != null && !mPlayer.canPause())
				mPauseButton.setEnabled(false);
		} catch (IncompatibleClassChangeError ex) {
		}
	}

	/**
	 * Change the animation style resource for this controller. If the
	 * controller is showing, calling this method will take effect only the next
	 * time the controller is shown.
	 * 
	 * @param animationStyle
	 *            animation style to use when the controller appears and
	 *            disappears. Set to -1 for the default animation, 0 for no
	 *            animation, or a resource identifier for an explicit animation.
	 */
	public void setAnimationStyle(int animationStyle) {
		mAnimStyle = animationStyle;
	}

	/**
	 * Show the controller on screen. It will go away automatically after
	 * 'timeout' milliseconds of inactivity.
	 * 
	 * @param timeout
	 *            The timeout in milliseconds. Use 0 to show the controller
	 *            until hide() is called.
	 */
	@Override
	public void show(int timeout) {
		if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {
			if (mPauseButton != null)
				mPauseButton.requestFocus();
			disableUnsupportedButtons();

			if (mFromXml) {
				setVisibility(View.VISIBLE);
			} else {
				int[] location = new int[2];

				mAnchor.getLocationInWindow(location);
				Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(),
						location[1] + mAnchor.getHeight());
				mWindow.setWidth(mAnchor.getWidth());
				mWindow.setHeight(mAnchor.getHeight());
				mWindow.setAnimationStyle(mAnimStyle);
				mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, 0, anchorRect.top);
			}
			mShowing = true;
			if (mShownListener != null)
				mShownListener.onShown();
		}
		updatePausePlay();
		mHandler.sendEmptyMessage(SHOW_PROGRESS);

		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT);
			mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
		}
	}

	public boolean isShowing() {
		return mShowing;
	}

	public void hide() {
		if (mAnchor == null)
			return;
		if (mShowing) {
			bitrateLinearLayout.setVisibility(View.INVISIBLE);
			try {
				mHandler.removeMessages(SHOW_PROGRESS);
				if (mFromXml)
					setVisibility(View.GONE);
				else
					mWindow.dismiss();
			} catch (IllegalArgumentException ex) {
				Log.d(TAG, "MediaController already removed");
			}
			mShowing = false;
			if (mHiddenListener != null)
				mHiddenListener.onHidden();
		}
	}

	public interface OnShownListener {
		public void onShown();
	}

	private OnShownListener mShownListener;

	public void setOnShownListener(OnShownListener l) {
		mShownListener = l;
	}

	public interface OnHiddenListener {
		public void onHidden();
	}

	private OnHiddenListener mHiddenListener;

	public void setOnHiddenListener(OnHiddenListener l) {
		mHiddenListener = l;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			long pos;
			switch (msg.what) {
			case FADE_OUT:
				hide();
				break;
			case SHOW_PROGRESS:
				pos = setProgress();
				if (!mDragging && mShowing) {
					msg = obtainMessage(SHOW_PROGRESS);
					sendMessageDelayed(msg, 1000 - (pos % 1000));
					updatePausePlay();
				}
				break;
			}
		}
	};

	private long setProgress() {
		if (mPlayer == null || mDragging)
			return 0;

		int position = mPlayer.getCurrentPosition();
		int duration = mPlayer.getDuration();
		if (mProgress != null) {
			if (duration > 0) {
				long pos = 1000L * position / duration;
				mProgress.setProgress((int) pos);
			}
			int percent = mPlayer.getBufferPercentage();
			mProgress.setSecondaryProgress(percent * 10);
		}

		mDuration = duration;
		if (mEndTime != null)
			mEndTime.setText(generateTime(mDuration));
		if (mCurrentTime != null)
			mCurrentTime.setText(generateTime(position));

		return position;
	}

	private static String generateTime(long position) {
		int totalSeconds = (int) (position / 1000);

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		if (hours > 0) {
			return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return String.format(Locale.US, "%02d:%02d", minutes, seconds).toString();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		show(sDefaultTimeout);
		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		show(sDefaultTimeout);
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		if (event.getRepeatCount() == 0 && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
				|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
			doPauseResume();
			show(sDefaultTimeout);
			if (mPauseButton != null)
				mPauseButton.requestFocus();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
			if (mPlayer.isPlaying()) {
				mPlayer.pause();
				updatePausePlay();
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isShowing()) {
				hide();
				return true;
			}
			
			if (isScreenPortrait() == false) {
				if (onBoardChangeListener != null)
					onBoardChangeListener.onLandscape();
				return true;
			}
			
			return false;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			show(sDefaultTimeout);
		} else {
			show(sDefaultTimeout);
		}
		return super.dispatchKeyEvent(event);
	}

	private View.OnClickListener mPauseListener = new View.OnClickListener() {
		public void onClick(View v) {
			doPauseResume();
			show(sDefaultTimeout);
		}
	};

	private void updatePausePlay() {
		if (mRoot == null || mPauseButton == null)
			return;
		if (mPlayer.isPlaying()) {
			mPauseButton.setImageResource(R.drawable.media_pause);
		} else {
			mPauseButton.setImageResource(R.drawable.media_play);
		}
	}

	private void doPauseResume() {
		if (mPlayer.isPlaying())
			mPlayer.pause();
		else
			mPlayer.start();
		updatePausePlay();
	}

	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			mDragging = true;
			show(3600000);
			mHandler.removeMessages(SHOW_PROGRESS);
			if (mInstantSeeking)
				mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);

		}

		public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
			if (!fromuser)
				return;

			long newposition = (mDuration * progress) / 1000;
			String time = generateTime(newposition);
			if (mInstantSeeking)
				mPlayer.seekTo(newposition);

			if (mCurrentTime != null)
				mCurrentTime.setText(time);
		}

		public void onStopTrackingTouch(SeekBar bar) {
			if (!mInstantSeeking)
				mPlayer.seekTo((mDuration * bar.getProgress()) / 1000);
			show(sDefaultTimeout);
			mHandler.removeMessages(SHOW_PROGRESS);
			mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);
			mDragging = false;
			mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
		}
	};

	@Override
	public void setEnabled(boolean enabled) {
		if (mPauseButton != null)
			mPauseButton.setEnabled(enabled);
		if (mProgress != null)
			mProgress.setEnabled(enabled);
		disableUnsupportedButtons();
		super.setEnabled(enabled);
	}

	public int getResourseIdByName(String className, String name) {
		Class r = null;
		int id = 0;
		try { // System.out.println(getPackageName() + ".R");
			r = Class.forName(mContext.getPackageName() + ".R");

			Class[] classes = r.getClasses();
			Class desireClass = null;

			for (int i = 0; i < classes.length; i++) {
				if (classes[i].getName().split("\\$")[1].equals(className)) {
					desireClass = classes[i];
					break;
				}
			}

			if (desireClass != null)
				id = desireClass.getField(name).getInt(desireClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		return id;

	}
	
	/**
	 * 设置IjkVideoView 对象，如果没有设置则在码率按钮切换码率的操作中会报错
	 * @param ijkVideoView
	 */
	public void setIjkVideoView(IjkVideoView ijkVideoView) {
		this.ijkVideoView = ijkVideoView;
	}
	
	@Override
	public void setViewBitRate(String vid, int bitRate) {
		new GetDFNumWork().execute(vid, String.valueOf(bitRate));
	}
	
	/**
	 * 取得dfNum 任务
	 * @author TanQu 2015-10-8
	 */
	private class GetDFNumWork extends AsyncTask<String, String, Integer> {

		private String vid = "";
		private int currBitRate = 0;
		
		@Override
		protected Integer doInBackground(String... params) {
			vid = params[0];
			currBitRate = Integer.parseInt(params[1]);
			if (currBitRate < 0) currBitRate = 0;
			int dfNum = PolyvSDKClient.getInstance().getVideoDBService().getDFNum(vid);
			return dfNum;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == 0) return;
			
			selectBitrate.setText(BitRateEnum.getBitRateName(currBitRate));
			List<BitRateEnum> list = BitRateEnum.getBitRateList(result);
			Button bitRateBtn = null;
			for (BitRateEnum bitRateEnum : list) {
				if (bitRateEnum == BitRateEnum.ziDong) continue;
				bitRateBtn = bitRateBtnArray.get(bitRateEnum.getNum());
				bitRateBtn.setVisibility(View.VISIBLE);
				bitRateBtn.setOnClickListener(new bitRateClientListener(vid, currBitRate, bitRateEnum.getNum()));
			}
		}
	}
	
	/**
	 * 码率按钮单击事件监听方法
	 * @author TanQu 2015-10-8
	 */
	private class bitRateClientListener implements View.OnClickListener {
		private final String vid;
		private final int currBitRate;
		private final int targetBitRate;
		
		public bitRateClientListener(String vid, int currBitRate, int targetBitRate) {
			this.vid = vid;
			this.currBitRate = currBitRate;
			this.targetBitRate = targetBitRate;
		}
		
		@Override
		public void onClick(View v) {
			if (currBitRate == targetBitRate) {
				bitrateLinearLayout.setVisibility(View.INVISIBLE);
				return;
			}
			
			ijkVideoView.setVid(vid, targetBitRate);
		}
	}
}
