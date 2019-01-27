package cn.weipass.biz;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.weipass.biz.nfc.BankCard.BankCardInfo;
import cn.weipass.biz.nfc.NFCManager;

/**
 * NFC：可以读到银行卡NFC标签
 * 
 * @author TIANHUI
 * 
 */
public class NfcBankActivity extends  Activity implements OnClickListener {

	private TextView stapInfoTv;
	private NFCManager mNFCManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_normal);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("NFC读银行卡");
		findViewById(R.id.btn_return).setOnClickListener(this);
		
		stapInfoTv = (TextView) findViewById(R.id.test_content_stap_info);

		setTestRange();

		mNFCManager = NFCManager.getInstance();
		mNFCManager.init(this);

	}

	private void updateLogInfo(String msg) {
		String str = stapInfoTv.getText().toString();
		StringBuffer sb = new StringBuffer();
		sb.append(str + "\n");
		sb.append(msg + "\n");

		stapInfoTv.setText(sb.toString());
	};

	@Override
	protected void onResume() {
		super.onResume();
		mNFCManager.setNFCListener(mNFCListener);
		mNFCManager.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNFCManager.onPause(this);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		try {
			mNFCManager.procBankNFCIntent(intent);
		} catch (IOException e) {
			e.printStackTrace();
			updateLogInfo("NFC读卡号失败," + e.getLocalizedMessage());
		}
		super.onNewIntent(intent);
	}

	private NFCManager.NFCListener mNFCListener = new NFCManager.NFCListener() {

		@Override
		public void onReciveDataOffline(final byte[] data) {

		}

		@Override
		public void onError(String error) {
			// TODO Auto-generated method stub
			updateLogInfo(error);
		}

		@Override
		public void onReciveBankDataOffline(BankCardInfo bankCard) {
			// TODO Auto-generated method stub
			if (bankCard != null && bankCard.id!=null) {
				StringBuilder sb = new StringBuilder();
				sb.append("NFC读卡成功:\n卡号：" + bankCard.id);
				sb.append("\n类型：" + bankCard.type);
				sb.append("\n名称：" + bankCard.name);
				updateLogInfo(sb.toString());
				mNFCManager.clearNFCParams();
			} else {
				if (bankCard.id==null) {
					updateLogInfo("不是银行NFC卡");
				}else{
					updateLogInfo("NFC读卡失败");
				}
			}
		}
	};

	/**
	 * 设置当前检测进度
	 */
	private void setTestRange() {
		StringBuilder sb = new StringBuilder();
		sb.append("NFC读卡demo:\n");
		sb.append("1、银行卡NFC卡放在设备头部带有NFC标志处。\n");
		sb.append("2、观察是否能够获取NFC卡号信息。\n");
		sb.append("3、注意确认银行卡是NFC卡，否则会获取不到信息。\n");

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
