/*
 * Created on 2005/6/5
 *
 */
package com.nv.commons.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.nv.commons.exceptions.ParameterNotFoundException;
import com.nv.module.backendapi.controller.JsonRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

/**
 * @author alanhu
 *
 */
public class RequestParser {

	// 這邊取18長度避免超過long的範圍值
	public static final int MAX_LONG_LENGTH = 18;

	// 檢查XSS攻擊的最小長度
	// 未經允許不可改動此長度
	private static int MIN_XSS_LENGTH = 20;

	public static Function<String, String> replaceComma = (s) -> {
		return s.trim().replace(",", "");
	};

	public static boolean getBooleanParameter(String value) throws ParameterNotFoundException, NumberFormatException {
		if (value.equalsIgnoreCase("1") || (value.equalsIgnoreCase("true")) || (value.equalsIgnoreCase("on")) || (value.equalsIgnoreCase("yes"))) {
			return true;
		} else if (value.equalsIgnoreCase("0") || (value.equalsIgnoreCase("false")) || (value.equalsIgnoreCase("off")) || (value.equalsIgnoreCase("no"))) {
			return false;
		} else {
			throw new NumberFormatException(" value " + value + " is not a boolean");
		}
	}

	public static boolean getBooleanParameter(ServletRequest request, String name) throws ParameterNotFoundException, NumberFormatException {
		String value = getStringParameter(request, 5, name).toLowerCase();
		return getBooleanParameter(value);
	}

	public static boolean getBooleanParameter(ServletRequest request, String name, boolean def) {
		try {
			return getBooleanParameter(request, name);
		} catch (Exception e) {
			return def;
		}
	}


	public static int getIntParameter(String value) throws NumberFormatException, ParameterNotFoundException {
		return Integer.parseInt(value);
	}

	public static int getIntParameter(ServletRequest request, String name)
		throws NumberFormatException, ParameterNotFoundException {
		try {
			return Integer.parseInt(getPreStringParameter(request, name).trim().replace(",", ""));
		} catch (NumberFormatException e) {
			throw new NumberFormatException(name + " is incorrect");
		}
	}

	public static int getIntParameter(ServletRequest request, int maxLength, String name, int def) {
		try {
			return Integer.parseInt(getStringParameter(request, maxLength, name).trim().replace(",", ""));
		} catch (Exception e) {
			return def;
		}
	}

	public static int getIntParameter(ServletRequest request, int maxLength, String name) {
		return Integer.parseInt(getStringParameter(request, maxLength, name).trim().replace(",", ""));
	}

	public static int getIntParameter(ServletRequest request, String name, Supplier<Integer> ifNull) {
		try {
			return Integer.parseInt(getPreStringParameter(request, name).trim().replace(",", ""));
		} catch (Exception e) {
			Integer result = ifNull.get();
			if (result == null) {
				throw e;
			}
			return result;
		}
	}

	public static int getIntParameter(ServletRequest request, String name, int def) {
		try {
			return Integer.parseInt(getPreStringParameter(request, name).trim().replace(",", ""));
		} catch (Exception e) {
			return def;
		}
	}

	public static int getIntParameter(ServletRequest request, Function<String, String> preprocessFunction, String name) {
		// 如果有指定前置處理函數，則使用前置處理函數，否則呼叫預設的取代逗號
		Function<String, Integer> function = (preprocessFunction == null ? replaceComma : preprocessFunction).andThen(Integer::parseInt);
		return function.apply(getPreStringParameter(request, name));
	}


	public static Part getPart(HttpServletRequest request, String name) throws ServletException, IOException {
		return request.getPart(name);
	}

	public static int[] getIntParameterValues(ServletRequest request, String name) throws ParameterNotFoundException {

		String[] values = request.getParameterValues(name);
		if (values == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (values[0].length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {
			int[] temp = new int[values.length];
			for (int i = 0; i < values.length; i++) {
				try {
					temp[i] = Integer.parseInt(values[i]);
				} catch (Exception ignored) {
				}
			}
			return temp;

		}
	}

	public static int[] getIntParameterValues(ServletRequest request, String name, int[] def) throws ParameterNotFoundException {
		try {
			return getIntParameterValues(request, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static int[] getIntParameterValues(ServletRequest request, Pattern splitPattern, String name) throws ParameterNotFoundException {
		String value = getPreStringParameter(request, name);

		if (value == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (value.length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {
			return splitPattern.splitAsStream(value).mapToInt(Integer::parseInt).toArray();
		}
	}

	public static int[] getIntParameterValues(ServletRequest request, Pattern splitPattern, String name, int[] def) throws ParameterNotFoundException {
		try {
			return getIntParameterValues(request, splitPattern, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static long getLongParameter(ServletRequest request, String name) throws ParameterNotFoundException, NumberFormatException {
		try {
			return Long.parseLong(getStringParameter(request, MAX_LONG_LENGTH, name).replace(",", ""));
		} catch (NumberFormatException e) {
			throw new NumberFormatException(name + " is incorrect");
		}
	}

	public static long getLongParameter(ServletRequest request, String name, long def) {
		try {
			return getLongParameter(request, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static long getLongParameter(ServletRequest request, String name, Supplier<Long> ifNull) {
		try {
			return Long.parseLong(getStringParameter(request, MAX_LONG_LENGTH, name).trim().replace(",", ""));
		} catch (Exception e) {
			Long result = ifNull.get();
			if (result == null) {
				throw e;
			}
			return result;
		}
	}

	public static long getLongParameter(ServletRequest request, Function<String, String> preprocessFunction, String name) {
		// 如果有指定前置處理函數，則使用前置處理函數，否則呼叫預設的取代逗號
		Function<String, Long> function = (preprocessFunction == null ? replaceComma : preprocessFunction).andThen(Long::parseLong);
		return function.apply(getStringParameter(request, MAX_LONG_LENGTH, name));
	}


	public static long[] getLongParameterValues(ServletRequest request, String name) throws ParameterNotFoundException {

		String[] values = request.getParameterValues(name);
		if (values == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (values[0].length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {
			long[] temp = new long[values.length];
			for (int i = 0; i < values.length; i++) {
				try {
					temp[i] = Long.parseLong(values[i]);
				} catch (Exception ignored) {
				}
			}
			return temp;

		}
	}

	public static long[] getLongParameterValues(ServletRequest request, String name, long[] def) throws ParameterNotFoundException {
		try {
			return getLongParameterValues(request, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static long[] getLongParameterValues(ServletRequest request, Pattern splitPattern, String name) throws ParameterNotFoundException {
		String value = getPreStringParameter(request, name);

		if (value == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (value.length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {
			return splitPattern.splitAsStream(value).mapToLong(Long::parseLong).toArray();
		}
	}

	public static long[] getLongParameterValues(ServletRequest request, Pattern splitPattern, String name, long[] def) throws ParameterNotFoundException {
		try {
			return getLongParameterValues(request, splitPattern, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static double getDoubleParameter(ServletRequest request, String name) throws ParameterNotFoundException, NumberFormatException {
		try {
			return Double.parseDouble(getPreStringParameter(request, name).replace(",", ""));
		} catch (NumberFormatException e) {
			throw new NumberFormatException(name + " is incorrect.");
		}
	}

	public static double getDoubleParameter(ServletRequest request, String name, double def) {
		try {
			return getDoubleParameter(request, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static double getDoubleParameter(ServletRequest request, String name, Supplier<Double> ifNull) {
		try {
			return Double.parseDouble(getPreStringParameter(request, name).trim().replace(",", ""));
		} catch (Exception e) {
			Double result = ifNull.get();
			if (result == null) {
				throw e;
			}
			return result;
		}
	}

	public static double getDoubleParameter(ServletRequest request, Function<String, String> preprocessFunction, String name) {
		// 如果有指定前置處理函數，則使用前置處理函數，否則呼叫預設的取代逗號
		Function<String, Double> function = (preprocessFunction == null ? replaceComma : preprocessFunction).andThen(Double::parseDouble);
		return function.apply(getPreStringParameter(request, name));
	}

	public static double[] getDoubleParameterValues(ServletRequest request, String name) throws ParameterNotFoundException {
		String[] values = request.getParameterValues(name);
		if (values == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (values[0].length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {
			double[] temp = new double[values.length];
			for (int i = 0; i < values.length; i++) {
				try {
					temp[i] = Double.parseDouble(values[i]);
				} catch (Exception ignored) {
				}
			}
			return temp;
		}
	}

	public static double[] getDoubleParameterValues(ServletRequest request, String name, double[] def) throws ParameterNotFoundException {
		try {
			return getDoubleParameterValues(request, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static double[] getDoubleParameterValues(ServletRequest request, Pattern splitPattern, String name) throws ParameterNotFoundException {
		String value = getPreStringParameter(request, name);

		if (value == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (value.length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {
			return splitPattern.splitAsStream(value).mapToDouble(Double::parseDouble).toArray();
		}
	}

	public static double[] getDoubleParameterValues(ServletRequest request, Pattern splitPattern, String name, double[] def) throws ParameterNotFoundException {
		try {
			return getDoubleParameterValues(request, splitPattern, name);
		} catch (Exception e) {
			return def;
		}
	}


	public static BigDecimal getBigDecimalParameter(ServletRequest request, String name) throws ParameterNotFoundException, NumberFormatException {
		try {
			return new BigDecimal(getStringParameter(request, MAX_LONG_LENGTH, name).replace(",", ""));
		} catch (NumberFormatException e) {
			throw new NumberFormatException(name + " is incorrect");
		}
	}

	public static BigDecimal getBigDecimalParameter(ServletRequest request, String name, BigDecimal def) {
		try {
			return getBigDecimalParameter(request, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static BigDecimal getBigDecimalParameter(ServletRequest request, String name, Supplier<BigDecimal> ifNull) {
		try {
			return new BigDecimal(getStringParameter(request, MAX_LONG_LENGTH, name).trim().replace(",", ""));
		} catch (Exception e) {
			BigDecimal result = ifNull.get();
			if (result == null) {
				throw e;
			}
			return result;
		}
	}

	public static BigDecimal getBigDecimalParameter(ServletRequest request, Function<String, String> preprocessFunction, String name) {
		// 如果有指定前置處理函數，則使用前置處理函數，否則呼叫預設的取代逗號
		Function<String, BigDecimal> function = (preprocessFunction == null ? replaceComma : preprocessFunction).andThen(BigDecimal::new);
		return function.apply(getStringParameter(request, MAX_LONG_LENGTH, name));
	}


	public static BigDecimal[] getBigDecimalParameterValues(ServletRequest request, String name) throws ParameterNotFoundException {

		String[] values = request.getParameterValues(name);
		if (values == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (values[0].length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {
			BigDecimal[] temp = new BigDecimal[values.length];
			for (int i = 0; i < values.length; i++) {
				try {
					temp[i] = new BigDecimal(values[i]);
				} catch (Exception ignored) {
				}
			}
			return temp;

		}
	}

	public static BigDecimal[] getBigDecimalParameterValues(ServletRequest request, String name, BigDecimal[] def) throws ParameterNotFoundException {
		try {
			return getBigDecimalParameterValues(request, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static BigDecimal[] getBigDecimalParameterValues(ServletRequest request, Pattern splitPattern, String name) throws ParameterNotFoundException {
		String value = getPreStringParameter(request, name);

		if (value == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (value.length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {
			return splitPattern.splitAsStream(value).map(BigDecimal::new).toArray(BigDecimal[]::new);
		}
	}

	public static BigDecimal[] getBigDecimalParameterValues(ServletRequest request, Pattern splitPattern, String name, BigDecimal[] def) throws ParameterNotFoundException {
		try {
			return getBigDecimalParameterValues(request, splitPattern, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static String getStringParameter(ServletRequest request, int maxLength, String name)
		throws ParameterNotFoundException {
		/*
		 * MEMO: workaround for json request
		 */
		if (request instanceof JsonRequest jsonRequest) {
			return jsonRequest.getString(maxLength, name);
		}

		String[] values = request.getParameterValues(name);

		// If getParameterValues returns null, try multipart parsing
		if (values == null && request instanceof HttpServletRequest httpRequest) {
			try {
				Part part = httpRequest.getPart(name);
				if (part != null && part.getSize() > 0) {
					// This is a form field in multipart data, not a file
					try (var inputStream = part.getInputStream()) {
						String value = new String(inputStream.readAllBytes(), "UTF-8").trim();
						if (value.length() == 0) {
							throw new ParameterNotFoundException(name + " was empty");
						}

						String result = value;
						if (result.length() > maxLength) {
							result = result.substring(0, maxLength);
						}

						if (result.length() > MIN_XSS_LENGTH) {
							result = Validator.stripXSS(result);
						}
						return result;
					}
				}
			} catch (ServletException | IOException e) {
				// Fall through to throw ParameterNotFoundException
			}
		}

		if (values == null) {
			throw new ParameterNotFoundException(name + " not found");
		}
		if (values[0].length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		}

		String result = values[0];
		if (result.length() > maxLength) {
			result = result.substring(0, maxLength);
		}

		if (result.length() > MIN_XSS_LENGTH) {
			result = Validator.stripXSS(result);
		}
		return result;
	}

	/**
	 * 僅為private，提供其他類型Parser使用
	 */
	private static String getPreStringParameter(ServletRequest request, String name) throws ParameterNotFoundException {
		String[] values = request.getParameterValues(name);

		// If getParameterValues returns null, try multipart parsing
		if (values == null && request instanceof HttpServletRequest httpRequest) {
			try {
				Part part = httpRequest.getPart(name);
				if (part != null && part.getSize() > 0) {
					// This is a form field in multipart data, not a file
					try (var inputStream = part.getInputStream()) {
						String value = new String(inputStream.readAllBytes(), "UTF-8").trim();
						if (value.length() == 0) {
							throw new ParameterNotFoundException(name + " was empty");
						}

						String result = value;
						if (result.length() > MIN_XSS_LENGTH) {
							result = Validator.stripXSS(result);
						}
						return result;
					}
				}
			} catch (ServletException | IOException e) {
				// Fall through to throw ParameterNotFoundException
			}
		}

		if (values == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (values[0].length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		}

		String result = values[0];
		if (result.length() > MIN_XSS_LENGTH) {
			result = Validator.stripXSS(result);
		}

		return result;
	}

	public static String getStringParameter(ServletRequest request, int maxLength, String name, String def) {
		try {
			return getStringParameter(request, maxLength, name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 *
	 * @param request
	 * @param maxLength 每個參數允許的最大長度
	 * @param name
	 * @return
	 * @throws ParameterNotFoundException
	 */
	public static String[] getStringParameterValues(ServletRequest request, int maxLength, String name) throws ParameterNotFoundException {

		String[] values = request.getParameterValues(name);
		if (values == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (values[0].length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {

			for (int i = 0; i < values.length; i++) {
				if (values[i].length() > maxLength) {
					values[i] = values[i].substring(0, maxLength);
				}

				if (values[i].length() > MIN_XSS_LENGTH) {
					values[i] = Validator.stripXSS(values[i]);
				}
			}

			return values;
		}
	}

	public static String[] getStringParameterValues(ServletRequest request, int maxLength, String name, String[] def) {
		try {
			return getStringParameterValues(request, maxLength, name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 *
	 * @param request
	 * @param splitPattern
	 * @param maxLength 切割後，單一元素的最大長度
	 * @param name
	 * @return
	 * @throws ParameterNotFoundException
	 */
	public static String[] getStringParameterValues(ServletRequest request, Pattern splitPattern, int maxLength, String name) throws ParameterNotFoundException {
		String value = getPreStringParameter(request, name);

		if (value == null) {
			throw new ParameterNotFoundException(name + " not found");
		} else if (value.length() == 0) {
			throw new ParameterNotFoundException(name + " was empty");
		} else {
			return splitPattern.splitAsStream(value)
				.map(s -> (s.length() > maxLength ? s.substring(0, maxLength) : s))
				.toArray(String[]::new);
		}
	}

	/**
	 *
	 * @param request
	 * @param splitPattern
	 * @param maxLength 切割後，單一元素的最大長度
	 * @param name
	 * @param def
	 * @return
	 * @throws ParameterNotFoundException
	 */
	public static String[] getStringParameterValues(ServletRequest request, Pattern splitPattern, int maxLength, String name, String[] def) throws ParameterNotFoundException {
		try {
			return getStringParameterValues(request, splitPattern, maxLength, name);
		} catch (Exception e) {
			return def;
		}
	}


	/** Date已經慢慢被棄置，但為了相容還是暫時保留，可以的話請儘量使用LocalDateTime
	 * @param request
	 * @param pattern
	 * @param name
	 * @param def
	 * @return
	 * @throws ParameterNotFoundException
	 */
	public static Date getDateParameter(ServletRequest request, String pattern, String name, Date def) {
		try {
			return getDateParameter(request, pattern, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static Date getDateParameter(ServletRequest request, String pattern, String name) {
		try {
			String value = getStringParameter(request, 32, name);
			// Java 8的轉換有問題，java 9有修復，所以現在先用舊的方式
			return ThreadLocalUtils.getSimpleDateFormat(pattern).parse(value);
		} catch (ParseException e) {
			throw new DateTimeException(e.getMessage(), e);
		}
	}

	public static Timestamp getTimestampParameter(ServletRequest request, String pattern, String name, Timestamp def) {
		try {
			return getTimestampParameter(request, pattern, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static Timestamp getTimestampParameter(ServletRequest request, String pattern, String name) {
		try {
			String value = getStringParameter(request, 32, name);
			// Java 8的轉換有問題，java 9有修復，所以現在先用舊的方式
			Date date = ThreadLocalUtils.getSimpleDateFormat(pattern).parse(value);
			return new Timestamp(date.getTime());
		} catch (ParseException e) {
			throw new DateTimeException(e.getMessage(), e);
		}
	}

	/**
	 * 時間比較難有預設值，所以當沒有值的時候傳回null
	 *
	 * @param request
	 * @param pattern 不含有時區資料的pattern
	 * @param name
	 * @return
	 * @throws ParameterNotFoundException
	 */
	public static LocalDateTime getLocalDateTimeParameter(ServletRequest request, String pattern, String name,
		LocalDateTime def) {
		try {
			return getLocalDateTimeParameter(request, pattern, name);
		} catch (Exception e) {
			return def;
		}
	}

	public static LocalDateTime getLocalDateTimeParameter(ServletRequest request, String pattern, String name) {
		try {
			String value = getStringParameter(request, 32, name);
			// Java 8的轉換有問題，java 9有修復，所以現在先用舊的方式
			Date date = ThreadLocalUtils.getSimpleDateFormat(pattern).parse(value);
			return DateTimeBuilder.localDateTime(date).toLocalDateTime();
		} catch (ParseException e) {
			throw new DateTimeException(e.getMessage(), e);
		}
	}

	public static <T> T getObjectParameter(HttpServletRequest request, String name, Class<T> clazz, T def) {
		try {
			if (request instanceof JsonRequest jsonRequest) {
				return jsonRequest.getObject(name, clazz, def);
			} else {
				throw new ParameterNotFoundException(name + " not found");
			}
		} catch (Exception e) {
			return def;
		}
	}

	public static <T> List<T> getObjectParameterValues(HttpServletRequest request, String name, Class<T> clazz, List<T> def) {
		try {
			if (request instanceof JsonRequest jsonRequest) {
				return jsonRequest.getObjectList(name, clazz, def);
			} else {
				throw new ParameterNotFoundException(name + " not found");
			}
		} catch (Exception e) {
			return def;
		}
	}

	public static <T> Object[] getObjectParameterValues(HttpServletRequest request, String name, Object[] def) {
		try {
			if (request instanceof JsonRequest jsonRequest) {
				return jsonRequest.getObjectArray(name, def);
			} else {
				throw new ParameterNotFoundException(name + " not found");
			}
		} catch (Exception e) {
			return def;
		}
	}

	public static String getPayLoad(HttpServletRequest request) {
		if (request instanceof JsonRequest jsonRequest) {
			return jsonRequest.getPayLoad();
		} else {
			return null;
		}
	}
}