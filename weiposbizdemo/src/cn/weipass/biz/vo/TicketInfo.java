package cn.weipass.biz.vo;

import android.os.Parcel;
import android.os.Parcelable;
import cn.shellinfo.wall.remote.ParamMap;
import cn.weipass.biz.vo.intr.BaseBean;

/**
 * 电影票信息
 * 
 * @author TIANHUI
 * 
 */
public class TicketInfo extends BaseBean {

	public String nickname;//
	public String name;// 
	public String film;//
	public String time;// 
	public String room;//
	public String username;// 
	public String place;//
	public String userid;

	public TicketInfo() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<TicketInfo> CREATOR = new Parcelable.Creator<TicketInfo>() {
		public TicketInfo createFromParcel(Parcel in) {
			return new TicketInfo(in);
		}

		public TicketInfo[] newArray(int size) {
			return new TicketInfo[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(nickname);
		dest.writeString(name);
		dest.writeString(film);
		dest.writeString(time);
		dest.writeString(room);
		dest.writeString(username);
		dest.writeString(place);
		dest.writeString(userid);
	}

	private TicketInfo(Parcel in) {
		nickname = in.readString();
		name = in.readString();
		film = in.readString();
		time = in.readString();
		room = in.readString();
		username = in.readString();
		place = in.readString();
		userid = in.readString();
	}

	/**
	 * 加载来自服务器的数据
	 * 
	 */

	@Override
	public void loadFromServerData(ParamMap param) {
		nickname = param.getString("nickname","");
		name = param.getString("name","");
		film = param.getString("film","");
		time = param.getString("time","");
		room = param.getString("room","");
		username = param.getString("username","");
		place = param.getString("place","");
		userid = param.getString("userId","");
	}
}
