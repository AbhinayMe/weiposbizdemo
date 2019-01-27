package cn.weipass.biz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cn.weipass.biz.util.HEX;
import cn.weipass.pos.sdk.PsamManager;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.sam.SamResult;

public class PSAMTestActivity extends Activity {

    private Button btnPsam1;
    private Button btnPsam2;
    private PsamManager psamManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psamtest);

        btnPsam1 = (Button) findViewById(R.id.btn_psam1);
        btnPsam2 = (Button) findViewById(R.id.btn_psam2);
        if (Build.MODEL.startsWith("WPOS-MINI") || isNET5()) {
            btnPsam2.setVisibility(View.GONE);
        }
        psamManager = WeiposImpl.as().openPsamManager();

        btnPsam1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 *   SLOT1 = 1;选择卡槽1
                 *   SLOT2 = 2;选择卡槽2
                 */
                psamManager.setSelectSlot(PsamManager.SLOT1);
                testPsam();
            }
        });

        btnPsam2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                psamManager.setSelectSlot(PsamManager.SLOT2);
                testPsam();
            }
        });

        findViewById(R.id.btn_return).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView title = (TextView) findViewById(R.id.page_top_title);
        title.setText("PSAM卡检测");
    }

    public static boolean isNET5() {
        return Build.MODEL.equals("WNET5") || Build.MODEL.equals("WISENET5");
    }

    /**
     * 检测PSAM数据交互
     */
    private void testPsam() {
        if (psamManager == null) {
            Toast.makeText(PSAMTestActivity.this, "尚未初始化打印sdk，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            /**
             * 设置波特率，例如：setBaund("410113"),这个数据是TLV格式的,如果为null将会设置成默认<br/>
             * 41 是标签
             * 01 是长度
             * 13 是波特率 38400
             * 以下为波特率代号对应的值参照表：
             * 11 是波特率 9600
             * 12 是波特率 19200
             * 13 是波特率 38400
             * 14 是波特率 76800
             */
            psamManager.setBaund("410113");

            /**
             * 需要发送的数据
             */
            byte[] commandByte = new byte[]{0x00, (byte) 0x84, 0x00, 0x00, 0x04};
            SamResult samResult = psamManager.doCommand(commandByte);
            byte[] ramdom = null;
            if (samResult != null) {
                /**
                 * 获取psam卡返回的数据
                 */
                ramdom = samResult.getData();
            }
            if (ramdom != null && ramdom.length != 0) {
                showMsgDialog("PSAM卡测试", "测试成功", "psam卡返回字节数据为(" + HEX.bytesToHex(ramdom) + ")");
                System.out.println("测试成功,psam卡返回字节数据为(" + HEX.bytesToHex(ramdom) + ")");
            } else {
                showMsgDialog("PSAM卡测试", "测试失败", "请确保设备插入PSAM卡，并且插入PSAM卡后重启设备。");
            }
        } catch (Exception e) {
            // TODO: handle exception
            showMsgDialog("PSAM卡测试", "测试失败", "请确保设备插入PSAM卡，并且插入PSAM卡后重启设备。");
        }
    }


    private void showMsgDialog(String title, String tip, String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
}
