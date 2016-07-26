package com.easefun.polyvsdk.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.Video;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.PopupWindow.OnDismissListener;

/**
 * 字幕选择弹出视图
 * @author TanQu 2016-5-9
 */
public class PolyvPlayerSRTPopupView extends RelativeLayout implements View.OnClickListener {
	private Context mContext = null;
	private PopupWindow mPopupWindow = null;
	private Button closeBtn = null;
	private LinearLayout sRTSelectListLayout = null;
	private List<SRTRadioButton> optionList = null;
	
	private Callback mCallback = null;
	private boolean isShowing = false;

	public PolyvPlayerSRTPopupView(Context context) {
		super(context);
		this.mContext = context;
		initViews();
	}
	
	public PolyvPlayerSRTPopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initViews();
    }
    
    public PolyvPlayerSRTPopupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initViews();
    }

	private void initViews() {
		LayoutInflater.from(mContext).inflate(R.layout.polyv_player_srt_popup_view, this);
		closeBtn = (Button) findViewById(R.id.srt_popup_view_close_btn);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hide();
			}
		});
		
		sRTSelectListLayout = (LinearLayout) findViewById(R.id.srt_select_list);
		optionList = new ArrayList<SRTRadioButton>();
		
		mPopupWindow = new PopupWindow(mContext);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					return true;
				}
				
				return false;
			}
		});
		
		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				isShowing = false;
				if (mCallback != null)
					mCallback.onPopupViewDismiss();
			}
		});
		
		mPopupWindow.setContentView(this);
	}

	public void show(View anchor, Video video, String currSRTKey) {
		sRTSelectListLayout.removeAllViews();
		optionList.clear();
		
		int index = 1;
		SRTRadioButton radioButton = null;
		radioButton = new SRTRadioButton(mContext, "");
		radioButton.setText("无");
		radioButton.setId(index);
		radioButton.setOnClickListener(this);
		if (TextUtils.isEmpty(currSRTKey)) {
			radioButton.setChecked(true);
		}
		sRTSelectListLayout.addView(radioButton);
		optionList.add(radioButton);
		
		if (video != null) {
			Map<String, String> sRTMap = video.getVideoSRT();
			for (Map.Entry<String, String> entry : sRTMap.entrySet()) {
				index++;
				radioButton = new SRTRadioButton(mContext, entry.getKey());
				radioButton.setText(entry.getKey());
				radioButton.setId(index);
				radioButton.setOnClickListener(this);
				if (entry.getKey().equals(currSRTKey)) {
					radioButton.setChecked(true);
				}
				sRTSelectListLayout.addView(radioButton);
				optionList.add(radioButton);
			}
		}
		
		mPopupWindow.setWidth(anchor.getWidth());
		mPopupWindow.setHeight(110);
		mPopupWindow.showAtLocation(anchor, Gravity.CENTER, 0, 0);
		isShowing = true;
	}

	public void hide() {
		mPopupWindow.dismiss();
	}

	public boolean isShowing() {
		return isShowing;
	}

	public void setCallback(Callback callback) {
		mCallback = callback;
	}

	public Callback getCallback() {
		return mCallback;
	}

	/**
	 * 回调
	 * @author TanQu 2015-11-26
	 */
	public interface Callback {
		
		/**
		 * 选择了码率
		 * @param bitRateEnum
		 */
		void onSRTSelected(String key);

		/**
		 * 弹出视图解除
		 */
		void onPopupViewDismiss();
	}

	@Override
	public void onClick(View v) {
		for (SRTRadioButton radioBtn : optionList) {
			if (radioBtn.getId() != v.getId()) {
				radioBtn.setChecked(false);
			}
		}
		
		SRTRadioButton radioBtn = (SRTRadioButton) v;
		if (mCallback != null) {
			mCallback.onSRTSelected(radioBtn.getKey());
		}
	}
	
	public class SRTRadioButton extends RadioButton {

		private String key = "";
		
		public SRTRadioButton(Context context) {
			super(context);
		}
		
		public SRTRadioButton(Context context, String key) {
			super(context);
			this.key = key;
		}

		public String getKey() {
			return key;
		}
		
	}
}
