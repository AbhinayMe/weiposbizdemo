package cn.weipass.biz;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import cn.weipass.pos.sdk.BlueBoxManager;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.bluetoothbox.IBluetoothBox;

/**
 * Created by bin on 2016/11/2.
 */
public class BluetoothCashActivity extends Activity implements View.OnClickListener{

    private static String TAG="BluetoothBox";
    private BlueBoxManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bluetoothcash);
        TextView topTitle = (TextView) findViewById(R.id.page_top_title);
        topTitle.setText("蓝牙钱箱");
        findViewById(R.id.btn_return).setOnClickListener(this);
        findViewById(R.id.btn_open_cash).setOnClickListener(this);
        manager = WeiposImpl.as().getService(BlueBoxManager.class);
        if (manager==null) {
            Toast.makeText(this,"SDK没有初始化",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_return:
                finish();
                break;
            case R.id.btn_open_cash:

                //调用SDK初始化连接前，请手动打开蓝牙开关连接好蓝牙钱箱，
                manager.setOnInitListener(new BlueBoxManager.InitConnectCallback(){

                    @Override
                    public void onInitResult(int status, String message){
                        Log.e(TAG,status+"----"+message);
                        if (status==0) {
                            manager.open();
                        } else {
                            Toast.makeText(BluetoothCashActivity.this,status+": "+message,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
    }
}
