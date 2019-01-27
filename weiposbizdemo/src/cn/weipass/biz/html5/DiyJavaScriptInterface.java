package cn.weipass.biz.html5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import cn.weipass.biz.R;
import cn.weipass.biz.WebTurnViewActivity;
import cn.weipass.pos.sdk.IPrint;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.Photograph;
import cn.weipass.pos.sdk.Scanner;
import cn.weipass.pos.sdk.impl.WeiposImpl;

public class DiyJavaScriptInterface {
    private WebTurnViewActivity context;
    private WebView webview;

    public DiyJavaScriptInterface(WebTurnViewActivity context, WebView webview) {
        this.webview = webview;
        this.context = (WebTurnViewActivity) context;
    }

    // 获取设备信息
    @JavascriptInterface
    public String getDeviceInfo() {
        String deviceInfo = WeiposImpl.as().getDeviceInfo();
        return deviceInfo;
    }

    @JavascriptInterface
    public void speechVoice(String speechContent) {
        WeiposImpl.as().speech(speechContent);
    }

    @JavascriptInterface
    public void scanCode(int type, final String onSuccess, final String onErr) {

        Scanner mScanner = WeiposImpl.as().openScanner();
        if (mScanner != null) {
            int scanType = Scanner.TYPE_QR;
            if (type == 1) {
                scanType = Scanner.TYPE_BAR;
            } else {
                scanType = Scanner.TYPE_QR;
            }
            mScanner.scan(scanType, new Scanner.OnResultListener() {

                @Override
                public void onResult(int result, String info) {
                    // TODO Auto-generated method stub
                    final String qr_result = info;
                    context.runOnUiThread(new Runnable() {
                        public void run() {
                            // 加载成功
                            webview.loadUrl("javascript:" + onSuccess + "('" + qr_result + "')");
                        }
                    });

                }
            });
        } else {
            context.runOnUiThread(new Runnable() {
                public void run() {
                    // 加载失败
                    webview.loadUrl(
                            "javascript:" + onErr + "('" + context.res.getString(R.string.scan_init_error) + "')");
                }
            });

        }
    }

    /**
     * @param fontSize  //字体大小，0：小号，1：中号，2：大号 默认1中号
     * @param gravity   //布局方式，0：居左，1：居中，2：居右 默认0居左
     * @param type      //打印类型，0:文本内容；1：二维码；2：条码（content必须为数字）
     * @param content
     */
    @JavascriptInterface
    public void printContent(final int fontSize, final int gravity, final int type, final String content) {
        // final int size_type,
        try {
            LatticePrinter printer = WeiposImpl.as().openLatticePrinter();
            Log.e("hsl", "printContent: ");
            doingPrinter(printer, fontSize, gravity, type, content);

        } catch (Exception e) {
            context.runOnUiThread(new Runnable() {
                public void run() {
                    // 加载失败
                    webview.loadUrl(
                            "javascript:error('" + context.res.getString(R.string.print_init_error) + "')");
                }
            });
        }
    }

    @JavascriptInterface
    public void submitPrint(){
        LatticePrinter printer = WeiposImpl.as().openLatticePrinter();
        HtmlSdkUtil.submitPrint(printer);
    }

    // 拍照获取图片
    @JavascriptInterface
    public void takePhone(final boolean isCrop, final String onSuccess, final String onErr) {
        try {
            Photograph mPhotograph = WeiposImpl.as().openPhotograph();
            // 设置拍照回调方法
            mPhotograph.setResultListener(new Photograph.OnResultListener() {

                @Override
                public void onResult(int result, byte[] data, String err) {
                    final String err_back = err;
                    if (data != null) {
                        final byte[] bytes = data;
                        context.runOnUiThread(new Runnable() {
                            public void run() {
                                String image64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                                System.out.println("image64---------------------------" + image64);
                                // 加载成功
                                webview.loadUrl("javascript:" + onSuccess + "('data:image/jpeg;base64," + image64.trim()
                                        + "')");
                            }
                        });

                    } else {
                        context.runOnUiThread(new Runnable() {
                            public void run() {
                                // 加载失败
                                webview.loadUrl("javascript:" + onErr + "('" + err_back + "')");
                            }
                        });
                    }

                }
            });
            // 是否需要剪切
            mPhotograph.takePicture(isCrop);

        } catch (Exception e) {
            context.runOnUiThread(new Runnable() {
                public void run() {
                    // 加载失败
                    webview.loadUrl("javascript:" + onErr + "('"
                            + context.res.getString(R.string.init_photograph_failed) + "')");
                }
            });
        }
    }

    /**
     * 点阵打印
     */
    private void doingPrinter(LatticePrinter latticePrinter, int fontSize, int gravity, final int type, final String content) {

        if (content == null || content.length() == 0) {
            context.runOnUiThread(new Runnable() {
                public void run() {
                    // 加载失败
                    webview.loadUrl(
                            "javascript:error('" + context.res.getString(R.string.print_init_error) + "')");
                }
            });
            return;
        }
        if (latticePrinter == null) {
            context.runOnUiThread(new Runnable() {
                public void run() {
                    // 加载失败
                    webview.loadUrl(
                            "javascript:error('" + context.res.getString(R.string.print_init_error) + "')");
                }
            });
            return;
        }
        // 打印内容赋值
        latticePrinter.setOnEventListener(new IPrint.OnEventListener() {
            @Override
            public void onEvent(final int what, String in) {
                // TODO Auto-generated method stub1
                final String info = in;
                Log.d("hsl", "onEvent: " + what + " - " + in);
                String message = info;
                if (what == IPrint.EVENT_CONNECT_FAILD || what == IPrint.EVENT_PAPER_JAM || what == IPrint.EVENT_UNKNOW
                        || what == IPrint.EVENT_NO_PAPER || what == IPrint.EVENT_HIGH_TEMP
                        || what == IPrint.EVENT_PRINT_FAILD) {
                    // 打印失败
                    webview.loadUrl("javascript:error('" + message + "')");
                } else if (what == IPrint.EVENT_OK) {
                    // 打印成功
                    String printOk = context.res.getString(R.string.print_success);
                    webview.loadUrl("javascript:success('" + printOk + "')");
                }
            }
        });
        // 调用打印，里面封装了点阵打印示例
        HtmlSdkUtil.printLattice(context, fontSize, gravity, type, content, latticePrinter);
    }

    /* */

    /**
     * 点阵打印
     *//*
     * private void doingPrinter(LatticePrinter printer,final int
     * fontSize,final int gravity,final int type, final String content,
     * final String onSuccess, final String onErr) { if (content == null
     * || content.length() == 0) { context.runOnUiThread(new Runnable()
     * { public void run() { // 加载失败 webview.loadUrl("javascript:" +
     * onErr + "('" + context.res.getString(R.string.print_init_error) +
     * "')"); } }); return; } if (printer == null) {
     * context.runOnUiThread(new Runnable() { public void run() { //
     * 加载失败 webview.loadUrl("javascript:" + onErr + "('" +
     * context.res.getString(R.string.print_init_error) + "')"); } });
     * return; } // 打印内容赋值 printer.setOnEventListener(new
     * IPrint.OnEventListener() {
     *
     * @Override public void onEvent(final int what, String in) { //
     * TODO Auto-generated method stub final String info = in; //
     * 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
     * context.runOnUiThread(new Runnable() { public void run() { final
     * String message = HtmlSdkUtil.getPrintErrorInfo( what, info); if
     * (message == null || message.length() < 1) { // 加载成功 String
     * printOk = context.res.getString(R.string.print_success);
     * webview.loadUrl("javascript:" + onSuccess + "('" + printOk +
     * "')"); return; } // 加载失败 webview.loadUrl("javascript:" + onErr +
     * "('" + message + "')"); } }); } }); printer.printText(content,
     * LatticePrinter.FontFamily.SONG, LatticePrinter.FontSize.MEDIUM,
     * LatticePrinter.FontStyle.BOLD); //最后进纸5行,方便撕纸 printer.feed(5); //
     * 真正提交打印事件 printer.submitPrint(); }
     */

    // 打印输出日志
    @JavascriptInterface
    public void log(String msg) {
        Log.e("Html5 : ", msg);
    }

    // 加载url
    @JavascriptInterface
    public void loadUrl(String pageUrl, final String onSuccess, final String onErr) {

        String str = "";// in.readLine();
        try {
            HashMap<String, String> paramsMap = new HashMap<String, String>();

            StringBuilder tempParams = new StringBuilder();
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                pos++;
            }
            String requestUrl = pageUrl + tempParams.toString();
            // 新建一个URL对象
            URL url = new URL(requestUrl);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接主机超时时间
            urlConn.setConnectTimeout(5 * 1000);
            // 设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // 设置是否使用缓存 默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("GET");
            // urlConn设置请求头信息
            // 设置请求中的媒体类型信息。
            urlConn.setRequestProperty("Content-Type", "application/json");
            // 设置客户端与服务连接类型
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            // 开始连接
            urlConn.connect();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                // 显示数据记录内容
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer s = new StringBuffer("");
                while ((str = in.readLine()) != null) {
                    s.append(str);
                }
                String result = s.toString();
                // 加载成功
                webview.loadUrl("javascript:" + onSuccess + "('" + result + "')");

                Log.e("loadurl", "Get方式请求成功，result--->" + result);
            } else {
                Log.e("loadurl", "Get方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            str = context.getResources().getString(R.string.failed_to_get_content);
            // 加载失败
            webview.loadUrl("javascript:" + onErr + "('" + str + "')");
        }
    }

    @JavascriptInterface
    public void httpPost(String pageUrl, String jsonData, String headers, String fb, String eb) {
        String str = "";// in.readLine();

        try {
            StringBuilder tempParams = new StringBuilder();
            HashMap<String, String> data = HtmlSdkUtil.parseJson2HashMap(jsonData);
            if (data != null) {
                int pos = 0;
                for (String key : data.keySet()) {
                    if (pos > 0) {
                        tempParams.append("&");
                    }
                    tempParams.append(String.format("%s=%s", key, URLEncoder.encode((String) data.get(key), "utf-8")));
                    pos++;
                }
            }

            String requestUrl = pageUrl + tempParams.toString();
            // 新建一个URL对象
            URL url = new URL(requestUrl);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接主机超时时间
            urlConn.setConnectTimeout(5 * 1000);
            // 设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // 设置是否使用缓存 默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("POST");
            // urlConn设置请求头信息
            // 设置请求中的媒体类型信息。
            urlConn.setRequestProperty("Content-Type", "application/json");
            // 设置客户端与服务连接类型
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            // 开始连接
            urlConn.connect();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                // 显示数据记录内容
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer s = new StringBuffer("");
                while ((str = in.readLine()) != null) {
                    s.append(str);
                }
                String result = s.toString();
                // 加载成功
                webview.loadUrl("javascript:" + fb + "('" + result + "')");

                Log.e("loadurl", "POST方式请求成功，result--->" + result);
            } else {
                Log.e("loadurl", "POST方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            str = context.getResources().getString(R.string.failed_to_get_content);
            // 加载失败
            webview.loadUrl("javascript:" + eb + "('" + str + "')");
        }
    }

    public void httpGet(String pageUrl, String headers, String fb, String eb) {
        String str = "";// in.readLine();
        try {
            StringBuilder tempParams = new StringBuilder();
            HashMap<String, String> data = HtmlSdkUtil.parseJson2HashMap(headers);
            if (data != null) {
                int pos = 0;
                for (String key : data.keySet()) {
                    if (pos > 0) {
                        tempParams.append("&");
                    }
                    tempParams.append(String.format("%s=%s", key, URLEncoder.encode((String) data.get(key), "utf-8")));
                    pos++;
                }
            }

            String requestUrl = pageUrl + tempParams.toString();
            // 新建一个URL对象
            URL url = new URL(requestUrl);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接主机超时时间
            urlConn.setConnectTimeout(5 * 1000);
            // 设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // 设置是否使用缓存 默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("GET");
            // urlConn设置请求头信息
            // 设置请求中的媒体类型信息。
            urlConn.setRequestProperty("Content-Type", "application/json");
            // 设置客户端与服务连接类型
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            // 开始连接
            urlConn.connect();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                // 显示数据记录内容
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer s = new StringBuffer("");
                while ((str = in.readLine()) != null) {
                    s.append(str);
                }
                String result = s.toString();
                // 加载成功
                webview.loadUrl("javascript:" + fb + "('" + result + "')");

                Log.e("loadurl", "POST方式请求成功，result--->" + result);
            } else {
                Log.e("loadurl", "POST方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            str = context.getResources().getString(R.string.failed_to_get_content);
            // 加载失败
            webview.loadUrl("javascript:" + eb + "('" + str + "')");
        }
    }

}
