package cn.weipass.biz.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;


public class PrinterUtils {
    private final static String TAG = "PrinterUtils";
    private static final int PAPER_WIDTH = 384;
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    
    public static int NormalFont = 24;
    public static int SmallFont = 16;
    public static int LargeFont = 32;

    public static String toUtf8(String str) {
         String result = null;
         try {
                 result = new String(str.getBytes("UTF-8"), "UTF-8");
         } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
             e.printStackTrace();
        }
        return result;
     }

   /*
    * @content 打印的内容
    * @font  字体
    * @lineSpacing 行间距
    * @fontSize  字体大小       一行可以显示12个大号字 16个中号字  24个消耗字
    * @scaleX 横向放大多少倍
    * @align 字体排列
    * @bold 是否加粗
    */
   public static byte[] printString(String content, Typeface font,float lineSpacing, int fontSize, float scaleX,int align, boolean isBold){
        Log.i(TAG, "printString content:" + content + " fontSize:" + fontSize);
        Bitmap bitmap = getBitmapFromString(content, 0, scaleX, lineSpacing,font, fontSize, align, isBold, false, false);
        return bitmap2Bytes(bitmap);
    }

   public  static Bitmap getBitmapFromString(String content, int leftOffset,float scaleX, float lineSpacing, Typeface font, int fontSize, int align, boolean isBold, boolean isItalic, boolean isUnderLine){
        Bitmap bitmap = null;
        try {
            TextPaint paint = new TextPaint();
            paint.setTextSize(fontSize);
            TextPaint textPaint = new TextPaint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(fontSize);
            textPaint.setTextScaleX(scaleX);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTypeface(font);
            if(isBold) {
            	textPaint.setFakeBoldText(true);
            }
            if(isUnderLine) {
            	textPaint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
            }
            Layout.Alignment alignment = null;
            switch (align){
                case ALIGN_LEFT:
                    alignment = Layout.Alignment.ALIGN_NORMAL;
                    break;
                case ALIGN_CENTER:
                    alignment = Layout.Alignment.ALIGN_CENTER;
                    break;
                case ALIGN_RIGHT:
                    alignment = Layout.Alignment.ALIGN_OPPOSITE;
                    break;
            }
            StaticLayout sl = new StaticLayout(content, textPaint,PAPER_WIDTH, alignment,1.0f,lineSpacing,true); //倒数第二个 设置行间距大小
            Log.i(TAG, "printString sl:" + sl.getHeight());
            bitmap = Bitmap.createBitmap(PAPER_WIDTH, sl.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);   //背景颜色
            canvas.save();
            canvas.translate(0,0);
            sl.draw(canvas);
            canvas.restore();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return bitmap;
    }


    // Bitmap → byte[]
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

}
