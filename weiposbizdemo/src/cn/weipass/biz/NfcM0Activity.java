package cn.weipass.biz;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cn.weipass.biz.nfc.CpuCardBiz;
import cn.weipass.biz.nfc.NFCManager;
import cn.weipass.biz.nfc.BankCard.BankCardInfo;
import cn.weipass.biz.util.HEX;
import cn.weipass.pos.sdk.PiccManager;
import cn.weipass.pos.sdk.impl.WeiposImpl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.TagTechnology;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class NfcM0Activity extends Activity implements OnClickListener {

    private final String TAG = "NfcM0Activity";
    private TextView stapInfoTv;
    private NFCManager mNFCManager;
    private IsoDep na;
    private PiccManager mPiccManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_normal);

        TextView topTitle = (TextView) findViewById(R.id.page_top_title);
        topTitle.setText("旺POS M0卡通讯");
        findViewById(R.id.btn_return).setOnClickListener(this);

        stapInfoTv = (TextView) findViewById(R.id.test_content_stap_info);

        setTestRange();

        mNFCManager = NFCManager.getInstance();
        mNFCManager.init(this);
        mPiccManager = WeiposImpl.as().openPiccManager();
    }


    private void updateLogInfo(String msg) {
        String str = stapInfoTv.getText().toString();
        StringBuffer sb = new StringBuffer();
        sb.append(str + "\n");
        sb.append(msg + "\n");

        stapInfoTv.setText(sb.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
//		mNFCManager.setNFCListener(mNFCListener);
        mNFCManager.onResume(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mNFCManager.onPause(this);
    }

    byte[] block1 = {1, 2, 3, 4};
    byte[] block2 = {5, 6, 7, 8};
    byte[] block3 = {9, 10, 11, 12};
    byte[] block4 = {13, 14, 15, 0};

    /*
     *  M0卡 读写数据地址范围0x00 -> 0x2a
     *  M0卡每次只能在1块写4个字节，每次读可以读4块共16个字节。
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.e(TAG, "onNewIntent: " + tag.toString());
        if (Build.MODEL.equals("WISENET5")) {//NET5设备
            String result = readTagUltralight(tag);
            updateLogInfo("M0卡读取成功 BLOCK数据为：" + result);
        } else {
            try {
                na = IsoDep.get(tag);
                na.setTimeout(5000);
                na.connect();
                int[] len = new int[1];
                int ret = mPiccManager.NFCTagWriteBlock(0x04, block1);
                ret = mPiccManager.NFCTagWriteBlock(0x05, block2);
                ret = mPiccManager.NFCTagWriteBlock(0x06, block3);
                ret = mPiccManager.NFCTagWriteBlock(0x07, block4);
                if (ret == 0) {
                    updateLogInfo("M0卡写入成功 BLOCK数据为：" + HEX.bytesToHex(block1) + HEX.bytesToHex(block2) + HEX.bytesToHex(block3) + HEX.bytesToHex(block4));
                }
                byte[] result = mPiccManager.NFCTagReadBlock(0x04, len);
                if (result != null) {
                    updateLogInfo("M0卡读取成功 BLOCK数据为：" + HEX.bytesToHex(result));
                }
                Log.i(TAG, "result:" + HEX.bytesToHex(result) + " ret:" + ret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "onNewIntent");
        super.onNewIntent(intent);
    }

    public String readTagUltralight(Tag tag) {
        MifareUltralight mifare = MifareUltralight.get(tag);
        try {
            mifare.connect();
            int size = mifare.PAGE_SIZE;
            byte[] payload = mifare.readPages(0);
            String result = "page1：" + HEX.bytesToHex(payload) + "\n" + "总容量：" + String.valueOf(size) + "\n";

//            //这里只读取了其中几个page
//            byte[] payload1 = mifare.readPages(4);
//            byte[] payload2 = mifare.readPages(8);
//            byte[] payload3 = mifare.readPages(12);
//            result += "page4:" + HEX.bytesToHex(payload1) + "\npage8:" + HEX.bytesToHex(payload2) + "\npage12：" + HEX.bytesToHex(payload3) + "\n";

            return result;
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight message...",
                    e);
            return "读取失败！";
        } catch (Exception ee) {
            Log.e(TAG, "IOException while writing MifareUltralight message...",
                    ee);
            return "读取失败！";
        } finally {
            if (mifare != null) {
                try {
                    mifare.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
    }
//	private NFCManager.NFCListener mNFCListener = new NFCManager.NFCListener() {
//
//		@Override
//		public void onReciveDataOffline(final byte[] data) {
//			if (data != null) {
//				try {
//					String cardId = new String(data, "UTF-8");
//					updateLogInfo("旺POS CPU NFC卡，卡号："+cardId);
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//					updateLogInfo("NFC读卡失败");
//				} 
//				mNFCManager.clearNFCParams();
//			} else {
//				updateLogInfo("NFC读卡失败");
//			}
//		}
//
//		@Override
//		public void onError(String error) {
//			// TODO Auto-generated method stub
//			updateLogInfo(error);
//		}
//
//		@Override
//		public void onReciveBankDataOffline(BankCardInfo bankCard) {
//			// TODO Auto-generated method stub
//			
//		}
//	};

    @SuppressLint("NewApi")
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
