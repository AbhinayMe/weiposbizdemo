package cn.weipass.biz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import cn.weipass.biz.util.SdkTools;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent it=null;
				it = new Intent(SplashActivity.this, MainNewActivity.class);
				it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(it);
				finish();
			}
		},2000);
	}

}
