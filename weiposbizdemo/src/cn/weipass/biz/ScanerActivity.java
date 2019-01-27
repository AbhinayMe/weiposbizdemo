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
import android.widget.Toast;

import cn.weipass.pos.sdk.Scanner;
import cn.weipass.pos.sdk.Scanner.OnResultListener;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.scan.ScanRuleBean;

/**
 * @author Tianhui
 * sdk使用DataChannel与服务端交互实现自定义业务
 */
public class ScanerActivity extends Activity implements
        OnClickListener {

    private Scanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.scaner);

        TextView topTitle = (TextView) findViewById(R.id.page_top_title);
        topTitle.setText("调用扫描二维码和条码");
        findViewById(R.id.btn_return).setOnClickListener(this);

        findViewById(R.id.btn_scaner_one).setOnClickListener(this);
        findViewById(R.id.btn_scaner_two).setOnClickListener(this);
        findViewById(R.id.btn_scaner_three).setOnClickListener(this);
        findViewById(R.id.btn_scaner_four).setOnClickListener(this);

        // 调用扫码二维码的activity需要在Manifest文件中配置android:exported="true"属性
        //获取sdk扫描对象
        scanner = WeiposImpl.as().openScanner();
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
            case R.id.btn_scaner_three://小米之家
// 调用扫码二维码的activity需要在Manifest文件中配置android:exported="true"属性
                if (scanner == null) {
                    Toast.makeText(ScanerActivity.this, "SDK扫描对象为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                /**
                 * 扫码类型，二维码
                 * TYPE_QR = 1;
                 *
                 * 扫描类型，条码
                 *  TYPE_BAR = 2;
                 *
                 * 扫描类型，条码（小米之家）
                 *  TYPE_SPECIAL_BAR = -1;
                 */

                ScanRuleBean rule1 = new ScanRuleBean("99", 0);
                ScanRuleBean rule2 = new ScanRuleBean("/2", 6);
                ScanRuleBean[] ruleBeans = {rule1, rule2};
                scanner.scanFilter(Scanner.TYPE_SPECIAL_BAR, ruleBeans, new OnResultListener() {

                    @Override
                    public void onResult(int what, String in) {
                        // TODO Auto-generated method stub

                        final String info = in;
                        // 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showResultInfo("扫码结果", "信息", info);

                            }
                        });
                    }
                });

                break;
            case R.id.btn_scaner_one:
                // 调用扫码二维码的activity需要在Manifest文件中配置android:exported="true"属性
                if (scanner == null) {
                    Toast.makeText(ScanerActivity.this, "SDK扫描对象为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                /**
                 * 扫码类型，二维码
                 * TYPE_QR = 1;
                 *
                 * 扫描类型，条码
                 *  TYPE_BAR = 2;
                 */
                scanner.scan(Scanner.TYPE_QR, new OnResultListener() {

                    @Override
                    public void onResult(int what, String in) {
                        // TODO Auto-generated method stub

                        final String info = in;
                        // 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showResultInfo("扫码结果", "二维码信息", info);

                            }
                        });
                    }
                });
                break;
            case R.id.btn_scaner_four:
                // 调用扫码二维码的activity需要在Manifest文件中配置android:exported="true"属性
                if (scanner == null) {
                    Toast.makeText(ScanerActivity.this, "SDK扫描对象为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                scanner.scan(Scanner.TYPE_SPECIAL_BAR, new OnResultListener() {

                    @Override
                    public void onResult(int what, String in) {
                        // TODO Auto-generated method stub

                        final String info = in;
                        // 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showResultInfo("扫码结果", "条码信息", info);

                            }
                        });
                    }
                });
                break;

            case R.id.btn_scaner_two:
                // 调用扫码二维码的activity需要在Manifest文件中配置android:exported="true"属性
                if (scanner == null) {
                    Toast.makeText(ScanerActivity.this, "SDK扫描对象为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                /**
                 * 扫码类型，二维码
                 * TYPE_QR = 1;
                 *
                 * 扫描类型，条码
                 *  TYPE_BAR = 2;
                 */
                scanner.scan(Scanner.TYPE_BAR, new OnResultListener() {

                    @Override
                    public void onResult(int what, String in) {
                        // TODO Auto-generated method stub

                        final String info = in;
                        // 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showResultInfo("扫码结果", "条码信息", info);

                            }
                        });
                    }
                });
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
