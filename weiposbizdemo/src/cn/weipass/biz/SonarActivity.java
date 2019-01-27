package cn.weipass.biz;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.weipass.biz.util.DataParser;
import cn.weipass.biz.vo.TicketInfo;
import cn.weipass.pos.sdk.DataChannel;
import cn.weipass.pos.sdk.Sonar;
import cn.weipass.pos.sdk.DataChannel.ResponseCallback;
import cn.weipass.pos.sdk.Sonar.OnReceiveListener;
import cn.weipass.pos.sdk.impl.WeiposImpl;
/**
 * 
 * @author Tianhui
 *	sdk使用 Sonar 实现声波通讯
 */
public class SonarActivity extends Activity implements
		OnClickListener {
	private TextView stapInfoTv;
	//sdk 设备通讯对象
	private Sonar sonar = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sonar);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("旺POS 声波通讯");
		findViewById(R.id.btn_return).setOnClickListener(this);
	
		stapInfoTv = (TextView) findViewById(R.id.test_content_stap_info);
		
		findViewById(R.id.btn_do_sonar).setOnClickListener(this);
		
		initSonar();
		
		setTestRange();
	}
	
	/**
	 * 设置当前检测进度
	 */
	private void setTestRange() {
		StringBuilder sb = new StringBuilder();
		sb.append("NFC读卡demo:\n");
		sb.append("1：点击按钮模拟声波通讯\n");

		stapInfoTv.setText(sb.toString());
	}

	private void initSonar(){
		sonar = WeiposImpl.as().openSonar();
	}
	
	private void updateLogInfo(String msg) {
		String str = stapInfoTv.getText().toString();
		StringBuffer sb = new StringBuffer();
		sb.append(str+"\n");
		sb.append(msg+"\n");

		stapInfoTv.setText(sb.toString());
	}
	
	private void doSonar(){
		if (sonar == null) {
			Toast.makeText(SonarActivity.this, "尚未初始化声波通讯sdk，请稍后再试", Toast.LENGTH_SHORT).show();
			return;
		}
		sonar.setOnReceiveListener(new OnReceiveListener() {

			@Override
			public void onReceive(byte[] d) {
				// TODO Auto-generated method stub
				// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
				final byte[] data = d;
				runOnUiThread(new Runnable() {
					public void run() {
						String receveInfo = DataParser.printdatas("声波接收数据：", data);
						updateLogInfo(receveInfo);
						StringBuilder sb = new StringBuilder(data.length * 3 + 50);
						for (int n : data) {
							sb.append(DataParser.CS[(n >> 4) & 0xF]);
							sb.append(DataParser.CS[(n >> 0) & 0xF]);
							sb.append(' ');
						}
						System.out.println(sb);
						showResultInfo("声波通讯", "接收到的数据", sb.toString());
					}
				});
			}
		});
		byte[] sendData = new byte[] { 'W', 'E', 'I', 'P', 'A', 'S', 'S', 'Q' };
		sonar.send(sendData);
		String info = DataParser.printdatas("声波发送数据：", sendData);
		updateLogInfo(info);
	}

	private void showResultInfo(String operInfo, String titleHeader, String info) {
		AlertDialog.Builder builder = new Builder(this);

		builder.setMessage(titleHeader + ":" + info);
		builder.setTitle(operInfo);
		builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
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
		case R.id.btn_do_sonar:
			doSonar();
			break;
		default:
			break;
		}
	}
	
}
