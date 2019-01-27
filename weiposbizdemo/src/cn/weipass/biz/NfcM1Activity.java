package cn.weipass.biz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import cn.weipass.biz.nfc.MifareBlock;
import cn.weipass.biz.nfc.MifareClassCard;
import cn.weipass.biz.nfc.MifareSector;
import cn.weipass.biz.nfc.NFCManager;
import cn.weipass.biz.util.Converter;
import cn.weipass.biz.util.HEX;

/**
 * M1 NFC：读写M1卡
 * 
 * 本示例只读写第二扇区中下标为4的block块
 * 
 * @author TIANHUI
 * 
 */
public class NfcM1Activity extends Activity implements OnClickListener {

	public static final String TAG = "M1NfcActivity";
	private TextView stapInfoTv;

	private RadioGroup rgOper;
	private EditText contentEt;
	private int operType = 0;// 0:读M1卡；1：写M1卡

	private static NfcAdapter mAdapter;
	private static PendingIntent mPendingIntent;
	private static IntentFilter[] mFilters;
	private static String[][] mTechLists;
	private boolean isNFC;
	private final int BLOCK_INDEX = 8;
	private final static int ID_SECTOR_INDEX = 2;
	
	private ScrollView scorll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m1_nfc);

		isNFC = getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_NFC);
		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("旺POS M1卡通讯");
		findViewById(R.id.btn_return).setOnClickListener(this);

		scorll = (ScrollView) findViewById(R.id.scorll);

		contentEt = (EditText) findViewById(R.id.write_to_m1_content);
		contentEt.setVisibility(View.GONE);

		rgOper = (RadioGroup) findViewById(R.id.m1_read_write);
		rgOper.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.read:
					contentEt.setVisibility(View.GONE);
					operType = 0;
					break;
				case R.id.write:
					contentEt.setVisibility(View.VISIBLE);
					operType = 1;
					break;
				default:
					break;
				}
			}
		});

		stapInfoTv = (TextView) findViewById(R.id.test_content_stap_info);

		setTestRange();

	}

	@SuppressLint("NewApi")
	private void initNFC() {
		if (isNFC) {
			mAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mAdapter.isEnabled()) {
				mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
						this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				IntentFilter ndef = new IntentFilter(
						NfcAdapter.ACTION_TECH_DISCOVERED);
				try {
					ndef.addDataType("*/*");
				} catch (MalformedMimeTypeException e) {
					throw new RuntimeException("fail", e);
				}
				mFilters = new IntentFilter[] { ndef, };
				mTechLists = new String[][] { new String[] { MifareClassic.class
						.getName() } };
				// Intent intent = getIntent();
				// getMifareClassic(intent);
			} else {
				Toast.makeText(this, "nfc没有开启", Toast.LENGTH_SHORT).show();
				new AlertDialog.Builder(this)
						.setTitle("提示")
						.setMessage("是否开启nfc?")
						.setNegativeButton("是",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										Intent callGPSSettingIntent = new Intent(
												android.provider.Settings.ACTION_WIRELESS_SETTINGS);
										startActivity(callGPSSettingIntent);
									}
								})
						.setPositiveButton("否",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).create().show();
			}

		} else {
			Toast.makeText(this, "当前设备不支持nfc", Toast.LENGTH_SHORT).show();
		}
	}

	private void updateLogInfo(String msg) {
		String str = stapInfoTv.getText().toString();
		StringBuffer sb = new StringBuffer();
		sb.append(str + "\n");
		sb.append(msg + "\n");

		stapInfoTv.setText(sb.toString());
		// 滚动到底部
		scorll.fullScroll(ScrollView.FOCUS_DOWN);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		super.onResume();
		initNFC();
		if (isNFC && mAdapter.isEnabled()) {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
					mTechLists);
		}

	}

	@SuppressLint("NewApi")
	@Override
	protected void onPause() {
		super.onPause();
		if (isNFC && mAdapter.isEnabled()) {
			mAdapter.disableForegroundDispatch(this);
		}
	}

	@SuppressLint("NewApi")
	static MifareClassic getMifareClassic(Intent intent) {
		MifareClassic mfc = null;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			mfc = MifareClassic.get(tagFromIntent);
		}
		return mfc;
	}

	@SuppressLint("NewApi")
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@SuppressLint("NewApi")
	private void doAllRead(MifareClassic mfc) {
		isDoingM1 = true;
		MifareClassCard mifareClassCard = null;
		try {
			if (mfc == null) {
				updateLogInfo("不支持读写M1卡，请检查设备类型是否支持M1卡读写");
				isDoingM1 = false;
				return;
			}
			if (!mfc.isConnected()) {
				//默认连接超时时间是1s,这里读卡寻卡一般要1-2s，所以要重新设置超时时间
				mfc.setTimeout(4000);
				mfc.connect();
			}
			boolean auth = false;
			int secCount = mfc.getSectorCount();
			mifareClassCard = new MifareClassCard(secCount);
			int bCount = 0;
			int bIndex = 0;
			Log.i(TAG, "secCount:" + secCount);
			for (int j = 0; j < secCount; j++) {
				MifareSector mifareSector = new MifareSector();
				mifareSector.sectorIndex = j;
				byte[] ks = new byte[6];
				Arrays.fill(ks, (byte) 0xFF);
				// auth = mfc.authenticateSectorWithKeyB(j, ks);
				// auth = mfc.authenticateSectorWithKeyA(j,
				// MifareClassic.KEY_DEFAULT);
				auth = mfc.authenticateSectorWithKeyA(j,
						MifareClassic.KEY_DEFAULT);
				mifareSector.authorized = auth;
				if (auth) {
					bCount = mfc.getBlockCountInSector(j);
					bCount = Math.min(bCount, MifareSector.BLOCKCOUNT);
					bIndex = mfc.sectorToBlock(j);
					for (int i = 0; i < bCount; i++) {
						byte[] data = mfc.readBlock(bIndex);
						Log.i(TAG, "data:" + data);
						MifareBlock mifareBlock = new MifareBlock(data);
						mifareBlock.blockIndex = bIndex;
						bIndex++;
						mifareSector.blocks[i] = mifareBlock;
					}
					mifareClassCard.setSector(mifareSector.sectorIndex,
							mifareSector);
				} else {
//					updateLogInfo("M1卡认证失败");
					//认证失败，直接过滤当次读取
					isDoingM1 = false;
					return;
				}
			}
			String readStr = null;
			ArrayList<String> blockData = new ArrayList<String>();
			int blockIndex = 0;
			for (int i = 0; i < secCount; i++) {
				//获取那个扇区的内容
				MifareSector mifareSector = mifareClassCard.getSector(i);
				for (int j = 0; j < MifareSector.BLOCKCOUNT; j++) {
					//一个扇区有4个block块
					MifareBlock mifareBlock = mifareSector.blocks[j];
					byte[] data = mifareBlock.getData();
					blockData.add("Block " + blockIndex++ + " : "
							+ Converter.getHexString(data, data.length));
					Log.i(TAG, "read:" + new String(data));
				}
				//前面是获取所有内容，下面是获取第二扇区的第一个block块的内容
				if (i==1) {
					MifareBlock mifareBlock = mifareSector.blocks[0];
					byte[] data = mifareBlock.getData();
					readStr = new String(data);
				}
			}
			if (readStr==null) {
				updateLogInfo("读取M1卡内容失败");
			}else{
				updateLogInfo("读取内容信息："+readStr);
			}
		
		} catch (IOException e) {
			Log.e(TAG, e.getLocalizedMessage());
			updateLogInfo("读取M1卡内容失败");
		} finally {
			if (mifareClassCard != null) {
				mifareClassCard.debugPrint();
			}

			isDoingM1 = false;
		}
	}
	
	@SuppressLint("NewApi")
	private void doRead(MifareClassic mfc) {
		isDoingM1 = true;
		MifareClassCard mifareClassCard = null;
		try {
			if (mfc == null) {
				updateLogInfo("不支持读写M1卡，请检查设备类型是否支持M1卡读写");
				isDoingM1 = false;
				return;
			}
			if (!mfc.isConnected()) {
				mfc.connect();
			}
			byte[] bs = null;
			boolean auth = false;
			String readStr = null;
			auth = mfc.authenticateSectorWithKeyA(ID_SECTOR_INDEX, MifareClassic.KEY_DEFAULT);
			Log.i(TAG, "doRead auth:" + auth);
			if (auth) {
				 bs = mfc.readBlock(BLOCK_INDEX);
			} else {
				Log.i(TAG, "auth  fail");
			}
			if(bs != null) {
				readStr = new String(bs);
			}
			if (readStr==null) {
				updateLogInfo("读取M1卡内容失败");
			}else{
				updateLogInfo("读取内容信息："+readStr);
			}
		
		} catch (IOException e) {
			Log.e(TAG, e.getLocalizedMessage());
			updateLogInfo("读取M1卡内容失败");
		} finally {
			if (mifareClassCard != null) {
				mifareClassCard.debugPrint();
			}

			isDoingM1 = false;
		}
	}


	@SuppressLint("NewApi")
	void doWrite(MifareClassic mfc) {
		isDoingM1 = true;
		try {
			if (mfc == null) {
				updateLogInfo("不支持读写M1卡，请检查是否为旺Pos 2S设备");
				isDoingM1 = false;
				return;
			}
			if (!mfc.isConnected()) {
				//默认连接超时时间是1s
				mfc.connect();
			}
			boolean auth = false;
			auth = mfc.authenticateSectorWithKeyA(ID_SECTOR_INDEX, MifareClassic.KEY_DEFAULT);
			String writeStr = contentEt.getText().toString();
			if (auth) {
				if (writeStr != null && writeStr.length() != 0) {
					/**
					 * 每个block只能存放16个字节bytes（不能多不能少） 每4个block为一个扇区，
					 * 0-3的第一个扇区存储M1卡出厂信息 4-7的第二个扇区可存储信息， 其中第7个block存放秘钥和权限信息：
					 * 内容为：000000000000FF078069FFFFFFFFFFFF(byte[]转换成的16进制字符串)
					 * 前面6个byte字节内容：000000000000为秘钥A 中间4个byte字节内容：FF078069为权限信息
					 * 后面6个byte字节内容：FFFFFFFFFFFF为秘钥B 第三至第N个扇区存储的内容和第二个扇区一样
					 */
					byte[] bs = new byte[16];
					byte[] bytes = writeStr.getBytes();
					int len = bytes.length;
					System.out.println("write len = "+len);
					if (bytes == null || len > 16) {
						updateLogInfo("写入内容内容过长,最长写入16字节长度");
						isDoingM1 = false;
						return;
					} else if (len < 16) {
						// 长度小于16，需要补足
						System.arraycopy(bytes, 0, bs, 0, bytes.length);
					}

					mfc.writeBlock(BLOCK_INDEX, bs);
					mfc.close();
					updateLogInfo("M1卡成功写入内容：" + writeStr);
				} else {
					updateLogInfo("请填写你要写入的内容");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
//			updateLogInfo("M1卡写入失败");
		} finally {
			isDoingM1 = false;
			try {
				if (mfc != null) {
					mfc.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	@SuppressLint("NewApi")
	private static byte[] readID(MifareClassic mfc) {
		byte[] keyA = { (byte) 0xF2, 0x7D, 0x36, (byte) 0xFE, (byte) 0xB7,
				(byte) 0xD4 };// 用于读取的KeyA
		try {
			if (!mfc.isConnected()) {
				mfc.connect();
			}
			boolean auth = false;
			auth = mfc.authenticateSectorWithKeyA(ID_SECTOR_INDEX, keyA);
			if (auth) {
				byte[] bs = mfc.readBlock(ID_SECTOR_INDEX * 4 + 1);
				return bs;
			} else {
				System.out.println("authenticateSectorWithKeyA err.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				mfc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 向M1卡中写入数据
	 * 
	 * @param mfc
	 * @param bs
	 * @return
	 */
	@SuppressLint("NewApi")
	private static boolean writeID(MifareClassic mfc, byte[] bs) {
		if (bs.length != 16) {
			return false;
		}
		byte[] keyA = { (byte) 0xF2, 0x7D, 0x36, (byte) 0xFE, (byte) 0xB7,
				(byte) 0xD4 };// 用于读取的KeyA
		byte[] Contol = { (byte) 0xF0, (byte) 0xF7, (byte) 0x80, 0x69 };
		byte[] keyB = { (byte) 0xE2, (byte) 0xA2, 0x7A, 0x1E, 0x47, (byte) 0xDA };// 用于写入的KeyB

		byte[] block3 = new byte[16];
		System.arraycopy(keyA, 0, block3, 0, 6);
		System.arraycopy(Contol, 0, block3, 6, 4);
		System.arraycopy(keyB, 0, block3, 10, 6);

		try {
			if (!mfc.isConnected()) {
				mfc.connect();
			}
			boolean auth = false;
			auth = mfc.authenticateSectorWithKeyA(ID_SECTOR_INDEX,
					MifareClassic.KEY_DEFAULT);
			if (auth) {
				mfc.writeBlock(ID_SECTOR_INDEX * 4 + 1, bs);
				mfc.writeBlock(ID_SECTOR_INDEX * 4 + 3, block3);
				System.out.println("写入成功");
				return true;
			} else {
				System.out.println("authenticateSectorWithKeyA err.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				mfc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@SuppressLint("NewApi")
	private static byte[] readDatasByKeyB(MifareClassic mfc) {
		byte[] keyB = { (byte) 0xE2, (byte) 0xA2, 0x7A, 0x1E, 0x47, (byte) 0xDA };// 用于读取的KeyB
		try {
			if (!mfc.isConnected()) {
				mfc.connect();
			}
			boolean auth = false;
			auth = mfc.authenticateSectorWithKeyB(ID_SECTOR_INDEX, keyB);
			if (auth) {
				byte[] bs1 = mfc.readBlock(ID_SECTOR_INDEX * 4 + 1);
				byte[] bs2 = mfc.readBlock(ID_SECTOR_INDEX * 4 + 3);
				bs1 = Arrays.copyOf(bs1, 32);
				System.arraycopy(bs2, 0, bs1, 16, 16);
				return bs1;
			} else {
				System.out.println("authenticateSectorWithKeyB err.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				mfc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private boolean isDoingM1 = false;
	@Override
	protected void onNewIntent(Intent intent) {
		Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);

		Log.i("WeiPos", "onNewIntent  isDoingM1:" + isDoingM1);
		if(isDoingM1){
			return;
		}

		MifareClassic mfc = getMifareClassic(intent);

		if (operType == 0) {
			doRead(mfc);
		} else if (operType == 1) {
			doWrite(mfc);
		}

		// byte[] bs = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD, 0xE,
		// 0xF };
		// boolean r = writeID(mfc, bs);
		//
		// System.out.println("writeID:" + r);
		//
		// byte[] rs = readID(mfc);
		// System.out.println("readID>" + Arrays.toString(rs));
		// System.out.println("readID>" +
		// Arrays.toString(readDatasByKeyB(mfc)));

		super.onNewIntent(intent);
	}

	/**
	 * 设置当前检测进度
	 */
	private void setTestRange() {
		StringBuilder sb = new StringBuilder();
		sb.append("M1卡读写demo:\n");
		sb.append("1、选择读写的操作类型。（如果是写入操作可自定义写入内容）\n");
		sb.append("2、M1卡放在设备头部带有NFC标志处。\n");
		sb.append("3、观察M1卡是否读写成功。\n");

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
