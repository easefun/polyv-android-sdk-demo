package com.easefun.polyvsdk.demo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.easefun.polyvsdk.PolyvQuestionUtil;
import com.easefun.polyvsdk.QAFormatVO;
import com.easefun.polyvsdk.QuestionVO;
import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.QuestionVO.ChoicesVO;
import com.easefun.polyvsdk.ijk.IjkVideoView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 问答界面
 * @author TanQu 2016-1-25
 */
public class PolyvQuestionView extends RelativeLayout implements OnCheckedChangeListener {

	private Context context = null;
	private PopupWindow popupWindow = null;
	private View anchorView = null;
	private IjkVideoView ijkVideoView = null;
	private TextView passBtn = null;
	private Button submitBtn = null;
	private LinearLayout questionLayout = null;
	private LinearLayout choicesRadioLayout = null;
	private LinearLayout choicesCheckLayout = null;
	private DisplayImageOptions mOptions = null;
	private List<LinearLayout> answerRadioLayoutList = null;
	private List<RadioButton> answerRadioList = null;
	private List<LinearLayout> answerCheckLayoutList = null;
	private List<CheckBox> answerCheckList = null;
	private QuestionVO questionVO = null;
	private int rightAnswerNum = 0;
	private static final int PLEASE_SELECT_MSG = 1;
	private static final int ANSWER_TIPS_MSG = 2;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (((Activity) context).isFinishing() == false) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(msg.getData().getString("msg"));
				builder.setCancelable(false);
				
				switch (msg.what) {
					case PLEASE_SELECT_MSG:
						builder.setTitle("提示");
						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						});
						break;
					case ANSWER_TIPS_MSG:
						builder.setTitle("答案提示");
						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
								//重要，调用此方法，才会继续问答逻辑
								ijkVideoView.answerQuestionFault();
							}
						});
						break;
				}
				
				builder.show();
			}
		}
	};

    public PolyvQuestionView(Context context) {
        super(context);
        this.context = context;
        initViews();
    }
    
    public PolyvQuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initViews();
    }
    
    public PolyvQuestionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initViews();
    }
    
    public void setIjkVideoView(IjkVideoView ijkVideoView) {
    	this.ijkVideoView = ijkVideoView;
    }
    
    @SuppressLint("ShowToast")
	private void initViews(){
    	LayoutInflater.from(getContext()).inflate(R.layout.question, this);
    	passBtn = (TextView) findViewById(R.id.pass_btn);
    	passBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ijkVideoView.skipQuestion();
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						hide();
					}
				});
			}
		});
    	
    	submitBtn = (Button) findViewById(R.id.submit_btn);
    	submitBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (rightAnswerNum == 0) return;
				List<Integer> indexList = new ArrayList<Integer>();
				if (rightAnswerNum > 1) {
					int index = 0;
					for (CheckBox answerCheckBox : answerCheckList) {
						if (answerCheckBox.isChecked()) {
							indexList.add(index);
						}
						
			    		index++;
			    	}
				} else {
					int index = 0;
					for (RadioButton answerRadioBtn : answerRadioList) {
			    		if (answerRadioBtn.isChecked()) {
			    			indexList.add(index);
			    		}
			    		
			    		index++;
			    	}
				}
				
				if (indexList.size() == 0) {
					Message message = new Message();
					message.what = PLEASE_SELECT_MSG;
					
					Bundle bundle = new Bundle();
					bundle.putString("msg", "请选择一个答案");
					message.setData(bundle);
					
					handler.sendMessage(message);
					return;
				}
				
				ijkVideoView.answerQuestion(indexList);
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						hide();
					}
				});
			}
		});
    	
    	questionLayout = (LinearLayout) findViewById(R.id.question_layout);
    	choicesRadioLayout = (LinearLayout) findViewById(R.id.choices_radio_layout);
    	choicesCheckLayout = (LinearLayout) findViewById(R.id.choices_check_layout);
    	
    	answerRadioLayoutList = new ArrayList<LinearLayout>();
    	LinearLayout answerRadioLayout1 = (LinearLayout) findViewById(R.id.answer_radio_layout_1);
    	answerRadioLayoutList.add(answerRadioLayout1);
    	LinearLayout answerRadioLayout2 = (LinearLayout) findViewById(R.id.answer_radio_layout_2);
    	answerRadioLayoutList.add(answerRadioLayout2);
    	LinearLayout answerRadioLayout3 = (LinearLayout) findViewById(R.id.answer_radio_layout_3);
    	answerRadioLayoutList.add(answerRadioLayout3);
    	LinearLayout answerRadioLayout4 = (LinearLayout) findViewById(R.id.answer_radio_layout_4);
    	answerRadioLayoutList.add(answerRadioLayout4);
    	
    	answerRadioList = new ArrayList<RadioButton>();
    	RadioButton answerRadio1 = (RadioButton) findViewById(R.id.answer_radio_1);
    	answerRadio1.setOnCheckedChangeListener(this);
    	answerRadioList.add(answerRadio1);
    	RadioButton answerRadio2 = (RadioButton) findViewById(R.id.answer_radio_2);
    	answerRadio2.setOnCheckedChangeListener(this);
    	answerRadioList.add(answerRadio2);
    	RadioButton answerRadio3 = (RadioButton) findViewById(R.id.answer_radio_3);
    	answerRadio3.setOnCheckedChangeListener(this);
    	answerRadioList.add(answerRadio3);
    	RadioButton answerRadio4 = (RadioButton) findViewById(R.id.answer_radio_4);
    	answerRadio4.setOnCheckedChangeListener(this);
    	answerRadioList.add(answerRadio4);
    	
    	answerCheckLayoutList = new ArrayList<LinearLayout>();
    	LinearLayout answerCheckLayout1 = (LinearLayout) findViewById(R.id.answer_check_layout_1);
    	answerCheckLayoutList.add(answerCheckLayout1);
    	LinearLayout answerCheckLayout2 = (LinearLayout) findViewById(R.id.answer_check_layout_2);
    	answerCheckLayoutList.add(answerCheckLayout2);
    	LinearLayout answerCheckLayout3 = (LinearLayout) findViewById(R.id.answer_check_layout_3);
    	answerCheckLayoutList.add(answerCheckLayout3);
    	LinearLayout answerCheckLayout4 = (LinearLayout) findViewById(R.id.answer_check_layout_4);
    	answerCheckLayoutList.add(answerCheckLayout4);
    	
    	answerCheckList = new ArrayList<CheckBox>();
    	CheckBox answerCheck1 = (CheckBox) findViewById(R.id.answer_check_1);
    	answerCheckList.add(answerCheck1);
    	CheckBox answerCheck2 = (CheckBox) findViewById(R.id.answer_check_2);
    	answerCheckList.add(answerCheck2);
    	CheckBox answerCheck3 = (CheckBox) findViewById(R.id.answer_check_3);
    	answerCheckList.add(answerCheck3);
    	CheckBox answerCheck4 = (CheckBox) findViewById(R.id.answer_check_4);
    	answerCheckList.add(answerCheck4);
    	
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
    		popupWindow = new PopupWindow(context);
    		popupWindow.setContentView(this);
    	}
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			for (RadioButton radioButton : answerRadioList) {
				if (radioButton.getId() != buttonView.getId()) {
					radioButton.setChecked(false);
				}
			}
		}
	}
	
	/**
	 * 显示
	 * @param anchorView
	 * @param questionVO
	 */
	public void show(View anchorView, QuestionVO questionVO) {
		this.anchorView = anchorView;
		this.questionVO = questionVO;
		refresh();
	}
	
	/**
	 * 重新设置控件
	 */
	public void refresh() {
		int[] location = new int[2];
		anchorView.getLocationInWindow(location);
		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchorView.getWidth(), location[1] + anchorView.getHeight());
		popupWindow.setWidth(anchorView.getWidth());
		popupWindow.setHeight(anchorView.getHeight());
		popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, 0, anchorRect.top);
		
		if (questionVO.isSkip()) {
    		passBtn.setVisibility(View.VISIBLE);
    	}
    	
    	questionLayout.removeAllViews();
    	
    	for (RadioButton answerRadioBtn : answerRadioList) {
    		answerRadioBtn.setVisibility(View.GONE);
    		answerRadioBtn.setChecked(false);
    	}
    	
    	for (CheckBox answerCheckBox : answerCheckList) {
    		answerCheckBox.setVisibility(View.GONE);
    		answerCheckBox.setChecked(false);
    	}
    	
    	for (LinearLayout answerRadioLayout : answerRadioLayoutList) {
    		answerRadioLayout.setVisibility(View.GONE);
    		for (int i = 1, length = answerRadioLayout.getChildCount() ; i < length ; i++) {
    			answerRadioLayout.removeViewAt(i);
    		}
    	}
    	
    	for (LinearLayout answerCheckLayout : answerCheckLayoutList) {
    		answerCheckLayout.setVisibility(View.GONE);
    		for (int i = 1, length = answerCheckLayout.getChildCount() ; i < length ; i++) {
    			answerCheckLayout.removeViewAt(i);
    		}
    	}
    	
    	List<QAFormatVO> list = null;
    	try {
    		list = PolyvQuestionUtil.parseQA(questionVO.getQuestion());
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	TextView textView = null;
    	ImageView imageView = null;
    	for (QAFormatVO qaFormatVO : list) {
    		switch (qaFormatVO.getStringType()) {
    		case STRING:
    			textView = new TextView(context);
    			textView.setText(qaFormatVO.getStr());
    			textView.setTextColor(Color.parseColor("#ffffff"));
    			questionLayout.addView(textView);
    			break;
    		case URL:
    			imageView = new ImageView(context);
    			ImageLoader.getInstance().displayImage(qaFormatVO.getStr(), imageView, mOptions, new AnimateFirstDisplayListener());
    			questionLayout.addView(imageView);
    			break;
    		}
    	}
    	
    	//单个正确答案是单选
    	//多个正确答案是多选
    	int rightAnswerNum = 0;
    	List<ChoicesVO> choicesList = questionVO.getChoicesList();
    	for (ChoicesVO choicesVO : choicesList) {
    		if (choicesVO.getRightAnswer() == 1) {
    			rightAnswerNum++;
    		}
    	}
    	
    	this.rightAnswerNum = rightAnswerNum;
    	if (rightAnswerNum > 1) {
    		choicesCheckLayout.setVisibility(View.VISIBLE);
    		choicesRadioLayout.setVisibility(View.GONE);
    	} else {
    		choicesCheckLayout.setVisibility(View.GONE);
    		choicesRadioLayout.setVisibility(View.VISIBLE);
    	}
    	
    	LinearLayout answerLayout = null;
    	RadioButton answerRadioBtn = null;
    	CheckBox answerCheckBox = null;
    	int index = 0;
    	for (ChoicesVO choicesVO : choicesList) {
    		if (rightAnswerNum > 1) {
    			answerLayout = answerCheckLayoutList.get(index);
    			answerCheckBox = answerCheckList.get(index);
    			answerCheckBox.setVisibility(View.VISIBLE);
    		} else {
    			answerLayout = answerRadioLayoutList.get(index);
    			answerRadioBtn = answerRadioList.get(index);
    			answerRadioBtn.setVisibility(View.VISIBLE);
    		}
    		
    		answerLayout.setVisibility(View.VISIBLE);
    		try {
				list = PolyvQuestionUtil.parseQA(choicesVO.getAnswer());
			} catch (JSONException e) {
				e.printStackTrace();
			}
    		
    		for (QAFormatVO qaFormatVO : list) {
	    		switch (qaFormatVO.getStringType()) {
	    		case STRING:
	    			textView = new TextView(context);
	    			textView.setText(qaFormatVO.getStr());
	    			textView.setTextColor(Color.parseColor("#ffffff"));
	    			answerLayout.addView(textView);
	    			break;
	    		case URL:
	    			imageView = new ImageView(context);
	    			ImageLoader.getInstance().displayImage(qaFormatVO.getStr(), imageView, mOptions, new AnimateFirstDisplayListener());
	    			answerLayout.addView(imageView);
	    			break;
	    		}
	    	}
    		
    		index++;
    	}
	}
	
	/**
	 * 是否在显示中
	 * @return
	 */
	public boolean isShowing() {
		return this.getVisibility() == View.VISIBLE;
	}
	
	/**
	 * 隐藏
	 */
	public void hide() {
		popupWindow.dismiss();
	}
	
	/**
	 * 显示答案提示
	 * @param msg
	 */
	public void showAnswerTips(String msg) {
		Message message = new Message();
		message.what = ANSWER_TIPS_MSG;
		
		Bundle bundle = new Bundle();
		bundle.putString("msg", msg);
		message.setData(bundle);
		
		handler.sendMessage(message);
	}
}