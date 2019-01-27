package cn.weipass.biz;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.weipass.biz.util.FormatTools;
import cn.weipass.pos.sdk.AuthorizationManager;
import cn.weipass.pos.sdk.AuthorizationManager.AuthorizeCallback;
import cn.weipass.pos.sdk.Weipos.OnInitListener;
import cn.weipass.pos.sdk.impl.WeiposImpl;
/**
 * 权限管理测试
 * @author Tianhui
 *
 */
public class AuthorizeTextActivity extends Activity implements OnClickListener {

	private Context mContext;
	private String className;
	private String packageName;
	private AuthorizationManager mAuthorizationManager;
	private TextView mTextView;
	private Button shouyinButton, jiesuanButton;
	private int click = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authorize_text_layout);
		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText(R.string.app_title);
		findViewById(R.id.btn_return).setOnClickListener(this);

		mContext = this;
		mTextView = (TextView) findViewById(R.id.author_text);
		mTextView.setText(R.string.other_1);
		shouyinButton = (Button) findViewById(R.id.shouyin_button);
		jiesuanButton = (Button) findViewById(R.id.jiesuan_button);
		WeiposImpl.as().init(mContext, new OnInitListener() {

			@Override
			public void onInitOk() {
				// TODO Auto-generated method stub
				// Toast.makeText(mContext, "init ok",
				// Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(String msg) {
				// TODO Auto-generated method stub
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onDestroy() {
				// TODO Auto-generated method stub
				
			}
		});
		// 授权管理测试首先需要在pos端在设置页开启授权管理
		
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
		RunningTaskInfo cinfo = runningTasks.get(0);
		ComponentName component = cinfo.topActivity;
		className = component.getClassName();
		packageName = this.getPackageName();
		InputStream is = FormatTools.getInstance().Drawable2InputStream(getResources().getDrawable(R.drawable.authorize_huiyuan));
		Bitmap bitmap = BitmapFactory.decodeStream(is);
		Log.i("AuthorizationManagerImp", " bitmap:" + bitmap);
		mAuthorizationManager = WeiposImpl.as().openAuthorizationManager();
		mAuthorizationManager.setAuthorizeCallback(mCallback);
		List<String> list = new ArrayList<String>();
		list.add(getResources().getString(R.string.other_2));
		list.add(getResources().getString(R.string.other_3));
		mAuthorizationManager.setAuthorizeApplicationList(getResources().getString(R.string.other_4), bitmap,
				list.size(), list);
		shouyinButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				requestAuthoirze(getResources().getString(R.string.other_2));
			}
		});
		jiesuanButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				requestAuthoirze(getResources().getString(R.string.other_3));
			}
		});
	}

	// Drawable转换成Bitmap
	public Bitmap drawable2Bitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	private void requestAuthoirze(String authorName) {
		try {
			mAuthorizationManager.requestAuthorize(packageName, getResources().getString(R.string.other_4),
					authorName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	AuthorizeCallback mCallback = new AuthorizeCallback() {

		@Override
		public void getAuthorizeResult(int arg0) {
			// TODO Auto-generated method stub
			if (arg0 == AuthorizationManager.AuthorSuccess) {
				Toast.makeText(mContext, getResources().getString(R.string.other_5), Toast.LENGTH_SHORT).show();
			} else if (arg0 == AuthorizationManager.AuthorFail) {
				Toast.makeText(mContext, getResources().getString(R.string.other_6), Toast.LENGTH_SHORT).show();
			} else if (arg0 == AuthorizationManager.AuthorException) {
				Toast.makeText(mContext, getResources().getString(R.string.other_7), Toast.LENGTH_SHORT).show();
			}
		}
	};

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
}
