package com.easefun.polyvsdk.demo.view;

import com.easefun.polyvsdk.Video;
import com.easefun.polyvsdk.Video.HlsSpeedType;
import com.easefun.polyvsdk.demo.MediaController;
import com.easefun.polyvsdk.ijk.IjkUtil;
import com.easefun.polyvsdk.ijk.IjkValidateM3U8VideoReturnType;
import com.easefun.polyvsdk.ijk.IjkVideoView;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * 倍速按钮
 *
 */
public class SpeedButton extends Button {
	private MediaController.OnUpdateStartNow onUpdateStartNow;
	private boolean is15Video = false;
	private float videoSpeed = 1.0f;
	private final int speed_05 = 5, speed_1 = 10, speed_12 = 12, speed_15 = 15, speed_2 = 20;
	private int currentSpeed = speed_1;

	private void updateCallback() {
		if (onUpdateStartNow != null)
			onUpdateStartNow.onUpdate(true);
	}

	private String getSpeedText(int speed) {
		String speedText = "";
		switch (speed) {
		case speed_05:
			speedText = "0.5x";
			break;
		case speed_1:
			speedText = " 1x ";
			break;
		case speed_12:
			speedText = "1.2x";
			break;
		case speed_15:
			speedText = "1.5x";
			break;
		case speed_2:
			speedText = " 2x ";
			break;
		}
		return speedText;
	}

	private String getReHlsSpeedText(int currentSpeed) {
		switch (currentSpeed) {
		case speed_1:
			return "1.5倍速";
		case speed_15:
			return "1倍速";
		}
		return "";
	}

	public SpeedButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SpeedButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SpeedButton(Context context) {
		this(context, null);
	}

	private void localPlayClickValidate(IjkVideoView ijkVideoView, Video video, int currentSpeed) {
		boolean isLocalPlay = ijkVideoView.isLocalPlay();
		int validateResult = -1;
		// 判断本地是否有完整的视频文件
		switch (currentSpeed) {
		case speed_1:
			validateResult = IjkUtil.validateM3U8Video(video.getVid(), ijkVideoView.getBitRate(),
					HlsSpeedType.SPEED_1_5X);
			break;
		case speed_15:
			validateResult = IjkUtil.validateM3U8Video(video.getVid(), ijkVideoView.getBitRate(),
					HlsSpeedType.SPEED_1X);
			break;
		}
		switch (validateResult) {
		// 存在完整的视频
		case IjkValidateM3U8VideoReturnType.M3U8_CORRECT:
			if (isLocalPlay)
				clickChangeSpeed(ijkVideoView, currentSpeed);
			else
				showHintDialog(getReHlsSpeedText(currentSpeed) + "视频已经缓存，是否切换到缓存播放", ijkVideoView, currentSpeed);
			break;
		// 文件为空
		case IjkValidateM3U8VideoReturnType.M3U8_FILE_NOT_FOUND:
			if (isLocalPlay)
				showHintDialog(getReHlsSpeedText(currentSpeed) + "视频没有缓存，是否切换到网络播放", ijkVideoView, currentSpeed);
			else
				clickChangeSpeed(ijkVideoView, currentSpeed);
			break;
		default:
			if (isLocalPlay)
				showHintDialog(getReHlsSpeedText(currentSpeed) + "视频本地文件损坏，是否切换到网络播放", ijkVideoView, currentSpeed);
			else
				clickChangeSpeed(ijkVideoView, currentSpeed);
			break;
		}
	}

	private void clickChangeSpeed(IjkVideoView ijkVideoView, int currentSpeed) {
		switch (currentSpeed) {
		case speed_1:
			updateCallback();
			ijkVideoView.changeHlsSpeedType(HlsSpeedType.SPEED_1_5X);
			setText(getSpeedText(speed_15));
			this.currentSpeed = speed_15;
			Toast.makeText(getContext(), "当前为1.5倍速", 0).show();
			break;
		case speed_15:
			updateCallback();
			ijkVideoView.changeHlsSpeedType(HlsSpeedType.SPEED_1X);
			setText(getSpeedText(speed_1));
			this.currentSpeed = speed_1;
			Toast.makeText(getContext(), "当前为1倍速", 0).show();
			break;
		}
	}

	private void showHintDialog(String msg, final IjkVideoView ijkVideoView, final int currentSpeed) {
		AlertDialog.Builder builder = new Builder(getContext());
		builder.setMessage(msg);

		builder.setTitle("提示");

		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				clickChangeSpeed(ijkVideoView, currentSpeed);
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.create().show();
	}

	public void init(final IjkVideoView ijkVideoView, MediaController.OnUpdateStartNow onUpdateStartNow) {
		this.onUpdateStartNow = onUpdateStartNow;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			setVisibility(View.VISIBLE);
			if (ijkVideoView.getHlsSpeedType().equals(HlsSpeedType.SPEED_1_5X)) {
				if (is15Video == false) {
					is15Video = true;
					videoSpeed = 1.5f;
					setText(getSpeedText(speed_15));
					currentSpeed = speed_15;
				}
			}
			// view被重置的时候，需要把text设置为之前的那个倍速
			// 由于videoview被释放，这里需要重新设置
			switch (currentSpeed) {
			case speed_05:
				ijkVideoView.setSpeed(0.5f / videoSpeed);
				setText(getSpeedText(speed_05));
				break;
			case speed_1:
				ijkVideoView.setSpeed(1f / videoSpeed);
				setText(getSpeedText(speed_1));
				break;
			case speed_12:
				ijkVideoView.setSpeed(1.2f / videoSpeed);
				setText(getSpeedText(speed_12));
				break;
			case speed_15:
				ijkVideoView.setSpeed(1.5f / videoSpeed);
				setText(getSpeedText(speed_15));
				break;
			case speed_2:
				ijkVideoView.setSpeed(2f / videoSpeed);
				setText(getSpeedText(speed_2));
				break;
			}
			setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (currentSpeed) {
					case speed_05:
						ijkVideoView.setSpeed(1f / videoSpeed);
						setText(getSpeedText(speed_1));
						currentSpeed = speed_1;
						Toast.makeText(getContext(), "当前为1倍速", 0).show();
						break;

					case speed_1:
						ijkVideoView.setSpeed(1.2f / videoSpeed);
						setText(getSpeedText(speed_12));
						currentSpeed = speed_12;
						Toast.makeText(getContext(), "当前为1.2倍速", 0).show();
						break;

					case speed_12:
						ijkVideoView.setSpeed(1.5f / videoSpeed);
						setText(getSpeedText(speed_15));
						currentSpeed = speed_15;
						Toast.makeText(getContext(), "当前为1.5倍速", 0).show();
						break;

					case speed_15:
						ijkVideoView.setSpeed(2f / videoSpeed);
						setText(getSpeedText(speed_2));
						currentSpeed = speed_2;
						Toast.makeText(getContext(), "当前为2倍速", 0).show();
						break;

					case speed_2:
						ijkVideoView.setSpeed(0.5f / videoSpeed);
						setText(getSpeedText(speed_05));
						currentSpeed = speed_05;
						Toast.makeText(getContext(), "当前为0.5倍速", 0).show();
						break;
					}
				}
			});
		} else {
			final Video video = ijkVideoView.getVideo();
			// 判断是否是加密视频
			// 6.0以下非加密视频不显示倍速按钮,加密视频没有倍速时也不显示倍速按钮
			if (video != null && (video.getFullmp4() == 1 || video.getSeed() == 1) && video.getHls15X().size() > 0) {
				setVisibility(View.VISIBLE);
				// 由于设置倍速后控件会被重置，故这里要先判断当前是否是1.5倍速
				if (ijkVideoView.getHlsSpeedType().equals(HlsSpeedType.SPEED_1_5X)) {
					setText(getSpeedText(speed_15));
					currentSpeed = speed_15;
				}
				setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						switch (currentSpeed) {
						case speed_1:
							localPlayClickValidate(ijkVideoView, video, speed_1);
							break;

						case speed_15:
							localPlayClickValidate(ijkVideoView, video, speed_15);
							break;
						}
					}
				});
			}
		}
	}

}
