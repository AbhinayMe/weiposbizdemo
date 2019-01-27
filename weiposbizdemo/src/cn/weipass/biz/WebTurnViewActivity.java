package cn.weipass.biz;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.TextView;
import cn.weipass.biz.html5.DiyJavaScriptInterface;
import cn.weipass.biz.html5.DiyWebChromeClient;
import cn.weipass.biz.html5.DiyWebViewClient;

/**
 * url跳转处理
 *
 * @author TIANHUI
 *
 */
public class WebTurnViewActivity extends Activity implements
		OnClickListener {
	protected WebView webView;
	protected String lastBackUrl;
	protected Handler handler;
	private boolean isLoading = false;
	private boolean isFirstLoadOk = false;
	private int sameBackCount = 0;
	public Resources res;

	private DiyJavaScriptInterface diyJs;
	private DiyWebChromeClient myDiyWebChromeClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.web_view_diy_home);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("JS与本地代码交互调用SDK");
		findViewById(R.id.btn_return).setOnClickListener(this);
		
		res = this.getResources();
		webView = (WebView) findViewById(R.id.web_view_diy_home);
		findViewById(R.id.btn_return).setOnClickListener(this);

		init();
	}
	@SuppressLint("JavascriptInterface")
	protected void init() {
		webView.getSettings().setAllowFileAccess(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);// 去掉右边的边框
		webView.setHorizontalScrollBarEnabled(false);
//		webView.getSettings().setPluginsEnabled(true);
		webView.getSettings().setPluginState(PluginState.ON);
		// 使得获取焦点以后可以使用软键盘
		webView.requestFocusFromTouch();
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		webView.setHorizontalScrollbarOverlay(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		webView.getSettings().setJavaScriptEnabled(true);

		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (!Thread.currentThread().isInterrupted()) {
					switch (msg.what) {
					case 0:// start
						isLoading = true;
						break;
					case 1:// ok
						int progress = msg.arg1;
						if (progress == 100) {
							isLoading = false;
						} else {
						}
						break;
					}
				}
				super.handleMessage(msg);
			}
		};
		webView.setWebViewClient(new DiyWebViewClient(handler) {
			public boolean shouldOverrideUrlLoading(final WebView view,
													final String url) {
				handler.sendEmptyMessage(0);
				if (url.startsWith("tel:")) {
					String moblie = url.replace("tel:", "");
					if (moblie != null && moblie.length() == 10) {
						moblie = 1 + moblie;
					}
					Intent it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
							+ moblie));
					view.getContext().startActivity(it);
				}
				return false;
			}

		});

		myDiyWebChromeClient = new DiyWebChromeClient(this, handler) {
			public void onProgressChanged(WebView view, int progress) {
				Message msg = handler.obtainMessage(1, progress, 0);
				msg.sendToTarget();
				super.onProgressChanged(view, progress);
			}
		};
		webView.setWebChromeClient(myDiyWebChromeClient);

		diyJs = new DiyJavaScriptInterface(this, webView);
		webView.addJavascriptInterface(diyJs, "androidJs");

		handler.sendEmptyMessage(0);
		webView.loadUrl("file:///android_asset/diy_h5_home.html");
		webView.requestFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (webView != null) {
			String webUrl = webView.getUrl();
			if (lastBackUrl != null) {
				if (lastBackUrl.equals(webUrl)) {
					sameBackCount++;
				} else {
					sameBackCount = 0;
				}
			}
			lastBackUrl = webUrl;
			if (sameBackCount < 1 && webUrl != null && webView.canGoBack()) {
				if (handler != null) {
					handler.sendEmptyMessage(0);
				}
				webView.goBack();
			} else {
				pageTurn();
			}
		} else {
			pageTurn();
		}
	}

	private void pageTurn(){
		this.setResult(RESULT_OK);
		this.finish();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_return:
			onBackPressed();
			break;
		default:
			break;
		}
	}


	@Override
	protected void onPause() {
		if (webView != null) {
			// webView.pauseTimers();
			try {
				webView.getClass().getMethod("onPause")
						.invoke(webView, (Object[]) null);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (webView != null) {
			// webView.resumeTimers();
			try {
				webView.getClass().getMethod("onResume")
						.invoke(webView, (Object[]) null);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.onResume();
	}
	private ValueCallback<Uri> mUploadMessage;
	private String mCameraFilePath;



	public ValueCallback<Uri> getmUploadMessage() {
		return mUploadMessage;
	}

	public void setmUploadMessage(ValueCallback<Uri> mUploadMessage) {
		this.mUploadMessage = mUploadMessage;
	}

	public String getmCameraFilePath() {
		return mCameraFilePath;
	}

	public void setmCameraFilePath(String mCameraFilePath) {
		this.mCameraFilePath = mCameraFilePath;
	}
	public static final int FILECHOOSER_RESULTCODE = 0;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage)
				return;
			Uri result = data == null || resultCode != RESULT_OK ? null : data
					.getData();
			if (result == null && data == null && resultCode == RESULT_OK) {
				File cameraFile = new File(mCameraFilePath);
				if (cameraFile.exists()) {
					result = Uri.fromFile(cameraFile);
					// Broadcast to the media scanner that we have a new photo
					// so it will be added into the gallery for the user.
					sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
				}
			}
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
		}
	}

}
