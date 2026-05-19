package com.nv.commons.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;

public class JSONUtils {

	private static final ObjectMapper mapper;
	private static final JsonFactory jsonFactory = new JsonFactory();

	public static final String EMPTY_JSON_STRING = "{}";
	public static final String EMPTY_JSON_ARRAY_STRING = "[]";
	public static final String STATUS_200_OK = "{\"status\":200,\"message\":\"OK\"}";
	public static final String STATUS_500 = "{\"status\":500}";

	public static final String emptyInboxJson = "{\"unreadMessageCount\":0,\"inboxMessages\":[],\"lastUpdateTime\":0,\"totalMessages\":0}";

	public final static String BASE_FILTER = "BaseFilter";

	static {
		mapper = JsonMapper.builder(jsonFactory)
			.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
			.build();
		mapper.registerModule(new JavaTimeModule());

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		mapper.configure(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM, false);

		// memo: https://fasterxml.github.io/jackson-databind/javadoc/2.9/com/fasterxml/jackson/databind/SerializationFeature.html#WRITE_NULL_MAP_VALUES
		// WRITE_NULL_MAP_VALUES Deprecated. Since 2.9
		mapper.configOverride(Map.class)
			.setInclude(
				JsonInclude.Value.construct(
					JsonInclude.Include.NON_NULL,
					JsonInclude.Include.NON_NULL));

		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}

	public static JsonFactory getFactory() {
		return jsonFactory;
	}

	public static ObjectMapper getObjectMapper() {
		return mapper;
	}

//	/**
//	 * map object to json string with include/exclude fields, object needs to
//	 * declared @JsonFilter("BaseFilter")
//	 *
//	 * @param instance target object
//	 * @param include  determine include fields or exclude fields
//	 * @param fields   fields include/exclude to serialize
//	 * @return
//	 * @throws JsonProcessingException
//	 */
//	public static String toJsonWithFields(Object instance, boolean include, String... fields)
//		throws JsonProcessingException {
//		SimpleBeanPropertyFilter filter = include
//			? SimpleBeanPropertyFilter.filterOutAllExcept(fields)
//			: SimpleBeanPropertyFilter.serializeAllExcept(fields);
//		FilterProvider provider = new SimpleFilterProvider().addFilter(JSONUtils.BASE_FILTER, filter);
//		return mapper.writer(provider).writeValueAsString(instance);
//	}

	/**
	 * create json String
	 *
	 * @param args key, value....
	 * @return
	 */
	public static String getJSONString(String... args) {
		if (args.length % 2 != 0) {
			throw new RuntimeException("error number of argements");
		}
		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = jsonFactory.createGenerator(out);

			jGenerator.writeStartObject();
			for (int i = 0; i < args.length; i = i + 2) {
				jGenerator.writeStringField(args[i], args[i + 1]);
			}
			jGenerator.writeEndObject();
		} catch (IOException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	public static String getJSONString(JsonGenerateProcessor processor) {

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = jsonFactory.createGenerator(out);

			jGenerator.writeStartObject();
			processor.process(jGenerator);
			jGenerator.writeEndObject();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	/*
	 * produce {"isAgent":"true","status":"200"}
	 */
	public static String getJSONString(Map<String, ?> args) {
		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = jsonFactory.createGenerator(out);

			jGenerator.writeStartObject();
			for (Entry<String, ?> entry : args.entrySet()) {
				jGenerator.writeStringField(entry.getKey(), String.valueOf(entry.getValue()));
			}
			jGenerator.writeEndObject();
		} catch (IOException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	public static <T> T jsonToObject(String jsonStr, Class<T> requiredType) {
		return jsonToObject(jsonStr.getBytes(StandardCharsets.UTF_8), requiredType);
	}

	public static <T> T jsonToObject(byte[] jsonBytes, Class<T> requiredType) {
		JsonParser jp = null;
		try {
			jp = jsonFactory.createParser(jsonBytes);
			return mapper.readValue(jp, requiredType);
		} catch (IOException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			JSONUtils.close(jp);
		}
		return null;
	}

	public static <K, V> Map<K, V> jsonToMap(String jsonStr, Class<K> keyType, Class<V> valueType) {

		JsonParser jp = null;
		try {
			byte[] jsonBytes = jsonStr.getBytes(StandardCharsets.UTF_8);
			jp = jsonFactory.createParser(jsonBytes);
			return mapper.readValue(jp, mapper.getTypeFactory().constructMapType(Map.class, keyType, valueType));
		} catch (IOException e) {
			LogUtils.SYS.error(jsonStr);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			JSONUtils.close(jp);
		}
		return null;
	}

	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, allowing JSON text
	 * to be delivered in HTML. In JSON text, a string cannot contain a control
	 * character or an unescaped quote or backslash.
	 *
	 * @param string A String
	 * @return A String correctly formatted for insertion in a JSON text.
	 */
	public static String quote(String string) {
		if (string == null || string.length() == 0) {
			return "\"\"";
		}

		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuilder sb = new StringBuilder(len + 4);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
				case '\\':
				case '"':
					sb.append('\\');
					sb.append(c);
					break;
				case '/':
					if (b == '<') {
						sb.append('\\');
					}
					sb.append(c);
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
						t = "000" + Integer.toHexString(c);
						sb.append("\\u").append(t.substring(t.length() - 4));
					} else {
						sb.append(c);
					}
			}
		}
		sb.append('"');
		return sb.toString();
	}

	public static JsonNode toJsonNode(String json) {
		try {
			return JSONUtils.getObjectMapper().readValue(json, JsonNode.class);
		} catch (IOException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public static <T> T readValue(String content, JavaType valueType) throws IOException {
		return mapper.readValue(content, valueType);
	}

	public static <T> T readValue(String content, Class<T> clazz) throws IOException {

//		TypeFactory t = TypeFactory.defaultInstance();
//		return mapper.readValue(content, t.uncheckedSimpleType(clazz));
		return mapper.readValue(content, clazz);
	}

	/*
	 *
	 */
	public static <T> List<T> parseJsonToObjectList(String jsonInput, Class<T> clazz) throws IOException {

		try {
			TypeFactory t = TypeFactory.defaultInstance();

			if (clazz == null) {
				clazz = (Class<T>) Map.class;
			}

			return JSONUtils.readValue(jsonInput, t.constructCollectionType(ArrayList.class, clazz));

		} catch (IOException e) {
			LogUtils.SYS.error("jsonInput: {} ", jsonInput);
			LogUtils.SYS.error(e.getMessage(), e);
			// throw e;
		}

		return null;
	}

	public static String writeValueAsString(Object object) throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}

	public static String getJSONArrayString(Collection<?> list) throws JsonProcessingException {
		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = jsonFactory.createGenerator(out);
			jGenerator.writeStartArray();
			for (Object obj : list) {
				jGenerator.writeString(writeValueAsString(obj));
			}
			jGenerator.writeEndArray();
		} catch (IOException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	public static void close(JsonGenerator jGenerator) {
		if (null != jGenerator) {
			try {
				jGenerator.close();
			} catch (IOException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
	}

	public static void close(JsonParser jp) {
		if (jp != null) {
			try {
				jp.close();
			} catch (IOException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
	}

	public static String toJsonString(Object instance) {
		if (null == instance) {
			return null;
		}
		byte[] byteResult = toJsonBytes(instance);
		if (null == byteResult) {
			return null;
		}
		return new String(byteResult, StandardCharsets.UTF_8);
	}

	public static byte[] toJsonBytes(Object instance) {
		try {
			return mapper.writeValueAsBytes(instance);
		} catch (JsonProcessingException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

//	public static <T, M> String toJsonStringWithView(Class<T> view, M model) {
//		try {
//			return mapper.writerWithView(view).writeValueAsString(model);
//		} catch (JsonProcessingException e) {
//			LogUtils.SYS.error(e.getMessage(), e);
//		}
//		return null;
//	}

	/*
	 *
	 */
//	public static String getBankInfoByStatus(String json) {
//		try {
//			Map<String, Object> map = JSONUtils.jsonToMap(json, String.class, Object.class);
//
//			String status = String.valueOf(map.get("status"));
//
//			if ("-1".equals(status)) {
//				return null;
//			}
//
//			Map<String, String> data = (Map<String, String>) map.get("data");
//
//			return data.get(status);
//
//		} catch (Exception e) {
//			LogUtils.SYS.error("json: " + json);
//			LogUtils.SYS.error(e.getMessage(), e);
//		}
//		return null;
//	}

	public static void checkNumberAndSet(JsonGenerator jGenerator, String key, Object object) throws IOException {

		String ObjectTypeStr = object instanceof Integer ?
			"Integer" :
			object instanceof Double ?
				"Double" :
				object instanceof Float ?
					"Float" :
					object instanceof BigDecimal ? "BigDecimal" : object instanceof Long ? "Long" : "NotNumber";
		switch (ObjectTypeStr) {
			case "Integer":
				jGenerator.writeNumberField(key, (Integer) object);
				break;
			case "Double":
				jGenerator.writeNumberField(key, (Double) object);
				break;
			case "Float":
				jGenerator.writeNumberField(key, (Float) object);
				break;
			case "BigDecimal":
				jGenerator.writeNumberField(key, (BigDecimal) object);
				break;
			case "Long":
				jGenerator.writeNumberField(key, (Long) object);
				break;
			default:
				jGenerator.writeObjectField(key, object);
				break;
		}
	}

	private static JsonNode tryParseJson(String json) {
		try {
			return JSONUtils.toJsonNode(json);
		} catch (Exception e) {
			return null;
		}
	}

	public static String formatJsonToKeyValueLines(String json) {

		JsonNode node = JSONUtils.tryParseJson(json);

		if (node == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		node.fieldNames().forEachRemaining(field -> {
			JsonNode valueNode = node.get(field);
			String valueText = valueNode.isNull() || valueNode.asText().isEmpty() ? "-" : valueNode.asText();
			sb.append(field)
				.append(" : ")
				.append(valueText)
				.append("<br/>");
		});
		return sb.toString();
	}

	public static <T> T requestBodyToObj(HttpServletRequest request, Class<T> clazz) throws IOException {
		//取得request body, 用Json轉為物件
		String body = request.getReader().lines()
			.reduce("", (accumulator, actual) -> accumulator + actual);
		return jsonToObject(body, clazz);
	}

}
