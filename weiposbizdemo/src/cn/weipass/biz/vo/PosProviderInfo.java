package cn.weipass.biz.vo;

import android.os.Parcel;
import android.os.Parcelable;
import cn.shellinfo.wall.remote.ParamMap;
import cn.weipass.biz.vo.intr.BaseBean;

/**
 * 支付提供方式
 * 
 * @author TIANHUI
 * 
 */
public class PosProviderInfo extends BaseBean {

	public String providerId;//
	public String name;// 
	public byte[] icon;

	public PosProviderInfo() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<PosProviderInfo> CREATOR = new Parcelable.Creator<PosProviderInfo>() {
		public PosProviderInfo createFromParcel(Parcel in) {
			return new PosProviderInfo(in);
		}

		public PosProviderInfo[] newArray(int size) {
			return new PosProviderInfo[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(providerId);
		dest.writeString(name);
		if (icon != null) {
			dest.writeInt(icon.length);
			dest.writeByteArray(icon);
		} else {
			dest.writeInt(0);
		}
	}

	private PosProviderInfo(Parcel in) {
		providerId = in.readString();
		name = in.readString();
		int len = in.readInt();
		if (len > 0) {
			icon = new byte[len];
			in.readByteArray(icon);
		}
	}

	/**
	 * 加载来自服务器的数据
	 * 
	 */

	@Override
	public void loadFromServerData(ParamMap param) {
		
	}
}
