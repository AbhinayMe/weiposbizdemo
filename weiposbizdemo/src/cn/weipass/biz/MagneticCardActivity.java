package cn.weipass.biz;

import java.io.ByteArrayInputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.weipass.pos.sdk.MagneticReader;
import cn.weipass.pos.sdk.Weipos.OnInitListener;
import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
 * 读取magnetic磁条卡信息
 * 
 * @author TIANHUI
 * 
 */
public class MagneticCardActivity extends Activity implements OnClickListener {

	private TextView stapInfoTv;
	private MagneticReader mMagneticReader;// 磁条卡管理

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_normal);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("旺POS 磁条卡刷卡");
		findViewById(R.id.btn_return).setOnClickListener(this);

		stapInfoTv = (TextView) findViewById(R.id.test_content_stap_info);

		setTestRange();

		mMagneticReader = WeiposImpl.as().openMagneticReader();
		if (mMagneticReader == null) {
			Toast.makeText(this, "磁条卡读取服务不可用！", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private void updateLogInfo(String msg) {
		if (stapInfoTv.getLineCount() >= 30) {
			setTestRange();
		}
		stapInfoTv.append("\n" + msg);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startTask();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopTask();
	}

	class ReadMagTask extends Thread implements Callback {
		private Handler H;
		private boolean isRun = false;

		public ReadMagTask() {
			H = new Handler(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			isRun = true;
			// 磁卡刷卡后，主动获取解码后的字符串数据信息
			try {
				while (isRun) {
					String decodeData = getMagneticReaderInfo();
					if (decodeData != null && decodeData.length() != 0) {
						System.out.println("final============>>>" + decodeData);
						Message m = H.obtainMessage(0);
						m.obj = decodeData;
						H.sendMessage(m);
					}
					Thread.sleep(500);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isRun = false;
			}
		}

		@Override
		public boolean handleMessage(Message msg) {
			/**
			 * 1：刷会员卡返回会员卡号后面变动的卡号，前面为固定卡号（没有写入到磁卡中）
			 * 如会员卡号：9999100100030318，读卡返回数据为00030318，前面99991001在磁卡中没有写入
			 * 2：刷银行卡返回数据格式为：卡号=有效期。
			 */
			updateLogInfo("磁条卡内容\n" + msg.obj);
			return false;
		}

	}

	// MagneticReader.readCard() 取得全部磁道数据,再用下面代码解码
	// 解码出3个磁道的数据
	public static byte[][] decodeMagcardPos3(byte[] bs) {
		byte[][] rs = new byte[3][];
		ByteArrayInputStream bais = new ByteArrayInputStream(bs);
		while (true) {
			try {
				int id = bais.read() - 1;
				int len = bais.read();
				System.out.println("id==========>>>" + id + "|" + len);
				if (id < 0 || id > 2 || len < 0) {
					break;
				}
				byte[] ts = new byte[len];
				bais.read(ts);
				rs[id] = ts;
				System.out.println("read:" + id + " " + len);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return rs;
	}

	public String getMagneticReaderInfo() {
		if (mMagneticReader == null) {
			Toast.makeText(this, "初始化磁条卡sdk失败", Toast.LENGTH_LONG).show();
			return "";
		}
		// 刷卡后，主动获取磁卡的byte[]数据
		// byte[] cardByte = mMagneticReader.readCard();

		// String decodeData = mMagneticReader.getCardDecodeData();

		// 磁卡刷卡后，主动获取解码后的字符串数据信息
		String[] decodeData = mMagneticReader.getCardDecodeThreeTrackData();//
		if (decodeData != null && decodeData.length > 0) {
			/**
			 * 1：刷会员卡返回会员卡号后面变动的卡号，前面为固定卡号（没有写入到磁卡中）
			 * 如会员卡号：9999100100030318，读卡返回数据为00030318，前面99991001在磁卡中没有写入
			 * 2：刷银行卡返回数据格式为：卡号=有效期。
			 */
			String retStr = "";
			for (int i = 0; i < decodeData.length; i++) {
				if (decodeData[i] == null)
					continue;
				String txt = decodeData[i];
//				if (retStr.length() > 0) {
//					retStr = retStr + "=";
//				} else {
//					if (txt.indexOf("=") >= 0) {
//						String[] arr = txt.split("=");
//						if (arr[0].length() == 16 || arr[0].length() == 19) {
//							return arr[0];
//						}
//					}
//				}
				if(txt != null) {
					retStr = retStr + "第" + (i+1) + "磁道数据:" + txt + "\n";
				}
			}
			return retStr;
		} else {
			// Toast.makeText(MainNewActivity.this, "获取磁条卡数据失败，请确保已经刷卡",
			// Toast.LENGTH_LONG).show();
			return "";
		}
	}

	private ReadMagTask mReadMagTask = null;

	private void startTask() {
		if (mReadMagTask == null) {
			mReadMagTask = new ReadMagTask();
			mReadMagTask.start();
		}
	}

	private void stopTask() {
		if (mReadMagTask != null) {
			mReadMagTask.interrupt();
			mReadMagTask = null;
		}
	}

	/**
	 * 设置当前检测进度
	 */
	private void setTestRange() {
		StringBuilder sb = new StringBuilder();
		sb.append("磁条卡读卡demo:\n");
		sb.append("1:将磁条卡在卡槽中进行刷卡动作\n");
		sb.append("2:等待获取数据信息\n");

		stapInfoTv.setText(sb.toString());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0:

			break;

		default:
			break;
		}
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
