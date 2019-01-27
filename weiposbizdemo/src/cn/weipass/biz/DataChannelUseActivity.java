package cn.weipass.biz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
/**
 * 
 * @author Tianhui
 *	sdk使用DataChannel与服务端交互实现自定义业务
 */
public class DataChannelUseActivity extends Activity implements
		OnClickListener {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.data_channel);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("使用DataChannel数据通道");
		findViewById(R.id.btn_return).setOnClickListener(this);
		
		findViewById(R.id.btn_data_channel_one).setOnClickListener(this);
		findViewById(R.id.btn_data_channel_two).setOnClickListener(this);
		findViewById(R.id.btn_data_channel_three).setOnClickListener(this);
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
		case R.id.btn_data_channel_one:
			intent = new Intent(DataChannelUseActivity.this, DataChannelTakeTicketOneActivity.class);
			DataChannelUseActivity.this.startActivity(intent);
			break;
			
		case R.id.btn_data_channel_two:
			intent = new Intent(DataChannelUseActivity.this, DataChannelTakeTicketTwoActivity.class);
			DataChannelUseActivity.this.startActivity(intent);
			break;
		case R.id.btn_data_channel_three:
			
			intent = new Intent(DataChannelUseActivity.this, DataChannelPushMsgActivity.class);
			DataChannelUseActivity.this.startActivity(intent);
			break;
		default:
			break;
		}
	}
	
}
