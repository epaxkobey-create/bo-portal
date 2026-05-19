package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.model.I18nKeyHolder;
import com.nv.commons.utils.JSONUtils;

public enum SystemTxnStatusType implements I18nKeyHolder {

	/**
	 * 未結算
	 */
	UNSETTLED(0, "Pending", "label-warning", "global.text.txn.status.unsettled"),

	/**
	 * 已結算
	 */
	SETTLED(1, "Complete", "label-success", "global.text.txn.status.settled"),

	/**
	 * 撤回
	 */
	REVOACTION(2, "Complete", "label-success", "global.text.txn.status.revoaction"),

	/**
	 * 結算後, 取消結算之後再取消投注
	 */
	VOID(3, "Complete", "label-success", "form.text.backend.status.void"),

	REFUND(4, "Complete", "label-success", "form.text.backend.status.refund");

	private static String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (SystemTxnStatusType systemTxnStatusType : SystemTxnStatusType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(systemTxnStatusType.unique()));
				jGenerator.writeStringField("simpleName", systemTxnStatusType.getSimpleName());
				jGenerator.writeStringField("simpleCss", systemTxnStatusType.getSimpleCss());
				jGenerator.writeEndObject();
				jGenerator.writeNumberField(systemTxnStatusType.name(), systemTxnStatusType.unique());
			}
			jGenerator.writeEndObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			JSONUtils.close(jGenerator);
		}
		json = out.toString();
	}

	public static String toJsonString() {
		return json;
	}

	private final int status;
	private final String simpleName;
	private final String simpleCss;
	private final String i18nKey;
//	public static int[] completeTxnStatus = null;

	SystemTxnStatusType(int status, String simpleName, String simpleCss, String i18nKey) {
		this.status = status;
		this.simpleName = simpleName;
		this.simpleCss = simpleCss;
		this.i18nKey = i18nKey;
	}

	public int unique() {
		return this.status;
	}

	public static SystemTxnStatusType getInstance(int status) {
		for (SystemTxnStatusType type : SystemTxnStatusType.values()) {
			if (type.status == status) {
				return type;
			}
		}
		return null;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getSimpleCss() {
		return simpleCss;
	}

	public String getI18nKey() {
		return i18nKey;
	}
}
