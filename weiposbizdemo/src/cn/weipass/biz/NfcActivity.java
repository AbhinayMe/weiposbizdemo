package cn.weipass.biz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

/**
 * @author Tianhui
 *         Activity不能设置为singleTask模式（onNewIntent会失效）
 *         nfc使用示例
 */
public class NfcActivity extends Activity implements
        OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nfc);

        TextView topTitle = (TextView) findViewById(R.id.page_top_title);
        topTitle.setText("旺POS NFC通讯");
        findViewById(R.id.btn_return).setOnClickListener(this);

        findViewById(R.id.btn_nfc_normal).setOnClickListener(this);
        findViewById(R.id.btn_nfc_cpu).setOnClickListener(this);
        findViewById(R.id.btn_nfc_m0).setOnClickListener(this);
        findViewById(R.id.btn_nfc_m1).setOnClickListener(this);
        findViewById(R.id.btn_nfc_unionpay).setOnClickListener(this);
        findViewById(R.id.btn_nfc_switch).setOnClickListener(this);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_return:
                onBackPressed();
                break;
            case R.id.btn_nfc_normal:
                intent = new Intent(this, NfcNormalActivity.class);
                //先将要启动的activity设置SINGLE_TOP，避免开启多个实现nfc功能页面产生冲突影响
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                this.startActivity(intent);
                break;

            case R.id.btn_nfc_cpu:
                intent = new Intent(this, NfcCpuActivity.class);
                //先将要启动的activity设置SINGLE_TOP，避免开启多个实现nfc功能页面产生冲突影响
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                this.startActivity(intent);
                break;
            case R.id.btn_nfc_m0:
                intent = new Intent(this, NfcM0Activity.class);
                //先将要启动的activity设置SINGLE_TOP，避免开启多个实现nfc功能页面产生冲突影响
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                this.startActivity(intent);
                break;
            case R.id.btn_nfc_m1:
                intent = new Intent(this, NfcM1Activity.class);
                //先将要启动的activity设置SINGLE_TOP，避免开启多个实现nfc功能页面产生冲突影响
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                this.startActivity(intent);
                break;
            case R.id.btn_nfc_unionpay:
                intent = new Intent(this, NfcBankActivity.class);
                //先将要启动的activity设置SINGLE_TOP，避免开启多个实现nfc功能页面产生冲突影响
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                this.startActivity(intent);
                break;
            case R.id.btn_nfc_switch:
                intent = new Intent(this, NfcSwitchNormalActivity.class);
                //先将要启动的activity设置SINGLE_TOP，避免开启多个实现nfc功能页面产生冲突影响
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                this.startActivity(intent);
                break;
            default:
                break;
        }
    }


    private void showResultInfo(String operInfo, String titleHeader, String info) {
        AlertDialog.Builder builder = new Builder(this);

        builder.setMessage(titleHeader + ":" + info);
        builder.setTitle(operInfo);
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

}
