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
 * Sdk uses DataChannel to interact with the server to implement custom services.
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
        topTitle.setText("Call scan QR code and barcode");
        findViewById(R.id.btn_return).setOnClickListener(this);

        findViewById(R.id.btn_scaner_one).setOnClickListener(this);
        findViewById(R.id.btn_scaner_two).setOnClickListener(this);
        findViewById(R.id.btn_scaner_three).setOnClickListener(this);
        findViewById(R.id.btn_scaner_four).setOnClickListener(this);

        // The activity that calls the scan code QR code needs to configure the android:exported="true" attribute in the Manifest file.
        // Get sdk scan object
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
            case R.id.btn_scaner_three://The house of Xiaomi
                // The activity that calls the scan code QR code needs to configure the android:exported="true" attribute in the Manifest file.
                if (scanner == null) {
                    Toast.makeText(ScanerActivity.this, "SDK scan object is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                /**
                 * Scan code type, QR code
                 * TYPE_QR = 1;
                 *
                 * Scan type, barcode
                 * TYPE_BAR = 2;
                 *
                 * Scan type, barcode (The House of Xiaomi)
                 * * TYPE_SPECIAL_BAR = -1;
                 */

                ScanRuleBean rule1 = new ScanRuleBean("99", 0);
                ScanRuleBean rule2 = new ScanRuleBean("/2", 6);
                ScanRuleBean[] ruleBeans = {rule1, rule2};
                scanner.scanFilter(Scanner.TYPE_SPECIAL_BAR, ruleBeans, new OnResultListener() {

                    @Override
                    public void onResult(int what, String in) {
                        // TODO Auto-generated method stub

                        final String info = in;
                        // The UI function cannot be done in the callback function, so you can use the runOnUiThread function to wrap the code block.
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showResultInfo("Scan code result", "information", info);

                            }
                        });
                    }
                });

                break;
            case R.id.btn_scaner_one:
                // The activity that calls the scan code QR code needs to configure the android:exported="true" attribute in the Manifest file.
                if (scanner == null) {
                    Toast.makeText(ScanerActivity.this, "SDK scan object is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                /**
                 * Scan code type, QR code
                 * TYPE_QR = 1;
                 *
                 * Scan type, barcode
                 *  TYPE_BAR = 2;
                 */
                scanner.scan(Scanner.TYPE_QR, new OnResultListener() {

                    @Override
                    public void onResult(int what, String in) {
                        // TODO Auto-generated method stub

                        final String info = in;
                        // The UI function cannot be done in the callback function, so you can use the runOnUiThread function to wrap the code block.
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showResultInfo("Scan code result", "QR code information", info);

                            }
                        });
                    }
                });
                break;
            case R.id.btn_scaner_four:
                // The activity that calls the scan code QR code needs to configure the android:exported="true" attribute in the Manifest file.
                if (scanner == null) {
                    Toast.makeText(ScanerActivity.this, "SDK scan object is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                scanner.scan(Scanner.TYPE_SPECIAL_BAR, new OnResultListener() {

                    @Override
                    public void onResult(int what, String in) {
                        // TODO Auto-generated method stub

                        final String info = in;
                        // The UI function cannot be done in the callback function, so you can use the runOnUiThread function to wrap the code block.
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showResultInfo("Scan code result", "Bar code information", info);

                            }
                        });
                    }
                });
                break;

            case R.id.btn_scaner_two:
                // The activity that calls the scan code QR code needs to configure the android:exported="true" attribute in the Manifest file.
                if (scanner == null) {
                    Toast.makeText(ScanerActivity.this, "SDK scan object is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                /**
                 * Scan code type, QR code
                 * TYPE_QR = 1;
                 *
                 * Scan type, barcode
                 *  TYPE_BAR = 2;
                 */
                scanner.scan(Scanner.TYPE_BAR, new OnResultListener() {

                    @Override
                    public void onResult(int what, String in) {
                        // TODO Auto-generated method stub

                        final String info = in;
                        // The UI function cannot be done in the callback function, so you can use the runOnUiThread function to wrap the code block.
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showResultInfo("Scan code result", "Bar code information", info);

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
}
