package com.nv.commons.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

public class StringUtil {

	public static final Pattern COMMA_PATTERN = Pattern.compile(",");
	public static final Pattern DASH_PATTERN = Pattern.compile("-");

	public static final Pattern[] FILEVERSION_PATTERN = new Pattern[] {
		Pattern.compile("&lt;%=FrontendUtils.getJsFileVersion\\(\\)%&gt;"),
		Pattern.compile("<%=FrontendUtils.getJsFileVersion\\(\\)%>")
	};

	private StringUtil() {
		throw new AssertionError();
	}

	/**
	 * 將陣列轉成字串. ex: 陣列:{"1","2","3"}, 間隔符號:逗號(,) => 1,2,3
	 *
	 * @param str  陣列
	 * @param sign 間隔符號
	 * @return
	 */
	public static String toString(String[] str, String sign) {
		if (str == null || sign == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String s : str) {
			sb.append(sign).append(s);
		}
		sb.delete(0, 1);
		return sb.toString();
	}

	/**
	 * 比對是否在字串內. ex: 來源字串:1,2,3,45,26   要比對的字串:4, 間隔符號:逗號(,) => false
	 *
	 * @param sourceStr 來源字串
	 * @param matchStr  要比對的字串
	 * @param sign      間隔符號
	 * @return
	 */
	public static boolean matches(String sourceStr, String matchStr, String sign) {
		if (sourceStr == null || sourceStr.trim().length() == 0) {
			return false;
		}
		String pattern = "(\\A||.*[" + sign + "])" + matchStr + "(\\z||[" + sign + "].*)";
		return sourceStr.trim().matches(pattern);
	}

	public static String reverseChars(String s) {
		s = s.replace("&#39;", "\'");
		s = s.replace("&quot;", "\""); // must call it in first
		s = s.replace("&#96;", "`");
		s = s.replace("&#92;", "\\");
		s = s.replace("&#59;", ";");
		return s;
	}

	/**
	 * 縮短字串度為15+3個.以利顯示
	 *
	 * @param sourceStr 來源字串
	 * @return 縮短後的字串
	 */
	public static String shortString(String sourceStr) {
		return shortString(sourceStr, 15);
	}

	/**
	 * 縮短字串以利顯示
	 *
	 * @param sourceStr 來源字串
	 * @param absLength 縮短後的長度（不包含...）
	 * @return 縮短後的字串
	 */
	public static String shortString(String sourceStr, int absLength) {
		if (sourceStr != null && sourceStr.length() > 0) {
			StringBuilder sb = new StringBuilder();
			int l = 0;
			int i = 0;
			int length = sourceStr.length();
			for (i = 0; i < length; i++) {
				l += countLength(sourceStr.charAt(i));
				if (l > absLength) {
					break;
				}
				sb.append(sourceStr.charAt(i));
			}
			if (i != length) {
				sb.append("...");
			}
			return sb.toString();
		}
		return sourceStr;
	}

	private static int countLength(char c) {
		// 單字元
		if ((int) c < 128) {
			return 1; // or < 256
		}
		// 中文字, 雙字元
		return 2;
	}

	public static String join(String[] list, String conjunction) {
		if (list == null || list.length == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list) {
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}

	public static String join(Collection<?> list, String conjunction) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Object item : list) {
			if (item == null) {
				continue;
			}
			sb.append(item).append(conjunction);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static String join(int[] list, String conjunction) {
		if (list == null || list.length == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int item : list) {
			sb.append(item).append(conjunction);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static int countByteArrayLengthOfString(CharSequence sequence) {
		final int len = sequence.length();
		int count = len;
		for (int i = 0; i < len; i++) {
			char ch = sequence.charAt(i);
			if (ch <= 0x7F) {
				// count++;
			} else if (ch <= 0x7FF) {
				count += 1;
			} else if (ch >= 0xD800 && ch <= 0xDBFF) {
				count += 2;
				++i;
			} else {
				count += 2;
			}
		}
		return count;
	}

	public static String firstCharUpper(String str) {
		if (str == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		str = str.toLowerCase();
		sb.append(Character.toUpperCase(str.charAt(0)));
		sb.append(str.substring(1));
		return sb.toString();
	}

	public static boolean isEmpty(String str) {

		if (str == null || str.length() == 0) {
			return true;
		}

		for (int index = 0; index < str.length(); index++) {

			if (!Character.isWhitespace(str.charAt(index))) {
				// has char not whitespace, so str is not empty
				return false;
			}
		}
		// all char are whitespace, so str is empty
		return true;
	}

	public static boolean isEmpty(String[] str) {
		return (str == null || str.length == 0);
	}

	/**
	 * URL Params Str to map Object
	 * A=1&B=2&C=3 to {A=1, B=2, C=3}
	 *
	 * @param str
	 * @return map
	 */
	public static Map<String, String> getUrlParamsToMap(String str) {
		return getUrlParamsToMap(str, null);
	}

	/**
	 * URL Params Str to map Object
	 * A=1separatorB=2separatorC=3 to {A=1, B=2, C=3}
	 *
	 * @param str
	 * @param separator
	 * @return map
	 */
	public static Map<String, String> getUrlParamsToMap(String str, String separator) {
		Map<String, String> map = new HashMap<>();
		if (StringUtil.isEmpty(str)) {
			return map;
		}
		if (separator == null) {
			separator = "&";
		}
		StringTokenizer st = new StringTokenizer(str, separator);

		while (st.hasMoreElements()) {
			String actualElement = st.nextToken();
			StringTokenizer et = new StringTokenizer(actualElement, "=");
			if (et.countTokens() != 2) {
				// 因 url params 若key的value為空值，et.countTokens()會只有1，故這邊給予""值。
				map.put(et.nextToken(), "");
				continue;
			}
			String key = et.nextToken();
			String value = et.nextToken();
			map.put(key, value);
		}
		return map;
	}

	public static Map<String, String> getUrlParamsToMap(String str, String separator, String subSeparator) {
		Map<String, String> map = new HashMap<>();
		if (StringUtil.isEmpty(str)) {
			return map;
		}
		if (separator == null) {
			separator = "&";
		}
		if (subSeparator == null) {
			subSeparator = "=";
		}
		StringTokenizer st = new StringTokenizer(str, separator);

		while (st.hasMoreElements()) {
			String actualElement = st.nextToken();
			//a=b=k==
			//url params 若key的value包含多個 '='
			String key = actualElement.substring(0, actualElement.indexOf("="));
			String value = actualElement.substring(actualElement.indexOf("=") + 1);
			map.put(key, value);
		}
		return map;
	}

	public static List<String> splitText(String text, int size) {
		List<String> textList = new ArrayList<>();

		for (int begin = 0; begin < text.length(); begin += size) {
			int end = Math.min(text.length(), begin + size);

			textList.add(text.substring(begin, end));
		}
		return textList;
	}

	public static String generateCode(int length) {
		Supplier<Object> simpleRandomUnit = () -> {
			int type = new Random().nextInt(3);
			if (0 == type) {
				return new Random().nextInt(10);
			}
			if (1 == type) {
				return (char) (new Random().nextInt(26) + 65);
			}
			return (char) (new Random().nextInt(26) + 97);
		};

		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(simpleRandomUnit.get());
		}
		return sb.toString();
	}

	public static String escapeHtml(String content) {
		if (content == null) {
			return null;
		}

		content = Validator.stripXSS(content);

		content = content.replaceAll("\r\n", "<br>");

		content = StringEscapeUtils.escapeEcmaScript(content);

		return content;
	}

	public static String escapeHtmlContent(String content) {
		if (content == null) {
			return null;
		}

		content = content.replaceAll("\r\n", "<br>");

		content = StringEscapeUtils.escapeHtml4(content);

		//		content = escapeHTML(content);

		return content;
	}

	public static String formatMessageTemplate(String message, String... args) {
		try {

			if (args == null || args.length == 0) {
				return message;
			}

			for (int i = 0; i < args.length; i++) {
				String replaceFrom = "_\\{" + i + "\\}_";
				String replaceFromI18N = "\\{" + i + "\\}";
				String replaceTo = Matcher.quoteReplacement(args[i]);

				message = message.replaceAll(replaceFrom, replaceTo).replaceAll(replaceFromI18N, replaceTo);
			}

			return message;
		} catch (Exception e) {
			return message;
		}
	}

	public static StringBuilder buildParamsString(Map<String, String> params, boolean isAlphabetical,
		String separator) {

		if (isAlphabetical && !(params instanceof TreeMap)) {
			params = new TreeMap<>(params);
		}

		StringBuilder plainText = new StringBuilder();

		params.forEach((key, value) -> {
			if (plainText.length() > 0) {
				plainText.append(separator);
			}
			plainText.append(key).append("=").append(value);
		});

		return plainText;
	}

	public static String mask(String text, int start, int length, char maskSymbol) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		if (start < 0) {
			start = 0;
		}
		if (length < 1) {
			return text;
		}

		StringBuilder sb = new StringBuilder();
		char[] cc = text.toCharArray();
		for (int i = 0; i < cc.length; i++) {
			if (i >= start && i < (start + length)) {
				sb.append(maskSymbol);
			} else {
				sb.append(cc[i]);
			}
		}
		return sb.toString();
	}

	/**
	 * 將字串去除http://與https://，末端去除/，頭尾去除空白，並轉為小寫
	 *
	 * @param target 目標字串
	 * @return 結果字串
	 */
	public static String formatAsDomain(String target) {
		return target.replaceAll("^https?://|\\s+$|/$", "").toLowerCase();
	}
}
