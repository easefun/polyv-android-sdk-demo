package com.easefun.polyvsdk.demo.view;

import com.easefun.polyvsdk.demo.MediaController;
import com.easefun.polyvsdk.ijk.IjkVideoView;

import android.content.Context;
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

	public SpeedButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SpeedButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SpeedButton(Context context) {
		this(context, null);
	}

	public void init(final IjkVideoView ijkVideoView, MediaController.OnUpdateStartNow onUpdateStartNow) {
		this.onUpdateStartNow = onUpdateStartNow;
		setVisibility(View.VISIBLE);
		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (currentSpeed) {
				case speed_05:
					ijkVideoView.setSpeed(1f);
					setText(getSpeedText(speed_1));
					currentSpeed = speed_1;
					Toast.makeText(getContext(), "当前为1倍速", 0).show();
					break;

				case speed_1:
					ijkVideoView.setSpeed(1.2f);
					setText(getSpeedText(speed_12));
					currentSpeed = speed_12;
					Toast.makeText(getContext(), "当前为1.2倍速", 0).show();
					break;

				case speed_12:
					ijkVideoView.setSpeed(1.5f);
					setText(getSpeedText(speed_15));
					currentSpeed = speed_15;
					Toast.makeText(getContext(), "当前为1.5倍速", 0).show();
					break;

				case speed_15:
					ijkVideoView.setSpeed(2f);
					setText(getSpeedText(speed_2));
					currentSpeed = speed_2;
					Toast.makeText(getContext(), "当前为2倍速", 0).show();
					break;

				case speed_2:
					ijkVideoView.setSpeed(0.5f);
					setText(getSpeedText(speed_05));
					currentSpeed = speed_05;
					Toast.makeText(getContext(), "当前为0.5倍速", 0).show();
					break;
				}
			}
		});
	}

}
