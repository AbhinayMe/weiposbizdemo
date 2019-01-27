package cn.weipass.biz;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.weipass.biz.util.DataParser;
import cn.weipass.biz.vo.TicketInfo;
import cn.weipass.pos.sdk.DataChannel;
import cn.weipass.pos.sdk.DataChannel.OnPushDataListener;
import cn.weipass.pos.sdk.DataChannel.ResponseCallback;
import cn.weipass.pos.sdk.impl.WeiposImpl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
/**
 * 通过旺POS数据通道，向bp服务端请求获取二维码信息，扫码打开对应网页，点击网页上连接通知服务端推送电影票信息到pos
 * @author Tianhui
 *
 */
public class DataChannelTakeTicketTwoActivity extends Activity implements OnClickListener {

	private TextView ticketInfoTv;

	private ProgressDialog pd;
	private TicketInfo ticketInfo;
	private ImageView iv_qrcode;

	private int qrImgWidth;

	private DataChannel dataChannel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.take_ticket_by_scan_qr_code);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("获取显示电影票二维码，\n监听服务端推送微信扫描结果");
		findViewById(R.id.btn_return).setOnClickListener(this);
		
		ticketInfoTv = (TextView) findViewById(R.id.ticket_info_tv);

		iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		qrImgWidth = (int) Math.ceil(screenWidth / 2f);
		LinearLayout.LayoutParams rlp = (LinearLayout.LayoutParams) iv_qrcode
				.getLayoutParams();
		if (rlp == null) {
			rlp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
		}

		rlp.width = qrImgWidth;
		rlp.height = qrImgWidth;
		iv_qrcode.setLayoutParams(rlp);

		pd = new ProgressDialog(this);
		pd.setMessage("正在获取二维码信息，请稍等...");
		pd.show();

		// 因为在前一个页面已经调用过WeiposImpl.as().init函数，并正常初始化了Weipos服务，
		// 所以这里直接获取能力对象并使用
		dataChannel = WeiposImpl.as().openDataChannel();

		// 设置push回调接口
		dataChannel.setOnPushDataListener(new OnPushDataListener() {

			@Override
			public void onDataReceived(final String data_json) {
				// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
				runOnUiThread(new Runnable() {
					public void run() {
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

		});

		getQrImg();
	}

	private void getQrImg() {
		try {
			JSONObject data_josn = new JSONObject();
			data_josn.put("action", 1);//  1:生成二维码;3:串码获取电影票信息;4:通知bp去push消息
			dataChannel.request(data_josn.toString(), new ResponseCallback() {

				@Override
				public void onResponse(final String data_json) {
					// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
					runOnUiThread(new Runnable() {
						public void run() {
							try {System.out.println("data_json=========>>"+data_json);
								JSONObject result = new JSONObject(data_json);
								if (result.has("qrImgUrl")) {
									String qrImgUrl = result.getString("qrImgUrl");
									if (qrImgUrl != null) {
										Bitmap src = createQRBitmap(qrImgUrl,
												qrImgWidth, qrImgWidth, false);
										iv_qrcode.setImageBitmap(src);
									} else {
										Toast.makeText(
												DataChannelTakeTicketTwoActivity.this,
												"获取二维码失败！", Toast.LENGTH_SHORT).show();
									}
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							pd.hide();
						}
					});
					
				}

				@Override
				public void onError(final String error) {
					// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
					runOnUiThread(new Runnable() {
						public void run() {
							pd.hide();
							Log.d("SignInWX", error);
							Toast.makeText(DataChannelTakeTicketTwoActivity.this, error,
									Toast.LENGTH_SHORT).show();
						}
					});
					
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 用字符串生成二维码图片
	 * 
	 * @param str
	 * @return
	 * @throws WriterException
	 */
	public static final Bitmap createQRBitmap(String str, int qrWidth,
			int qrHeight, boolean closing) {
		// 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
		Bitmap bitmap = null;
		try {
			BitMatrix matrix = new MultiFormatWriter().encode(str,
					BarcodeFormat.QR_CODE, qrWidth, qrHeight);
			int width = 0;
			int height = 0;
			int[] pixels = null;
			if (closing) {
				int[] rect = matrix.getEnclosingRectangle();
				width = rect[2];
				height = rect[3];
				int startx = rect[0];
				int starty = rect[1];
				// 二维矩阵转为一维像素数组,也就是一直横着排了
				pixels = new int[width * height];
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (matrix.get(startx + x, starty + y)) {
							pixels[y * width + x] = 0xff000000;
						} else {
							pixels[y * width + x] = -1;
						}
					}
				}
			} else {
				width = matrix.getWidth();
				height = matrix.getHeight();
				pixels = new int[width * height];

				for (int y = 0; y < height; y++) {
					int offset = y * width;
					for (int x = 0; x < width; x++) {
						pixels[(offset + x)] = (matrix.get(x, y) ? -16777216
								: -1);
					}
				}
			}
			bitmap = Bitmap
					.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			// 通过像素数组生成bitmap,具体参考api
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bitmap;
	}

	private void showTicketInfo() {
		StringBuilder sb = new StringBuilder();
		if (ticketInfo == null) {
			sb.append("获取信息失败");
		} else {
			sb.append("获取的电影票信息" + "\n");
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
		case R.id.btn_return:
			onBackPressed();
			break;

		default:
			break;
		}
	}
}
