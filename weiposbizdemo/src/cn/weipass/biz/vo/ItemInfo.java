package cn.weipass.biz.vo;

import android.os.Parcel;
import android.os.Parcelable;
import cn.shellinfo.wall.remote.ParamMap;
import cn.weipass.biz.vo.intr.BaseBean;

/**
 * 菜品信息
 * 
 * @author TIANHUI
 * 
 */
public class ItemInfo extends BaseBean {

	public String name;// 
	public String count;//
	public String price;// 

	public ItemInfo() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ItemInfo> CREATOR = new Parcelable.Creator<ItemInfo>() {
		public ItemInfo createFromParcel(Parcel in) {
			return new ItemInfo(in);
		}

		public ItemInfo[] newArray(int size) {
			return new ItemInfo[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(count);
		dest.writeString(price);
	}

	private ItemInfo(Parcel in) {
		name = in.readString();
		count = in.readString();
		price = in.readString();
	}

	/**
	 * 加载来自服务器的数据
	 * 
	 */

	@Override
	public void loadFromServerData(ParamMap param) {
	}
}
