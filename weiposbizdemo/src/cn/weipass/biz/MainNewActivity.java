package cn.weipass.biz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.weipass.biz.util.HEX;
import cn.weipass.biz.util.SdkTools;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.sam.SamResult;

public class MainNewActivity extends Activity implements OnClickListener {
	private ArrayList<String> dataList = new ArrayList<String>();
	private ListView lv;
	private MainAdapter adapter;
	private ProgressDialog pd = null;

	private ImageView imageView;
	private Activity thisActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_new);

		//Initialize sdk, only need to initialize the apk startup entry once, when the application completely exits, it will automatically call sdk's onDestroy()
		SdkTools.initSdk(this);
				
		thisActivity = this;
		
		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("旺POS业务demo");
		findViewById(R.id.btn_return).setOnClickListener(this);
		pd = new ProgressDialog(this);
		imageView = (ImageView) findViewById(R.id.app_demo_take_phone);

		lv = (ListView) findViewById(R.id.app_demo_lv);
		adapter = new MainAdapter(this, dataList);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(onItemClickListener);

		// Determine the action item after playing the device type
		initData();

	}

	@Override
	public void onResume(){
		super.onResume();
		if ("tab".equals(SdkTools.deviceType)) {
			if (!dataList.contains("Bluetooth cash box")) {
				dataList.add("Bluetooth cash box");
				updateView();
			}
		} else  {
			dataList.remove("Bluetooth cash box");
		}
	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			Intent intent = null;
			switch (position) {
			case 0:
				/**
				 * The return information is a json string. The specific parameters are as follows: mcode: Merchant Code model: Device type name
				 * deviceType: device type, 2, 2s, 3; (2: pos2, 2s: pos2s, 3: pos3)
				 * ota-name: device ota version name name: login user name snCode: device SN number en: device EN number
				 * longitude: longitude latitude: dimension loginType: employee card type
				 */
				String deviceInfo = WeiposImpl.as().getDeviceInfo();
				showMsgDialog("Device Information", "Return result：", deviceInfo);
				break;
			case 1:
				//Scan QR code and barcode information
				intent = new Intent(thisActivity, ScanerActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 2:
				//Local call to SDK broadcast
				String voiceStr = "Hello, is calling the SDK to play the voice."; // here is the content that needs to be broadcast
				WeiposImpl.as().speech(voiceStr);//Local call broadcast request
				break;
			case 3:
				//Sdk uses datachannel to interact with the server
				intent = new Intent(thisActivity, DataChannelUseActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 4:
				//Sdk calls pos print
				intent = new Intent(thisActivity, PrinterActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 5:
				//Wang POS NFC Communication
				intent = new Intent(thisActivity, NfcActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 6:
				//Wang POS Acoustic Communication
				intent = new Intent(thisActivity, SonarActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 7:
				//Wang POS magnetic stripe card swipe
				intent = new Intent(thisActivity, MagneticCardActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 8:
				//Local BP application calls cashier payment
				intent = new Intent(thisActivity, CashierInvokeActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 9:
				//Open custom webview
				intent = new Intent(thisActivity, WebTurnViewActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 10:
				//Authorization management test
				//Authorization management test first needs to open the authorization management on the setup page in the pos terminal.
				intent = new Intent(MainNewActivity.this, AuthorizeTextActivity.class);
				MainNewActivity.this.startActivity(intent);
				break;
			case 11:
				//PSAM card detection
				intent = new Intent(MainNewActivity.this,PSAMTestActivity.class);
				MainNewActivity.this.startActivity(intent);
				break;
			case 12:
				//RSA interface test
				intent = new Intent(MainNewActivity.this, RsaTestActivity.class);
				MainNewActivity.this.startActivity(intent);
				break;
			case 13:
					intent = new Intent(MainNewActivity.this, DockPictureActivity.class);
					MainNewActivity.this.startActivity(intent);
			break;
				case 14:
					if ("tab".equals(SdkTools.deviceType)) {
						intent = new Intent(MainNewActivity.this, BluetoothCashActivity.class);
						MainNewActivity.this.startActivity(intent);
					} else {
						Toast.makeText(MainNewActivity.this,"This feature can only be used on TAB",Toast.LENGTH_SHORT).show();
					}
					break;
			default:
				break;
			}
		}
	};
	

	private boolean isRun=false;
	private Runnable testPsamRunnable=new Runnable() {
		@Override
		public void run() {
			isRun=true;
			while (isRun) {
				final byte[] cmd_selectMF = new byte[]{0x00, (byte) 0xa4, 0x00, 0x00, 0x02, 0x3f, 0x00};
				SamResult samResult = WeiposImpl.as().openPsamManager().doCommand(cmd_selectMF);
				if(samResult==null)
					continue;
				System.out.println("cmd_selectMF=========>>>" + samResult.code + "|" + HEX.bytesToHex(samResult.data));
				byte[] cmd_getRespose = new byte[]{0x00, (byte) 0xC0, 0x00, 0x00, 0x00};
				cmd_getRespose[4] = (byte) (samResult.code & 0xff);// 长度
				samResult = WeiposImpl.as().openPsamManager().doCommand(cmd_getRespose);
				if(samResult==null)
					continue;
				System.out.println("cmd_getRespose=========>>>" + samResult.code + "|" + HEX.bytesToHex(samResult.data));
				final byte[] cmd_selectDF = new byte[]{0x00, (byte) 0xa4, 0x00, 0x00, 0x02, 0x3f, 0x01};
				samResult = WeiposImpl.as().openPsamManager().doCommand(cmd_selectDF);
				if(samResult==null)
					continue;
				System.out.println("cmd_selectDF=========>>>" + samResult.code + "|" + HEX.bytesToHex(samResult.data));
				WeiposImpl.as().openPsamManager().reset();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private void showMsgDialog(String title, String tip, String info) {
		AlertDialog.Builder builder = new Builder(this);

		builder.setMessage(tip + ":" + info);
		builder.setTitle(title);
		builder.setPositiveButton("confirm", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});
		builder.setNegativeButton("cancel", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void initData() {
		dataList.clear();
		dataList.add("Get device information");
		dataList.add("call scan code \n (two-dimensional code, barcode)");
		dataList.add("Local call SDK broadcast");
		dataList.add("Use DataChannel data channel\n (pass code picking, scan ticket picking, push broadcast)");
		dataList.add("call POS print\n (plain print - no maintenance, dot matrix print - recommended)");
		dataList.add("wang POS NFC Communication\n (Standard Card, CUP Card, M1 Card, UnionPay Card)");
		dataList.add("wang POS Sonic Communication");
		dataList.add("wang POS magnetic stripe card swipe");
		dataList.add("Local BP application calls cashier payment");
		dataList.add("JS interacts with native code to call SDK");
		dataList.add("authorization management test");
		dataList.add("PSAM card detection");
		dataList.add("RSA interface test");
		dataList.add("VGA test");
		dataList.add("Bluetooth cash box");
		//The Bluetooth cashbox is only displayed on the TAB. If a new item is added later, it is recommended to change the case value of the onclick to the last one.
		updateView();
	}

	private void updateView() {
		adapter.mList = dataList;
		adapter.notifyDataSetChanged();
	}

	public class MainAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<String> mList = new ArrayList<String>();

		public MainAdapter(Context context, ArrayList<String> list) {
			this.mContext = context;
			this.mList = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return mList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View view, ViewGroup arg2) {
			// TODO Auto-generated method stub
			String str = mList.get(arg0);
			if (view == null) {
				view = LayoutInflater.from(mContext).inflate(R.layout.main_item, null);
			}
			Button btnName = (Button) view.findViewById(R.id.btn_name);
			btnName.setText(str);

			return view;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// Note: The destroy function is called on the onDestroy of the first-level root page to prevent it from being in the secondary page or returning to the first-level page.
		// Use the weipos capability object (for example: Printer) to throw a service uninitialized exception.
		try {
			WeiposImpl.as().destroy();
		} catch (Exception e) {
			// TODO: handle exception
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
