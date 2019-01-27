package cn.weipass.biz;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.weipass.pos.sdk.DataChannel;
import cn.weipass.pos.sdk.DataChannel.OnPushDataListener;
import cn.weipass.pos.sdk.DataChannel.ResponseCallback;
import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
 * 通过旺pos数据通道向服务端请求发送语音播报消息
 * 
 * @author Tianhui
 *
 */
public class DataChannelPushMsgActivity extends Activity implements OnClickListener {

	private EditText voiceEt;
	private ProgressDialog pd;

	private DataChannel dataChannel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.notify_bp_2_push_msg);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("通知业务方下发PUSH消息");
		findViewById(R.id.btn_return).setOnClickListener(this);

		voiceEt = (EditText) findViewById(R.id.voice_content);
		findViewById(R.id.btn_notify_2_push).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				notifyBp2PushMsg();
			}
		});
		pd = new ProgressDialog(this);
		pd.setMessage("正在通知PUSH，请稍等...");

		// 因为在前一个页面MainActivity中已经调用过WeiposImpl.as().init函数，并正常初始化了Weipos服务，
		// 所以这里直接获取能力对象并使用
		dataChannel = WeiposImpl.as().openDataChannel();
	}

	private void notifyBp2PushMsg() {
		pd.show();
		String voice = voiceEt.getText().toString();
		if (voice == null || voice.length() == 0) {
			Toast.makeText(this, "发音内容不能为空", Toast.LENGTH_SHORT).show();
			return;
		}

		dataChannel.setOnPushDataListener(new OnPushDataListener() {

			@Override
			public void onDataReceived(String msg) {
				// TODO Auto-generated method stub
				// 这里获取服务端推送的数据，逻辑处理自己定制
				try {
					//如果是json字符串，获取voice播音内容进行播音
					JSONObject json = new JSONObject(msg);
					WeiposImpl.as().speech(json.getString("voice"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					// 如果不是json数据，直接播放语音
					WeiposImpl.as().speech(msg);
				}

			}
		});

		try {
			// data_josn为与bp服务器协定好的字符串
			JSONObject data_josn = new JSONObject();
			data_josn.put("action", 4);// 1:生成二维码;3:串码获取电影票信息;4:通知bp去push消息
			// 必须传入的参数
			data_josn.put("msgType", 1);// 1 消息 不存在则为普通push信息不播音
			data_josn.put("voice", voice);// 发音的内容
			data_josn.put("content", "业务demo通知服务端推送消息");// 消息描述
			data_josn.put("count", 1);// 消息数（必须大于0才会播音）
			// 客户端发送请求，并且实时获取请求结果返回
			dataChannel.request(data_josn.toString(), new ResponseCallback() {

				@Override
				public void onResponse(final String data_json) {
					// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
					runOnUiThread(new Runnable() {
						public void run() {
							pd.hide();
							Toast.makeText(DataChannelPushMsgActivity.this, "接口调用成功", Toast.LENGTH_SHORT).show();
						}
					});
				}

				@Override
				public void onError(final String error) {
					// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
					runOnUiThread(new Runnable() {
						public void run() {
							pd.hide();
							Toast.makeText(DataChannelPushMsgActivity.this, error, Toast.LENGTH_SHORT).show();
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
		case R.id.btn_return:
			onBackPressed();
			break;

		default:
			break;
		}
	}
}
