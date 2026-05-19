package com.nv.commons.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.model.dto.MultiKey;

public class ThreadLocalUtils {

	//SimpleDateFormat is not thread-safe
	private static ThreadLocal<HashMap<MultiKey, SimpleDateFormat>> results = ThreadLocal.withInitial(
		HashMap::new);

	//DecimalFormat is not thread-safe
	private static ThreadLocal<HashMap<String, DecimalFormat>> decimalFormats = ThreadLocal.withInitial(
		HashMap::new);

	private static final ThreadLocal<WebSiteType> webSiteTypeThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<CurrencyType> currencyTypeThreadLocal = new ThreadLocal<>();

	public static SimpleDateFormat getSimpleDateFormat(String format, Locale locale) {
		MultiKey key = new MultiKey(format, locale);
		HashMap<MultiKey, SimpleDateFormat> hm = results.get();
		SimpleDateFormat obj = hm.get(key);

		if (obj != null) {
			return obj;
		}
		obj = new SimpleDateFormat(format, locale);
		hm.put(key, obj);
		return obj;
	}

	public static SimpleDateFormat getSimpleDateFormat(String format) {
		MultiKey key = new MultiKey(format, Locale.getDefault());
		HashMap<MultiKey, SimpleDateFormat> hm = results.get();
		SimpleDateFormat obj = hm.get(key);

		if (obj != null) {
			return obj;
		}
		obj = new SimpleDateFormat(format);
		hm.put(key, obj);
		return obj;
	}

	public static SimpleDateFormat getSimpleDateFormat(String format, Locale locale, TimeZone tz) {
		MultiKey key = new MultiKey(format, locale, tz);
		HashMap<MultiKey, SimpleDateFormat> hm = results.get();
		SimpleDateFormat obj = hm.get(key);

		if (obj != null) {
			return obj;
		}
		obj = new SimpleDateFormat(format, locale);
		obj.setTimeZone(tz);
		hm.put(key, obj);
		return obj;
	}

	public static SimpleDateFormat getSimpleDateFormat(String format, TimeZone tz) {
		MultiKey key = new MultiKey(format, Locale.getDefault(), tz);
		HashMap<MultiKey, SimpleDateFormat> hm = results.get();
		SimpleDateFormat obj = hm.get(key);

		if (obj != null) {
			return obj;
		}
		obj = new SimpleDateFormat(format);
		obj.setTimeZone(tz);
		hm.put(key, obj);
		return obj;
	}

	public static DecimalFormat getDecimalFormat(String format) {
		HashMap<String, DecimalFormat> hm = decimalFormats.get();
		DecimalFormat obj = hm.get(format);

		if (obj != null) {
			return obj;
		}
		obj = new DecimalFormat(format);
		hm.put(format, obj);
		return obj;
	}

	public static void set(WebSiteType webSiteType) {
		webSiteTypeThreadLocal.set(webSiteType);
	}

	public static WebSiteType getWebSiteType() {
		return webSiteTypeThreadLocal.get();
	}

	public static void set(CurrencyType currencyType) {
		currencyTypeThreadLocal.set(currencyType);
	}

	public static CurrencyType getCurrencyType() {
		return currencyTypeThreadLocal.get();
	}
}
