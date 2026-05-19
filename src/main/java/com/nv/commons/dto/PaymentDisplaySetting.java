package com.nv.commons.dto;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.utils.JSONUtils;

/**
 * @author Luke Chi
 */
public class PaymentDisplaySetting {

	private long id;

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "payment_id")
	private int paymentId;

	@Column(name = "payment_method")
	private int paymentMethod;

	@Column(name = "bank_type")
	private int bankType;

	@Column(name = "transaction_type")
	private int transactionType;

	private int priority;

	private String userId;

	@Column(name = "user_id")
	private String originalUserId;

	private String excludeUserId;

	private String vipLevel;

	@Column(name = "vip_level")
	private String originalVipLevel;

	private String excludeVipLevel;

	private String userGroupId;

	@Column(name = "user_group_id")
	private String originalUserGroupId;

	private String excludeUserGroupId;

	@Column(name = "min_amount")
	private BigDecimal minAmount;

	@Column(name = "max_amount")
	private BigDecimal maxAmount;

	@Column(name = "currency_type_id")
	private int currencyTypeId;

	@Column(name = "manual_input")
	private int manualInput;

	private int status;

	private int display;

	private int recommend;

	@Column(name = "is_random")
	private boolean random;

	@Column(name = "integer_amount_json")
	private String integerAmountJson;

	@Column(name = "decimal_amount")
	private String decimalAmount;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "note_json")
	private String noteJson;

	private List<BigDecimal> integerAmountList;

	private Map<String, String> noteMap;

	private boolean isPaymentGateway = false;

	private boolean isCompanyBank = false;

	private boolean isDeposit = false;

	private boolean isWithdrawal = false;

	@Column(name = "display_name")
	private String displayName;

	public void setTransactionType(int transactionType) {
		this.transactionType = transactionType;
		if (transactionType == MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique()
			|| transactionType == MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique()) {
			this.isPaymentGateway = true;
		} else if (transactionType == MoneyTransactionType.DEPOSIT.unique()
			|| transactionType == MoneyTransactionType.WITHDRAWALS.unique()) {
			this.isCompanyBank = true;
		}

		if (transactionType == MoneyTransactionType.DEPOSIT.unique()
			|| transactionType == MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique()) {
			this.isDeposit = true;
		} else if (transactionType == MoneyTransactionType.WITHDRAWALS.unique()
			|| transactionType == MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique()) {
			this.isWithdrawal = true;
		}
	}

	public String getUserId() {
		if (userId == null) {
		}
		return userId;
	}

	public String getVipLevel() {
		if (vipLevel == null) {
		}
		return vipLevel;
	}

	public void setIntegerAmountJson(String integerAmountJson) {
		this.integerAmountJson = integerAmountJson;
		try {
			if (null != integerAmountJson) {
				this.integerAmountList = JSONUtils.parseJsonToObjectList(integerAmountJson, BigDecimal.class);
			}
		} catch (IOException ignore) {
		}
	}

	public void setNoteJson(String noteJson) {
		this.noteJson = noteJson;
		if (null != noteJson) {
			this.noteMap = JSONUtils.jsonToMap(noteJson, String.class, String.class);
		}
	}

	public void setIntegerAmountList(List<BigDecimal> integerAmountList) {
		this.integerAmountList = integerAmountList;
		this.integerAmountJson = JSONUtils.toJsonString(integerAmountList);
	}

	public void setNoteMap(Map<String, String> noteMap) {
		this.noteMap = noteMap;
		this.noteJson = JSONUtils.toJsonString(noteMap);
	}

	public boolean isPaymentGateway() {
		return isPaymentGateway;
	}

	public boolean isCompanyBank() {
		return isCompanyBank;
	}

	public boolean isDeposit() {
		return isDeposit;
	}

	public boolean isWithdrawal() {
		return isWithdrawal;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public int getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}

	public int getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(int paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public int getBankType() {
		return bankType;
	}

	public void setBankType(int bankType) {
		this.bankType = bankType;
	}

	public int getTransactionType() {
		return transactionType;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getOriginalUserId() {
		return originalUserId;
	}

	public void setOriginalUserId(String originalUserId) {
		this.originalUserId = originalUserId;
	}

	public String getOriginalVipLevel() {
		return originalVipLevel;
	}

	public void setOriginalVipLevel(String originalVipLevel) {
		this.originalVipLevel = originalVipLevel;
	}

	public String getOriginalUserGroupId() {
		return originalUserGroupId;
	}

	public void setOriginalUserGroupId(String originalUserGroupId) {
		this.originalUserGroupId = originalUserGroupId;
	}

	public BigDecimal getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(BigDecimal minAmount) {
		this.minAmount = minAmount;
	}

	public BigDecimal getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(BigDecimal maxAmount) {
		this.maxAmount = maxAmount;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public int getManualInput() {
		return manualInput;
	}

	public void setManualInput(int manualInput) {
		this.manualInput = manualInput;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getDisplay() {
		return display;
	}

	public void setDisplay(int display) {
		this.display = display;
	}

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}

	public boolean isRandom() {
		return random;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}

	public String getIntegerAmountJson() {
		return integerAmountJson;
	}

	public String getDecimalAmount() {
		return decimalAmount;
	}

	public void setDecimalAmount(String decimalAmount) {
		this.decimalAmount = decimalAmount;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getNoteJson() {
		return noteJson;
	}

	public List<BigDecimal> getIntegerAmountList() {
		return integerAmountList;
	}

	public Map<String, String> getNoteMap() {
		return noteMap;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getExcludeUserId() {
		return excludeUserId;
	}

	public void setExcludeUserId(String excludeUserId) {
		this.excludeUserId = excludeUserId;
	}

	public void setVipLevel(String vipLevel) {
		this.vipLevel = vipLevel;
	}

	public String getExcludeVipLevel() {
		return excludeVipLevel;
	}

	public void setExcludeVipLevel(String excludeVipLevel) {
		this.excludeVipLevel = excludeVipLevel;
	}

	public String getUserGroupId() {
		return userGroupId;
	}

	public void setUserGroupId(String userGroupId) {
		this.userGroupId = userGroupId;
	}

	public String getExcludeUserGroupId() {
		return excludeUserGroupId;
	}

	public void setExcludeUserGroupId(String excludeUserGroupId) {
		this.excludeUserGroupId = excludeUserGroupId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PaymentDisplaySetting that = (PaymentDisplaySetting) o;
		return id == that.id &&
				websiteType == that.websiteType &&
				paymentId == that.paymentId &&
				paymentMethod == that.paymentMethod &&
				bankType == that.bankType &&
				transactionType == that.transactionType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, websiteType, paymentId, paymentMethod, bankType, transactionType);
	}

//	public static PaymentDisplaySettingBuilder builder() {
//		return new PaymentDisplaySettingBuilder();
//	}

//	public static class PaymentDisplaySettingBuilder {
//		private long id;
//		private int websiteType;
//		private int paymentId;
//		private int paymentMethod;
//		private int bankType;
//		private int transactionType;
//		private int priority;
//		private String userId;
//		private String originalUserId;
//		private String excludeUserId;
//		private String vipLevel;
//		private String originalVipLevel;
//		private String excludeVipLevel;
//		private String userGroupId;
//		private String originalUserGroupId;
//		private String excludeUserGroupId;
//		private BigDecimal minAmount;
//		private BigDecimal maxAmount;
//		private int currencyTypeId;
//		private int manualInput;
//		private int status;
//		private int display;
//		private int recommend;
//		private boolean random;
//		private String integerAmountJson;
//		private String decimalAmount;
//		private Timestamp createTime;
//		private Timestamp updateTime;
//		private String noteJson;
//		private List<BigDecimal> integerAmountList;
//		private Map<String, String> noteMap;
//		private String displayName;
//
//		public PaymentDisplaySettingBuilder id(long id) {
//			this.id = id;
//			return this;
//		}
//
//		public PaymentDisplaySettingBuilder websiteType(int websiteType) {
//			this.websiteType = websiteType;
//			return this;
//		}
//
//		public PaymentDisplaySettingBuilder paymentId(int paymentId) {
//			this.paymentId = paymentId;
//			return this;
//		}
//
//		public PaymentDisplaySettingBuilder paymentMethod(int paymentMethod) {
//			this.paymentMethod = paymentMethod;
//			return this;
//		}
//
//		public PaymentDisplaySettingBuilder bankType(int bankType) {
//			this.bankType = bankType;
//			return this;
//		}
//
//		public PaymentDisplaySettingBuilder transactionType(int transactionType) {
//			this.transactionType = transactionType;
//			return this;
//		}
//
//		public PaymentDisplaySettingBuilder priority(int priority) {
//			this.priority = priority;
//			return this;
//		}
//
//		public PaymentDisplaySettingBuilder userId(String userId) {
//			this.userId = userId;
//			return this;
//		}
//
//		public PaymentDisplaySettingBuilder displayName(String displayName) {
//			this.displayName = displayName;
//			return this;
//		}
//
//		public PaymentDisplaySetting build() {
//			PaymentDisplaySetting setting = new PaymentDisplaySetting();
//			setting.setId(this.id);
//			setting.setWebsiteType(this.websiteType);
//			setting.setPaymentId(this.paymentId);
//			setting.setPaymentMethod(this.paymentMethod);
//			setting.setBankType(this.bankType);
//			setting.setTransactionType(this.transactionType);
//			setting.setPriority(this.priority);
//			setting.setUserId(this.userId);
//			setting.setOriginalUserId(this.originalUserId);
//			setting.setExcludeUserId(this.excludeUserId);
//			setting.setVipLevel(this.vipLevel);
//			setting.setOriginalVipLevel(this.originalVipLevel);
//			setting.setExcludeVipLevel(this.excludeVipLevel);
//			setting.setUserGroupId(this.userGroupId);
//			setting.setOriginalUserGroupId(this.originalUserGroupId);
//			setting.setExcludeUserGroupId(this.excludeUserGroupId);
//			setting.setMinAmount(this.minAmount);
//			setting.setMaxAmount(this.maxAmount);
//			setting.setCurrencyTypeId(this.currencyTypeId);
//			setting.setManualInput(this.manualInput);
//			setting.setStatus(this.status);
//			setting.setDisplay(this.display);
//			setting.setRecommend(this.recommend);
//			setting.setRandom(this.random);
//			setting.setIntegerAmountJson(this.integerAmountJson);
//			setting.setDecimalAmount(this.decimalAmount);
//			setting.setCreateTime(this.createTime);
//			setting.setUpdateTime(this.updateTime);
//			setting.setNoteJson(this.noteJson);
//			setting.setIntegerAmountList(this.integerAmountList);
//			setting.setNoteMap(this.noteMap);
//			setting.setDisplayName(this.displayName);
//			return setting;
//		}
//	}
}
