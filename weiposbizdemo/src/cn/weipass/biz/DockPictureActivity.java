package cn.weipass.biz;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.weipass.pos.sdk.BlueBoxManager;
import cn.weipass.pos.sdk.DockPictureManager;
import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
 * Created by bin on 2016/11/10.
 */
public class DockPictureActivity extends Activity implements View.OnClickListener {
    private static String TAG = "BluetoothBox";
    private DockPictureManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bluetoothcash);
        TextView topTitle = (TextView) findViewById(R.id.page_top_title);
        topTitle.setText("VGA测试");
        findViewById(R.id.btn_return).setOnClickListener(this);
        Button b = (Button) findViewById(R.id.btn_open_cash);
        b.setText("发送图片");
        findViewById(R.id.btn_open_cash).setOnClickListener(this);
        findViewById(R.id.btn_send).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_send).setOnClickListener(this);
        manager = WeiposImpl.as().getService(DockPictureManager.class);
        if (manager == null) {
            Toast.makeText(this, "SDK没有初始化", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return:
                finish();
                break;
            case R.id.btn_open_cash:
                //注意事项：  发送的图片必须JPG格式，小于1M，800*600 ，调用API间隔大于1s
                String oldPath ="image1.jpg";
                String path = Environment.getExternalStorageDirectory()+"/test_vga_img1.jpg";
                sendPicture(oldPath,path);
                break;
            case R.id.btn_send:
                //注意事项：  发送的图片必须JPG格式，小于1M，800*600 ，调用API间隔大于1s,路径为绝对路径
                String oldPath2 ="image2.jpg";
                String path2 = Environment.getExternalStorageDirectory()+"/test_vga_img2.jpg";
                sendPicture(oldPath2,path2);
                break;
        }
    }

    public void sendPicture(String oldPath,String path) {
        if (manager != null) {
            try {
                File file = new File(path);
                if (file==null||!file.exists()) {
                    copyFiletoSD(oldPath, path);
                }
                int ret = manager.sendPicture(path);
                Toast.makeText(DockPictureActivity.this, "发送图片2====" + ret, Toast.LENGTH_SHORT).show();

            } catch(Exception ex){
                Log.e(TAG,ex.getMessage());
            }
        }
    }

    private void copyFiletoSD(String oldPath,String newPath) {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/"+oldPath);
            FileOutputStream fos = new FileOutputStream(new File(newPath));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
            is.close();
            fos.close();
        } catch (Exception e) {

        }
    }

}
