package com.nv.commons.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nv.commons.constants.CallingCodeType;
import com.nv.commons.constants.SystemConstants;

public class Validator {

	private static final Map<String, Pattern> patterns = new HashMap<>();

	private static final Pattern ipv4Pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
	private static final Pattern jsonPatterns = Pattern.compile("\"\\{(.*?)\\}\"");
	private static final Pattern[] xssPatterns = new Pattern[] {
		// Script fragments
		Pattern.compile("&lt;script&gt;(.*?)&lt;/script&gt;", Pattern.CASE_INSENSITIVE),
		Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
		// src='...'
		Pattern.compile("src[\r\n]*=[\r\n]*\\'(.*?)\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		// lonely script tags
		Pattern.compile("&lt;/script&gt;", Pattern.CASE_INSENSITIVE),
		Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
		Pattern.compile("&lt;script(.*?)&gt;", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		// eval(...)
		Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		// expression(...)
		Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		// javascript:...
		Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
		// vbscript:...
		Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
		// onload(...)=...
		Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
		// onload(...)=...
		Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
	};

	// contributed by Scott Gonzalez: http://projects.scottsplayground.com/email_address_validation/
	private static final Pattern emailPattern = Pattern.compile("^((([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+(\\.([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/="
																+ "\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+)*)|((\\x22)((((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|\\x21|[\\x23-\\x5b]|["
																+ "\\x5d-\\x7e]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\uFFEF])|(\\\\([\\x01-\\x09\\x0b\\x0c\\x0d-\\x7f]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]))))*(((\\x20|\\x09)*(\\x0d\\x0a))?("
																+ "\\x20|\\x09)+)?(\\x22)))@((([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF"
																+ "\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.)+(([a-z]|[\\u00A0-\\uD7FF\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|[\\u00A0-\\uD7FF\\uF900-"
																+ "\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))$");

	/*
	 * 各國國碼及手機格式
	 * ref: https://robarter.pixnet.net/blog/post/222839599-%E5%90%84%E5%9C%8B%E5%9C%8B%E7%A2%BC%E5%8F%8A%E6%89%8B%E6%A9%9F%E6%A0%BC%E5%BC%8F
	 */
	//	Phone:  (CNY:11 digits; SGD:8 digits; MYR:11 digits; VND: 10 digits)
	//	private static final Pattern simpleCellPhonePattern = Pattern.compile("^\\d{8,11}$");
	// MEMO: CurrencyType -> CountryType, assume 只有 一對一 的對應
	private static final Pattern simpleCNCellPhonePattern = Pattern.compile("^[1]\\d{10}$");
	private static final Pattern simpleSGCellPhonePattern = Pattern.compile("^[89]\\d{7}$");
	private static final Pattern simpleMYCellPhonePattern = Pattern.compile("^0?[1-9]\\d{8,9}$");
	private static final Pattern simpleVNCellPhonePattern = Pattern.compile("^\\d{9,10}$");
	private static final Pattern simpleKRCellPhonePattern = Pattern.compile("^\\d{10,11}$");
	private static final Pattern simpleTHCellPhonePattern = Pattern.compile("^\\d{9}$");
	// IDR : 8 或 08 開頭 長度 : 8 ~ 15
	private static final Pattern simpleIDCellPhonePattern = Pattern.compile("^0?[8]\\d{7,13}$");
	private static final Pattern simpleINCellPhonePattern = Pattern.compile("^\\d{10}$");
	private static final Pattern simpleBDCellPhonePattern = Pattern.compile("^\\d{10,11}$");
	private static final Pattern simplePHCellPhonePattern = Pattern.compile("^[9]\\d{9}$");
	private static final Pattern simplePKCellPhonePattern = Pattern.compile("^\\d{10}$");
	private static final Pattern simpleMXCellPhonePattern = Pattern.compile("^\\d{10}$");
	private static final Pattern simpleUSCellPhonePattern = Pattern.compile("^\\d{10}$");
	private static final Pattern simpleKHCellPhonePattern = Pattern.compile("^\\d{8,9}$");
	private static final Pattern simpleBRCellPhonePattern = Pattern.compile("^\\d{10,11}$");
	private static final Pattern simpleNGCellPhonePattern = Pattern.compile("^\\d{10}$");
	private static final Pattern simpleZACellPhonePattern = Pattern.compile("^\\d{9}$");
	private static final Pattern simpleGHCellPhonePattern = Pattern.compile("^\\d{9}$");
	//	private static final Pattern simpleLKCellPhonePattern = Pattern.compile("^\\d{10}$");
	private static final Pattern simpleLKCellPhonePattern = Pattern.compile("^\\d{9,10}$");
	private static final Pattern simpleNPCellPhonePattern = Pattern.compile("^\\d{10}$");
	private static final Pattern simpleAUCellPhonePattern = Pattern.compile("^\\d{9}$");
	private static final Pattern simpleNZCellPhonePattern = Pattern.compile("^\\d{8,10}$");
	private static final Pattern simpleAFGCellPhonePattern = Pattern.compile("^\\d{9,10}$");
	private static final Pattern simpleBTCellPhonePattern = Pattern.compile("^\\d{8,9}$");
	private static final Pattern simpleMVCellPhonePattern = Pattern.compile("\\d{7}$");
	private static final Pattern simpleIRCellPhonePattern = Pattern.compile("^\\d{10}$");
	private static final Pattern simpleCACellPhonePattern = Pattern.compile("^\\d{10}$");
	private static final Pattern simpleHKCellPhonePattern = Pattern.compile("^[569]\\d{8}$");
	private static final Pattern simpleMTCellPhonePattern = Pattern.compile("^(77|79|96|98|99)\\d{6}$");
	private static final Pattern simpleUKCellPhonePattern = Pattern.compile( "^(?:0|\\+44)\\d{10}$");
	private static final Pattern generalPhonePattern = Pattern.compile("^\\d{10}$");

	//	private static final Pattern datePat = Pattern.compile("(0[1-9]|[12][0-9]|3[01])[-](0[1-9]|1[012])[-](19|20)[0-9]{2}");
	private static final Pattern datePattern_YYYYMMDD = Pattern.compile("^(?:19|20)[0-9]{2}/(?:0[1-9]|1[012])/(?:0[1-9]|[12][0-9]|3[01])$"); // yyyy/MM/dd
	private static final Pattern datePattern_DDMMYYYY = Pattern.compile("^(?:0[1-9]|[12][0-9]|3[01])/(?:0[1-9]|1[012])/(?:19|20)[0-9]{2}$"); // dd/MM/yyyy

//	private static final String userIdValidChar = "[0-9a-z]";
	private static final String managerIdValidChar = "[0-9a-z]";
//	private static final String operatorIdValidChar = "[0-9a-z]";
//	private static final String affiliateIdValidChar = "[0-9a-z]";

	//	UserId: 長度6 - 15 英數混合，不允許特殊字元, 只能是小寫英文字母
	//	private static final Pattern userIdPattern = Pattern.compile("^[1-9a-z][0-9a-z]{5,11}$");
	//	private static final Pattern userIdPattern = Pattern.compile("^" + userIdValidChar + "{4,15}$");
	// MEMO: 根据需求 UserId == Email
	private static final Pattern userIdPattern = emailPattern;
	//	ManagerId: 長度6 - 12 英數混合，不允許特殊字元, 只能是小寫英文字母
	private static final Pattern managerIdPattern = Pattern.compile("^" + managerIdValidChar + "{6,12}$");

//	private static final Pattern operatorIdPattern = Pattern.compile("^" + operatorIdValidChar + "{6,12}$");

//	private static final Pattern affiliateIdPattern = Pattern.compile("^" + affiliateIdValidChar + "{6,50}$");

	//	private static final Pattern partOfUserIdPattern = Pattern.compile("^[1-9a-z][0-9a-z]{1,11}$");
//	private static final Pattern partOfUserIdPattern = Pattern.compile("^" + userIdValidChar + "{1,15}$");

	private static final Pattern playerEasyPasswordPattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*#]{6,20}$"); // for isEasyPasswordValid
	private static final Pattern playerStrictPasswordPattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#])[a-zA-Z\\d@$!%*#]{6,20}$");

	//	Bank Account Number: Digits only, 8-34 digits
	private static final Pattern bankAccountNumberPattern = Pattern.compile("^\\d{8,34}$");

	private static final Pattern imageFormatPattern = Pattern.compile("(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|JPEG)$");

	private static final Pattern numPat = Pattern.compile("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
	//	private static final Pattern numPat = Pattern.compile("^\\d+$");

	private static final Pattern posDoublePat = Pattern.compile("^\\d+(\\.\\d+)?$");
	//	private static final Pattern validRacePat = Pattern.compile("^[1-9]?[0-9]{1,1}$");

	// "{}" -> {}
//	private static final Pattern jsonPatterns = Pattern.compile("\"\\{(.*?)\\}\"");

	private static final Pattern moneyAmountPattern = Pattern.compile("^\\d{1,20}(\\.\\d{1,2})?$");

	private static final Pattern MM_YY = Pattern.compile("^(0[1-9]|1[0-2])/([0-9]{2})$");

	/**
	 *
	 */
	public static boolean isValidatedIP(String value) {
		if (isEmpty(value)) {
			return false;
		}
		if (value.length() > SystemConstants.IP_VALID_MAX_LENGTH) {
			value = value.substring(0, SystemConstants.IP_VALID_MAX_LENGTH);
		}
		return ipv4Pattern.matcher(value).matches();
	}

	public static boolean isEmpty(String value) {
		return value == null || value.trim().isEmpty();
	}

	// 防止XSS攻擊
	public static String stripXSS(String value) {
		if (value != null) {
			value = value.replaceAll("\0|%22|%27|%3c|%3e|%26", "");

			//            value = value.replaceAll("\0|\"|'|<|>|&|%22|%27|%3c|%3e|%26","");

			for (Pattern scriptPattern : xssPatterns) {
				value = scriptPattern.matcher(value).replaceAll("");
			}
		}
		return value;
	}


	public static boolean isMatched(Pattern pattern, String value) {
		return pattern.matcher(value).matches();
	}

	public static boolean isValidatedEmail(String value) {
		if (isEmpty(value)) {
			return false;
		}
		return isMatched(emailPattern, value);
	}


	public static boolean isValidatedCellPhone(String value, CallingCodeType type) {
		if (isEmpty(value)) {
			return false;
		}

		return switch (type) {
			case China -> simpleCNCellPhonePattern.matcher(value).matches();
			case Singapore -> simpleSGCellPhonePattern.matcher(value).matches();
			case Malaysia -> simpleMYCellPhonePattern.matcher(value).matches();
			case Vietnam -> simpleVNCellPhonePattern.matcher(value).matches();
			case SouthKorea -> simpleKRCellPhonePattern.matcher(value).matches();
			case Thailand -> simpleTHCellPhonePattern.matcher(value).matches();
			case Indonesia -> simpleIDCellPhonePattern.matcher(value).matches();
			case India -> simpleINCellPhonePattern.matcher(value).matches();
			case Bangladesh -> simpleBDCellPhonePattern.matcher(value).matches();
			case Philippines -> simplePHCellPhonePattern.matcher(value).matches();
			case Pakistan -> simplePKCellPhonePattern.matcher(value).matches();
			case Mexico -> simpleMXCellPhonePattern.matcher(value).matches();
			case US -> simpleUSCellPhonePattern.matcher(value).matches();
			case Cambodia -> simpleKHCellPhonePattern.matcher(value).matches();
			case Brazil -> simpleBRCellPhonePattern.matcher(value).matches();
			case Nigeria -> simpleNGCellPhonePattern.matcher(value).matches();
			case ZA -> simpleZACellPhonePattern.matcher(value).matches();
			case GH -> simpleGHCellPhonePattern.matcher(value).matches();
			case SriLanka -> simpleLKCellPhonePattern.matcher(value).matches();
			case Nepal -> simpleNPCellPhonePattern.matcher(value).matches();
			case Australia -> simpleAUCellPhonePattern.matcher(value).matches();
			case NewZealand -> simpleNZCellPhonePattern.matcher(value).matches();
			case Afghanistan -> simpleAFGCellPhonePattern.matcher(value).matches();
			case Bhutan -> simpleBTCellPhonePattern.matcher(value).matches();
			case Maldives -> simpleMVCellPhonePattern.matcher(value).matches();
			case Iran -> simpleIRCellPhonePattern.matcher(value).matches();
			case Canada -> simpleCACellPhonePattern.matcher(value).matches();
			case HongKong -> simpleHKCellPhonePattern.matcher(value).matches();
			case Malta -> simpleMTCellPhonePattern.matcher(value).matches();
			case UK -> simpleUKCellPhonePattern.matcher(value).matches();
			default -> generalPhonePattern.matcher(value).matches();
		};
	}

	public static boolean isValidatedDate(String value) {
		if (isEmpty(value)) {
			return false;
		}
		Matcher matcher1 = datePattern_YYYYMMDD.matcher(value);
		Matcher matcher2 = datePattern_DDMMYYYY.matcher(value);
		return matcher1.matches() || matcher2.matches();
	}

	public static boolean isAgeEqualOrGreaterThan(int age, String dateStr, String pattern) {
		if (dateStr == null || dateStr.isEmpty()) {
			return false;
		}
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			LocalDate birthday = LocalDate.parse(dateStr, formatter);
			LocalDate today = LocalDate.now();
			Period period = Period.between(birthday, today);
			return period.getYears() >= age;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	public static boolean isValidatedUserId(String value) {
		if (isEmpty(value)) {
			return false;
		}
		return userIdPattern.matcher(value).matches();
	}

	public static boolean isValidatedPlayerLoginPassword(String value) {
		if (isEmpty(value)) {
			return false;
		}
		return isMatched(playerEasyPasswordPattern, value);
	}

	public static boolean isValidatedPlayerStrictPassword(String value) {
		if (isEmpty(value)) {
			return false;
		}

		return isMatched(playerStrictPasswordPattern, value);
	}

	public static boolean isValidatedBankAccNumber(String value) {
		if (isEmpty(value)) {
			return false;
		}
		return bankAccountNumberPattern.matcher(value).matches();
	}

	public static boolean isValidateImageFormat(String value) {
		if (isEmpty(value)) {
			return false;
		}
		return imageFormatPattern.matcher(value).matches();
	}

	public static boolean isNumeric(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}

		return numPat.matcher(value).matches();
	}

	public static boolean isPositiveDouble(String value) {
		if (isEmpty(value)) {
			return false;
		}
		return isMatched(posDoublePat, value) && !(Double.valueOf(value) <= 0.0);
	}

	public static boolean isValidatedMoneyAmount(String value) {
		if (isEmpty(value)) {
			return false;
		}
		return moneyAmountPattern.matcher(value).matches();
	}

	/**
	 * 驗證長度
	 *
	 * @param value
	 * @param precision
	 * @return
	 */
	public static boolean verifyLength(BigDecimal value, Integer precision) {

		return precision != null && value.precision() > precision;
	}

	public static boolean isValidatedManagerId(String value) {
		if (isEmpty(value)) {
			return false;
		}
		return managerIdPattern.matcher(value).matches();
	}

	public static String stripJson(String value) {

		StringBuilder correctValue = new StringBuilder();

		Matcher matcher = jsonPatterns.matcher(value);
		while (matcher.find()) {
			String errorData = matcher.group(0);
			int errorStart = value.indexOf(errorData);
			correctValue.append(value, 0, errorStart);

			int errorLength = errorData.length();

			errorData = errorData.replaceAll("(?:\\\\+|\\/+)", "");
			errorData = errorData.replaceAll("^\\\"|\\\"$", "");

			Map<String, String> sub = JSONUtils.jsonToMap(errorData, String.class, String.class);

			if (sub == null) {
				correctValue.append("\"\""); // 解析不出來的暫給空字串
			} else {
				correctValue.append("\"" + ReferenceDataUtils.parseDataWithSemicolon(errorData) + "\"");
			}
			//			correctValue.append(errorData);
			//			correctValue.append(errorData.replaceAll("^\"|\"$", ""));
			correctValue.append(value.substring(errorStart + errorLength));
		}
		if (correctValue.isEmpty()) {
			correctValue.append(value);
		}
		return correctValue.toString();
	}

	public static boolean isValidateExpiryDate(String mmYY) {
		if (isEmpty(mmYY)) {
			return false;
		}

		var m = MM_YY.matcher(mmYY);
		if (!m.matches()) {
			return false;
		}

		int month = Integer.parseInt(m.group(1));
		int year = 2000 + Integer.parseInt(m.group(2));

		YearMonth now = YearMonth.now();
		YearMonth exp = YearMonth.of(year, month);

		return !exp.isBefore(now);
	}

	/**
	 * 驗證長度且不得為0
	 *
	 * @param value
	 * @param precision
	 * @return
	 */
	public static boolean verifyLengthAndEqualToZero(BigDecimal value, Integer precision) {

		if (verifyLength(value, precision)) {
			// true means not valid
			return true;
		}

		return value.signum() == 0;
	}

}
