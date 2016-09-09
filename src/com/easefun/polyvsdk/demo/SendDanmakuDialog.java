package com.easefun.polyvsdk.demo;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.danmaku.DanmakuInfo;
import com.easefun.polyvsdk.danmaku.DanmakuManager;
import com.easefun.polyvsdk.danmaku.DanmakuManager.SendDanmakuListener;
import com.easefun.polyvsdk.ijk.IjkVideoView;
import com.easefun.polyvsdk.screenshot.ActivityTool;
import com.easefun.polyvsdk.util.TimeTool;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

public class SendDanmakuDialog extends Dialog {
	private static SendDanmakuDialog sendDanmakuDialog;
	DanmakuManager danmakuManager;
	private DanmakuContext mContext;
	private IDanmakuView mDanmakuView;
	private BaseDanmakuParser mParser;
	private IjkVideoView ijkVideoView;
	private String vid;
	private Context context;

	private EditText et_senddanmaku;
	private Button sendDanmaku;
	private Spinner sp_selcor, sp_fontsize, sp_fontmode;
	private int color = Color.WHITE, fontsize = 24;
	private String fontmode = "roll";

	public SendDanmakuDialog(Context context) {
		super(context, R.style.NoBorderDialog);
		this.context = context;
	}

	public SendDanmakuDialog setIjkVideoView(IjkVideoView ijkVideoView) {
		this.ijkVideoView = ijkVideoView;
		return this;
	}

	public SendDanmakuDialog setVid(String vid) {
		this.vid = vid;
		return this;
	}

	public SendDanmakuDialog setDanmakuContext(DanmakuContext mContext) {
		this.mContext = mContext;
		return this;
	}

	public SendDanmakuDialog setIDanmakuView(IDanmakuView mDanmakuView) {
		this.mDanmakuView = mDanmakuView;
		return this;
	}

	public SendDanmakuDialog setBaseDanmakuParser(BaseDanmakuParser mParser) {
		this.mParser = mParser;
		return this;
	}

	public static SendDanmakuDialog getInstance(Context context) {
		if (sendDanmakuDialog == null) {
			synchronized (SendDanmakuDialog.class) {
				if (sendDanmakuDialog == null)
					sendDanmakuDialog = new SendDanmakuDialog(context);
			}
		}
		return sendDanmakuDialog;
	}

	public static void realse() {
		sendDanmakuDialog = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_senddanmaku);
		getWindow().setGravity(Gravity.TOP | Gravity.CENTER);
		initView();
	}

	private void initView() {
		danmakuManager = new DanmakuManager();
		et_senddanmaku = (EditText) findViewById(R.id.et_senddanmaku);
		et_senddanmaku.setTextColor(Color.WHITE);
		et_senddanmaku.setImeOptions(0x2000000 | 4);
		et_senddanmaku.setSingleLine();
		et_senddanmaku.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					// 发送弹幕
					sendDanmaku();
					dismiss();
					return true;
				}
				return false;
			}
		});
		
		//发送弹幕的按钮
		sendDanmaku=(Button) findViewById(R.id.sendDanmaku);
		sendDanmaku.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 发送弹幕
				sendDanmaku();
				dismiss();
			}
		});

		sp_selcor = (Spinner) findViewById(R.id.sp_selcol);
		sp_selcor.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				int[] colors = new int[] { Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
						Color.parseColor("#A020F0"), Color.BLACK };
				if (view instanceof TextView) {
					TextView tv_color = (TextView) view;
					tv_color.setHintTextColor(colors[position]);
					tv_color.setTextColor(colors[position]);
				}
				et_senddanmaku.setTextColor(colors[position]);
				et_senddanmaku.setHintTextColor(colors[position]);
				color = colors[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		sp_fontmode = (Spinner) findViewById(R.id.sp_fontmode);
		sp_fontmode.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String[] fontmodes = new String[] { "roll", "top", "bottom" };
				fontmode = fontmodes[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		sp_fontsize = (Spinner) findViewById(R.id.sp_fontsize);
		sp_fontsize.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				int[] fontsizes = new int[] { 24, 18, 16 };
				fontsize = fontsizes[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		this.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(ijkVideoView!=null)
					ijkVideoView.start();
				if (mDanmakuView != null)
					mDanmakuView.resume();
			}
		});
		this.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				// 显示键盘
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(et_senddanmaku, InputMethodManager.SHOW_IMPLICIT);
			}
		});
	}

	private void sendDanmaku() {
		// 弹幕在视频中生成的时间，格式为：01:03:05
		if (ijkVideoView == null)
			return;
		String time = TimeTool.generateTime(ijkVideoView.getCurrentPosition());
		// 设置要发送的弹幕
		/**
		 * DanmakuInfo
		 * 
		 * @param vid
		 *            视频id
		 * @param msg
		 *            弹幕信息，不能为空
		 * @param time
		 *            弹幕出现时间 格式：01:03:05
		 * @param fontSize
		 *            字体的大小 如：16,18,24 默认为18
		 * @param fontMode
		 *            弹幕的样式 取值有：top，bottom，roll 默认为roll
		 * @param fontColor
		 *            字体颜色 默认为：0xFFFFFF
		 */
		danmakuManager.setSendDanmaku(
				new DanmakuInfo(vid, et_senddanmaku.getText().toString(), time, fontsize, fontmode, color));
		// 是否成功将弹幕发送到服务器的监听器
		danmakuManager.setSendDanmakuListener(new SendDanmakuListener() {
			@Override
			public void fail(int category) {
				switch (category) {
				case DanmakuManager.DANMAKUINFO_IS_NULL:
					ActivityTool.toastMsg(context, "发送弹幕失败：弹幕对象为null");
					break;
				case DanmakuManager.NETWORK_EXCEPTION:
					ActivityTool.toastMsg(context, "发送弹幕失败：网络异常");
					break;
				case DanmakuManager.VID_OR_MSG_OR_TIME_ERROR:
					ActivityTool.toastMsg(context, "发送弹幕失败：弹幕信息/vid为空/时间格式错误");
					break;
				case DanmakuManager.RESPONSE_FAIL:
					ActivityTool.toastMsg(context, "发送弹幕失败：响应失败");
					break;
				}
			}

			@Override
			public void success() {
				ActivityTool.toastMsg(context, "发送弹幕成功");
			}
		});
		// 显示弹幕
		danmakuManager.showDanmaku(mDanmakuView, mParser, mContext);
		// 发送弹幕到服务器
		danmakuManager.sendDanmakuToServer();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(ijkVideoView!=null)
			ijkVideoView.start();
		if (mDanmakuView != null)
			mDanmakuView.resume();
	}
}
