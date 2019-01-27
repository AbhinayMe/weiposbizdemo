package cn.weipass.biz.vo.intr;

import java.io.IOException;

import android.os.Parcelable;
import cn.shellinfo.wall.remote.ParamMap;

public abstract class BaseBean implements Parcelable{
	
	/**
	 * 加载来自服务器的数据
	 * @param is
	 * 
	 * @throws IOException
	 */
	public abstract void loadFromServerData(ParamMap param);
}
