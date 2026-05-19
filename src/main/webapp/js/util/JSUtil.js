/*** StringUtil ***/
if (typeof (StringUtil) == 'undefined') {
	StringUtil = {};
}

(function() {
	StringUtil.startsWith = function(target, prefix) {
		return (target.substr(0, prefix.length) == prefix);
	};
	StringUtil.endsWith = function(target, suffix) {
		return target.substring(target.length - suffix.length) == suffix;
	};
	StringUtil.concat = function(target, str) {
		return new String(target.toString() + str);
	};
	StringUtil.toCharArray = function(target) {
		var charArr = new Array(target.length);
		for (var i = 0; i < target.length; i++) {
			charArr[i] = target.charAt(i);
		}
		return charArr;
	};
	StringUtil.trim = function(target) {
		return target.replace(/(^\s*)|(\s*$)/g, "");
	};
	StringUtil.replaceAll = function(Source, stringToFind, stringToReplace) {
		var temp = Source;
		var index = temp.indexOf(stringToFind);
		while (index != -1) {
			temp = temp.replace(stringToFind, stringToReplace);
			index = temp.indexOf(stringToFind);
		}
		return temp;
	};
	StringUtil.isOnlyLowerCaseLetterAndNumber = function(text) {
		var regExp = /^[\d|a-z]+$/;

		if (regExp.test(text)) {
			return true;
		}
		return false;
	};
	StringUtil.isOnlyNumber = function(text) {
		var regExp = /^[\d]+$/;

		if (regExp.test(text)) {
			return true;
		}
		return false;
	};
	StringUtil.isOnlyNumberAndColon = function(text) {
		var regExp = /^[\d:]+$/;

		if (regExp.test(text)) {
			return true;
		}
		return false;
	};
	StringUtil.makeRandom = function(length) {
		var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
		var result = '';
		for (var i = 0; i < length; i++) {
			result += chars.charAt(Math.floor(Math.random() * chars.length));
		}
		return result;
	};

	StringUtil.format = function(key, values) {
		var str = key.toString() || '';
		if (typeof values !== 'undefined' && values != null && values.length > 0) {
			var pattern = new RegExp('{([0-' + values.length + '])}', 'g');
			return String(str).replace(pattern, function(match, index) {
				return values[index];
			});
		} else {
			return str;
		}
	};

	StringUtil.isString = function(value) {
		return (typeof value === 'string');
	};
})();

/*** ArrayUtil ***/
if (typeof (ArrayUtil) == 'undefined') {
	ArrayUtil = {};
}

(function() {
	ArrayUtil.max = function(target) {
		var i, max = target[0];
		for (i = 1; i < target.length; i++) {
			if (max < target[i]) {
				max = target[i];
			}
		}
		return max;
	};

	ArrayUtil.contains = function(target, element) {
		for (var i = 0; i < target.length; i++) {
			if (target[i] === element) {
				return true;
			}
		}
		return false;
	};

	ArrayUtil.remove = function(target, element) {
		for (var i = 0; i < target.length; i++) {
			if (target[i] === element) {
				target.splice(i, 1);
				return true;
			}
		}
		return false;
	};

	ArrayUtil.isArray = function(o) {
		return Object.prototype.toString.call(o) === '[object Array]';
	};

	ArrayUtil.distinct = function(target) {
		var result = [];
		for (var i = 0, l = target.length; i < l; i++) {
			var j = 0;
			for (len = result.length; j < len; j++) {
				if (target[i] === result[j]) {
					break;
				}
			}
			if (j == result.length) {
				result.push(target[i]);
			}
		}
		return result;
	};
})();

// more faster than Map
function HashMap() {

	this.length = 0;
	this.elements = {};

	this.size = function() {
		return this.length;
	};

	this.put = function(_key, _value) {
		if (!this.elements.hasOwnProperty(_key)) {
			this.length++;
		}
		this.elements[_key] = _value;
	};

	this.putAll = function(newMap) {
		for (var p in newMap) {
			if (!this.elements.hasOwnProperty(p)) {
				this.length++;
			}
			this.elements[p] = newMap[p];
		}
	};

	this.remove = function(_key) {
		if (this.elements.hasOwnProperty(_key)) {
			this.length--;
			delete this.elements[_key];
			return true;
		}
		return false;
	};
	this.containsKey = function(_key) {
		return this.elements.hasOwnProperty(_key);
	};
	this.get = function(_key) {
		return this.elements.hasOwnProperty(_key) ? this.elements[_key] : null;
	};
	this.values = function() {
		var temp = new Array(this.length);
		var i = 0;
		for (var p in this.elements) {
			temp[i++] = this.elements[p];
		}
		return temp;
	};
	this.entrySet = function() {
		var temp = new Array(this.length);
		var i = 0;
		for (var p in this.elements) {
			temp[i++] = {key: p, value: this.elements[p]};
		}
		return temp;
	};

	this.clear = function() {
		this.elements = {};
		this.length = 0;
	};
	this.keySet = function() {
		var temp = new Array(this.length);
		var i = 0;
		for (var p in this.elements) {
			temp[i++] = p;
		}
		return temp;
	};
}


////--------
//// replace with JsCache
////
//if (typeof (JCache) == 'undefined') {
//	JCache = {};
//
//	(function() {
//		var pool={};
//		JCache.get = function(element) {
//			if(element==null || element.length==0) {
//				return null;
//			}
//			var result = pool[element];
//			if(!result) {
//				result = $(element);
//				pool[element] = result;		
//			}
//			return result;
//		};
//		JCache.clone = function(element) {
//			var obj = JCache.get(element);
//			if (obj == null) {
//				return null;
//			}
//			return obj.clone();
//		};
//	}());
//}


if (typeof (DateUtil) == 'undefined') {
	DateUtil = {};
}

(function() {
	var months = {
		1: 'JAN', 2: 'FEB', 3: 'MAR', 4: 'APR', 5: 'MAY', 6: 'JUN',
		7: 'JUL', 8: 'AUG', 9: 'SEP', 10: 'OCT', 11: 'NOV', 12: 'DEC'
	};
	DateUtil.format = function(date, format) {

		// Passing date through Date applies Date.parse, if necessary
		date = date ? new Date(date) : new Date();
		if (isNaN(date)) throw SyntaxError("invalid date");

//		format = format.replace(/yyyy/, date.getFullYear());
		format = format.replace(/yyyy/i, date.getFullYear());//大小寫都吃


		var temp = date.getMonth() + 1;
		format = format.replace(/MMM/, months[temp]);
		format = format.replace(/MM/, (temp < 10 ? "0" + temp : temp));//限定大寫

		temp = date.getDate();
		format = format.replace(/dd/, (temp < 10 ? "0" + temp : temp));//限定小寫

		temp = date.getHours();
//		format = format.replace(/hh/, (temp < 10 ? "0"+temp:temp));
		format = format.replace(/hh/i, (temp < 10 ? "0" + temp : temp));//大小寫都吃
		format = format.replace(/TT/, temp < 12 ? "AM" : "PM");

		temp = date.getMinutes();
		format = format.replace(/mm/, (temp < 10 ? "0" + temp : temp));//限定小寫

		temp = date.getSeconds();
		format = format.replace(/ss/, (temp < 10 ? "0" + temp : temp));//限定小寫

		return format;
	};

	DateUtil.formatYYYYMMDD = function(dateTimestamp) {

		var iniTime = new Date();
		if (parseInt(dateTimestamp)) {
			iniTime = new Date(parseInt(dateTimestamp));
		}

		var timezoneOffset = 8; // GMT+8
		var nf = function(num, size) {
			var s = "000000000" + num;
			return s.substr(s.length - size);
		};

		var numTime = iniTime.getTime();
		var TimeToGMT = iniTime.getTimezoneOffset() / 60;
		var gmtTime = numTime + TimeToGMT * 3600 * 1000;

		if (timezoneOffset !== TimeToGMT) {
			timezoneOffset = -1 * TimeToGMT;
		}

		var time;

		if (timezoneOffset != undefined) {
			time = new Date(gmtTime + timezoneOffset * 3600 * 1000);
		} else {
			time = iniTime;
		}

		return time.getFullYear() + '/' + nf(time.getMonth() + 1, 2) + '/' + nf(time.getDate(), 2);
	};


	DateUtil.formatHHmmss = function(dateTimestamp) {

		var iniTime = new Date();
		if (parseInt(dateTimestamp)) {
			iniTime = new Date(parseInt(dateTimestamp));
		}

		var timezoneOffset = 8; // GMT+8
		var nf = function(num, size) {
			var s = "000000000" + num;
			return s.substr(s.length - size);
		};

		var numTime = iniTime.getTime();
		var TimeToGMT = iniTime.getTimezoneOffset() / 60;
		var gmtTime = numTime + TimeToGMT * 3600 * 1000;

		if (timezoneOffset !== TimeToGMT) {
			timezoneOffset = -1 * TimeToGMT;
		}

		var time;

		if (timezoneOffset != undefined) {
			time = new Date(gmtTime + timezoneOffset * 3600 * 1000);
		} else {
			time = iniTime;
		}

		return nf(time.getHours(), 2) + ':' + nf(time.getMinutes(), 2) + ':' + nf(time.getSeconds(), 2);
	};

	DateUtil.formatWithZone = function(dateTimestamp) {

		var iniTime = new Date();
		if (parseInt(dateTimestamp)) {
			iniTime = new Date(parseInt(dateTimestamp));
		}

		var timezoneOffset = 8; // GMT+8
		var nf = function(num) {
			return "" + num;
		};
		var getGMT = function(gmt) {
			if (gmt <= 0) {
				return '+' + nf(-1 * gmt);
			} else {
				return '-' + nf(gmt)
			}
		};

		var TimeToGMT = iniTime.getTimezoneOffset() / 60;

		if (timezoneOffset !== TimeToGMT) {
			timezoneOffset = -1 * TimeToGMT;
		}

		var gmt;

		if (timezoneOffset != undefined) {
			gmt = -1 * timezoneOffset;
		} else {
			gmt = TimeToGMT;
		}

		return '(GMT' + getGMT(gmt) + ')';
	};

	DateUtil.countTime = function(btn, date) {
		//加一分鐘 60 * 1000
		date.setTime(date.getTime() + 60000);
		btn.html(DateUtil.format(date, "MM/dd/yyyy hh:mm") + " GMT+8");
		setTimeout(function() {
			DateUtil.countTime(btn, date);
		}, 60000);
	};

	DateUtil.showTime = function(btn) {
		var date = new Date(PageConfig.serverCurrentTimeMillis);

		btn.html(DateUtil.format(date, "MM/dd/yyyy hh:mm") + " GMT+8");
		var sec = date.getSeconds();
		//去除多餘秒數
		date.setTime(date.getTime() - (sec * 1000));
		setTimeout(function() {
			DateUtil.countTime(btn, date);
		}, (60 - sec) * 1000);
	};
	DateUtil.compare = function(a, b) {
		// Compare two dates (could be of any type supported by the convert
		// function above) and returns:
		//  -1 : if a < b
		//   0 : if a = b
		//   1 : if a > b
		// NaN : if a or b is an illegal date
		// NOTE: The code inside isFinite does an assignment (=).


		return (
			isFinite(a = DateUtil.convert(a).valueOf()) &&
			isFinite(b = DateUtil.convert(b).valueOf()) ?
				(a > b) - (a < b) :
				NaN
		);
	};
	DateUtil.convert = function(d) {
		// Converts the date in d to a date-object. The input can be:
		//   a date object: returned without modification
		//  an array      : Interpreted as [year,month,day]. NOTE: month is 0-11.
		//   a number     : Interpreted as number of milliseconds
		//                  since 1 Jan 1970 (a timestamp)
		//   a string     : Any format supported by the javascript engine, like
		//                  "YYYY/MM/DD", "MM/DD/YYYY", "Jan 31 2009" etc.
		//  an object     : Interpreted as an object with year, month and date
		//                  attributes.  **NOTE** month is 0-11.
		return (
			d.constructor === Date ? d :
				d.constructor === Array ? new Date(d[0], d[1], d[2]) :
					d.constructor === Number ? new Date(d) :
						d.constructor === String ? new Date(d) :
							typeof d === "object" ? new Date(d.year, d.month, d.date) :
								NaN
		);
	};

	// Converts the date dd/MM/yyyy to yyyy/MM/dd
	DateUtil.getDateFormat = function(d) {
		from = d.split("/");
		return new Date(from[2], from[1] - 1, from[0]);
	};

})();


if (typeof (CurrencyUtil) == 'undefined') {
	CurrencyUtil = {};
}

(function() {

	CurrencyUtil.roundingMode = {};

	//Rounding mode to round towards zero. Never increments the digit prior to a discarded fraction (i.e., truncates). 
	//Note that this rounding mode never increases the magnitude of the calculated value.
	CurrencyUtil.roundingMode.DOWN = 'DOWN';
	CurrencyUtil.roundingMode.HALF_UP = 'HALF_UP';

	CurrencyUtil.locales = 'en-US';
	CurrencyUtil.formatSetting = {
		maximumFractionDigits: 2
	}

	CurrencyUtil.format = function(arg, extendSetting) {

		if (typeof FE !== 'undefined' && FE.PageConfig.balanceLocale) {
			CurrencyUtil.locales = FE.PageConfig.balanceLocale;
		}

		if (extendSetting) {
			CurrencyUtil.formatSetting = extendSetting;
		}
		let value = new String(arg).replace(/,/g, '');

		if (!CurrencyUtil.isNumber(value)) {
			return arg;
		}

		if (value < 0) {
			return '<span style="color: red;">(' + Math.abs(parseFloat(value)).toLocaleString(CurrencyUtil.locales, CurrencyUtil.formatSetting) + ')</span>';
		}

		return parseFloat(value).toLocaleString(CurrencyUtil.locales, CurrencyUtil.formatSetting);

	};

	CurrencyUtil.isNumber = function(inputData) {
		if (parseFloat(inputData).toString() == 'NaN') {
			return false;
		} else {
			return true;
		}
	}

	CurrencyUtil.isNumeric = function(sText, decimals, negatives) {
		var isNumber = true;
		var numDecimals = 0;
		var validChars = "0123456789";
		if (decimals) validChars += ".";
		if (negatives) validChars += "-";
		var thisChar;
		for (var i = 0; i < sText.length && isNumber == true; i++) {
			thisChar = sText.charAt(i);
			if (negatives && thisChar == "-" && i > 0) isNumber = false;
			if (decimals && thisChar == ".") {
				numDecimals = numDecimals + 1;
				if (i == 0 || i == sText.length - 1) isNumber = false;
				if (numDecimals > 1) isNumber = false;
			}
			if (validChars.indexOf(thisChar) == -1) isNumber = false;
		}
		return isNumber;
	};

	/**
	 * Input: 1: Currency (USD, Yen, TWD...etc)
	 *		2: format ( 123.456 ...etc integers...etc)
	 *		3: dollar (in double)
	 *		4: trailingZeros(true, false)
	 * output:  $123456.789   (320.45),
	 *
	 * Use:
	 *	  CurrencyUtil.format(12345.678);
	 *
	 *  Change the precision or change the type
	 *	  CurrencyUtil.updateSetting({"formatter" : TWFormatter });
	 *	  CurrencyUtil.updateSetting({"precision" : 2, "formatter":CurrencyUtil.USFormatter });
	 *	  CurrencyUtil.updateSetting({"precision" : 2, "formatter":CurrencyUtil.TWFormatter, "separateSign": "" });
	 *	  CurrencyUtil.updateSetting({"precision" : 2, "formatter":CurrencyUtil.TWFormatter, "separateSign": "", "trailingZeros": true });
	 * Write the new format
	 * Currency.SGFormatter = function(money){...};
	 * CurrencyUtil.updateSetting({"precision" : 4, "separateSign" : "#"});
	 * CurrencyUtil.updateSetting({"formatter" : SGFormatter });
	 *
	 *
	 */


	/**
	 * formatter2:  1,000 -----> $1,000
	 *			   -1000.05 --> <font color='red'>-$1000.05</font>
	 */
	CurrencyUtil.formatter2 = function(money) {
		if (money.indexOf('-') == 0) {
			return "<span style='color:red'>" + money + "</span>";
		}
		return money;
	};

	/**
	 * formatter1:  1000 -----> 1,000
	 *			   		 -1000.05 --> <font color='red'>(1,000.05)</font>
	 */
	CurrencyUtil.formatter1 = function(money) {
		if (money.indexOf('-') == 0) {
			return "<span style='color:red'>( " + money.substr(1) + " )</span>";
		}
		return money;
	};


	CurrencyUtil.setting = {
//		"precision": 3,
		"precision": 2,
		"separateSign": ",",
		"formatter": CurrencyUtil.formatter1,
		"trailingZeros,": false,
		"roundingMode": CurrencyUtil.roundingMode.HALF_UP
	};

	CurrencyUtil.setting2 = {
		"precision": 2,
		"separateSign": ",",
		"formatter": CurrencyUtil.formatter2,
		"trailingZeros,": false,
		"roundingMode": CurrencyUtil.roundingMode.HALF_UP
	};

	CurrencyUtil.updateSetting = function(obj) {
		for (var name in obj) {
			if (!obj.hasOwnProperty(name)) {
				continue;
			}
			if (name == "precision") {
				var precision = obj[name];
				if (precision % 1 === 0 && precision > -1) {
					CurrencyUtil.setting[name] = precision;
				}
			} else {
				CurrencyUtil.setting[name] = obj[name];
			}
		}
	};
	CurrencyUtil.formatter = function(money, setting) {
		setting = (setting == undefined ? CurrencyUtil.setting : setting);
		money = formatMoney(money, setting.precision, setting.separateSign, setting.roundingMode);
//		if (setting.trailingZeros) {
//			money = trailingZeros(money);
//		}
		return setting.formatter(money);
	};

	CurrencyUtil.formatterWithCurrency = function(money, currencySymbol, setting) {
		let format = CurrencyUtil.formatter(money, setting);
		if (format.includes('(')) {
			return format.replace('(', '( ' + currencySymbol);
		} else {
			return currencySymbol + ' ' + format;
		}
	};

	/**
	 * formatMoney will change the pure double  to the correct format of the currency (negative + comma and fixed number)
	 * (optional) c means precision
	 * (optional) t means separateSign (separate every 3 digits), default is comma (,)
	 */
	function formatMoney(money, c, t, roundingMode) {
		if (roundingMode) {
			if (CurrencyUtil.roundingMode.DOWN == roundingMode) {
				if (money < 0) {
					return CurrencyUtil.thousandComma((Math.ceil((money * Math.pow(10, c)).toFixed(1)) / Math.pow(10, c)).toFixed(c));
				}
				return CurrencyUtil.thousandComma((Math.floor((money * Math.pow(10, c)).toFixed(1)) / Math.pow(10, c)).toFixed(c));
			} else if (CurrencyUtil.roundingMode.HALF_UP == roundingMode) {
				var sign = money < 0 ? -1 : 1;
				money = sign * money;

//				var numberResult = (sign*(Math.round((money * Math.pow(10, c)).toFixed(1)) / Math.pow(10, c))).toFixed(c);
				//第一個toFixed原本是為了解決精準度的issue，但在49.504950495049506的時候，.toFixed(1)會判斷為4的時候進位了
				//改用強制取1位
				//TODO:第二個toFixed，先不改它，如果以後還是有精準度的issue，就改為用手動運算吧

				var money2 = (money * Math.pow(10, c)).toString();
				var numberResult = (sign * (Math.round(money2.substring(0, money2.split('.')[0].length + 2)) / Math.pow(10, c))).toFixed(c);
				if (t) {
					numberResult = CurrencyUtil.thousandComma(numberResult);
				}
				return numberResult;
			}
			return 0;
		}

		var n = money,
			c = isNaN(c = Math.abs(c)) ? 2 : c;
		var d = ".";
		t = t == undefined ? "," : t;
		s = n < 0 ? "-" : "";
		i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "";
		j = (j = i.length) > 3 ? j % 3 : 0;
		return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
	};

	/**
	 * Remove all zeros at the end after floating point
	 *  ex: 7.50 -->7.5   100.0 -->100
	 */
	function trailingZeros(x) {
		if (x.match(/\./)) {
			return x.replace(/\.?0*$/, '');
		}
		return x;
	}

	CurrencyUtil.thousandComma = function(number) {
		var parts = number.toString().split(".");
		var pattern = /(-?\d+)(\d{3})/;
		while (pattern.test(parts[0])) {
			parts[0] = parts[0].replace(pattern, "$1,$2");
		}
		if (parts[1]) {
			return parts[0] + "." + parts[1];
		}
		return parts[0];
	}

	CurrencyUtil.getFormatter = function(settings) {
		var formatter = {};
		var newSetting = {}
		for (var name in CurrencyUtil.setting) {
			newSetting[name] = settings.hasOwnProperty(name) ? settings[name]
				: CurrencyUtil.setting[name];
		}

		formatter.setting = newSetting;
		formatter.format = function(money) {
			return CurrencyUtil.formatter(money, formatter.setting);
		}
		return formatter;
	};
})();


if (typeof (MathUtil) == 'undefined') {
	MathUtil = {};
}

(function() {
	MathUtil.decimal = {};
	//依照起始跟結尾還有scale去建立陣列
	MathUtil.decimal.createDecimalSet = function(start, end, scale) {
		var temp = new Array();
		for (var i = start; i <= end; i = MathUtil.decimal.add(i, scale)) {
			temp.push(i);
		}
		return temp;
	};
	MathUtil.decimal.add = function(v1, v2) {
		return function(arg1, arg2) {
			if (arg1 == null || arg1.length == 0) {
				arg1 = 0;
			}
			;
			if (arg2 == null || arg2.length == 0) {
				arg2 = 0;
			}
			;
			var r1, r2, m;
			try {
				r1 = arg1.toString().split(".")[1].length;
			} catch (e) {
				r1 = 0;
			}
			try {
				r2 = arg2.toString().split(".")[1].length;
			} catch (e) {
				r2 = 0;
			}
			m = Math.pow(10, Math.max(r1, r2));
			return (mul(arg1, m) + mul(arg2, m)) / m;
		}(v1, v2);
	};
	MathUtil.decimal.subtract = function(v1, v2) {
		return function(arg1, arg2) {
			if (arg1 == null || arg1.length == 0) {
				arg1 = 0;
			}
			;
			if (arg2 == null || arg2.length == 0) {
				arg2 = 0;
			}
			;
			var r1, r2, m;
			try {
				r1 = arg1.toString().split(".")[1].length;
			} catch (e) {
				r1 = 0;
			}
			try {
				r2 = arg2.toString().split(".")[1].length;
			} catch (e) {
				r2 = 0;
			}
			m = Math.pow(10, Math.max(r1, r2));
			return (mul(arg1, m) - mul(arg2, m)) / m;// .toFixed(n);
		}(v1, v2);
	};

	MathUtil.decimal.multiply = function(v1, v2) {
		return function(arg1, arg2) {
			if (arg1 == null || arg1.length == 0) {
				arg1 = 0;
			}
			;
			if (arg2 == null || arg2.length == 0) {
				arg2 = 0;
			}
			;
			return mul(arg1, arg2);
		}(v1, v2);
	};

	MathUtil.decimal.divide = function(v1, v2) {
		return function(arg1, arg2) {
			var t1 = 0, t2 = 0, r1, r2;
			try {
				t1 = arg1.toString().split(".")[1].length;
			} catch (e) {
			}
			try {
				t2 = arg2.toString().split(".")[1].length;
			} catch (e) {
			}

			r1 = Number(arg1.toString().replace(".", ""));
			r2 = Number(arg2.toString().replace(".", ""));
			return mul((r1 / r2), Math.pow(10, t2 - t1));
		}(v1, v2);
	};

	MathUtil.isInteger = function(value) {
		if (value.match(/^\d+$/) == null) {
			return false;
		} else {
			return true;
		}
	};

	MathUtil.isNumeric = function(sText, decimals, negatives) {
		var isNumber = true;
		var numDecimals = 0;
		var validChars = "0123456789";
		if (decimals) validChars += ".";
		if (negatives) validChars += "-";
		var thisChar;
		if (sText.length == 0) {
			return false;
		}
		if (sText.length == 1 && sText.charAt(0) == "-") {
			return false;
		}
		for (var i = 0; i < sText.length && isNumber == true; i++) {
			thisChar = sText.charAt(i);
			if (negatives && thisChar == "-" && i > 0) isNumber = false;
			if (decimals && thisChar == ".") {
				numDecimals = numDecimals + 1;
				if (i == 0 || i == sText.length - 1) isNumber = false;
				if (numDecimals > 1) isNumber = false;
			}
			if (validChars.indexOf(thisChar) == -1) isNumber = false;
		}
		return isNumber;
	};

	MathUtil.isPositive = function(number) {
		if ((number.toString()).indexOf(",") > -1) {
			number = parseInt(StringUtil.replaceAll(number, ",", ""));
		}
		return (number >= 0);
		/*
		var isPositive=true;
		if (number<0)  isPositive = false;
		return isPositive;
		*/
	};

	MathUtil.roundp = function(num, pos) {
		var size = Math.pow(10, pos);
		return Math.round(num * size) / size;
	};

	MathUtil.floor = function(num, pos) {
		var size = Math.pow(10, pos);
		return Math.floor(num * size) / size;
	};

	MathUtil.ceil = function(num, pos) {
		var size = Math.pow(10, pos);
		return Math.ceil(num * size) / size;
	};

	var mul = function(arg1, arg2) {
		var t1 = 0, t2 = 0, r1, r2, s1 = arg1.toString(), s2 = arg2.toString();
		try {
			t1 = s1.split(".")[1].length;
		} catch (e) {
		}
		try {
			t2 = s2.split(".")[1].length;
		} catch (e) {
		}
		r1 = Number(s1.toString().replace(".", ""));
		r2 = Number(s2.toString().replace(".", ""));
		return r1 * r2 / Math.pow(10, t1 + t2);
	};
})();


/*** TaskExecuter ***/
if (typeof (TaskExecuter) == 'undefined') {
	TaskExecuter = {};
}
(function() {
	var queue = [];
	var currentJob = null;
	TaskExecuter.execute = function() {

		if (queue.length != 0) {
			currentJob = queue.pop();
			currentJob.execute();
		} else {
			setTimeout(TaskExecuter.execute, 500);
		}
	};
	TaskExecuter.addTask = function(task) {
		queue.push(task);
	};
	TaskExecuter.reExecute = function() {
		currentJob.execute();
	};
})();


/************************ Task Creater ******************************/
if (typeof (TaskHelper) == 'undefined') {
	TaskHelper = {};
}
(function() {
	TaskHelper.createTask = function(iCycleTime, iCycleTick, iWinType, executeMethod) {
		var task = {
			cycleTime: iCycleTime,
			cycleTick: iCycleTick,
			winType: iWinType,
			execute: executeMethod,
			run: function() {
				this.cycleTick = this.cycleTime;
				TaskExecuter.addTask(this);
			},
			check: function() {
				if (this.cycleTick < 0) {
					return;
				}
				var that = this;
				if (this.cycleTick == 0) {
					this.cycleTick = -1;
					setTimeout(function() {
						that.run();
					}, 100);
					return;
				}
				if (this.cycleTick > 0) {
					this.cycleTick--;
					setTimeout(function() {
						that.check();
					}, 1000);
					return;
				}
			},
			refresh: function() {
				this.cycleTick = 0;
			}

		};
		return task;
	};
}());


if (typeof (I18N) == 'undefined') {
	I18N = {};
}

(function() {
	var resource = undefined;

	I18N.setResource = function(resourceMap) {
		resource = resourceMap;
	};

	I18N.get = function(key, values) {
		key = key.toString() || '';
		var str = key;
		if (typeof resource !== 'undefined') {
			str = (resource[key] ? resource[key] : key);
		}
		if (typeof values !== 'undefined' && values != null && values.length > 0) {
			var pattern = new RegExp('{([0-' + values.length + '])}', 'g');
			return String(str).replace(pattern, function(match, index) {
				return values[index];
			});
		} else {
			return str;
		}
	};
})();

/**
 ********************************************
 */
if (typeof (JsCache) == 'undefined') {
	JsCache = {};
}

(function() {
	var pool = {};

	JsCache.get = function(element) {

		if (element == null || element.length == 0) {
			return null;
		}
		var result = pool[element];

		if (!result) {
			result = $(element);
			if (result.length == 0) {
				return undefined;
			}
			pool[element] = result;
		}
		return result;
	};

	JsCache.clone = function(element) {
		var obj = JsCache.get(element);
		if (obj == null) {
			return null;
		}
		return obj.clone();
	};

	JsCache.removeCache = function(element) {
		if (element == null || element.length == 0) {
			return null;
		}

		pool[element] = null;
	}

}());

/***********************************/
if (typeof (WindowEventUtil) == 'undefined') {
	WindowEventUtil = {};
}

(function() {
	/*
		 * events should be dealt carefully in firefox,
		 * the following will solve the problem.
		 * but remember to pass in the event as a parameter whenever neccessary
		 *
		 * isStopBubble  == true: just stopPropagation;
		 * isStopPrevent == true: just preventDefault;
		 *
		 * Use:
		 *     /cfbook/frontend/js/agent/security/passwordOnSignIn.js
		 *
		 *     //showErrorMsg is your function name
		 *     var event = window.event == null ? showErrorMsg.caller.arguments[0] : window.event;
		 *     WindowEventUtil.stopEvent(event);
		 *
		 */
	WindowEventUtil.stopEvent = function(e, isStopBubble, isStopPrevent) {
		if (!e) var e = window.event;

		if (!isStopBubble && !isStopPrevent) {
			isStopBubble = true;
			isStopPrevent = true;
		}

		if (isStopBubble) {
			if (e.stopPropagation) {
				e.stopPropagation();
			} else {
				e.cancelBubble = true; //e.cancelBubble is supported by IE -
			}
		}

		if (isStopPrevent) {
			if (e.preventDefault) {
				e.preventDefault();
			} else {
				e.returnValue = false;
			}
		}

		return false;
	};
}());

/**
 * Used in check the key event. and can build up in bind keydown or keyup event.
 * Will add bind or only digits event later.
 * Do not use this for character codes of keypress events:
 * keypress character codes are not key-specific and may be enirely different from keydown/keyup key codes.
 */
//var keyCode = e.which || e.keyCode;
//event.which is undefined in IE<9
//event.keyCode is 0 in Gecko (Seamonkey, Firefox) on keypress
//http://unixpapa.com/js/key.html
if (typeof (KeyEventUtils) == 'undefined') {
	KeyEventUtils = {};
}
(function() {
	// "0-9"
	KeyEventUtils.isNumberKey = function(e) {
		var keyCode = e.which || e.keyCode;
		return (!e.shiftKey && ((keyCode > 47 && keyCode < 58) || (keyCode > 95 && keyCode < 106)));
	};

	KeyEventUtils.isBackspaceKey = function(keyCode) {
		return (keyCode === 8);
	};

	KeyEventUtils.isDeleteKey = function(keyCode) {
		return (keyCode === 46);
	};

	KeyEventUtils.isEnterKey = function(keyCode) {
		return (keyCode === 13);
	};

	KeyEventUtils.isShiftKey = function(keyCode) {
		return (keyCode === 16);
	};

	KeyEventUtils.isCtrlKey = function(keyCode) {
		return (keyCode === 17);
	};

	KeyEventUtils.isAltKey = function(keyCode) {
		return (keyCode === 18);
	};

	KeyEventUtils.isArrowKey = function(keyCode) {
		return (keyCode > 36 && keyCode < 41);
	};

	KeyEventUtils.isF5Key = function(keyCode) {
		return (keyCode == 116);
	};

	KeyEventUtils.isTabKey = function(keyCode) {
		return (keyCode == 9);
	};

	// "."
	KeyEventUtils.isDecimalPointKey = function(keyCode) {
		return (keyCode == 110 || keyCode == 190);
	};

	// "-"
	KeyEventUtils.isSubtractKey = function(e) {
		//numeric keypad : 109(all browsers)
		//not numeric keypad : 173 (Firefox), 189(other browsers)
		var keyCode = e.which || e.keyCode;
		return (!e.shiftKey && (keyCode == 109 || keyCode == 189 || keyCode == 173));
	};

	KeyEventUtils.isSpaceKey = function(keyCode) {
		return (keyCode == 32);
	};

	KeyEventUtils.isAlphabetKey = function(keyCode) {
		return (keyCode > 64 && keyCode < 91);
	};

	KeyEventUtils.isParenthesis = function(e) {
		var keyCode = e.which || e.keyCode;
		return (e.shiftKey && (keyCode == 57 || keyCode == 48));
	};

	KeyEventUtils.isIME = function(keyCode) {
		return (keyCode == 229);
	};

	KeyEventUtils.isRefresh = function(e) {
		var keyCode = e.which || e.keyCode;
		return KeyEventUtils.isF5Key(keyCode) || keyCode == 82 && e.ctrlKey;
	};
})();

/*** FormatUtil ***/
if (typeof (FormatUtil) == 'undefined') {
	FormatUtil = {};
}

(function() {
	FormatUtil = {};

	// EX: FormatUtil.padLeft(10, 5) => 00010
	FormatUtil.padLeft = function(nr, n, str) {
		return Array(n - String(nr).length + 1).join(str || '0') + nr;
	};
})();


if (typeof (PageInfoHandler) == 'undefined') {
	PageInfoHandler = {};
}
(function() {
	var totalCount = 0;
	var pageSize = 10;
	var pageNumber = 1;
	PageInfoHandler.getTotalCount = function() {
		return totalCount;
	};
	PageInfoHandler.getPageSize = function() {
		return pageSize;
	};
	PageInfoHandler.getPageNumber = function() {
		return pageNumber;
	};

	PageInfoHandler.setTotalCount = function(pTotalCount) {
		totalCount = pTotalCount;
	};
	PageInfoHandler.setPageSize = function(pPageSize) {
		pageSize = pPageSize;
	};
	PageInfoHandler.setPageNumber = function(pPageNumber) {
		pageNumber = pPageNumber;
	};

	PageInfoHandler.getTotalPage = function() {
		var totalPage = parseInt(totalCount / pageSize);
		if (totalCount % pageSize > 0) {
			totalPage++;
		}
		return totalPage;
	};
	PageInfoHandler.getFirstRowNumber = function() {
		return pageSize * (pageNumber - 1);
	};
	PageInfoHandler.getLastRowNumber = function() {
		return pageSize * pageNumber;
	};
}());

/***********************************/
if (typeof (VipIconUtil) == 'undefined') {
	VipIconUtil = {};
}
(function() {

	// TODO: revise this
	var vipGroupDisplayConfig = {
		yb: {
			showHiddenStar: true
//			minueDisplayLevel: 0
		},
		ld: {
			showHiddenStar: false
//			minueDisplayLevel: 1
		}
	};

	var vipGroupIconConfig = {
		yb: [
			{
				group: 0,
				icon: ''
			},
			{
				group: 1,
				icon: 'star.png'
			},
			// {
			// 	group: 2,
			// 	icon: 'penguin.png'
			// },
			// {
			// 	group: 3,
			// 	icon: 'shark.png'
			// },
			// {
			// 	group: 4,
			// 	icon: 'panda.png'
			// },
			// {
			// 	group: 5,
			// 	icon: 'panda.png'
			// },
			// {
			// 	group: 6,
			// 	icon: 'monkey.png'
			// },
		],
		ld: [
			{
				group: 0,
				icon: ''
			},
			{
				group: 1,
				icon: '',
				gradeSetting: [
					{
						grade: 0,
						icon: ''
					},
					{
						grade: 1,
						icon: 'star.png'
					},
					{
						grade: 2,
						icon: 'bronze.png'
					},
					{
						grade: 3,
						icon: 'silver.png'
					},
					{
						grade: 4,
						icon: 'blue.png'
					},
					{
						grade: 5,
						icon: 'gold.png'
					},
				]
			},
		],
		tf: [
			{
				group: 0,
				icon: ''
			},
			{
				group: 1,
				icon: '',
				gradeSetting: [
					{
						grade: 0,
						icon: ''
					},
					{
						grade: 1,
						icon: 'dragon0.png'
					},
					{
						grade: 2,
						icon: 'dragon1.png'
					},
					{
						grade: 3,
						icon: 'dragon2.png'
					},
					{
						grade: 4,
						icon: 'dragon3.png'
					},
					{
						grade: 5,
						icon: 'dragon4.png'
					},
					{
						grade: 6,
						icon: 'dragon5.png'
					},
				]
			},
		],
		bv: [
			{
				group: 0,
				icon: ''
			},
			{
				group: 1,
				icon: '',
				gradeSetting: [
					{
						grade: 0,
						icon: ''
					},
					{
						grade: 1,
						icon: 'silver.png'
					},
					{
						grade: 2,
						icon: 'gold.png'
					},
					{
						grade: 3,
						icon: 'platinum.png'
					},
					{
						grade: 4,
						icon: 'diamond.png'
					},
					{
						grade: 5,
						icon: 'silverLv5.png'
					}
				]
			},
		],
		vb: [
			{
				group: 0,
				icon: ''
			},
			{
				group: 1,
				icon: '',
				gradeSetting: [
					{
						grade: 0,
						icon: ''
					},
					{
						grade: 1,
						icon: 'vip/vip1.png'
					},
					{
						grade: 2,
						icon: 'vip/vip2.png'
					},
					{
						grade: 3,
						icon: 'vip/vip3.png'
					},
					{
						grade: 4,
						icon: 'vip/vip4.png'
					}
				]
			}
		]
	};

	/*
	 * 
	 */
	VipIconUtil.getIcon = function(vipLevel) {
		var vipGroup = 1; // MEMO: fixed
		var vipGrade = vipLevel;
		var vipGroupIconSetting = vipGroupIconConfig[PageConfig.webSiteType];

		if (vipGroupIconSetting == null) {
			return 'star.png';
		}

		var vipGroupIcon = '';

		if (vipGroupIconSetting[vipGroup] != null) {

			vipGroupIcon = vipGroupIconSetting[vipGroup].icon;

			if (vipGroupIconSetting[vipGroup]['gradeSetting'] != null) {

				var gradeSetting = vipGroupIconSetting[vipGroup].gradeSetting;

				if (gradeSetting[vipGrade] != null) {
					vipGroupIcon = gradeSetting[vipGrade].icon;
				}
			}
		}

		if (PageConfig.webSiteType == 'bv') {
			return PageConfig.lang + '-' + vipGroupIcon;
		}
		return vipGroupIcon;
	};

	/*
	 * 
	 */
	VipIconUtil.needShowHiddenStar = function() {

		if (vipGroupDisplayConfig[PageConfig.webSiteType] == null) {
			return true;
		}
		return (vipGroupDisplayConfig[PageConfig.webSiteType]['showHiddenStar'] != false);
	};

}());

/*** ReportUtil ***/
if (typeof (ReportUtil) == 'undefined') {
	ReportUtil = {};
}

(function() {
	var datePattern = "yyyy/MM/dd";
	var dateTimePattern = "yyyy/MM/dd HH:mm:ss";
	ReportUtil.setDefConditionOfTimeRange = function(startTimeObj, endTimeObj) {
		var today = new Date();
		var endDate = DateUtil.format(today, datePattern);
		$('#' + endTimeObj).val(endDate);

		var startDate = DateUtil.format(today.setDate(today.getDate() - 7), datePattern);
		$('#' + startTimeObj).val(startDate);
	};

	ReportUtil.setDefConditionOfTimeRangeByDOM = function(startTimeObj, endTimeObj) {
		var today = new Date();
		var endDate = DateUtil.format(today, datePattern);
		endTimeObj.val(endDate);

		var startDate = DateUtil.format(today.setDate(today.getDate() - 7), datePattern);
		startTimeObj.val(startDate);
	};

	ReportUtil.setDefPaymentConditionOfTimeRange = function(startTimeObj, endTimeObj) {
		var today = new Date();
		today.setHours(23, 59, 59, 999);

		var endDate = DateUtil.format(today, dateTimePattern);
		$('#' + endTimeObj).val(endDate);

		today.setHours(0, 0, 0, 0);
		var startDate = DateUtil.format(today.setDate(today.getDate() - 1), dateTimePattern);
		$('#' + startTimeObj).val(startDate);
	};

	ReportUtil.setDefPaymentConditionOfTimeRangeByDom = function(startTimeObj, endTimeObj) {
		var today = new Date();
		today.setHours(23, 59, 59, 999);

		var endDate = DateUtil.format(today, dateTimePattern);
		endTimeObj.val(endDate);

		today.setHours(0, 0, 0, 0);
		var startDate = DateUtil.format(today.setDate(today.getDate() - 1), dateTimePattern);
		startTimeObj.val(startDate);
	};


	ReportUtil.validationMaxDate = function(timeObj, maxValue) {
		$('#' + timeObj).on('blur', function() {
			var timeValue = $(this).val();
			if (DateUtil.compare(timeValue, maxValue) > 0) {
				$(this).val('');
			}
		});
	};

	ReportUtil.validationMinDate = function(timeObj, minValue) {
		$('#' + timeObj).on('blur', function() {
			var timeValue = $(this).val();
			if (DateUtil.compare(timeValue, minValue) < 0) {
				$(this).val('');
			}
		});
	};

	ReportUtil.validationMaxDateByDOM = function(timeObj, maxValue) {
		timeObj.on('blur', function() {
			var timeValue = $(this).val();
			if (DateUtil.compare(timeValue, maxValue) > 0) {
				$(this).val('');
			}
		});
	};

	ReportUtil.validationMinDateByDOM = function(timeObj, minValue) {
		timeObj.on('blur', function() {
			var timeValue = $(this).val();
			if (DateUtil.compare(timeValue, minValue) < 0) {
				$(this).val('');
			}
		});
	};

})();

if (typeof (CheckBoxUtil) == 'undefined') {
	CheckBoxUtil = {};
}

(function() {
	CheckBoxUtil.bindCheckAll = function(event, column, dataCount) {
		var value = event.target.value;
		var $targetColumn = $('[name=' + column + ']');
		var $all = $('[name=' + column + '][value=-1]');

		if (value == -1) {
			if ($all.prop("checked")) {
				$targetColumn.not($all).prop('checked', true);
			} else {
				$targetColumn.not($all).prop('checked', false);
			}
		} else {
			if ($('[name=gameType]:checked').not($all).length !== dataCount) {
				$all.prop('checked', false);
			} else {
				$all.prop('checked', true);
			}
		}

		$targetColumn.uniform();
	};
})();

/*** provided by Bruce: BridgeUtils for communicate with Native App ***/
if (typeof (BridgeUtils) == 'undefined') {
	BridgeUtils = {};
}

(function() {

	function connectWebViewJavascriptBridge(callback) {
		if (window.WebViewJavascriptBridge) {
			callback(WebViewJavascriptBridge);
		} else {
			document.addEventListener('WebViewJavascriptBridgeReady', function() {
				callback(WebViewJavascriptBridge);
			}, false);
		}
	}

	function setupWebViewJavascriptBridge(callback) {
		if (window.WebViewJavascriptBridge) {
			return callback(WebViewJavascriptBridge);
		}
		if (window.WVJBCallbacks) {
			return window.WVJBCallbacks.push(callback);
		}
		window.WVJBCallbacks = [callback];
		var WVJBIframe = document.createElement('iframe');
		WVJBIframe.style.display = 'none';
		WVJBIframe.src = 'https://__bridge_loaded__';
		document.documentElement.appendChild(WVJBIframe);
		setTimeout(function() {
			document.documentElement.removeChild(WVJBIframe)
		}, 0)
	}

	BridgeUtils.sendMessage = function(message) {
		// iOS
		setupWebViewJavascriptBridge(function(bridge) {
			bridge.callHandler('ObjC Echo', message);
		});

		// Android
		console.log(message);
	};

	// iOS bridge listener
	BridgeUtils.bridgeIOSRegister = function() {
		setupWebViewJavascriptBridge(function(bridge) {
			bridge.registerHandler('JS Echo', function(data, responseCallback) {
				//do something

			});
		});
	};

}());

/*
 * 
 */
if (typeof (DataImageUtils) == 'undefined') {
	DataImageUtils = {};
}

(function() {

	DataImageUtils.setBackground = function(container) {

		if (!container) {
			return;
		}
		var list = container.find('.data-image[data-image]');

		for (var i = 0; i < list.size(); i++) {
			var elem = $(list.get(i));
			var originPath = elem.data('image') || '';
			var cssStr = "url('" + originPath + "')";

			if (typeof (PageConfig) !== 'undefined' && PageConfig.cdnDomain && originPath.indexOf(PageConfig.cdnDomain) == -1) {

				cssStr = "url('" + PageConfig.cdnDomain + originPath + "')";

			} else if (typeof (FE) !== 'undefined' && typeof (FE.PageConfig) !== 'undefined' && FE.PageConfig.cdnDomain
				&& originPath.indexOf(FE.PageConfig.cdnDomain) == -1) {

				cssStr = "url('" + FE.PageConfig.cdnDomain + originPath + "')";
			}

			elem.css('backgroundImage', cssStr);
		}
	};

	DataImageUtils.setSEOTitle = function(container) {

		if (!container) {
			return;
		}
		var list = container.find('.data-title[data-title]');

		for (var i = 0; i < list.size(); i++) {
			var elem = $(list.get(i));
			elem.attr('title', elem.data('title'));
		}
	}

}());

/*
 ******************************
 */

if (typeof (getDataByAjax) == 'undefined') {

	getDataByAjax = function(url, callback, data) {

		if (!data) {
			data = {};
		}

		$.ajax({
			'url': url + Math.random(),
			'type': 'GET',
			'dataType': 'json',
			'data': data
		}).done(function(response) {

			if (!response.status) {
				callback(response);
				return;
			}

			if (response.status == 200) {
				callback(response);
			} else {
				Trace.error(url);
				Trace.error(response);
			}

		}).fail(function(err) {
			Trace.error(url);
			Trace.error(err);
			DialogHandler.showErrorAlert();
		});
	};
}

if (typeof (DomUtils) == 'undefined') {
	DomUtils = {};
}

(function() {

	DomUtils.findAncestor = function(el, sel) {
		while ((el = el.parentElement) && !((el.matches || el.matchesSelector).call(el, sel))) ;
		return el;
	};

	DomUtils.isImage = function(fileName) {
		let parts = fileName.split('.');
		let extension = parts[parts.length - 1];
		switch (extension.toLowerCase()) {
			case 'jpg':
			case 'jpeg':
			case 'png':
				return true;
		}
		return false;
	};

	DomUtils.nameStartsWith = function(sel, str) {
		const el = document.querySelectorAll(sel), res = [];
		for (let i = 0, n = el.length; i < n; i++) {
			if (el[i].name.indexOf(str) === 0) {
				res.push(el[i]);
			}
		}
		return res;
	};

}());


if (typeof (UUIDUtils) == 'undefined') {
	UUIDUtils = {};
}

(function() {

	UUIDUtils.get = function() {
		return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
			(c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
		);
	};


}());
