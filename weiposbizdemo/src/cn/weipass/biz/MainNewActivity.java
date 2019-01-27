package cn.weipass.biz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import cn.weipass.pos.sdk.Ped;
import cn.weipass.pos.sdk.PsamManager;
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

		//初始化sdk，只需要在apk启动入口初始化一次，当应用完全退出是会自动调用sdk的onDestroy()
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

		// 判断玩设备类型后显示操作项
		initData();

	}

	@Override
	public void onResume(){
		super.onResume();
		if ("tab".equals(SdkTools.deviceType)) {
			if (!dataList.contains("蓝牙钱箱")) {
				dataList.add("蓝牙钱箱");
				updateView();
			}
		} else  {
			dataList.remove("蓝牙钱箱");
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
				 * 返回信息为json字符串，具体参数说明如下： mcode：商户Code model: 设备类型名
				 * deviceType:设备类型，2，2s，3；（2：pos2,2s:pos2s,3:pos3）
				 * ota-name:设备ota版本名称 name：登陆用户名 snCode：设备SN号 en：设备EN号
				 * longitude:经度 latitude:维度 loginType：员工卡类型
				 */
				String deviceInfo = WeiposImpl.as().getDeviceInfo();
				showMsgDialog("设备信息", "返回结果：", deviceInfo);
				break;
			case 1:
				//扫描二维码和条码信息
				intent = new Intent(thisActivity, ScanerActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 2:
				//本地调用SDK播音
				String voiceStr = "你好，正在调用SDK播放语音。";//这里是需要播音的内容
				WeiposImpl.as().speech(voiceStr);//本地调用播音请求
				break;
			case 3:
				//sdk使用datachannel和服务端交互
				intent = new Intent(thisActivity, DataChannelUseActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 4:
				//sdk调用pos打印
				intent = new Intent(thisActivity, PrinterActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 5:
				//旺POS NFC通讯
				intent = new Intent(thisActivity, NfcActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 6:
				//旺POS 声波通讯
				intent = new Intent(thisActivity, SonarActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 7:
				//旺POS 磁条卡刷卡
				intent = new Intent(thisActivity, MagneticCardActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 8:
				//本地BP应用调用收银支付
				intent = new Intent(thisActivity, CashierInvokeActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 9:
				//打开自定义webview
				intent = new Intent(thisActivity, WebTurnViewActivity.class);
				thisActivity.startActivity(intent);
				break;
			case 10:
				//授权管理测试
				// 授权管理测试首先需要在pos端在设置页开启授权管理
				intent = new Intent(MainNewActivity.this, AuthorizeTextActivity.class);
				MainNewActivity.this.startActivity(intent);
				break;
			case 11:
				//PSAM卡检测
				intent = new Intent(MainNewActivity.this,PSAMTestActivity.class);
				MainNewActivity.this.startActivity(intent);
				break;
			case 12:
				//RSA接口测试
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
						Toast.makeText(MainNewActivity.this,"此功能只能在TAB上使用",Toast.LENGTH_SHORT).show();
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
		builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void initData() {
		dataList.clear();
		dataList.add("获取设备信息");
		dataList.add("调用扫码\n(二维码、条码)");
		dataList.add("本地调用SDK播音");
		dataList.add("使用DataChannel数据通道\n(输码取票、扫描取票、推送播音)");
		dataList.add("调用POS打印\n(普通打印-已不维护、点阵打印-推荐)");
		dataList.add("旺POS NFC通讯\n(标准卡、CUP卡、M1卡、银联卡)");
		dataList.add("旺POS 声波通讯");
		dataList.add("旺POS 磁条卡刷卡");
		dataList.add("本地BP应用调用收银支付");
		dataList.add("JS与本地代码交互调用SDK");
		dataList.add("授权管理测试");
		dataList.add("PSAM卡检测");
		dataList.add("RSA接口测试");
		dataList.add("VGA测试");
		dataList.add("蓝牙钱箱");
		//蓝牙钱箱只显示在TAB上，后面如果增加新的item，建议将onclick的case 数值，蓝牙钱箱改为最后一个
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
		// 注意：destroy函数在一级根页面的onDestroy调用，以防止在二级页面或者返回到一级页面中
		// 使用weipos能力对象（例如：Printer）抛出服务未初始化的异常.
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
