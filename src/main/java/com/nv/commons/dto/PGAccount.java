package com.nv.commons.dto;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nv.commons.annotation.Column;
import com.nv.commons.cache.PaymentGatewayCache;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.PaymentType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.paymentGateway.proxy.PaymentGatewayProxy;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Title: com.nv.commons.dto.PGAccount<br>
 * Description: 線上支付收款帳號-資料庫物件
 *
 * @author: Daniel.Hsieh
 * @version: 1.0
 */
public class PGAccount {

	@Column(name = "id")
	private int id;

	@Column(name = "company_id")
	private int companyId;

	private String companyName;

	@Column(name = "payment_method")
	private String paymentMethod;

	private List<Integer> paymentMethodList;

	private String paymentMethodName;
	
	private Map<String, String> methodNameMap = null;

	@Column(name = "transaction_limit")
	private Long transactionLimit;

	@Column(name = "current_amount")
	private BigDecimal currentAmount;

	@Column(name = "pending_time_limit")
	private Integer pendingTimeLimit;

	@Column(name = "merchant_id")
	private String merchantId;

	@Column(name = "encryption_private_key")
	private String encryptionPrivateKey;

	@Column(name = "encryption_public_key")
	private String encryptionPublicKey;

	@Column(name = "webshop_api_url")
	private String webshopApiUrl;

	@Column(name = "status")
	private int status;

	@Column(name = "remark")
    private String remark;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "payment_method_setting")
	private String paymentMethodSetting;

	private List<PaymentMethod> paymentMethodSettingList;

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "extra_data")
	private String extraData;

	@Column(name = "currency_type_id")
	private int currencyTypeId;

	@Column(name = "start_time")
	private String startTime;

	@Column(name = "end_time")
	private String endTime;

	private int purpose;

	private Map<String, String> extraDataMap = new HashMap<>();
	
//	private Map<Integer, List<PaymentMethod>> paymentMethodSettings;

	@Column(name = "display_name")
	private String displayName;

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
		PaymentGatewayProxy pgProxy = PaymentGatewayCache.getInstance().getProxy(this.companyId);
		if (pgProxy != null) {
			this.companyName = pgProxy.getCompanyInfo().getName();
		}
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
		updateMethodNameMap();
	}

	public String getPaymentMethodName(String langKey) {
		try {
			if (methodNameMap == null) {
				updateMethodNameMap();
			}
			return methodNameMap.get(langKey);

		} catch (Exception e) {
			// paymentMethod is null or empty
		}
		return "";
	}

	
	public void setPaymentMethodName(String paymentMethodName) {
		this.paymentMethodName = paymentMethodName;
		try {
			if (methodNameMap != null) {
				updateMethodNameMap();
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}
	
	private void updateMethodNameMap() {
		methodNameMap = null;
		this.paymentMethodList = null;
		
		if (paymentMethod != null && !"".equals(paymentMethod)) {
			
			methodNameMap = new HashMap<>();
			
			List<Integer> tempList = new ArrayList<>();
			List<String> tempNameList = new ArrayList<>();
//			List<String> tempCNNameList = new ArrayList<>();
			
			for (String method : paymentMethod.split(",")) {
				try {
					int methodInt = Integer.parseInt(method);

					tempList.add(methodInt);

					PaymentType aPGPaymentType = PaymentType.getInstanceOf(methodInt);

					if (aPGPaymentType == null) {
						LogUtils.SYS.debug("aPGPaymentType null, methodInt = {}", methodInt);
					} else {
						tempNameList.add(aPGPaymentType.getName());
//						tempCNNameList.add(aPGPaymentType.getFullName(LanguageType.CHINESE.getLangMessage()));
					}
				} catch (Exception e) {
					LogUtils.SYS.error(e.getMessage(), e);
				}
			}

			this.paymentMethodList = tempList;

			Collections.sort(tempNameList);

			methodNameMap.put(LanguageType.ENGLISH.getLanguageResourceKey(), String.join(",", tempNameList));

//			Collections.sort(tempCNNameList);

//			methodNameMap.put(LanguageType.CHINESE.getLanguageResourceKey(), String.join(",", tempCNNameList));
		}
	}

	// 因為 BO 可以將 PGAccount 的  WebShopApiUrl 設定為空，當 WebShopApiUrl 為空時，取預設的 Url
	public String getPaymentWebshopApiUrl() {
		if (webshopApiUrl == null || webshopApiUrl.isEmpty()) {
//			if (SystemInfo.getInstance().isProduction()) {
//				return SystemConstants.IN_HOUSE_API_URL;
//			} else {
				return SystemConstants.TEST_IN_HOUSE_API_URL;
//			}
		}
		return webshopApiUrl;
	}

	public void setPaymentMethodSetting(String paymentMethodSetting) {
		if(paymentMethodSetting != null) {
			try {
				this.paymentMethodSettingList = JSONUtils.parseJsonToObjectList(paymentMethodSetting, PaymentMethod.class);
			} catch (IOException e) {
				LogUtils.SYS.error("Transfer payment method setting fail!", e);
			}
		}
		this.paymentMethodSetting = paymentMethodSetting;
	}

	public PaymentMethod getPaymentMethodSetting(int paymentTypeId) {
		if(paymentMethodSettingList != null) {
			Optional<PaymentMethod> result = paymentMethodSettingList.stream().filter(o -> o.getPaymentMethodType() == paymentTypeId).findFirst();
			if (result.isPresent()) {
				return result.get();
			}
		}
		return null;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
		updateExtraDataMap();
	}
	
	public void addExtraData(String key, String value){
		extraDataMap.put(key, value);
		setExtraData(JSONUtils.getJSONString(extraDataMap));
	}

	public String getExtraDataElement(String key) {
		String value = extraDataMap.get(key);
		if (StringUtils.isEmpty(value)) {
			updateExtraDataMap();
			return extraDataMap.get(key);
		}
		return value;
	}

	public void updateExtraDataMap() {
		if (!StringUtils.isEmpty(extraData)) {
			extraDataMap = JSONUtils.jsonToMap(extraData, String.class, String.class);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCompanyId() {
		return companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public List<Integer> getPaymentMethodList() {
		return paymentMethodList;
	}

	public String getPaymentMethodName() {
		return paymentMethodName;
	}

	public Long getTransactionLimit() {
		return transactionLimit;
	}

	public void setTransactionLimit(Long transactionLimit) {
		this.transactionLimit = transactionLimit;
	}

	public BigDecimal getCurrentAmount() {
		return currentAmount;
	}

	public void setCurrentAmount(BigDecimal currentAmount) {
		this.currentAmount = currentAmount;
	}

	public Integer getPendingTimeLimit() {
		return pendingTimeLimit;
	}

	public void setPendingTimeLimit(Integer pendingTimeLimit) {
		this.pendingTimeLimit = pendingTimeLimit;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getEncryptionPrivateKey() {
		return encryptionPrivateKey;
	}

	public void setEncryptionPrivateKey(String encryptionPrivateKey) {
		this.encryptionPrivateKey = encryptionPrivateKey;
	}

	public String getEncryptionPublicKey() {
		return encryptionPublicKey;
	}

	public void setEncryptionPublicKey(String encryptionPublicKey) {
		this.encryptionPublicKey = encryptionPublicKey;
	}

	public String getWebshopApiUrl() {
		return webshopApiUrl;
	}

	public void setWebshopApiUrl(String webshopApiUrl) {
		this.webshopApiUrl = webshopApiUrl;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	public String getPaymentMethodSetting() {
		return paymentMethodSetting;
	}

	public List<PaymentMethod> getPaymentMethodSettingList() {
		return paymentMethodSettingList;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public String getExtraData() {
		return extraData;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getPurpose() {
		return purpose;
	}

	public void setPurpose(int purpose) {
		this.purpose = purpose;
	}

	public Map<String, String> getExtraDataMap() {
		return extraDataMap;
	}

	public void setExtraDataMap(Map<String, String> extraDataMap) {
		this.extraDataMap = extraDataMap;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
