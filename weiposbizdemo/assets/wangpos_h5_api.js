var API = (function() {
	ANDROID = {};

	ANDROID.get = function(id) {
		return document.getElementById(id);
	};
	ANDROID.isString = function(o) {
		return ({}).toString.call(o) == '[object String]';
	};

	ANDROID.isNumber = function(o) {
		return ({}).toString.call(o) == '[object Number]';
	};

	ANDROID.isFunction = function(o) {
		return ({}).toString.call(o) == '[object Function]';
	};

	ANDROID.isArray = function(o) {
		return ({}).toString.call(o) == '[object Array]';
	};

	ANDROID.json2str = function _json2str(o) {
		var arr = [];
		var fmt = function(s) {
			if (typeof s == 'object' && s != null)
				return _json2str(s);
			return /^(string|number)$/.test(typeof s) ? "'" + s + "'" : s;
		}
		for ( var i in o)
			arr.push("'" + i + "':" + fmt(o[i]));
		return '{' + arr.join(',') + '}';
	};
	
	ANDROID.__callback__ = {};

	function param2string(obj) {
		if (ANDROID.isString(obj) || ANDROID.isNumber(obj))
			return obj + '';

		if (ANDROID.isFunction(obj)) {
			var func_name = new Date().getTime()
					+ Math.floor(Math.random() * 1000) + '';
			ANDROID.__callback__[func_name] = function() {
				ANDROID.__callback__[func_name] = null;
				obj.apply(null, arguments);
			};
			return 'window.ANDROID.__callback__["' + func_name + '"]';
		}

		if (ANDROID.isArray(obj)) {
			var rt = [];
			var len = obj.length;
			ANDROID.each(obj, function(i, v) {
				rt.push(encodeURIComponent(v + ''));
			});
			return rt.join(':');
		}

		var rt = [];
		for ( var key in obj) {
			rt.push(encodeURIComponent(key) + ':'
					+ encodeURIComponent(obj[key] + ''));
		}
		return rt.join(';');
	}

	// construct
	function Api() {
	}

	// 获取设备信息
	Api.prototype.getDeviceInfo = function() {
		var retV = eval('(' + window.androidJs.getDeviceInfo() + ')');
		return retV;
	};

    //调用pos播音
	Api.prototype.speechVoice = function(content) {
		window.androidJs.speechVoice(content);
	};

	// 扫描二维码或者条码
    Api.prototype.scanCode = function(type,onSuccess, onError) {
    	window.androidJs.scanCode(type,param2string(onSuccess),
            param2string(onError));
    };
    

    // 调用打印机打印
    Api.prototype.printContent = function(fontSize,gravity,type,content) {
        window.androidJs.printContent(fontSize,gravity,type,content);
    };
    // 调用打印机打印
    Api.prototype.submitPrint = function() {
        window.androidJs.submitPrint();
    };

    Api.prototype.takePhone = function(isCrop, onSuccess, onError) {
        window.androidJs.takePhone(isCrop,param2string(onSuccess), param2string(onError));
    };

	// 打印日志到客户短显示
	Api.prototype.log = function(msg) {
		window.androidJs.log(msg);
	}

	// 加载网络资源
	Api.prototype.loadUrl = function(url, onSuccess, onError) {
		window.androidJs.loadUrl(url, param2string(onSuccess),
				param2string(onError));
	}

	//
	Api.prototype.putFile = function(filepath, onSuccess, onError) {
		window.androidJs.putFile(filepath, param2string(onSuccess),
				param2string(onError));
	}

	//
	Api.prototype.http = {};
	Api.prototype.http.post = function(url, data, headers, fb, eb) {
		window.androidJs.httpPost(url, ANDROID.json2str(data), ANDROID.json2str(headers), param2string(fb),
				param2string(eb));
	}

	Api.prototype.http.get = function(url, headers, fb, eb) {
		window.androidJs.httpGet(url, ANDROID.json2str(headers), param2string(fb),
				param2string(eb));
	}

	return Api;
})();

window.Api = new API();