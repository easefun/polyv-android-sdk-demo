package com.easefun.polyvsdk.demo;

import java.util.ArrayList;

import com.easefun.polyvsdk.R;
import com.easefun.polyvsdk.demo.RecordActivity;
import com.easefun.polyvsdk.demo.download.PolyvDownloadListActivity;
import com.easefun.polyvsdk.demo.upload.PolyvUploadListActivity;
import com.easefun.polyvsdk.demo.IjkVideoActicity;
import com.easefun.polyvsdk.server.AndroidService;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;

/**
 * 主界面
 * @author TanQu 2016-7-7
 */
public class NewTestActivity extends Activity implements View.OnClickListener {
	
	// sl8da4jjbx684cdae6bf17b1b70a8354_s 非加密
	// sl8da4jjbx80cb8878980c1626c51923_s 加密
	private static String videoId = "sl8da4jjbx80cb8878980c1626c51923_s";
	
	private final static int VIDEO_LIST_RESULT = 100;
    private final static int DOWNLOAD_LIST_RESULT = 101;
    private final static int ONLINE_VIDEO_PORTRAIT_RESULT = 102;
    private final static int ONLINE_VIDEO_LAND_SCAPE_RESULT = 103;
    private final static int RECORD_VIDEO_RESULT = 104;
    private final static int UPLOAD_RESULT = 105;
    private final static int SETTING_RESULT = 106;
	
	private MyBroadcastReceiver myBroadcastReceiver = null;
	private SharedPreferences sharedPreferences = null;
	/** 在线视频列表 */
	private Button videoListBtn = null;
	/** 下载任务列表 */
	private Button downloadListBtn = null;
	/** 播放(竖屏) */
	private Button onlineVideoPortraitBtn = null;
	/** 播放(横屏) */
	private Button onlineVideoLandScapeBtn = null;
	/** 录制视频 */
	private Button recordVideoBtn = null;
	/** 上传视频 */
	private Button uploadBtn = null;
	
	/** 请求的权限列表 */
	private ArrayList<String> permissionsToRequest = null;
	/** 拒绝的权限列表 */
    private ArrayList<String> permissionsRejected = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
		videoListBtn = (Button) findViewById(R.id.video_list);
		videoListBtn.setOnClickListener(this);
		downloadListBtn = (Button) findViewById(R.id.download_list);
		downloadListBtn.setOnClickListener(this);
		onlineVideoPortraitBtn = (Button) findViewById(R.id.online_video_portrait);
		onlineVideoPortraitBtn.setOnClickListener(this);
		onlineVideoLandScapeBtn = (Button) findViewById(R.id.online_video_land_scape);
		onlineVideoLandScapeBtn.setOnClickListener(this);
		recordVideoBtn = (Button) findViewById(R.id.record_video);
		recordVideoBtn.setOnClickListener(this);
		uploadBtn = (Button) findViewById(R.id.upload);
		uploadBtn.setOnClickListener(this);
		
		//如果httpd service 启动失败，就会发送消息上来提醒失败了
		IntentFilter statusIntentFilter = new IntentFilter(AndroidService.SERVICE_ERROR_BROADCAST_ACTION);
		statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		myBroadcastReceiver = new MyBroadcastReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, statusIntentFilter);
	}
	
	@Override
    public void onClick(View v) {
//		targetSdkVersion 设置了23或以上需要实现运行时权限功能，否则无法播放视频
//		onClickExecuteOptions(v.getId());
//		非23或以上可以直接调用onClickExecuteOptions方法，注释掉以下使用到的其他代码
		if (canMakeSmores() == false) {
			onClickExecuteOptions(v.getId());
			return;
		}
		
        ArrayList<String> permissions = new ArrayList<String>();
        int resultCode = 0;
        switch(v.getId()){
        case R.id.video_list:
        	//播放视频需要的权限
        	permissions.add(permission.READ_PHONE_STATE);
        	permissions.add(permission.WRITE_EXTERNAL_STORAGE);
        	resultCode = VIDEO_LIST_RESULT;
        	break;
        case R.id.download_list:
        	//下载需要的权限
        	permissions.add(permission.READ_PHONE_STATE);
        	permissions.add(permission.WRITE_EXTERNAL_STORAGE);
        	resultCode = DOWNLOAD_LIST_RESULT;
        	break;
        case R.id.online_video_portrait:
        	//播放视频需要的权限
        	permissions.add(permission.READ_PHONE_STATE);
        	permissions.add(permission.WRITE_EXTERNAL_STORAGE);
        	resultCode = ONLINE_VIDEO_PORTRAIT_RESULT;
        	break;
        case R.id.online_video_land_scape:
        	//播放视频需要的权限
        	permissions.add(permission.READ_PHONE_STATE);
        	permissions.add(permission.WRITE_EXTERNAL_STORAGE);
        	resultCode = ONLINE_VIDEO_LAND_SCAPE_RESULT;
        	break;
        case R.id.record_video:
        	//录制视频需要的权限
        	permissions.add(permission.CAMERA);
        	permissions.add(permission.RECORD_AUDIO);
        	permissions.add(permission.WRITE_EXTERNAL_STORAGE);
        	resultCode = RECORD_VIDEO_RESULT;
        	break;
        case R.id.upload:
        	//上传需要的权限
        	permissions.add(permission.READ_PHONE_STATE);
        	permissions.add(permission.WRITE_EXTERNAL_STORAGE);
        	resultCode = UPLOAD_RESULT;
        	break;
        }

        //筛选出我们已经接受的权限
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.
        permissionsRejected = findRejectedPermissions(permissions);

        if(permissionsToRequest.size()>0){//we need to ask for permissions
            //but have we already asked for them?
            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), resultCode);
            //mark all these as asked..
            for(String perm : permissionsToRequest){
                markAsAsked(perm);
            }
        }else{
            if(permissionsRejected.size()>0){
                //we have none to request but some previously rejected..tell the user.
                AlertDialog.Builder builder = new AlertDialog.Builder(NewTestActivity.this);
				builder.setTitle("提示");
				builder.setMessage("需要权限被拒绝，是否允许再次提示权限申请？");
				builder.setPositiveButton("允许", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						for(String perm: permissionsRejected){
                            clearMarkAsAsked(perm);
                        }
						
						dialog.dismiss();
					}
				});
				
				builder.setNegativeButton("取消", null);
				builder.setCancelable(true);
				builder.show();
            } else {
            	requestPermissionWriteSettings(v.getId());
            }
        }
    }
	
	private int id = 0;
	
	/**
	 * 请求写入设置的权限
	 * @param id
	 */
	@SuppressLint("InlinedApi")
	private void requestPermissionWriteSettings(int id) {
		if (Settings.System.canWrite(this)) {
			onClickExecuteOptions(id);
		} else {
			this.id = id;
			Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, SETTING_RESULT);
		}
	}
	
	@Override
	@SuppressLint("InlinedApi")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == SETTING_RESULT) {
	    	if (Settings.System.canWrite(this)) {
	    		if (id == 0) {
	    			new AlertDialog.Builder(this)
		                .setTitle("message")
		                .setMessage("id is 0")
		                .setPositiveButton(android.R.string.ok, null)
		                .show();
	    		} else {
	    			onClickExecuteOptions(id);
	    		}
	    	} else {
	    		new AlertDialog.Builder(this)
	                .setTitle("showPermissionInternet")
	                .setMessage(Settings.ACTION_MANAGE_WRITE_SETTINGS + " not granted")
	                .setPositiveButton(android.R.string.ok, null)
	                .show();
	    	}
	    }
	}
	
	private void onClickExecuteOptions(int id) {
		switch(id){
        case R.id.video_list:
        	Intent videolist = new Intent(NewTestActivity.this, VideoListActivity.class);
			startActivity(videolist);
        	break;
        case R.id.download_list:
        	Intent downloadlist = new Intent(NewTestActivity.this, PolyvDownloadListActivity.class);
			startActivity(downloadlist);
        	break;
        case R.id.online_video_portrait:
        	IjkVideoActicity.intentTo(NewTestActivity.this, IjkVideoActicity.PlayMode.portrait, IjkVideoActicity.PlayType.vid, videoId, false);
        	break;
        case R.id.online_video_land_scape:
        	IjkVideoActicity.intentTo(NewTestActivity.this, IjkVideoActicity.PlayMode.landScape, IjkVideoActicity.PlayType.vid, videoId, false);
        	break;
        case R.id.record_video:
        	Intent myIntent = new Intent(NewTestActivity.this, RecordActivity.class);
			startActivity(myIntent);
        	break;
        case R.id.upload:
        	Intent intent = new Intent(NewTestActivity.this, PolyvUploadListActivity.class);
			startActivity(intent);
        	break;
        }
	}
	
	/**
     * This is the method that is hit after the user accepts/declines the
     * permission you requested. For the purpose of this example I am showing a "success" header
     * when the user accepts the permission and a snackbar when the user declines it.  In your application
     * you will want to handle the accept/decline in a way that makes sense.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    	switch (requestCode) {
    	case VIDEO_LIST_RESULT:
    		if (hasPermission(permission.READ_PHONE_STATE)
    				&& hasPermission(permission.WRITE_EXTERNAL_STORAGE)) {
    			requestPermissionWriteSettings(R.id.video_list);
    		} else {
    			makePostRequestSnack();
    		}
    		break;
    	case DOWNLOAD_LIST_RESULT:
    		if (hasPermission(permission.READ_PHONE_STATE)
    				&& hasPermission(permission.WRITE_EXTERNAL_STORAGE)) {
    			requestPermissionWriteSettings(R.id.download_list);
    		} else {
    			makePostRequestSnack();
    		}
    		break;
    	case ONLINE_VIDEO_PORTRAIT_RESULT:
    		if (hasPermission(permission.READ_PHONE_STATE)
    				&& hasPermission(permission.WRITE_EXTERNAL_STORAGE)) {
    			requestPermissionWriteSettings(R.id.online_video_portrait);
    		} else {
    			makePostRequestSnack();
    		}
    		break;
    	case ONLINE_VIDEO_LAND_SCAPE_RESULT:
    		if (hasPermission(permission.READ_PHONE_STATE)
    				&& hasPermission(permission.WRITE_EXTERNAL_STORAGE)) {
    			requestPermissionWriteSettings(R.id.online_video_land_scape);
    		} else {
    			makePostRequestSnack();
    		}
    		break;
    	case RECORD_VIDEO_RESULT:
    		if (hasPermission(permission.CAMERA)
    				&& hasPermission(permission.RECORD_AUDIO)
    				&& hasPermission(permission.WRITE_EXTERNAL_STORAGE)) {
    			requestPermissionWriteSettings(R.id.record_video);
    		} else {
    			makePostRequestSnack();
    		}
    		break;
    	case UPLOAD_RESULT:
    		if (hasPermission(permission.READ_PHONE_STATE)
    				&& hasPermission(permission.WRITE_EXTERNAL_STORAGE)) {
    			requestPermissionWriteSettings(R.id.upload);
    		} else {
    			makePostRequestSnack();
    		}
    		break;
    	}
    }
	
    /**
     * a method that will centralize the showing of a snackbar
     */
    private void makePostRequestSnack() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(NewTestActivity.this);
		builder.setTitle("提示");
		builder.setMessage("需要权限被拒绝，是否允许再次提示权限申请？");
		builder.setPositiveButton("允许", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				for(String perm: permissionsRejected){
                    clearMarkAsAsked(perm);
                }
				
				dialog.dismiss();
			}
		});
		
		builder.setNegativeButton("取消", null);
		builder.setCancelable(true);
		builder.show();
    }
    
	/**
     * method that will return whether the permission is accepted. By default it is true if the user is using a device below
     * version 23
     * @param permission
     * @return
     */
    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            return(checkSelfPermission(permission)== PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    /**
     * method to determine whether we have asked
     * for this permission before.. if we have, we do not want to ask again.
     * They either rejected us or later removed the permission.
     * @param permission
     * @return
     */
    private boolean shouldWeAsk(String permission) {
        return(sharedPreferences.getBoolean(permission, true));
    }

    /**
     * we will save that we have already asked the user
     * @param permission
     */
    private void markAsAsked(String permission) {
        sharedPreferences.edit().putBoolean(permission, false).apply();
    }

    /**
     * We may want to ask the user again at their request.. Let's clear the
     * marked as seen preference for that permission.
     * @param permission
     */
    private void clearMarkAsAsked(String permission) {
        sharedPreferences.edit().putBoolean(permission, true).apply();
    }


    /**
     * This method is used to determine the permissions we do not have accepted yet and ones that we have not already
     * bugged the user about.  This comes in handle when you are asking for multiple permissions at once.
     * @param wanted
     * @return
     */
    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm) && shouldWeAsk(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    /**
     * this will return us all the permissions we have previously asked for but
     * currently do not have permission to use. This may be because they declined us
     * or later revoked our permission. This becomes useful when you want to tell the user
     * what permissions they declined and why they cannot use a feature.
     * @param wanted
     * @return
     */
    private ArrayList<String> findRejectedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm) && !shouldWeAsk(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    /**
     * Just a check to see if we have marshmallows (version 23)
     * @return
     */
    private boolean canMakeSmores() {
        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myBroadcastReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcastReceiver);
			myBroadcastReceiver = null;
		}
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int count = intent.getIntExtra("count", 0);
			AlertDialog.Builder builder = new AlertDialog.Builder(NewTestActivity.this);
			builder.setTitle("提示");
			builder.setMessage(String.format("%d次重试都没有成功开启server,请截图联系客服", count));
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			});
			
			builder.setCancelable(false);
			builder.show();
		}
	}
}
