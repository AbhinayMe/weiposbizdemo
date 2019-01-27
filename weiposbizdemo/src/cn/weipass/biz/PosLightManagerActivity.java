package cn.weipass.biz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import cn.weipass.pos.sdk.MiniLightManager;
import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
 * 
 * @author Tianhui MINI灯光控制
 */
public class PosLightManagerActivity extends Activity implements OnClickListener, SurfaceHolder.Callback {

	private MiniLightManager mMiniLightManger;

	private static final String tag = "PosLightManagerActivity";
	private boolean isPreview = false;
	private SurfaceView mPreviewSV = null;
	private SurfaceHolder mySurfaceHolder = null;
	private Camera myCamera = null;
	private AutoFocusCallback myAutoFocusCallback = null;
	boolean flag = true;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		Window myWindow = this.getWindow();
		myWindow.setFlags(flag, flag);

		setContentView(R.layout.pos_light_manager);

		mMiniLightManger = WeiposImpl.as().openMiniLightManager();

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("旺POS灯光管理测试");
		findViewById(R.id.btn_return).setOnClickListener(this);

		findViewById(R.id.btn_oper_1).setOnClickListener(this);
		findViewById(R.id.btn_oper_2).setOnClickListener(this);
		findViewById(R.id.btn_oper_3).setOnClickListener(this);
		findViewById(R.id.btn_oper_4).setOnClickListener(this);

		mPreviewSV = (SurfaceView) findViewById(R.id.capture_preview);

		mySurfaceHolder = mPreviewSV.getHolder();
		mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		mySurfaceHolder.addCallback(this);
		mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		myAutoFocusCallback = new AutoFocusCallback() {

			public void onAutoFocus(boolean success, Camera camera) {
				// TODO Auto-generated method stub
				if (success) {
					Log.i(tag, "myAutoFocusCallback: success...");

				} else {
					Log.i(tag, "myAutoFocusCallback: 澶辫触浜�?.");

				}

			}
		};

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = null;
		switch (v.getId()) {
		case R.id.btn_return:
			onBackPressed();
			break;
		case R.id.btn_oper_1:
			if (mMiniLightManger != null) {
				mMiniLightManger.closeRedOpenWhite();
			} else {
				Toast.makeText(this, "初始化MINI灯光服务失败", Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.btn_oper_2:
			if (mMiniLightManger != null) {
				mMiniLightManger.openRedCloseWhite();
			} else {
				Toast.makeText(this, "初始化MINI灯光服务失败", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_oper_3:
			if (mMiniLightManger != null) {
				mMiniLightManger.openRedAndWhite();
			} else {
				Toast.makeText(this, "初始化MINI灯光服务失败", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_oper_4:
			if (mMiniLightManger != null) {
				mMiniLightManger.closeRedAndWhite();
			} else {
				Toast.makeText(this, "初始化MINI灯光服务失败", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		Log.i(tag, "SurfaceHolder.Callback:surfaceChanged!");
		initCamera();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		try {
			myCamera = Camera.open();
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(this, "开启相机失败", Toast.LENGTH_SHORT).show();
		}
		
		if (myCamera==null) {
			onBackPressed();
			return;
		}
		try {
			myCamera.setPreviewDisplay(mySurfaceHolder);
			Log.i(tag, "SurfaceHolder.Callback: surfaceCreated!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (null != myCamera) {
				myCamera.release();
				myCamera = null;
			}
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.i(tag, "SurfaceHolder.Callback锛歋urface Destroyed");
		if (null != myCamera) {
			myCamera.setPreviewCallback(null);

			myCamera.stopPreview();
			isPreview = false;
			myCamera.release();
			myCamera = null;
		}
	}

	public void initCamera() {
		if (isPreview) {
			myCamera.stopPreview();
		}
		if (null != myCamera) {
			Camera.Parameters myParam = myCamera.getParameters();

			myParam.setPictureFormat(PixelFormat.JPEG);// 璁剧疆鎷嶇収鍚庡瓨鍌ㄧ殑鍥剧墖鏍煎紡

			// myParam.set("rotation", 90);
			myCamera.setDisplayOrientation(90);
			List<String> focuseMode = (myParam.getSupportedFocusModes());
			for (int i = 0; i < focuseMode.size(); i++) {
				Log.i(tag, focuseMode.get(i));
				if (focuseMode.get(i).contains("continuous")) {
					myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				} else {
					myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

				}
			}
			myCamera.setParameters(myParam);
			myCamera.startPreview();
			myCamera.autoFocus(myAutoFocusCallback);
			isPreview = true;
		}
	}
}
