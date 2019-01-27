package cn.weipass.biz.html5;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DiyWebViewClient extends WebViewClient {
	private Handler handler;
	public DiyWebViewClient(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		view.requestFocus();
		view.requestFocusFromTouch();

	}

	@Override
	public void onReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		super.onReceivedError(view, errorCode, description, failingUrl);
	}
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (url.startsWith("tel:")) {
			String moblie = url.replace("tel:", "");
			if (moblie != null && moblie.length() == 10) {
				moblie = 1 + moblie;
			}
			Intent it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ moblie));
			view.getContext().startActivity(it);
		} else {
			handler.sendEmptyMessage(0);
			//view.loadUrl(url);
		}
		return false;
		//return true;
	}

	// /**
	// * 处理ssl请求
	// */
	// @Override
	// public void onReceivedSslError(WebView view,
	// SslErrorHandler handler, SslError error) {
	// handler.proceed();
	// }

}
