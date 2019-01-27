package cn.weipass.biz.html5;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import cn.weipass.biz.WebTurnViewActivity;
import cn.weipass.biz.R;


public class DiyWebChromeClient extends WebChromeClient {
	private Handler handler;
	private Context context;
	private WebTurnViewActivity act;

	public DiyWebChromeClient(Activity c, Handler handler) {
		this.handler = handler;
		if (c.getParent() != null) {
			this.context = c.getParent();
		} else {
			this.context = c;
		}
		this.act = (WebTurnViewActivity) c;

	}

	@Override
	public void onCloseWindow(WebView window) {
		super.onCloseWindow(window);
	}

	@Override
	public boolean onCreateWindow(WebView view, boolean dialog,
			boolean userGesture, Message resultMsg) {
		return super.onCreateWindow(view, dialog, userGesture, resultMsg);
	}

	@Override
	public void onShowCustomView(View view, CustomViewCallback callback) {
		super.onShowCustomView(view, callback);
	}

	/**
	 * 覆盖默认的window.alert展示界面，避免title里显示为“：来自file:////”
	 */
	public boolean onJsAlert(WebView view, String url, String message,
			JsResult result) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		Resources res = context.getResources();
		builder.setTitle(res.getString(R.string.point)).setMessage(message)
				.setPositiveButton(res.getString(R.string.ok), null);

		AlertDialog dialog = builder.create();
		dialog.show();
		result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内容。
		return true;
	}

	public boolean onJsBeforeUnload(WebView view, String url, String message,
			JsResult result) {
		return super.onJsBeforeUnload(view, url, message, result);
	}

	/**
	 * 覆盖默认的window.confirm展示界面，避免title里显示为“：来自file:////”
	 */
	public boolean onJsConfirm(WebView view, String url, String message,
			final JsResult result) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		Resources res = context.getResources();
		builder.setTitle(res.getString(R.string.point))
				.setMessage(message)
				.setPositiveButton(res.getString(R.string.ok),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();
							}
						})
				.setNeutralButton(res.getString(R.string.cancel),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								result.cancel();
							}
						});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				result.cancel();
			}
		});

		// 屏蔽keycode等于84之类的按键，避免按键后导致对话框消息而页面无法再弹出对话框的问题
		// builder.setOnKeyListener(new OnKeyListener() {
		// @Override
		// public boolean onKey(DialogInterface dialog, int keyCode,KeyEvent
		// event) {
		// Log.v("onJsConfirm", "keyCode==" + keyCode + "event="+ event);
		// return true;
		// }
		// });
		// 禁止响应按back键的事件
		// builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
		return true;
		// return super.onJsConfirm(view, url, message, result);
	}

	/**
	 * 覆盖默认的window.prompt展示界面，避免title里显示为“：来自file:////”
	 * window.prompt('请输入您的域名地址', '618119.com');
	 */
	public boolean onJsPrompt(WebView view, String url, String message,
			String defaultValue, final JsPromptResult result) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		Resources res = context.getResources();
		builder.setTitle(res.getString(R.string.point)).setMessage(message);

		final EditText et = new EditText(view.getContext());
		et.setSingleLine();
		et.setText(defaultValue);
		builder.setView(et)
				.setPositiveButton(res.getString(R.string.ok),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm(et.getText().toString());
							}

						})
				.setNeutralButton(res.getString(R.string.cancel),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								result.cancel();
							}
						});

		AlertDialog dialog = builder.create();
		dialog.show();
		return true;
	}

	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		Message msg = handler.obtainMessage(1, newProgress, 0);
		msg.sendToTarget();
		super.onProgressChanged(view, newProgress);
	}

	@Override
	public void onReceivedIcon(WebView view, Bitmap icon) {
		super.onReceivedIcon(view, icon);
	}

	@Override
	public void onReceivedTitle(WebView view, String title) {
		super.onReceivedTitle(view, title);
	}

	@Override
	public void onRequestFocus(WebView view) {
		super.onRequestFocus(view);
	}
	
	// For Android 3.0+  
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		this.act.setmUploadMessage(uploadMsg);
		this.act.startActivityForResult(createDefaultOpenableIntent(),
				WebTurnViewActivity.FILECHOOSER_RESULTCODE);
	}

	// For Android < 3.0
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		openFileChooser(uploadMsg, "");
	}

	// For Android > 4.1.1
	public void openFileChooser(ValueCallback<Uri> uploadMsg,
			String acceptType, String capture) {
		openFileChooser(uploadMsg, acceptType);
	}

	private Intent createDefaultOpenableIntent() {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");

		Intent chooser = createChooserIntent(createCameraIntent(),
				createCamcorderIntent(), createSoundRecorderIntent());
		chooser.putExtra(Intent.EXTRA_INTENT, i);
		return chooser;
	}

	private Intent createChooserIntent(Intent... intents) {
		Intent chooser = new Intent(Intent.ACTION_CHOOSER);
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
		chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
		return chooser;
	}

	private Intent createCameraIntent() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File externalDataDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		File cameraDataDir = new File(externalDataDir.getAbsolutePath()
				+ File.separator + "browser-photos");
		cameraDataDir.mkdirs();
		String mCameraFilePath = cameraDataDir.getAbsolutePath()
				+ File.separator + System.currentTimeMillis() + ".jpg";
		this.act.setmCameraFilePath(mCameraFilePath);

		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(mCameraFilePath)));
		return cameraIntent;
	}

	private Intent createCamcorderIntent() {
		return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
	}

	private Intent createSoundRecorderIntent() {
		return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
	}

}
