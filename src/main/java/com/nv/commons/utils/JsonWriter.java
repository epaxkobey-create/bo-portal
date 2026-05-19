package com.nv.commons.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.WritableTypeId;

/**
 * 強化JsonGenerator建立json的過程
 * 1. 改善JsonGenerator的排版，利用縮排讓閱讀更直觀
 * 2. 使用attr統一欄位的輸出方式
 * 3. 避免撰寫 "{", "}", "[", "]" 等輸出
 *
 * @author Alan 2024/12/17
 */
public class JsonWriter {



	@FunctionalInterface
	public interface DateProcessor {
		void process() throws IOException;
	}

	@FunctionalInterface
	public interface InitDateProcessor {
		void process(JsonWriter jsonWriter) throws IOException;
	}

	private JsonGenerator generator;

	private DateProcessor initProcessor;

	private JsonWriter() {
	}

	public static JsonWriter build() {
		return new JsonWriter();
	}

	private void run(DateProcessor processor)  {
		try {
			processor.process();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public JsonWriter object(String field, DateProcessor processor)  {
		run(() -> {
			this.generator.writeObjectFieldStart(field);
			processor.process();
			this.generator.writeEndObject();
		});
		return this;
	}

	public JsonWriter object(InitDateProcessor processor)  {
		this.initProcessor = () -> {
			generator.writeStartObject();
			processor.process(this);
			generator.writeEndObject();
		};
		return this;
	}

	public JsonWriter object(DateProcessor processor)  {
		run(() -> {
			this.generator.writeStartObject();
			processor.process();
			this.generator.writeEndObject();
		});
		return this;
	}

	public JsonWriter objects(DateProcessor... processors)  {

		for(DateProcessor processor : processors) {
			run(() -> {
				this.generator.writeStartObject();
				processor.process();
				this.generator.writeEndObject();
			});
		}

		return this;
	}

	public JsonWriter array(String field, DateProcessor processor)  {
		run(() -> {
			this.generator.writeArrayFieldStart(field);
			processor.process();
			this.generator.writeEndArray();
		});
		return this;
	}

	public JsonWriter array(DateProcessor processor)  {
		run(() -> {
			this.generator.writeStartArray();
			processor.process();
			this.generator.writeEndArray();
		});
		return this;
	}

	public JsonWriter array(InitDateProcessor processor)  {
		this.initProcessor = () -> {
			JsonGenerator generator = this.generator;
			generator.writeStartArray();
			processor.process(this);
			generator.writeEndArray();
		};
		return this;
	}

	public void attr(String field, String value) throws IOException {
		generator.writeStringField(field, value);
	}

	public void attr(String field, long value) throws IOException {
		generator.writeNumberField(field, value);
	}

	public void attr(String field, float value) throws IOException {
		generator.writeNumberField(field, value);
	}

	public void attr(String field, double value) throws IOException {
		generator.writeNumberField(field, value);
	}

	public void attr(String field, boolean value) throws IOException {
		generator.writeBooleanField(field, value);
	}

	public void attr(String field, Object value) throws IOException {
		generator.writeObjectField(field, value);
	}

	public void raw(String value) throws IOException {
		generator.writeRawValue(value);
	}

	public String writeJson() {

		if(this.initProcessor == null) {
			throw new RuntimeException("initProcessor is null");
		}

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);

			this.generator = jGenerator;
			initProcessor.process();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			JSONUtils.close(jGenerator);
			this.initProcessor = null;
		}
		return out.toString();
	}

	// 有些情境是由外部指定JsonGenerator，產生部分的程式碼
	public void run(JsonGenerator jGenerator) {
		try {
			this.generator = jGenerator;
			initProcessor.process();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			this.initProcessor = null;
		}
	}


	public void writeStartArray() throws IOException {
		generator.writeStartArray();
	}


	public void writeEndArray() throws IOException {
		generator.writeEndArray();
	}

	
	public void writeStartObject() throws IOException {
		generator.writeStartObject();
	}

	
	public void writeStartObject(Object forValue) throws IOException {
		generator.writeStartObject(forValue);
	}

	
	public void writeStartObject(Object forValue, int size) throws IOException {
		generator.writeStartObject(forValue, size);
	}

	
	public void writeEndObject() throws IOException {
		generator.writeEndObject();
	}

	
	public void writeFieldName(String s) throws IOException {
		generator.writeFieldName(s);
	}

	
	public void writeFieldName(SerializableString serializableString) throws IOException {
		generator.writeFieldName(serializableString);
	}


	public void writeFieldId(long id) throws IOException {
		generator.writeFieldId(id);
	}

	
	public void writeArray(int[] array, int offset, int length) throws IOException {
		generator.writeArray(array, offset, length);
	}

	
	public void writeArray(long[] array, int offset, int length) throws IOException {
		generator.writeArray(array, offset, length);
	}

	
	public void writeArray(double[] array, int offset, int length) throws IOException {
		generator.writeArray(array, offset, length);
	}

	
	public void writeArray(String[] array, int offset, int length) throws IOException {
		generator.writeArray(array, offset, length);
	}

	
	public void writeString(String s) throws IOException {
		generator.writeString(s);
	}

	
	public void writeString(Reader reader, int len) throws IOException {
		generator.writeString(reader, len);
	}

	
	public void writeString(char[] chars, int i, int i1) throws IOException {
		generator.writeString(chars, i, i1);
	}

	
	public void writeString(SerializableString serializableString) throws IOException {
		generator.writeString(serializableString);
	}

	
	public void writeRawUTF8String(byte[] bytes, int i, int i1) throws IOException {
		generator.writeRawUTF8String(bytes, i, i1);
	}

	
	public void writeUTF8String(byte[] bytes, int i, int i1) throws IOException {
		generator.writeUTF8String(bytes, i, i1);
	}

	
	public void writeRaw(String s) throws IOException {
		generator.writeRaw(s);
	}

	
	public void writeRaw(String s, int i, int i1) throws IOException {
		generator.writeRaw(s, i, i1);
	}

	
	public void writeRaw(char[] chars, int i, int i1) throws IOException {
		generator.writeRaw(chars, i, i1);
	}

	
	public void writeRaw(char c) throws IOException {
		generator.writeRaw(c);
	}

	
	public void writeRaw(SerializableString raw) throws IOException {
		generator.writeRaw(raw);
	}

	
	public void writeRawValue(String s) throws IOException {
		generator.writeRawValue(s);
	}

	
	public void writeRawValue(String s, int i, int i1) throws IOException {
		generator.writeRawValue(s, i, i1);
	}

	
	public void writeRawValue(char[] chars, int i, int i1) throws IOException {
		generator.writeRawValue(chars, i, i1);
	}

	
	public void writeRawValue(SerializableString raw) throws IOException {
		generator.writeRawValue(raw);
	}

	
	public void writeBinary(Base64Variant base64Variant, byte[] bytes, int i, int i1) throws IOException {
		generator.writeBinary(base64Variant, bytes, i, i1);
	}

	
	public void writeBinary(byte[] data, int offset, int len) throws IOException {
		generator.writeBinary(data, offset, len);
	}

	
	public void writeBinary(byte[] data) throws IOException {
		generator.writeBinary(data);
	}

	
	public int writeBinary(InputStream data, int dataLength) throws IOException {
		return generator.writeBinary(data, dataLength);
	}

	
	public int writeBinary(Base64Variant base64Variant, InputStream inputStream, int i) throws IOException {
		return generator.writeBinary(base64Variant, inputStream, i);
	}

	
	public void writeNumber(short v) throws IOException {
		generator.writeNumber(v);
	}

	
	public void writeNumber(int i) throws IOException {
		generator.writeNumber(i);
	}

	
	public void writeNumber(long l) throws IOException {
		generator.writeNumber(l);
	}

	
	public void writeNumber(BigInteger bigInteger) throws IOException {
		generator.writeNumber(bigInteger);
	}

	
	public void writeNumber(double v) throws IOException {
		generator.writeNumber(v);
	}

	
	public void writeNumber(float v) throws IOException {
		generator.writeNumber(v);
	}

	
	public void writeNumber(BigDecimal bigDecimal) throws IOException {
		generator.writeNumber(bigDecimal);
	}

	
	public void writeNumber(String s) throws IOException {
		generator.writeNumber(s);
	}

	
	public void writeNumber(char[] encodedValueBuffer, int offset, int len) throws IOException {
		generator.writeNumber(encodedValueBuffer, offset, len);
	}

	
	public void writeBoolean(boolean b) throws IOException {
		generator.writeBoolean(b);
	}

	
	public void writeNull() throws IOException {
		generator.writeNull();
	}

	
	public void writeEmbeddedObject(Object object) throws IOException {
		generator.writeEmbeddedObject(object);
	}

	
	public void writeObjectId(Object id) throws IOException {
		generator.writeObjectId(id);
	}

	
	public void writeObjectRef(Object referenced) throws IOException {
		generator.writeObjectRef(referenced);
	}

	
	public void writeTypeId(Object id) throws IOException {
		generator.writeTypeId(id);
	}

	
	public WritableTypeId writeTypePrefix(WritableTypeId typeIdDef) throws IOException {
		return generator.writeTypePrefix(typeIdDef);
	}

	
	public WritableTypeId writeTypeSuffix(WritableTypeId typeIdDef) throws IOException {
		return generator.writeTypeSuffix(typeIdDef);
	}

	
	public void writePOJO(Object pojo) throws IOException {
		generator.writePOJO(pojo);
	}

	
	public void writeObject(Object o) throws IOException {
		generator.writeObject(o);
	}

	
	public void writeTree(TreeNode treeNode) throws IOException {
		generator.writeTree(treeNode);
	}

	
	public void writeBinaryField(String fieldName, byte[] data) throws IOException {
		generator.writeBinaryField(fieldName, data);
	}

	
	public void writeBooleanField(String fieldName, boolean value) throws IOException {
		generator.writeBooleanField(fieldName, value);
	}

	
	public void writeNullField(String fieldName) throws IOException {
		generator.writeNullField(fieldName);
	}

	
	public void writeStringField(String fieldName, String value) throws IOException {
		generator.writeStringField(fieldName, value);
	}

	
	public void writeNumberField(String fieldName, short value) throws IOException {
		generator.writeNumberField(fieldName, value);
	}

	
	public void writeNumberField(String fieldName, int value) throws IOException {
		generator.writeNumberField(fieldName, value);
	}

	
	public void writeNumberField(String fieldName, long value) throws IOException {
		generator.writeNumberField(fieldName, value);
	}

	
	public void writeNumberField(String fieldName, BigInteger value) throws IOException {
		generator.writeNumberField(fieldName, value);
	}

	
	public void writeNumberField(String fieldName, float value) throws IOException {
		generator.writeNumberField(fieldName, value);
	}

	
	public void writeNumberField(String fieldName, double value) throws IOException {
		generator.writeNumberField(fieldName, value);
	}

	
	public void writeNumberField(String fieldName, BigDecimal value) throws IOException {
		generator.writeNumberField(fieldName, value);
	}

	public void writeArrayFieldStart(String fieldName) throws IOException {
		generator.writeArrayFieldStart(fieldName);
	}

	public void writeObjectFieldStart(String fieldName) throws IOException {
		generator.writeObjectFieldStart(fieldName);
	}


	public void writeObjectField(String fieldName, Object pojo) throws IOException {
		generator.writeObjectField(fieldName, pojo);
	}

}
