package cn.weipass.biz;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.weipass.biz.util.CashierSign;
import cn.weipass.pos.sdk.BizServiceInvoker;
import cn.weipass.pos.sdk.BizServiceInvoker.OnResponseListener;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.bizInvoke.RequestInvoke;
import cn.weipass.service.bizInvoke.RequestResult;

/**
 * 本地bp应用调用收银进行支付
 * 
 * @author TIANHUI
 * 
 */
public class CashierInvokeActivity extends Activity implements OnClickListener {

	private TextView stapInfoTv;

	private ProgressDialog pd = null;

	private BizServiceInvoker mBizServiceInvoker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cashier_invoke);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("本地BP应用调用收银支付");
		findViewById(R.id.btn_return).setOnClickListener(this);

		stapInfoTv = (TextView) findViewById(R.id.test_content_stap_info);

		setTestRange();

		findViewById(R.id.btn_do_cashier).setOnClickListener(this);
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
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (pd != null) {
			pd.dismiss();
		}
	}

	/**
	 * 设置当前检测进度
	 */
	private void setTestRange() {
		StringBuilder sb = new StringBuilder();
		sb.append("本地BP应用调用收银支付\n");
		sb.append("1:首先POS设备需要绑定BP应用\n");
		sb.append("2:自动判断是否开通对应的收银服务\n");
		sb.append("3:如果没有开通则自动申请开通服务\n");
		sb.append("4:开通收银服务后，再次点击按钮请求调用\n");

		stapInfoTv.setText(sb.toString());
	}

	/**
	 * 本地调用收银服务
	 */
	private void requestCashier() {

		try {
			// 初始化服务调用
			mBizServiceInvoker = WeiposImpl.as().getService(BizServiceInvoker.class);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (mBizServiceInvoker == null) {
			Toast.makeText(this, "初始化服务调用失败", Toast.LENGTH_SHORT).show();
			return;
		}
		// 设置请求订阅服务监听结果的回调方法
		mBizServiceInvoker.setOnResponseListener(mOnResponseListener);
		innerRequestCashier();
	}


	//业务demo在bp平台中的的bpid，这里填写对应应用所属bp账号的bpid和对应的key--------------需要动态改变
	private String InvokeCashier_BPID="53b3a1ca45ceb5f96d153eec";
	private String InvokeCashier_KEY="LIz6bPS2z8jUnwLHRYzcJ6WK2X87ziWe";
	
	// 1001 现金
	// 1003 微信
	// 1004 支付宝
	// 1005 百度钱包
	// 1006 银行卡
	// 1007 易付宝
	// 1009 京东钱包
	// 1011 QQ钱包
	private String pay_type = "1003";
	// 第三方订单流水号，非空,发起请求，tradeNo不能相同，相同在收银会提示有存在订单
	private String tradeNo = System.currentTimeMillis() + "";
			
	private String channel = "POS";//标明是pos调用，不需改变
	private String body = "订单商品body描述";//订单body描述信息 ，不可空
	private String attach = "备注信息";//备注信息，可空，订单信息原样返回，可空
	private String total_fee = "1";//支付金额，单位为分，1=0.01元，100=1元，不可空
	private String seqNo = "1";//服务端请求序列,本地应用调用可固定写死为1
	// 如果需要页面调回到自己的App，需要在调用中增加参数package和classpath(如com.xxx.pay.ResultActivity)，并且这个跳转的Activity需要在AndroidManifest.xml中增加android:exported=”true”属性。
	// 如果不需要回调页面，则backPkgName和backClassPath需要同时设置为空字符串 ："";
	private String backPkgName = "cn.weipass.biz";//，可空
	private String backClassPath = "cn.weipass.biz.CashierInvokeActivity";//，可空
	//指定接收收银结果的url地址默认为："http://apps.weipass.cn/pay/notify"，可填写自己服务器接收地址
	private String notifyUrl = "http://apps.weipass.cn/pay/notify";//，可空
	
	// 1.执行调用之前需要调用WeiposImpl.as().init()方法，保证sdk初始化成功。
	//
	// 2.调用收银支付成功后，收银支付结果页面完成后，BizServiceInvoker.OnResponseListener后收到响应的结果
	//
	// 3.如果需要页面调回到自己的App，需要在调用中增加参数package和classpath(如com.xxx.pay.ResultActivity)，并且这个跳转的Activity需要在AndroidManifest.xml中增加android:exported=”true”属性。
	private void innerRequestCashier() {
		
		try {
			RequestInvoke cashierReq = new RequestInvoke();
			cashierReq.pkgName = this.getPackageName();
			cashierReq.sdCode = CashierSign.Cashier_sdCode;// 收银服务的sdcode信息
			cashierReq.bpId = InvokeCashier_BPID;
			cashierReq.launchType = CashierSign.launchType;
			
			cashierReq.params = CashierSign.sign(InvokeCashier_BPID, InvokeCashier_KEY, channel,
					pay_type, tradeNo, body, attach, total_fee, backPkgName, backClassPath, notifyUrl);
			cashierReq.seqNo = seqNo;

			RequestResult r = mBizServiceInvoker.request(cashierReq);
			Log.i("requestCashier", r.token + "," + r.seqNo + "," + r.result);
			// 发送调用请求
			if (r != null) {
				Log.d("requestCashier", "request result:" + r.result + "|launchType:" + cashierReq.launchType);
				String err = null;
				switch (r.result) {
				case BizServiceInvoker.REQ_SUCCESS: {
					// 调用成功
					Toast.makeText(this, "收银服务调用成功", Toast.LENGTH_SHORT).show();
					break;
				}
				case BizServiceInvoker.REQ_ERR_INVAILD_PARAM: {
					Toast.makeText(this, "请求参数错误！", Toast.LENGTH_SHORT).show();
					break;
				}
				case BizServiceInvoker.REQ_ERR_NO_BP: {
					Toast.makeText(this, "未知的合作伙伴！", Toast.LENGTH_SHORT).show();
					break;
				}
				case BizServiceInvoker.REQ_ERR_NO_SERVICE: {
					//调用结果返回，没有订阅对应bp账号中的收银服务，则去调用sdk主动订阅收银服务
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (pd == null) {
								pd = new ProgressDialog(CashierInvokeActivity.this);
							}
							pd.setMessage("正在申请订阅收银服务...");
							pd.show();
							// 如果没有订阅，则主动请求订阅服务
							mBizServiceInvoker.subscribeService(CashierSign.Cashier_sdCode,
									InvokeCashier_BPID);
						}
					});
					break;
				}
				case BizServiceInvoker.REQ_NONE: {
					Toast.makeText(this, "请求未知错误！", Toast.LENGTH_SHORT).show();
					break;
				}
				}
				if (err != null) {
					Log.w("requestCashier", "serviceInvoker request err:" + err);
				}
			}else{
				Toast.makeText(this, "请求结果对象为空！", Toast.LENGTH_SHORT).show();
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 这个是服务调用完成后的响应监听方法
	 */
	private OnResponseListener mOnResponseListener = new OnResponseListener() {

		@Override
		public void onResponse(String sdCode, String token, byte[] data) {
			// 收银服务调用完成后的返回方法
			Log.e("requestCashier onResponse",
					"sdCode = " + sdCode + " , token = " + token + " , data = " + new String(data));
			/* new String(data) 为支付结果json，内容样式为:
			   {
			    "errCode": "-1",
			    "errMsg": "取消交易",
			    "out_trade_no": "1474442791311",
			    "trade_status": null,
			    "input_charset": "UTF-8",
			    "cashier_trade_no": null,
			    "pay_type": null,
			    "pay_info": "取消交易"
			}*/
			if (pd != null) {
				pd.hide();
			}
			
		}

		@Override
		public void onFinishSubscribeService(boolean result, String err) {
			// TODO Auto-generated method stub
			// 申请订阅收银服务结果返回
			if (pd != null) {
				pd.hide();
			}
			// bp订阅收银服务返回结果
			if (!result) {
				//订阅失败
				Toast.makeText(CashierInvokeActivity.this, err, Toast.LENGTH_SHORT).show();
			}else{
				//订阅成功
				Toast.makeText(CashierInvokeActivity.this, "订阅收银服务成功，请按home键回调主页刷新订阅数据后重新进入调用收银", Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_return:
			onBackPressed();
			break;
		case R.id.btn_do_cashier:
			requestCashier();
			break;
		default:
			break;
		}
	}

}
