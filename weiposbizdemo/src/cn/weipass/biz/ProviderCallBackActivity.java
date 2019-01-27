package cn.weipass.biz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ProviderCallBackActivity extends Activity implements OnClickListener {

	private TextView backInfoTv;
	private String balance = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.provider_cal_back);
		
		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText(R.string.other_54);
		findViewById(R.id.btn_return).setOnClickListener(this);
		
		procIntent(getIntent());

	}

	private void procIntent(Intent data) {
		if (data != null) {
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null) {
				balance = bundle.getString("balance");
			}

			backInfoTv = (TextView) findViewById(R.id.provider_call_back_info);
			backInfoTv.setText(R.string.other_55 + balance);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		procIntent(intent);
		super.onNewIntent(intent);
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
}
