package cn.weipass.biz;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import cn.weipass.biz.util.GenerateKeyRSA;
import cn.weipass.biz.util.HEX;
import cn.weipass.pos.sdk.RSAManager;
import cn.weipass.pos.sdk.impl.WeiposImpl;

public class RsaTestActivity extends Activity implements View.OnClickListener {
    private RSAManager mRSAManager;
    private byte[] publicKey;
    private byte[] privateKeyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rsa_test);

        TextView topTitle = (TextView) findViewById(R.id.page_top_title);
        topTitle.setText(R.string.other_57);
        findViewById(R.id.btn_return).setOnClickListener(this);

        mRSAManager = WeiposImpl.as().getService(RSAManager.class);
        genKeyRSA();

        findViewById(R.id.btn_testdecrypt).setOnClickListener(this);
        findViewById(R.id.btn_testsign).setOnClickListener(this);
    }

    /**
     * 生成密钥对
     */
    private void genKeyRSA() {
//        publicKey= mRSAManager.generateKeyRSA(1024);//使用rsa服务生成秘钥对
        genKeysLocal();
    }

    /**
     * 本地生成rsa秘钥对，并保存
     */
    private void genKeysLocal() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA"); // 创建‘密匙对’生成器
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        kpg.initialize(1024); // 指定密匙长度（取值范围：512～2048）
        KeyPair kp = kpg.genKeyPair(); // 生成‘密匙对’，其中包含着一个公匙和一个私匙的信息
        publicKey = kp.getPublic().getEncoded();// 公钥
        privateKeyData = kp.getPrivate().getEncoded();
        boolean save = mRSAManager.saveSecretKeys(privateKeyData, publicKey);
        System.out.println("save========>>" + save);
    }

    private void testDecrypt() {
        if (publicKey != null) {
            byte[] orgData = getResources().getString(R.string.other_58).getBytes();
            byte[] outData = GenerateKeyRSA.rsaEncryptNoPadding_public(orgData, publicKey);//使用公钥加密
            System.out.println("outData============>>>" + outData);
            if (outData == null) {
                Toast.makeText(this, getResources().getString(R.string.other_59), Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] decryptData = mRSAManager.decrypt(outData);
            if (decryptData == null) {
                Toast.makeText(this, getResources().getString(R.string.other_60), Toast.LENGTH_SHORT).show();
                return;
            }
            String outStr = new String(decryptData);
            System.out.println("outStr============>>>" + outStr);
            if (Arrays.equals(orgData, decryptData)) {
                Toast.makeText(this, getResources().getString(R.string.other_61), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.other_62), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.other_63), Toast.LENGTH_SHORT).show();
        }
    }

    private void testSign() {
        if (publicKey != null) {
            byte[] orgData = getResources().getString(R.string.other_64).getBytes();
            byte[] outData = mRSAManager.sign(orgData);
            System.out.println("outData============>>>" + outData);
            if (outData == null) {
                Toast.makeText(this, getResources().getString(R.string.other_65), Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] decryptData = GenerateKeyRSA.rsaDecryptNoPadding_public(outData, publicKey);
            if (decryptData == null) {
                Toast.makeText(this, getResources().getString(R.string.other_66), Toast.LENGTH_SHORT).show();
                return;
            }
            String outStr = new String(decryptData);
            System.out.println("outStr============>>>" + outStr);
            if (Arrays.equals(orgData, decryptData)) {
                Toast.makeText(this, getResources().getString(R.string.other_67), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.other_68), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.other_69), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return:
                onBackPressed();
                break;
            case R.id.btn_testdecrypt: {
                testDecrypt();
                break;
            }
            case R.id.btn_testsign: {
                testSign();
                break;
            }
            default:
                break;
        }
    }
}
