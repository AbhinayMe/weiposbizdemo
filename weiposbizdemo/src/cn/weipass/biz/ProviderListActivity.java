package cn.weipass.biz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.weipass.biz.vo.PosProviderInfo;
import cn.weipass.pos.sdk.ServiceManager;
import cn.weipass.pos.sdk.ServiceManager.EventCallback;
import cn.weipass.pos.sdk.impl.WeiposImpl;

@SuppressWarnings("deprecation")
public class ProviderListActivity extends Activity implements OnClickListener{
	private ArrayList<Parcelable> providerList = new ArrayList<Parcelable>();
	private ListView lv;
	private MainAdapter adapter;
	
	private ServiceManager mServiceManager = null;
	
	private ProgressDialog pd = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.provider_list);
		
		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText(R.string.other_56);
		findViewById(R.id.btn_return).setOnClickListener(this);
		
		//获取服务管理对象
		mServiceManager = WeiposImpl.as().openServiceManager();
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle!=null) {
			providerList = (ArrayList<Parcelable>) bundle.get("providerList");
		}
		if (providerList == null) {
			providerList = new ArrayList<Parcelable>();
		}
		lv = (ListView) findViewById(R.id.provider_lv);
		adapter = new MainAdapter(this, providerList);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				PosProviderInfo item = (PosProviderInfo) providerList.get(position);
				
				//调用第三方支付基础服务
				//providerId:支付提供号，已经回调activity路径
				/**
				 * 第一个参数：基础服务的类型
				 * 第二个参数：基础服务的providerId
				 * 第三个参数：传入的Map参数
				 * 第四个参数：传入第三方服务需回调自己界面的activity，需定义为外部可调用（exported=true）
				 */
				mServiceManager.invokeService(ServiceManager.SERVICE_TYPE_PAYMENT,item.providerId, null, "cn.weipass.biz.ProviderCallBackActivity");
				
			}
		});
		
		//主动获取旺pos收银支持的支付方式
		getProviderInfo();
		
		TextView emptyView = (TextView) LayoutInflater.from(this).inflate(R.layout.empty_layout, null);
		emptyView.setText("暂无订单信息");
		((ViewGroup) lv.getParent()).addView(emptyView);//需要这样添加才能显示，只需要添加一次
		lv.setEmptyView(emptyView);
	}
	
	/**
	 * 获取设备支持的支付方式 备注：只有设备申请开通并且审核通过的支付基础服务才能获取到
	 */
	@SuppressWarnings("deprecation")
	private void getProviderInfo() {
		
		if (mServiceManager == null) {
			Toast.makeText(this, "初始化支付提供sdk错误", Toast.LENGTH_LONG).show();
			return;
		}

		if (pd==null) {
			pd = new ProgressDialog(this);
		}
		pd.setMessage("正在获取支持的支付方式....");
		pd.show();
		// 设置数据返回监听
		mServiceManager.setEventCallback(new EventCallback() {

			@Override
			public void onGetProviders(List<Map<String, Object>> providers, int serviceType) {
				if (pd!=null) {
					pd.hide();
				}
				if (providers != null) {
					int size = providers.size();
					if (size > 0) {
						providerList.clear();
						for (Map<String, Object> item : providers) {
							PosProviderInfo providerInfo = new PosProviderInfo();
							String providerId = (String) item.get(ServiceManager.KEY_PROVIDER_ID);
							String name = (String) item.get(ServiceManager.KEY_NAME);
							byte[] icon = (byte[]) item.get(ServiceManager.KEY_ICON);
							providerInfo.providerId = providerId;
							providerInfo.name = name;
							providerInfo.icon = icon;

							providerList.add(providerInfo);
						}
						
						updateView();

					} else {
						Toast.makeText(ProviderListActivity.this, "暂无可用支付基础服务", Toast.LENGTH_LONG).show();
					}
				}
			}

			@Override
			public void onError(String err) {
				final String e = err;
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (pd!=null) {
							pd.hide();
						}
						Toast.makeText(ProviderListActivity.this, e, Toast.LENGTH_SHORT).show();
					}

				});
			}
		});
		// 主动获获取支付方式
		mServiceManager.getAllServiceProvider(ServiceManager.SERVICE_TYPE_PAYMENT);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (pd!=null) {
			pd.dismiss();
		}
	}
	private void updateView(){
		adapter.mList = providerList;
		adapter.notifyDataSetChanged();
		
	}
	
	public class MainAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<Parcelable> mList = new ArrayList<Parcelable>();

		public MainAdapter(Context context, ArrayList<Parcelable> list) {
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
			PosProviderInfo item = (PosProviderInfo) mList.get(arg0);
			if (view == null) {
				view = LayoutInflater.from(mContext).inflate(
						R.layout.provider_list_item, null);
			}
			ImageView iv = (ImageView) view.findViewById(R.id.img);
			TextView name = (TextView) view.findViewById(R.id.name);
			ImageView ivMore = (ImageView) view.findViewById(R.id.icon_more);
			name.setText(item.name);
			if (item.icon!=null) {
				Bitmap src = BitmapFactory.decodeByteArray(item.icon, 0, item.icon.length);
				iv.setImageBitmap(src);
			}
			
			return view;
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
