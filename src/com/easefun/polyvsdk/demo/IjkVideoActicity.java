package com.easefun.polyvsdk.demo;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import com.easefun.polyvsdk.QuestionVO;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.Video;
import com.easefun.polyvsdk.Video.ADMatter;
import com.easefun.polyvsdk.danmaku.DanmakuInfo;
import com.easefun.polyvsdk.danmaku.DanmakuManager;
import com.easefun.polyvsdk.danmaku.DanmakuManager.GetDanmakuListener;
import com.easefun.polyvsdk.ijk.IjkVideoView;
import com.easefun.polyvsdk.ijk.IjkVideoView.ErrorReason;
import com.easefun.polyvsdk.ijk.OnPreparedListener;
import com.easefun.polyvsdk.screenshot.ActivityTool;
import com.easefun.polyvsdk.srt.PolyvSRTItemVO;
import com.easefun.polyvsdk.util.ScreenTool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;

public class IjkVideoActicity extends Activity {
	private static final String TAG = "IjkVideoActicity";
	private IjkVideoView videoView = null;
	private PolyvQuestionView questionView = null;
	private PolyvAuditionView auditionView = null;
	private PolyvPlayerAdvertisementView adView = null;
	private MediaController mediaController = null;
	private TextView videoAdCountDown = null;
	// 取消掉变量引用的注释即可打开logo功能
	private ImageView logo = null;
	private TextView srtTextView = null;
	private PolyvPlayerFirstStartView playerFirstStartView = null;
	int w = 0, h = 0, adjusted_h = 0;
	private boolean startNow = false;
	// videoview的容器
	private VideoViewContainer rl = null;
	// 弹幕
	private IDanmakuView mDanmakuView;
	private BaseDanmakuParser mParser;
	private DanmakuContext mContext;
	private DanmakuManager danmakuManager;
	private String vid;
	private int fastForwardPos = 0;
	private boolean isPlay = false;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			AlertDialog.Builder builder = new AlertDialog.Builder(IjkVideoActicity.this);
			builder.setTitle("提示");
			builder.setMessage(msg.getData().getString("msg"));
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			});

			builder.setCancelable(false);
			builder.show();
		}
	};

	public static Intent newIntent(Context context, PlayMode playMode, PlayType playType, String value,
			boolean startNow) {
		Intent intent = new Intent(context, IjkVideoActicity.class);
		intent.putExtra("playMode", playMode.getCode());
		intent.putExtra("playType", playType.getCode());
		intent.putExtra("value", value);
		intent.putExtra("startNow", startNow);
		return intent;
	}

	public static Intent newIntent(Context context, PlayMode playMode, PlayType playType, String value,
			boolean isMustFromLocal, Video.HlsSpeedType hlsSpeedType, boolean startNow) {
		Intent intent = new Intent(context, IjkVideoActicity.class);
		intent.putExtra("playMode", playMode.getCode());
		intent.putExtra("playType", playType.getCode());
		intent.putExtra("value", value);
		intent.putExtra("isMustFromLocal", isMustFromLocal);
		intent.putExtra("hlsSpeedType", hlsSpeedType.getName());
		intent.putExtra("startNow", startNow);
		return intent;
	}

	public static void intentTo(Context context, PlayMode playMode, PlayType playType, String value, boolean startNow) {
		context.startActivity(newIntent(context, playMode, playType, value, startNow));
	}

	public static void intentTo(Context context, PlayMode playMode, PlayType playType, String value,
			boolean isMustFromLocal, Video.HlsSpeedType hlsSpeedType, boolean startNow) {
		context.startActivity(newIntent(context, playMode, playType, value, isMustFromLocal, hlsSpeedType, startNow));
	}

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.video_small2);

		// handle arguments
		int playModeCode = getIntent().getIntExtra("playMode", 0);
		PlayMode playMode = PlayMode.getPlayMode(playModeCode);
		int playTypeCode = getIntent().getIntExtra("playType", 0);
		final PlayType playType = PlayType.getPlayType(playTypeCode);
		final String value = getIntent().getStringExtra("value");
		final boolean isMustFromLocal = getIntent().getBooleanExtra("isMustFromLocal", false);
		Video.HlsSpeedType hlsSpeedType = Video.HlsSpeedType
				.getHlsSpeedType(getIntent().getStringExtra("hlsSpeedType"));
		if (hlsSpeedType == null)
			hlsSpeedType = Video.HlsSpeedType.SPEED_1X;
		startNow = getIntent().getBooleanExtra("startNow", false);
		if (playMode == null || playType == null || TextUtils.isEmpty(value)) {
			Log.e(TAG, "Null Data Source");
			finish();
			return;
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		int[] wh = ScreenTool.getNormalWH(this);
		w = wh[0];
		h = wh[1];
		// 小窗口的比例
		float ratio = (float) 4 / 3;
		adjusted_h = (int) Math.ceil((float) w / ratio);
		rl = (VideoViewContainer) findViewById(R.id.rl);
		rl.setLayoutParams(new RelativeLayout.LayoutParams(w, adjusted_h));
		videoView = (IjkVideoView) findViewById(R.id.videoview);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.loadingprogress);
		videoAdCountDown = (TextView) findViewById(R.id.count_down);
		logo = (ImageView) findViewById(R.id.logo);
		srtTextView = (TextView) findViewById(R.id.srt);
		// 在缓冲时出现的loading
		videoView.setMediaBufferingIndicator(progressBar);
		videoView.setOpenTeaser(true);
		videoView.setOpenAd(true);
		videoView.setOpenQuestion(true);
		videoView.setOpenSRT(true);
		videoView.setNeedGestureDetector(true);
		videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_SCALE);
		videoView.setAutoContinue(true);
		videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(IMediaPlayer mp) {
				videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_SCALE);
				// logo.setVisibility(View.VISIBLE);

				if (startNow == false) {
					videoView.pause(true);
					if (playType == PlayType.vid) {
						playerFirstStartView.show(rl, value);
					}
				}

				String msg = String.format("是否在线播放 %b", videoView.isLocalPlay() == false);
				Log.d(TAG, msg);
				Toast.makeText(IjkVideoActicity.this, msg, Toast.LENGTH_SHORT);
			}
		});

		videoView.setOnVideoStatusListener(new IjkVideoView.OnVideoStatusListener() {

			@Override
			public void onStatus(int status) {
				if (status < 60) {
					Log.e(TAG, String.format("状态错误 %d", status));
				} else {
					Log.d(TAG, String.format("状态正常 %d", status));
				}
			}
		});

		videoView.setOnVideoPlayErrorLisener(new IjkVideoView.OnVideoPlayErrorLisener() {

			@Override
			public boolean onVideoPlayError(ErrorReason errorReason) {
				// 播放错误，暂停弹幕
				if (mDanmakuView != null && mDanmakuView.isPrepared())
					mDanmakuView.pause();

				switch (errorReason.getType()) {
				case BITRATE_ERROR:
					sendMessage("设置的码率错误");
					break;
				case CAN_NOT_CHANGE_BITRATE:
					sendMessage("未开始播放视频不能切换码率");
					break;
				case CAN_NOT_CHANGE_HLS_SPEED:
					sendMessage("未开始播放视频不能切换播放速度");
					break;
				case CHANGE_EQUAL_BITRATE:
					sendMessage("切换码率相同");
					break;
				case CHANGE_EQUAL_HLS_SPEED:
					sendMessage("切换播放速度相同");
					break;
				case HLS_15X_URL_ERROR:
					sendMessage("1.5倍当前码率视频正在编码中");
					break;
				case HLS_15X_ERROR:
					sendMessage("视频不支持1.5倍当前码率播放");
					break;
				case HLS_15X_INDEX_EMPTY:
					sendMessage("视频不支持1.5倍自动码率播放");
					break;
				case HLS_SPEED_TYPE_NULL:
					sendMessage("请设置播放速度");
					break;
				case LOCAL_VIEWO_ERROR:
					sendMessage("本地视频文件损坏");
					break;
				case M3U8_15X_LINK_NUM_ERROR:
					sendMessage("HLS 1.5倍播放地址服务器数据错误");
					break;
				case M3U8_LINK_NUM_ERROR:
					sendMessage("HLS 播放地址服务器数据错误");
					break;
				case MP4_LINK_NUM_ERROR:
					sendMessage("MP4 播放地址服务器数据错误");
					break;
				case NETWORK_DENIED:
					sendMessage("无法连接网络");
					break;
				case NOT_LOCAL_VIDEO:
					sendMessage("找不到本地下载的视频文件，请连网后重新下载或播放");
					break;
				case NOT_PERMISSION:
					sendMessage("没有权限，不能播放该视频");
					break;
				case OUT_FLOW:
					sendMessage("流量超标");
					break;
				case QUESTION_JSON_ERROR:
					sendMessage("问答数据加载为空");
					break;
				case QUESTION_JSON_PARSE_ERROR:
					sendMessage("问答数据格式化错误");
					break;
				case LOADING_VIDEO_ERROR:
					sendMessage("视频信息加载过程中出错");
					break;
				case START_ERROR:
					sendMessage("开始播放视频错误，请重试");
					break;
				case TIMEOUT_FLOW:
					sendMessage("账号过期");
					break;
				case USER_TOKEN_ERROR:
					sendMessage("没有设置用户数据");
					break;
				case VIDEO_NULL:
					sendMessage("视频信息为空");
					break;
				case VIDEO_STATUS_ERROR:
					sendMessage("视频状态错误");
					break;
				case VID_ERROR:
					sendMessage("设置的vid错误");
					break;
				default:
					break;

				}

				// 返回 false，sdk中会弹出一个默认的错误提示框
				// 返回 true，sdk中不会弹出一个错误提示框
				return true;
			}
		});

		videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(IMediaPlayer arg0, int arg1, int arg2) {
				// TODO 可以在这里重新请求setVid方法重试播放
				sendMessage("播放异常，请稍后再试");
				// 返回 false，sdk中会弹出一个默认的错误提示框
				// 返回 true，sdk中不会弹出一个错误提示框
				return true;
			}
		});

		videoView.setOnQuestionOutListener(new IjkVideoView.OnQuestionOutListener() {

			@Override
			public void onOut(final QuestionVO questionVO) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						switch (questionVO.getType()) {
						case QuestionVO.TYPE_QUESTION:
							if (questionView == null) {
								questionView = new PolyvQuestionView(IjkVideoActicity.this);
								questionView.setIjkVideoView(videoView);
							}

							questionView.show(rl, questionVO);
							break;

						case QuestionVO.TYPE_AUDITION:
							if (auditionView == null) {
								auditionView = new PolyvAuditionView(IjkVideoActicity.this);
								auditionView.setIjkVideoView(videoView);
							}

							auditionView.show(rl, questionVO);
							break;
						}
					}
				});
			}
		});

		videoView.setOnQuestionAnswerTipsListener(new IjkVideoView.OnQuestionAnswerTipsListener() {

			@Override
			public void onTips(String msg) {
				questionView.showAnswerTips(msg);
			}
		});

		videoView.setOnAdvertisementOutListener(new IjkVideoView.OnAdvertisementOutListener() {

			@Override
			public void onOut(ADMatter adMatter) {
				if (adView == null) {
					adView = new PolyvPlayerAdvertisementView(IjkVideoActicity.this);
					adView.setIjkVideoView(videoView);
				}

				adView.show(rl, adMatter);
			}
		});

		videoView.setOnAdvertisementCountDownListener(new IjkVideoView.OnAdvertisementCountDownListener() {

			@Override
			public void onEnd() {
				videoAdCountDown.setVisibility(View.GONE);
			}

			@Override
			public void onCountDown(int num) {
				videoAdCountDown.setText(String.format("广告也精彩：%d秒", num));
				videoAdCountDown.setVisibility(View.VISIBLE);
			}
		});

		videoView.setOnPlayPauseListener(new IjkVideoView.OnPlayPauseListener() {

			@Override
			public void onPlay() {

			}

			@Override
			public void onPause() {

			}

			@Override
			public void onCompletion() {
				// logo.setVisibility(View.GONE);
				mediaController.setProgressMax();
				// 播放完成，暂停弹幕
				mDanmakuView.pause();
			}
		});

		videoView.setOnVideoSRTListener(new IjkVideoView.OnVideoSRTListener() {

			@Override
			public void onVideoSRT(PolyvSRTItemVO sRTItemVO) {
				if (sRTItemVO == null) {
					srtTextView.setText("");
				} else {
					srtTextView.setText(sRTItemVO.getSubTitle());
				}

				srtTextView.setVisibility(View.VISIBLE);
			}
		});

		videoView.setClick(new IjkVideoView.Click() {

			@Override
			public void callback(boolean start, boolean end) {
				mediaController.toggleVisiblity();
			}
		});

		videoView.setLeftUp(new IjkVideoView.LeftUp() {

			@Override
			public void callback(boolean start, boolean end) {
				Log.d(TAG, String.format("LeftUp %b %b brightness %d", start, end, videoView.getBrightness()));
				int brightness = videoView.getBrightness() + 5;
				if (brightness > 100) {
					brightness = 100;
				}

				videoView.setBrightness(brightness);
			}
		});

		videoView.setLeftDown(new IjkVideoView.LeftDown() {

			@Override
			public void callback(boolean start, boolean end) {
				Log.d(TAG, String.format("LeftDown %b %b brightness %d", start, end, videoView.getBrightness()));
				int brightness = videoView.getBrightness() - 5;
				if (brightness < 0) {
					brightness = 0;
				}

				videoView.setBrightness(brightness);
			}
		});

		videoView.setRightUp(new IjkVideoView.RightUp() {

			@Override
			public void callback(boolean start, boolean end) {
				Log.d(TAG, String.format("RightUp %b %b volume %d", start, end, videoView.getVolume()));
				// 加减单位最小为10，否则无效果
				int volume = videoView.getVolume() + 10;
				if (volume > 100) {
					volume = 100;
				}

				videoView.setVolume(volume);
			}
		});

		videoView.setRightDown(new IjkVideoView.RightDown() {

			@Override
			public void callback(boolean start, boolean end) {
				Log.d(TAG, String.format("RightDown %b %b volume %d", start, end, videoView.getVolume()));
				// 加减单位最小为10，否则无效果
				int volume = videoView.getVolume() - 10;
				if (volume < 0) {
					volume = 0;
				}

				videoView.setVolume(volume);
			}
		});

		videoView.setSwipeLeft(new IjkVideoView.SwipeLeft() {

			@Override
			public void callback(boolean start, boolean end) {
				// TODO 左滑事件
				Log.d(TAG, String.format("SwipeLeft %b %b", start, end));
				if (fastForwardPos == 0) {
					fastForwardPos = videoView.getCurrentPosition();
				}

				if (end) {
					if (fastForwardPos < 0)
						fastForwardPos = 0;
					videoView.seekTo(fastForwardPos);
					fastForwardPos = 0;
				} else {
					fastForwardPos -= 10000;
				}
			}
		});

		videoView.setSwipeRight(new IjkVideoView.SwipeRight() {

			@Override
			public void callback(boolean start, boolean end) {
				// TODO 右滑事件
				Log.d(TAG, String.format("SwipeRight %b %b", start, end));
				if (fastForwardPos == 0) {
					fastForwardPos = videoView.getCurrentPosition();
				}

				if (end) {
					if (fastForwardPos > videoView.getDuration())
						fastForwardPos = videoView.getDuration();
					videoView.seekTo(fastForwardPos);
					fastForwardPos = 0;
				} else {
					fastForwardPos += 10000;
				}
			}
		});

		// 设置缓存监听
		videoView.setOnInfoListener(new OnInfoListener() {

			@Override
			public boolean onInfo(IMediaPlayer arg0, int arg1, int arg2) {
				switch (arg1) {
				case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
					// 缓存的时候暂停
					if (mDanmakuView != null)
						mDanmakuView.pause();
					break;

				case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
					// 恢复
					if (mDanmakuView != null)
						// resume方法中已包含了start
						mDanmakuView.resume();
					// 把缓冲后的时间设置给控制器
					mediaController.setNewtime(videoView.getCurrentPosition());
					break;
				}
				return false;
			}
		});

		mediaController = new MediaController(this, false);
		mediaController.setIjkVideoView(videoView);
		mediaController.setAnchorView(videoView);
		mediaController.setInstantSeeking(false);
		videoView.setMediaController(mediaController);

		// 设置切屏事件
		mediaController.setOnBoardChangeListener(new MediaController.OnBoardChangeListener() {

			@Override
			public void onPortrait() {
				changeToLandscape();
			}

			@Override
			public void onLandscape() {
				changeToPortrait();
			}
		});

		mediaController.setOnResetViewListener(new MediaController.OnResetViewListener() {

			@Override
			public void onReset() {
				srtTextView.setVisibility(View.GONE);
			}
		});

		mediaController.setOnUpdateStartNow(new MediaController.OnUpdateStartNow() {

			@Override
			public void onUpdate(boolean isStartNow) {
				startNow = isStartNow;
			}
		});

		switch (playMode) {
		case landScape:
			changeToLandscape();
			break;

		case portrait:
			changeToPortrait();
			break;
		}

		switch (playType) {
		case vid:
			videoView.setVid(value, isMustFromLocal, hlsSpeedType);
			break;
		case url:
			progressBar.setVisibility(View.GONE);
			videoView.setVideoPath(value);
			break;
		}

		// 由videoview的容器处理videoview之外的触摸事件
		rl.setVideoView(videoView);
		// 设置隐藏状态栏的监听器
		ScreenTool.setHideStatusBarListener(this, 2000);
		// 弹幕
		vid = value;
		danmakuManager = new DanmakuManager();
		mDanmakuView = (IDanmakuView) findViewById(R.id.sv_danmaku);
		mContext = DanmakuContext.create();
		// 设置最大显示行数,当有换行时算作一行
		HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
		maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
		maxLinesPair.put(BaseDanmaku.TYPE_FIX_TOP, 5);
		maxLinesPair.put(BaseDanmaku.TYPE_FIX_BOTTOM, 5);
		// 设置是否禁止重叠
		HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
		overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, false);
		overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
		overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);
		// 设置弹幕样式
		mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_SHADOW, IDisplayer.DANMAKU_STYLE_SHADOW)
				.setMaximumLines(maxLinesPair).preventOverlapping(overlappingEnablePair)
				.setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.5f).setScaleTextSize(1.0f);
		// 设置监听器
		danmakuManager.setGetDanmakuListener(new GetDanmakuListener() {

			@Override
			public void success(int total, List<DanmakuInfo> getDanmakus) {
				// 弹幕总数，获取到的弹幕数据
				ActivityTool.toastMsg(IjkVideoActicity.this, "获取弹幕成功，总数:" + total);
			}

			@Override
			public void fail(int category) {
				switch (category) {
				case DanmakuManager.VIDEO_NOT_DANMAKU:
					ActivityTool.toastMsg(IjkVideoActicity.this, "获取弹幕失败：该视频还没有弹幕");
					break;
				case DanmakuManager.NETWORK_EXCEPTION:
					ActivityTool.toastMsg(IjkVideoActicity.this, "获取弹幕失败：网络异常");
					break;
				case DanmakuManager.RESPONSE_FAIL:
					ActivityTool.toastMsg(IjkVideoActicity.this, "获取弹幕失败：响应失败");
					break;
				}
			}
		});
		// 获取弹幕
		new Thread(new Runnable() {
			public void run() {
				// 获取弹幕,该方法是阻塞方法
				InputStream danmakuStream = danmakuManager.getDanmakuByServer(vid, 200);
				mParser = danmakuManager.getBaseDanmakuParser(danmakuStream);
				IjkVideoActicity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						SendDanmakuDialog.getInstance(IjkVideoActicity.this).setBaseDanmakuParser(mParser);
					}
				});
				if (mDanmakuView != null)
					mDanmakuView.prepare(mParser, mContext);
			}
		}).start();

		// 这段代码要放到初始化弹幕view的后面
		if (startNow) {
			videoView.start();
			// 开启弹幕
			startDanmaku();
		} else {
			if (playType == PlayType.vid) {
				if (playerFirstStartView == null) {
					playerFirstStartView = new PolyvPlayerFirstStartView(this);
					playerFirstStartView.setCallback(new PolyvPlayerFirstStartView.Callback() {

						@Override
						public void onClickStart() {
							videoView.start();
							// 开启弹幕
							startDanmaku();
							playerFirstStartView.hide();
						}
					});
				}
			}
		}
	}

	// 开始弹幕
	private void startDanmaku() {
		if (videoView.isPlaying())
			mDanmakuView.start();
		// 把弹幕view传到控制器
		mediaController.setIDanmakuView(mDanmakuView);
		// 设置参数到发送弹幕的对话框中
		SendDanmakuDialog.getInstance(this).setDanmakuContext(mContext).setIDanmakuView(mDanmakuView)
				.setIjkVideoView(videoView).setVid(vid);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 暂停弹幕
		if (mDanmakuView != null && mDanmakuView.isPrepared())
			mDanmakuView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 恢复弹幕
		if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused() && videoView != null
				&& videoView.isPlaying())
			mDanmakuView.resume();
		
		if (videoView != null && videoView.isPausState() && isPlay) {
			if (adView != null) {
				adView.hide();
			}
			
			videoView.start();
		}
	}

	/**
	 * 切换到横屏
	 */
	public void changeToLandscape() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// 由于切换到横屏获取到的宽高可能和竖屏的不一样，所以需要重新获取宽高
		int[] wh = ScreenTool.getNormalWH(this);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(wh[0], wh[1]);
		rl.setLayoutParams(p);
	}

	/**
	 * 切换到竖屏
	 */
	public void changeToPortrait() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, adjusted_h);
		rl.setLayoutParams(p);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// 隐藏或显示状态栏
		ScreenTool.reSetStatusBar(this);
	}

	// 配置文件设置congfigchange 切屏调用一次该方法，hide()之后再次show才会出现在正确位置
	@Override
	public void onConfigurationChanged(Configuration arg0) {
		super.onConfigurationChanged(arg0);
		videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_SCALE);
		mediaController.hide();
		// 隐藏或显示状态栏
		ScreenTool.reSetStatusBar(this);

		if (questionView != null && questionView.isShowing()) {
			questionView.hide();
			questionView.refresh();
		} else if (auditionView != null && auditionView.isShowing()) {
			auditionView.hide();
			auditionView.refresh();
		}

		if (playerFirstStartView != null && playerFirstStartView.isShowing()) {
			playerFirstStartView.hide();
			playerFirstStartView.refresh();
		}

		if (adView != null && adView.isShowing()) {
			adView.hide();
			adView.refresh();
		}

		// 清除弹幕再显示
		if (mDanmakuView != null && mDanmakuView.isShown()) {
			mDanmakuView.clearDanmakusOnScreen();
			mDanmakuView.hide();
			mDanmakuView.show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean value = mediaController.dispatchKeyEvent(event);
		if (value)
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (videoView != null) {
			videoView.destroy();
		}

		if (questionView != null) {
			questionView.hide();
		}

		if (auditionView != null) {
			auditionView.hide();
		}

		if (playerFirstStartView != null) {
			playerFirstStartView.hide();
		}

		if (adView != null) {
			adView.hide();
		}

		// 释放资源
		if (mDanmakuView != null) {
			// dont forget release!
			mDanmakuView.release();
			mDanmakuView = null;
		}
		danmakuManager.release();
		SendDanmakuDialog.realse();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (videoView != null && videoView.isPlayState()) {
			isPlay = true;
			videoView.pause();
		} else {
			isPlay = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (videoView != null) {
			videoView.destroy();
		}

		if (questionView != null) {
			questionView.hide();
		}

		if (auditionView != null) {
			auditionView.hide();
		}

		if (playerFirstStartView != null) {
			playerFirstStartView.hide();
		}

		if (adView != null) {
			adView.hide();
		}

		// 释放资源
		if (mDanmakuView != null) {
			// dont forget release!
			mDanmakuView.release();
			mDanmakuView = null;
		}
		danmakuManager.release();
		SendDanmakuDialog.realse();
	};

	/**
	 * 播放类型
	 * 
	 * @author TanQu
	 */
	public enum PlayType {
		/** 使用vid播放 */
		vid(1),
		/** 使用url播放 */
		url(2);

		private final int code;

		private PlayType(int code) {
			this.code = code;
		}

		/**
		 * 取得类型对应的code
		 * 
		 * @return
		 */
		public int getCode() {
			return code;
		}

		public static PlayType getPlayType(int code) {
			switch (code) {
			case 1:
				return vid;
			case 2:
				return url;
			}

			return null;
		}
	}

	/**
	 * 播放模式
	 * 
	 * @author TanQu
	 */
	public enum PlayMode {
		/** 横屏 */
		landScape(3),
		/** 竖屏 */
		portrait(4);

		private final int code;

		private PlayMode(int code) {
			this.code = code;
		}

		/**
		 * 取得类型对应的code
		 * 
		 * @return
		 */
		public int getCode() {
			return code;
		}

		public static PlayMode getPlayMode(int code) {
			switch (code) {
			case 3:
				return landScape;
			case 4:
				return portrait;
			}

			return null;
		}
	}

	private void sendMessage(String info) {
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putString("msg", info);
		msg.setData(data);
		handler.sendMessage(msg);
	}
	
//	public void btn1(View view) {
//		videoView.setVid("2273463aee027300ec391928e431fe8b_2");
//	}
//	
//	public void btn2(View view) {
//		videoView.setVid("2273463aee8bb8027f8fb60d1ee090fb_2");
//	}
}
