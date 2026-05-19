package com.nv.commons.utils;

import com.nv.commons.constants.LanguageType;

public class FormatResponseUtils {

	public static String formatbtResponseData(LanguageType languageType) {
		return formatbtResponseData("000000", "SUCCESS", "fs.api.response.success", null, languageType);
	}

	public static String formatbtResponseData(String rawValue, LanguageType languageType) {
		return formatbtResponseData("000000", "SUCCESS", "fs.api.response.success", rawValue, languageType);
	}

	public static String formatbtResponseData(String status, String message, String messageKey, String rawValue,
		LanguageType languageType) {
		return JSONUtils.getJSONString(jGenerator -> {
			jGenerator.writeStringField("status", status);
			jGenerator.writeStringField("message",
				languageType == null ? message : languageType.getLangMessage().get(messageKey));
			jGenerator.writeStringField("messageKey", messageKey);
			if (rawValue != null) {
				jGenerator.writeFieldName("data");
				jGenerator.writeRawValue(rawValue);
			}
		});
	}

	public static String formatbtResponseData(Object data, LanguageType languageType) {
		return formatbtResponseData("000000", "SUCCESS", "fs.api.response.success", data, languageType);
	}

	public static String formatbtResponseData(String status, String message, String messageKey, Object data,
		LanguageType languageType) {
		return JSONUtils.getJSONString(jGenerator -> {
			jGenerator.writeStringField("status", status);
			jGenerator.writeStringField("message",
				languageType == null ? message : languageType.getLangMessage().get(messageKey));
			jGenerator.writeStringField("messageKey", messageKey);
			if (data != null) {
				if (jGenerator.getCodec() == null) {
					jGenerator.setCodec(JSONUtils.getObjectMapper());
				}
				jGenerator.writeFieldName("data");
				jGenerator.writeObject(data);
			}
		});
	}
}
