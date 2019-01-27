package cn.weipass.biz;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.weipass.biz.util.DataParser;
import cn.weipass.biz.util.ToolsUtil;
import cn.weipass.biz.vo.TicketInfo;
import cn.weipass.pos.sdk.DataChannel;
import cn.weipass.pos.sdk.IPrint;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.Printer;
import cn.weipass.pos.sdk.DataChannel.ResponseCallback;
import cn.weipass.pos.sdk.IPrint.OnEventListener;
import cn.weipass.pos.sdk.impl.WeiposImpl;
/**
 * 
 * @author Tianhui
 *	旺POS使用sdk调用打印功能，标准打印和点阵打印示例
 */
public class PrinterActivity extends Activity implements
		OnClickListener {
	private Printer printer = null;//普通打印对象
	
	private LatticePrinter latticePrinter;// 点阵打印
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.printer);

		TextView topTitle = (TextView) findViewById(R.id.page_top_title);
		topTitle.setText("调用POS打印");
		findViewById(R.id.btn_return).setOnClickListener(this);
		
		findViewById(R.id.btn_printer_one).setOnClickListener(this);
		findViewById(R.id.btn_printer_two).setOnClickListener(this);
		findViewById(R.id.btn_printer_kaiti).setOnClickListener(this);
		
		
		try {
			// 设备可能没有打印机，open会抛异常
			printer = WeiposImpl.as().openPrinter();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			// 设备可能没有打印机，open会抛异常
			latticePrinter = WeiposImpl.as().openLatticePrinter();
		} catch (Exception e) {
			// TODO: handle exception
		}
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
		case R.id.btn_printer_one:
			//普通打印
			if (printer == null) {
				Toast.makeText(PrinterActivity.this, "尚未初始化打印sdk，请稍后再试", Toast.LENGTH_SHORT).show();
				return;
			}
			printer.setOnEventListener(new OnEventListener() {

				@Override
				public void onEvent(final int what, String in) {
					// TODO Auto-generated method stub
					final String info = in;
					// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
					runOnUiThread(new Runnable() {
						public void run() {
							final String message = ToolsUtil.getPrintErrorInfo(what, info);
							if (message == null || message.length() < 1) {
								return;
							}
							showResultInfo("打印", "打印结果信息", message);
						}
					});
				}
			});
			ToolsUtil.printNormal(PrinterActivity.this, printer);
			break;
			
		case R.id.btn_printer_two:
			//点阵打印
			if (latticePrinter == null) {
				Toast.makeText(PrinterActivity.this, "尚未初始化点阵打印sdk，请稍后再试", Toast.LENGTH_SHORT).show();
				return;
			}
			// 打印内容赋值
			latticePrinter.setOnEventListener(new OnEventListener() {

				@Override
				public void onEvent(final int what, String in) {
					// TODO Auto-generated method stub
					final String info = in;
					// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
					runOnUiThread(new Runnable() {
						public void run() {
							final String message = ToolsUtil.getPrintErrorInfo(what, info);
							if (message == null || message.length() < 1) {
								return;
							}
							showResultInfo("打印", "打印结果信息", message);
						}
					});
				}
			});
			ToolsUtil.printLattice(PrinterActivity.this, latticePrinter);
			break;
		case R.id.btn_printer_kaiti:
			if (latticePrinter == null) {
				Toast.makeText(PrinterActivity.this, "尚未初始化点阵打印sdk，请稍后再试", Toast.LENGTH_SHORT).show();
				return;
			}
			latticePrinter.setOnEventListener(new OnEventListener() {

				@Override
				public void onEvent(final int what, String in) {
					// TODO Auto-generated method stub
					final String info = in;
					// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
					runOnUiThread(new Runnable() {
						public void run() {
							final String message = ToolsUtil.getPrintErrorInfo(what, info);
							if (message == null || message.length() < 1) {
								return;
							}
							showResultInfo("打印", "打印结果信息", message);
						}
					});
				}
			});
			ToolsUtil.printKaiti(PrinterActivity.this, latticePrinter);
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
