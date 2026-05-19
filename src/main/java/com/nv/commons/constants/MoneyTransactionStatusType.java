package com.nv.commons.constants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.model.I18nKeyHolder;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

public enum MoneyTransactionStatusType implements UniqueValueHolder, I18nKeyHolder {
	NEW(0, "Pending", "label-primary", "Create", "global.text.moneyTransactionStatusType.NEW"),    //淺藍
	PENDING_APPROVAL(1, "Verified", "label-warning", "Verify",
		"global.text.moneyTransactionStatusType.PENDING_APPROVAL"),    //黃，提醒
	REJECTED(-1, "Rejected", "label-default", "Reject", "global.text.moneyTransactionStatusType.REJECTED"),    //紅，有問題
	CONFIRMED(2, "Approved", "label-success", "Approve", "global.text.moneyTransactionStatusType.CONFIRMED"),    //綠，ok
	CLOSE(-2, "Disapproved", "label-danger", "Disapproved", "global.text.moneyTransactionStatusType.CLOSE"),    //灰，忽略
	ON_HOLD(3, "On Hold", "label-hold", "Hold", "global.text.moneyTransactionStatusType.ON_HOLD"),    //咖啡，忽略
	PROCESSING(4, "Processing", "label-processing", "Processing", "global.text.moneyTransactionStatusType.PROCESSING"),
	AWAITED(5, "Awaited", "label-await", "Awaited", "global.text.moneyTransactionStatusType.AWAITED"),//紫色，處理中
	REVERTED(-3, "Reverted", "label-revert", "Reverted", "global.text.moneyTransactionStatusType.REVERTED"),
	;

	public static final MoneyTransactionStatusType[] WITHDRAWAL_PENDING = new MoneyTransactionStatusType[] {
		NEW, PENDING_APPROVAL, PROCESSING, ON_HOLD, AWAITED
	};
	public static final MoneyTransactionStatusType[] PENDING = new MoneyTransactionStatusType[] {
		NEW, PENDING_APPROVAL, PROCESSING, ON_HOLD, AWAITED
	};

	public static final int[] WITHDRAWAL_PENDING_UNIQUE = Arrays.stream(WITHDRAWAL_PENDING)
		.mapToInt(MoneyTransactionStatusType::unique).toArray();
	public static final int[] PENDING_UNIQUE = Arrays.stream(PENDING)
		.mapToInt(MoneyTransactionStatusType::unique).toArray();

	//	"狀態
	//	0 等待驗證
	//	1: 驗證通過    -1:驗證不通過
	//	2: 審核通過    -2:審核不通過"
	//	"
	//	0=New
	//	1=Pending Approval
	//	2=Confirmed/Close
	//	3=Rejected
	//	"
	//	<option value="9999">ALL</option>
	//	<option value="-2">審核不通過/Close</option>
	//	<option value="-1">驗證不通過/Rejected</option>
	//	<option value="0">等待驗證/New</option>
	//	<option value="1">驗證通過/Pending Approval</option>
	//	<option value="2">審核通過/Confirmed</option>

	//	<option value="<%=MoneyTransactionStatusType.CLOSE.unique()%>">審核不通過/Close</option>
	//	<option value="<%=MoneyTransactionStatusType.REJECTED.unique()%>">驗證不通過/Rejected</option>
	//	<option value="<%=MoneyTransactionStatusType.NEW.unique()%>">等待驗證/New</option>
	//	<option value="<%=MoneyTransactionStatusType.PENDING_APPROVAL.unique()%>">驗證通過/Pending Approval</option>
	//	<option value="<%=MoneyTransactionStatusType.CONFIRMED.unique()%>">審核通過/Confirmed</option>

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (MoneyTransactionStatusType moneyTransactionStatusType : MoneyTransactionStatusType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(moneyTransactionStatusType.unique()));
				jGenerator.writeStringField("name", moneyTransactionStatusType.name());
				jGenerator.writeStringField("getName", moneyTransactionStatusType.getName());
				jGenerator.writeStringField("getAction", moneyTransactionStatusType.getAction());
				jGenerator.writeStringField("css", moneyTransactionStatusType.getCss());
				jGenerator.writeStringField("getFullName", moneyTransactionStatusType.getFullName());
				jGenerator.writeEndObject();
				jGenerator.writeNumberField(moneyTransactionStatusType.name(), moneyTransactionStatusType.unique());
			}

			jGenerator.writeArrayFieldStart("order");
			for (MoneyTransactionStatusType moneyTransactionStatusType : MoneyTransactionStatusType.values()) {
				jGenerator.writeNumber(moneyTransactionStatusType.unique());
			}
			jGenerator.writeEndArray();
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

	private final int value;
	private final String name;
	private final String css;
	private final String action;
	private final String fullName;

	MoneyTransactionStatusType(int value, String name, String css, String action, String fullName) {
		this.value = value;
		this.name = name;
		this.css = css;
		this.action = action;
		this.fullName = fullName;
	}

	public static MoneyTransactionStatusType getInstance(int value) {
		for (MoneyTransactionStatusType e : MoneyTransactionStatusType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const MoneyTransactionStatusType. value:" + value);
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getCss() {
		return css;
	}

	public String getFullName() {
		return fullName;
	}

	public String getAction() {
		return action;
	}

	public String getI18nKey() {
		return this.fullName;
	}

}
