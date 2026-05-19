package com.nv.commons.utils;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReferenceDataUtils {

	public static String parseDataWithSemicolon(String text) {
		if (StringUtils.isEmpty(text)) {
			return "";
		}
		try {
			JSONObject jsonObject = new JSONObject(text);
			Iterator<String> keys = jsonObject.keys();
			return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(keys, Spliterator.ORDERED),
				false).map(key -> {
				try {
					if (jsonObject.get(key) instanceof JSONArray) {
						JSONArray jsonArray = (JSONArray) jsonObject.get(key);
						StringBuilder subStr = new StringBuilder("[");
						for (int count = 0; count < jsonArray.length(); count++) {
							subStr.append("{");
							subStr.append(parseDataWithSemicolon(jsonArray.getString(count)));
							subStr.append("}; ");
						}
						return key + "=" + subStr.delete(subStr.lastIndexOf("; "), subStr.length()).append("]");
					}
					return key + "=" + jsonObject.get(key);
				} catch (JSONException e) {
					LogUtils.SYS.error(e.getMessage(), e);
				}
				return "";
			}).collect(Collectors.joining("; "));

		} catch (JSONException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return "";
	}
}
