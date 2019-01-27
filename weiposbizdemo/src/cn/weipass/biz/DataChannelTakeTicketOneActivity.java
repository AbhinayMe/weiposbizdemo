package cn.weipass.biz;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import cn.weipass.pos.sdk.DataChannel.ResponseCallback;
import cn.weipass.pos.sdk.impl.WeiposImpl;
/**
 * 通过旺POS数据通道，向bp服务端请求获取电影票数据
 * @author Tianhui
 *
 */
public class DataChannelTakeTicketOneActivity extends Activity implements
		OnClickListener {

	private EditText codeEt;
	private TextView ticketInfoTv;
	private Button commitBtn;

	private ProgressDialog pd;
	private TicketInfo ticketInfo;

	private DataChannel dataChannel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.take_ticket_by_code);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("输入验证码获取电影票");
		findViewById(R.id.btn_return).setOnClickListener(this);
		
		codeEt = (EditText) findViewById(R.id.code_input_et);
		ticketInfoTv = (TextView) findViewById(R.id.ticket_info_tv);

		commitBtn = (Button) findViewById(R.id.btn_get_ticket_by_code);
		commitBtn.setOnClickListener(this);

		pd = new ProgressDialog(this);
		pd.setMessage("正在获取电影票信息，请稍等...");

		// 因为在前一个页面MainActivity中已经调用过WeiposImpl.as().init函数，并正常初始化了Weipos服务，
		// 所以这里直接获取能力对象并使用
		dataChannel = WeiposImpl.as().openDataChannel();
	}


	private void getTicketInfo() {
		pd.show();
		String code = codeEt.getText().toString();
		if (code == null || code.length() == 0) {
			Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		ticketInfoTv.setText("");

		try {
			JSONObject data_josn = new JSONObject();
			data_josn.put("action", 3);// 1:生成二维码;3:串码获取电影票信息;4:通知bp去push消息
			data_josn.put("code", code);// 串码信息
			dataChannel.request(data_josn.toString(), new ResponseCallback() {

				@Override
				public void onResponse(final String data_json) {
					// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
					runOnUiThread(new Runnable() {
						public void run() {
							pd.hide();
							ticketInfo = new TicketInfo();
							try {
								ticketInfo.loadFromServerData(DataParser
										.parseJSON2Map(new JSONObject(data_json)));
							} catch (JSONException e) {
								e.printStackTrace();
							}
							showTicketInfo();
						}
					});
				}

				@Override
				public void onError(final String error) {
					// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
					runOnUiThread(new Runnable() {
						public void run() {
							pd.hide();
							Toast.makeText(DataChannelTakeTicketOneActivity.this, error,
									Toast.LENGTH_SHORT).show();
						}
					});
					
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			pd.hide();
		}

	}

	private void showTicketInfo() {
		StringBuilder sb = new StringBuilder();
		if (ticketInfo == null) {
			sb.append("获取信息失败");
		} else {
			sb.append("获取的电影票信息\n");
			sb.append("昵称：" + ticketInfo.nickname + "\n");
			sb.append("影院：" + ticketInfo.name + "\n");
			sb.append("影片：" + ticketInfo.film + "\n");
			sb.append("时间：" + ticketInfo.time + "\n");
			sb.append("影厅：" + ticketInfo.room + "\n");
			sb.append("姓名：" + ticketInfo.username + "\n");
			sb.append("座位：" + ticketInfo.place + "\n");
		}

		ticketInfoTv.setText(sb.toString());
	}

	@Override
	protected void onDestroy() {
		if (pd != null)
			pd.dismiss();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_get_ticket_by_code:
			InputMethodManager imm = (InputMethodManager) v.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm.isActive()) {
				imm.hideSoftInputFromWindow(codeEt.getApplicationWindowToken(),
						0);
			}
			if (dataChannel == null) {
				Toast.makeText(DataChannelTakeTicketOneActivity.this,
						"SDK未初始化完成，请稍后再试", Toast.LENGTH_SHORT);
				return;
			}
			getTicketInfo();
			break;
		case R.id.btn_return:
			onBackPressed();
			break;
		default:
			break;
		}
	}
	
}
