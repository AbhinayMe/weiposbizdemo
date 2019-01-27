package cn.weipass.biz.html5;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import cn.weipass.pos.sdk.LatticePrinter;

/**
 * Created by Tianhui on 2016/6/24.
 */
public class HtmlSdkUtil {
	/**
	 * font_size:字体大小枚举值 SMALL:16x16大小; MEDIUM:24x24大小; LARGE:32x32大小;
	 * EXTRALARGE:48x48 一行的宽度为384
	 * (当宽度大小为16时可打印384/16=24个字符;为24时可打印384/24=16个字符;为32时可
	 * 打印384/32=12个字符;为48时可打印384/48=8个字符（一个汉字占1个字符，一个字母 、空格或者数字占半字符）
	 *
	 * 标准打印示例
	 *
	 * @param context
	 * @param printer
	 */
	public static final int rowSize = 384;
	// public static final int smallSize = (int) (384/16d);
	// public static final int mediumSize = (int) (384/24d);
	// public static final int largeSize = (int) (384/32d);
	// public static final int extralargeSize = (int) (384/48d);
	public static final int smallSize = 24 * 2;
	public static final int mediumSize = 16 * 2;
	public static final int largeSize = 12 * 2;
	public static final int extralargeSize = 8 * 2;

	public static void submitPrint(LatticePrinter latticePrinter){
		// 真正提交打印事件
		latticePrinter.submitPrint();
	}
	/**
	 * \n 代表换行
	 * 
	 * @param context
	 * @param fontSize
	 *            字体大小设置 0：小号，1：中号，2：大号
	 * @param gravity
	 *            布局方式设置 0：居左，1：居中，2：居右
	 * @param type
	 *            打印类型，0:文本内容；1：二维码；2：条码（content必须为数字）
	 * @param printContent
	 *            打印内容
	 * @param latticePrinter
	 */
	public static void printLattice(Context context, int fontSize, int gravity, int type, String printContent,
			LatticePrinter latticePrinter) {

		if (type == 1) {
			// 1：二维码
			Bitmap qrCodeBmp = createQRImage(printContent, 250, 250);
			byte[] qrCodeByte = bitmap2Bytes(qrCodeBmp);
			if (qrCodeByte != null) {
				latticePrinter.printImage(qrCodeByte, cn.weipass.pos.sdk.IPrint.Gravity.CENTER);
			}
		} else if (type == 2) {
			// 2：条码（content必须为数字）
			Bitmap oneCodeBmp = creatBarcode(context, printContent, 500, 150);
			byte[] oneCodeByte = bitmap2Bytes(oneCodeBmp);
			if (oneCodeByte != null) {
				latticePrinter.printImage(oneCodeByte, cn.weipass.pos.sdk.IPrint.Gravity.CENTER);
			}
		} else {
			if (fontSize == 2) {

				String contentStr = "";
				int blockSize = largeSize - length(printContent);

				if (gravity == 2) {
					// 居右
					// 文字居中需要在前面补足相应空格，后面可以用换行符换行
					contentStr = getBlankBySize((int) (blockSize)) + printContent;
				} else if (gravity == 1) {
					// 居中
					// 文字居中需要在前面补足相应空格，后面可以用换行符换行
					contentStr = getBlankBySize((int) (blockSize / 2d)) + printContent;
				} else {
					// 默认居左
					contentStr = printContent;
				}
				latticePrinter.printText(contentStr + "\n", LatticePrinter.FontFamily.SONG,
						LatticePrinter.FontSize.LARGE, LatticePrinter.FontStyle.BOLD);
			} else if (fontSize == 0) {
				String contentStr = "";
				int blockSize = smallSize - length(printContent);

				if (gravity == 2) {
					// 居右
					// 文字居中需要在前面补足相应空格，后面可以用换行符换行
					contentStr = getBlankBySize((int) (blockSize)) + printContent;
				} else if (gravity == 1) {
					// 居中
					// 文字居中需要在前面补足相应空格，后面可以用换行符换行
					contentStr = getBlankBySize((int) (blockSize / 2d)) + printContent;
				} else {
					// 默认居左
					contentStr = printContent;
				}
				latticePrinter.printText(contentStr + "\n", LatticePrinter.FontFamily.SONG,
						LatticePrinter.FontSize.SMALL, LatticePrinter.FontStyle.BOLD);
			} else {
				// 默认使用中号大小 fontSize==1
				String contentStr = "";
				int blockSize = mediumSize - length(printContent);

				if (gravity == 2) {
					// 居右
					// 文字居中需要在前面补足相应空格，后面可以用换行符换行
					contentStr = getBlankBySize((int) (blockSize)) + printContent;
				} else if (gravity == 1) {
					// 居中
					// 文字居中需要在前面补足相应空格，后面可以用换行符换行
					contentStr = getBlankBySize((int) (blockSize / 2d)) + printContent;
				} else {
					// 默认居左
					contentStr = printContent;
				}
				latticePrinter.printText(contentStr + "\n", LatticePrinter.FontFamily.SONG,
						LatticePrinter.FontSize.MEDIUM, LatticePrinter.FontStyle.BOLD);
			}
		}

		// 真正提交打印事件
//		latticePrinter.submitPrint();
	}

	/**
	 * 生成二维码 要转换的地址或字符串,可以是中文
	 *
	 * @param url
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap createQRImage(String url, final int width, final int height) {
		try {
			// 判断URL合法性
			if (url == null || "".equals(url) || url.length() < 1) {
				return null;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height, hints);
			int[] pixels = new int[width * height];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					} else {
						pixels[y * width + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isLetter(char c) {
		int k = 0x80;
		return c / k == 0 ? true : false;
	}

	/**
	 * 判断字符串是否为空
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str) {
		if (str == null || str.trim().equals("") || str.trim().equalsIgnoreCase("null")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为2,英文字符长度为1
	 *
	 * @param s
	 *            s 需要得到长度的字符串
	 * @return int 得到的字符串长度
	 */
	public static int length(String s) {
		if (s == null)
			return 0;
		char[] c = s.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++) {
			len++;
			if (!isLetter(c[i])) {
				len++;
			}
		}
		return len;
	}

	/**
	 * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为1,英文字符长度为0.5
	 *
	 * @param s
	 *            s 需要得到长度的字符串
	 * @return int 得到的字符串长度
	 */
	public static double getLength(String s) {
		if (s == null) {
			return 0;
		}
		double valueLength = 0;
		String chinese = "[\u4e00-\u9fa5]";
		// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
		for (int i = 0; i < s.length(); i++) {
			// 获取一个字符
			String temp = s.substring(i, i + 1);
			// 判断是否为中文字符
			if (temp.matches(chinese)) {
				// 中文字符长度为1
				valueLength += 1;
			} else {
				// 其他字符长度为0.5
				valueLength += 0.5;
			}
		}
		// 进位取整
		return Math.ceil(valueLength);
	}

	public static String getBlankBySize(int size) {
		String resultStr = "";
		for (int i = 0; i < size; i++) {
			resultStr += " ";
		}
		return resultStr;
	}

	/**
	 * 生成条形码
	 *
	 * @param context
	 * @param contents
	 *            需要生成的内容
	 * @param desiredWidth
	 *            生成条形码的宽带
	 * @param desiredHeight
	 *            生成条形码的高度
	 * @return
	 */
	public static Bitmap creatBarcode(Context context, String contents, int desiredWidth, int desiredHeight) {
		Bitmap ruseltBitmap = null;
		/**
		 * 图片两端所保留的空白的宽度
		 */
		int marginW = 0;
		/**
		 * 条形码的编码类型
		 */
		BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

		ruseltBitmap = encodeAsBitmap(contents, barcodeFormat, desiredWidth, desiredHeight);

		return ruseltBitmap;
	}

	/**
	 * 生成条形码的Bitmap
	 *
	 * @param contents
	 *            需要生成的内容
	 * @param format
	 *            编码格式
	 * @param desiredWidth
	 * @param desiredHeight
	 * @return
	 */
	protected static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int desiredWidth, int desiredHeight) {
		final int WHITE = 0xFFFFFFFF;
		final int BLACK = 0xFF000000;

		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = null;
		try {
			result = writer.encode(contents, format, desiredWidth, desiredHeight, null);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 将两个Bitmap合并成一个
	 *
	 * @param first
	 * @param second
	 * @param fromPoint
	 *            第二个Bitmap开始绘制的起始位置（相对于第一个Bitmap）
	 * @return
	 */
	protected static Bitmap mixtureBitmap(Bitmap first, Bitmap second, PointF fromPoint) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}
		int marginW = 20;
		Bitmap newBitmap = Bitmap.createBitmap(first.getWidth() + second.getWidth() + marginW,
				first.getHeight() + second.getHeight(), Bitmap.Config.ARGB_4444);
		Canvas cv = new Canvas(newBitmap);
		cv.drawBitmap(first, marginW, 0, null);
		cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
		cv.save(Canvas.ALL_SAVE_FLAG);
		cv.restore();

		return newBitmap;
	}

	// 将Drawable转化为Bitmap
	public static Bitmap drawableToBitmap(Drawable drawable) {
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// 取 drawable 的颜色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		// 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}

	// Bitmap → byte[]
	public static byte[] bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * json字符串 转成 map
	 * 
	 * @param jsonStr
	 * @return
	 * @throws Exception
	 */
	public static HashMap<String, String> parseJson2HashMap(String jsonStr) {
		if (jsonStr == null || "".equals(jsonStr)) {
			return null;
		}
		HashMap<String, String> retMap =  new HashMap<String, String>();
		try {
			retMap = new HashMap<String, String>();
			JSONObject json = new JSONObject(jsonStr);

			for (Iterator<String> keys = json.keys(); keys.hasNext();) {
				String key = keys.next();
				String value = (String) json.get(key);

				retMap.put(key, value);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retMap;
	}

}
