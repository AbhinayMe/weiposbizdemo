package cn.weipass.biz;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import java.io.IOException;

import cn.weipass.biz.nfc.CpuCardException;
import cn.weipass.biz.nfc.NFCManager;
import cn.weipass.biz.util.HEX;

public class NfcSwitchNormalActivity extends Activity implements OnClickListener {
	private TextView stapInfoTv;
	private NfcAdapter nfcAdapter;

	private IsoDep na;
	private RadioGroup rgOper;
	final int DETECT_FLAG_DEFAULT = 0;
	final int DETECT_FLAG_PICC = 1;
	final int DETECT_FLAG_ICC = 2;
	final String KEY_DETECT_FLAG = "detectFlag";
	private int currDetectFlag = DETECT_FLAG_DEFAULT;
	public static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
	private Bundle bundle = new Bundle();

	/**
	 * 监听获取到的nfc卡信息
	 */
	@SuppressLint("NewApi")
	private ReaderCallback mReaderCallback = new ReaderCallback() {

		@Override
		public void onTagDiscovered(final Tag tag) {
			IsoDep na = IsoDep.get(tag);
			try {
				na.connect();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						updateLogInfo("标准NFC卡，卡ID：" + HEX.bytesToHex(tag.getId()));
					}
				});
			} catch (CpuCardException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						updateLogInfo("NFC读卡失败");
					}
				});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						updateLogInfo("NFC读卡失败");
					}
				});
			}

		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_normal_switch);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("切换非接卡与IC卡寻卡模式通讯演示");
		findViewById(R.id.btn_return).setOnClickListener(this);

		stapInfoTv = (TextView) findViewById(R.id.test_content_stap_info);

		rgOper = (RadioGroup) findViewById(R.id.rg_select_flag);
		rgOper.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_picc: {
					currDetectFlag = DETECT_FLAG_PICC;
					break;
				}
				case R.id.rb_icc: {
					currDetectFlag = DETECT_FLAG_ICC;
					break;
				}
				}
				if (!isEnabled()) {
					return;
				}
				nfcAdapter.disableReaderMode(NfcSwitchNormalActivity.this);
				bundle.putInt(KEY_DETECT_FLAG, currDetectFlag);
				nfcAdapter.enableReaderMode(NfcSwitchNormalActivity.this, NfcSwitchNormalActivity.this.mReaderCallback,
						READER_FLAGS, bundle);
			}
		});
		setTestRange();
		init(this);
	}

	@SuppressLint("NewApi")
	public void init(Context context) {
		if (Build.VERSION.SDK_INT < 10) {
			return;
		}
		try {
			nfcAdapter = NfcAdapter.getDefaultAdapter(context);
		} catch (Exception e1) {
			e1.printStackTrace();
			nfcAdapter = null;
			showMsgDialog(e1.getMessage());
			return;
		}
		// 判断2
		if (nfcAdapter == null) {
			// 如果手机不支持NFC，或者NFC没有打开就直接返回
			Log.d(this.getClass().getName(), "手机不支持NFC功能！");
			showMsgDialog("设备不支持NFC！");
			return;
		}
		if (!nfcAdapter.isEnabled()) {
			Log.d(this.getClass().getName(), "手机NFC功能没有打开！");
			enableDialog(context);
			return;
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onResume()
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (nfcAdapter != null) {
			nfcAdapter.enableReaderMode(this, this.mReaderCallback, READER_FLAGS, bundle);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (nfcAdapter != null) {
			nfcAdapter.disableReaderMode(this);
		}

	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	@SuppressLint("NewApi")
	@Override
	protected void onNewIntent(Intent intent) {
		if (isEnabled()) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			try {
				na = IsoDep.get(tag);
				na.connect();
				// 在这里可以进行nfc交互处理
				updateLogInfo("标准NFC卡，卡ID：" + HEX.bytesToHex(tag.getId()));
			} catch (Exception e) {
				e.printStackTrace();
				updateLogInfo("NFC不可用：" + e.getMessage());
			}

		} else {
			updateLogInfo("NFC不可用");
		}
		super.onNewIntent(intent);
	}

	public boolean isEnabled() {
		return nfcAdapter != null && nfcAdapter.isEnabled();
	}

	private void enableDialog(final Context context) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle("提醒");
		ab.setMessage("手机NFC开关未打开，是否现在去打开？");
		ab.setNeutralButton("否", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ab.setNegativeButton("是", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				dialog.dismiss();
			}
		});
		ab.create().show();
	}

	private void showMsgDialog(String msg) {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("提醒");
		ab.setMessage(msg);
		ab.setNeutralButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ab.create().show();
	}

	private void updateLogInfo(String msg) {
		String str = stapInfoTv.getText().toString();
		StringBuffer sb = new StringBuffer();
		sb.append(str + "\n");
		sb.append(msg + "\n");

		stapInfoTv.setText(sb.toString());
	}

	/**
	 * 设置当前检测进度
	 */
	private void setTestRange() {
		StringBuilder sb = new StringBuilder();
		sb.append("NFC读卡demo:\n");
		sb.append("1、NFC卡放在设备头部带有NFC标志处。\n");
		sb.append("2、观察是否能够获取NFC卡号信息。\n");

		stapInfoTv.setText(sb.toString());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		finish();
	}

}
