package com.easefun.polyvsdk.demo;
  
import java.io.File;
import java.io.IOException;

import com.easefun.polyvsdk.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mHolder;
	private Camera mCamera;

	public MyCameraSurfaceView(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int weight, int height) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// make any resize, rotate or reformatting changes here

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();

		} catch (Exception e) {
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}
}
 
public class RecordActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record, menu);
		return true;
	}


	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Are you sure you want to exit?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    RecordActivity.this.finish();
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	               }
	           });
	    AlertDialog alert = builder.create(); 
	    alert.show();
	}
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		
		TextView timeCountTextView;
		private long startTime = 0L;
		long timeInMillies = 0L;
		long timeSwap = 0L;
		long finalTime = 0L;
		private Handler timeCounttingHandler = new Handler();
		private Runnable updateTimerMethod = new Runnable() {

			  public void run() {
			   timeInMillies = SystemClock.uptimeMillis() - startTime;
			   finalTime = timeSwap + timeInMillies;

			   int seconds = (int) (finalTime / 1000);
			   int minutes = seconds / 60;
			   seconds = seconds % 60;
			   timeCountTextView.setText("" + String.format("%02d", minutes) + ":"
			     + String.format("%02d", seconds) );
			   timeCounttingHandler.postDelayed(this, 0);
			  }

	   };


			
		private MediaRecorder mMediaRecorder;
		private Camera myCamera;
		private Button start;
		File path;
		boolean recording;
		private MyCameraSurfaceView myCameraSurfaceView;
		
		
		public PlaceholderFragment() {
		}
		@Override
	    public void onDetach() {
			releaseMediaRecorder(); // release the MediaRecorder
						// object
			releaseCamera();
			// Exit after saved
			
	        super.onDetach();
	        
	    }
		private void openCamera() {
			// TODO Auto-generated method stub
			if(myCamera==null){
				myCamera = Camera.open();
			}
			
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_record, container, false);
			start = (Button) rootView.findViewById(R.id.startButton);
			start.setOnClickListener(startListener);
			

			path = new File(Environment.getExternalStorageDirectory(), "myRecording.mp4");
			openCamera();
			if (myCamera == null) {
				Toast.makeText(getActivity(), "Fail to get Camera", Toast.LENGTH_LONG).show();
			}
			
			FrameLayout myCameraPreview = (FrameLayout) rootView.findViewById(R.id.videoview);
			myCameraSurfaceView = new MyCameraSurfaceView(getActivity(), myCamera);
			myCameraPreview.addView(myCameraSurfaceView);
			// Log.d("t", path.toString());
			// prepareMediaRecorder();
			
			
			timeCountTextView = (TextView) rootView.findViewById(R.id.timeCountTextView);
			timeCountTextView.setTextColor(Color.YELLOW);
			timeCountTextView.setTextSize(30);
			return rootView;
		}

		private void releaseMediaRecorder() {
			if (mMediaRecorder != null) {
				mMediaRecorder.stop();
				mMediaRecorder.reset(); // clear recorder configuration
				mMediaRecorder.release(); // release the recorder object
				mMediaRecorder = null;
				//myCamera.lock(); // lock camera for later use
			}
		}

		private void releaseCamera() {
			if (myCamera != null) {
				myCamera.release(); // release the camera for other applications
				myCamera = null;
			}
		}

		/*
		 * private void prepareMediaRecorder() { recorder = new MediaRecorder();
		 * //myCamera = getCameraInstance(); //myCamera.unlock();
		 * recorder.setCamera(myCamera);
		 * recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		 * recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		 * recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		 * recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		 * recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		 * //recorder
		 * .setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		 * recorder.setOutputFile(path.getAbsolutePath());
		 * 
		 * 
		 * 
		 * recorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface(
		 * )); try { recorder.prepare(); } catch (Exception e) {
		 * e.printStackTrace(); } }
		 */

		private boolean prepareVideoRecorder() {

			if (mMediaRecorder == null) {
				mMediaRecorder = new MediaRecorder();
			} else {
				Log.d("t", "MediaRecoder is Not Null");
			}
			openCamera();
			//myCamera.setDisplayOrientation(90);
			
			// Step 1: Unlock and set camera to MediaRecorder
			myCamera.stopPreview();
			myCamera.unlock();
			mMediaRecorder.setCamera(myCamera);

			// Step 2: Set sources
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

			// Step 3: Set output format and encoding (for versions prior to API
			// Level 8)
			CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
			camcorderProfile.videoFrameWidth = 1024;
			camcorderProfile.videoFrameHeight = 768;
			// camcorderProfile.videoFrameRate = 15;
			camcorderProfile.videoCodec = MediaRecorder.VideoEncoder.H264;
			// camcorderProfile.audioCodec = MediaRecorder.AudioEncoder.AAC;
			camcorderProfile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;

			mMediaRecorder.setProfile(camcorderProfile);

			mMediaRecorder.setMaxDuration(50000);
			
			// Step 4: Set output file
			mMediaRecorder.setOutputFile(path.getAbsolutePath());

			// Step 5: Set the preview output
			mMediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());

			// Step 6: Prepare configured MediaRecorder
			try {

				mMediaRecorder.prepare();
			} catch (IllegalStateException e) {
				Log.d("DEBUG", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
				releaseMediaRecorder();
				return false;
			} catch (IOException e) {
				Log.d("DEBUG", "IOException preparing MediaRecorder: " + e.getMessage());
				releaseMediaRecorder();
				return false;
			}
			return true;
		}

		private View.OnClickListener startListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					/*
					 * if(recording){
					 * 
					 * }else{ recording =true; } prepareVideoRecorder();
					 * mMediaRecorder.start();
					 * 
					 * start.setEnabled(false); stop.setEnabled(true);
					 */
					if (recording) {
						// stop recording and release camera
						//mMediaRecorder.stop(); // stop the recording
						releaseMediaRecorder(); // release the MediaRecorder
												// object
						releaseCamera();
						// Exit after saved
						getActivity().finish();
					} else {

						// Release Camera before MediaRecorder start
						releaseCamera();

						if (!prepareVideoRecorder()) {
							Toast.makeText(getActivity(),
									"Fail in prepareMediaRecorder()!\n - Ended -",
									Toast.LENGTH_LONG).show();
							getActivity().finish();
						}

						mMediaRecorder.start();
						recording = true;
						start.setText("结束");
						startTime = SystemClock.uptimeMillis();
						timeCounttingHandler.postDelayed(updateTimerMethod, 0);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		
	}

}
