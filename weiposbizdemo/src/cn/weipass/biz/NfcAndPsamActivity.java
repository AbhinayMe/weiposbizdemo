package cn.weipass.biz;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.weipass.biz.nfc.BankCard.BankCardInfo;
import cn.weipass.biz.nfc.NFCManager;
import cn.weipass.biz.util.HEX;
import cn.weipass.pos.sdk.PsamManager;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.sam.SamResult;

/**
 * NFC：可以读到NFC标签
 * 
 * @author TIANHUI
 * 
 */
public class NfcAndPsamActivity extends  Activity implements OnClickListener {

	private TextView stapInfoTv;
	private NFCManager mNFCManager;
	
	private ScrollView scrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_normal);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("NFC和PSAM卡同时");
		findViewById(R.id.btn_return).setOnClickListener(this);
		
		stapInfoTv = (TextView) findViewById(R.id.test_content_stap_info);

		scrollView = (ScrollView) findViewById(R.id.scroll_view);
		
		setTestRange();

		mNFCManager = NFCManager.getInstance();
		mNFCManager.init(this);
		
		new Handler().postAtTime(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				checkPsam();
			}
		}, time);
	}

	private final Handler mHandler = new Handler();
	private final int time = 1000 * 5;
	private boolean isCheckPsam = false;
	Runnable checkConnectRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					testPsam();
				}
			});
			if (isCheckPsam) {
				mHandler.postDelayed(checkConnectRunnable, time);
			}
		}
	};
	

	private void checkPsam(){
		isCheckPsam = true;
		mHandler.postDelayed(checkConnectRunnable, time);
	}
	
	private void updateLogInfo(String msg) {
		String str = stapInfoTv.getText().toString();
		StringBuffer sb = new StringBuffer();
		sb.append(str+"\n");
		sb.append(msg+"\n");

		stapInfoTv.setText(sb.toString());
		if (scrollView!=null) {
			new Handler().post(new Runnable() {  
			    @Override  
			    public void run() {  
			        scrollView.fullScroll(ScrollView.FOCUS_DOWN);  
			    }  
			}); 
		}
	}
	
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


	@Override
	protected void onNewIntent(Intent intent) {
		try {
			mNFCManager.procNFCIntent(intent);
		} catch (IOException e) {
			e.printStackTrace();
			updateLogInfo("NFC读卡号失败," + e.getLocalizedMessage());
		}
		super.onNewIntent(intent);
	}

	private NFCManager.NFCListener mNFCListener = new NFCManager.NFCListener() {

		@Override
		public void onReciveDataOffline(final byte[] data) {
			if (data != null) {
				try {
					String cardId = new String(data, "UTF-8");
					updateLogInfo("旺POS CPU NFC卡，卡号："+cardId);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					updateLogInfo("NFC读卡失败");
				} 
				mNFCManager.clearNFCParams();
			} else {
				updateLogInfo("NFC读卡失败");
			}
		}

		@Override
		public void onError(String error) {
			// TODO Auto-generated method stub
			updateLogInfo(error);
		}

		@Override
		public void onReciveBankDataOffline(BankCardInfo bankCard) {
			// TODO Auto-generated method stub
			
		}
	};

	
	/**
	 * 设置当前检测进度
	 */
	private void setTestRange() {
		StringBuilder sb = new StringBuilder();
		sb.append("NFC读卡时写PSAM卡:\n");
		sb.append("1、NFC卡放在设备头部带有NFC标志处。\n");
		sb.append("2、观察是否能够获取NFC卡号信息。\n");
		sb.append("2、观察是否获取写PSAM卡返回数据\n");

		stapInfoTv.setText(sb.toString());
	}
	
	private PsamManager psamManager;
	/**
	 * 检测PSAM数据交互
	 */
	private void testPsam() {
		try {
			psamManager = WeiposImpl.as().openPsamManager();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (psamManager == null) {
			Toast.makeText(NfcAndPsamActivity.this, "尚未初始化打印sdk，请稍后再试", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			psamManager.setSelectSlot(PsamManager.SLOT2);
			byte[] commandByte = new byte[] { 0x00, (byte) 0x84, 0x00, 0x00, 0x04 };
			SamResult samResult = psamManager.doCommand(commandByte);
			byte[] ramdom = null;
			if (samResult != null) {
				ramdom = samResult.getData();
			}
			if (ramdom != null && ramdom.length != 0) {
				updateLogInfo("psam卡返回字节数据为(" + HEX.bytesToHex(ramdom) + ")");
			} else {
				updateLogInfo("PSAM卡测试失败");
			}
		} catch (Exception e) {
			// TODO: handle exception
			updateLogInfo("PSAM卡测试失败");
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isCheckPsam = false;
//		if (psamManager == null) {
//			psamManager.destory();
//		}
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
