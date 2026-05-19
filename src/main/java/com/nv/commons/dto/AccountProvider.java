package com.nv.commons.dto;


import com.nv.commons.annotation.Column;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//import java.util.Date;

public class AccountProvider {
    //	user_id
    //	website_type
    //	provider_id
    //	provider_account
    //	provider_password
    //	provider_balance
    //	provider_create_date

//    public AccountProvider() {
//    }
//
//    public AccountProvider(String userId, int websiteType, int providerId, String providerAccount, String providerPassword, BigDecimal providerBalance, Timestamp providerCreateTime, Timestamp providerUpdateTime, int currencyTypeId, String providerExtraData, String providerBOAccount, BigDecimal exposure, long bonusTurnoverId, long lastCallGetBalanceTimeMillis, Map<String, ?> providerExtraDataMap, Provider provider) {
//        this.userId = userId;
//        this.websiteType = websiteType;
//        this.providerId = providerId;
//        this.providerAccount = providerAccount;
//        this.providerPassword = providerPassword;
//        this.providerBalance = providerBalance != null ? providerBalance : BigDecimal.ZERO;
//        this.providerCreateTime = providerCreateTime;
//        this.providerUpdateTime = providerUpdateTime;
//        this.currencyTypeId = currencyTypeId;
//        this.providerExtraData = providerExtraData;
//        this.providerBOAccount = providerBOAccount;
//        this.exposure = exposure != null ? exposure : BigDecimal.ZERO;
//        this.bonusTurnoverId = bonusTurnoverId;
//        this.lastCallGetBalanceTimeMillis = lastCallGetBalanceTimeMillis;
//        this.providerExtraDataMap = providerExtraDataMap != null ? providerExtraDataMap : new HashMap<>();
//        this.provider = provider;
//    }

    @Column(name = "user_id")
    private String userId;

    @Column(name = "website_type")
    private int websiteType;

    
    @Column(name = "provider_id")
    private int providerId;

    
    @Column(name = "provider_account")
    private String providerAccount;

    @Column(name = "provider_password")
    private String providerPassword;

    
    @Column(name = "provider_balance")
    private BigDecimal providerBalance = BigDecimal.ZERO;

    
    @Column(name = "provider_create_time")
    private Timestamp providerCreateTime;

    @Column(name = "provider_update_time")
    private Timestamp providerUpdateTime;

    @Column(name = "currency_type_id")
    private int currencyTypeId;

    
    @Column(name = "provider_extra_data")
    private String providerExtraData;

    
    private String providerBOAccount;

    
    @Column(name = "exposure")
    private BigDecimal exposure = BigDecimal.ZERO;

    
    @Column(name = "bonus_turnover_id")
    private long bonusTurnoverId;

    private long lastCallGetBalanceTimeMillis = 0;

    private Map<String, ?> providerExtraDataMap = new HashMap<>();

    
    private Provider provider;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getWebsiteType() {
        return websiteType;
    }

    public void setWebsiteType(int websiteType) {
        this.websiteType = websiteType;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderAccount() {
        return providerAccount;
    }

    public void setProviderAccount(String providerAccount) {
        this.providerAccount = providerAccount;
    }

    public String getProviderPassword() {
        return providerPassword;
    }

    public void setProviderPassword(String providerPassword) {
        this.providerPassword = providerPassword;
    }

    public BigDecimal getProviderBalance() {
        return providerBalance;
    }

    public void setProviderBalance(BigDecimal providerBalance) {
        this.providerBalance = providerBalance;
    }

    public Timestamp getProviderCreateTime() {
        return providerCreateTime;
    }

    public void setProviderCreateTime(Timestamp providerCreateTime) {
        this.providerCreateTime = providerCreateTime;
    }

    public Timestamp getProviderUpdateTime() {
        return providerUpdateTime;
    }

    public void setProviderUpdateTime(Timestamp providerUpdateTime) {
        this.providerUpdateTime = providerUpdateTime;
    }

    public int getCurrencyTypeId() {
        return currencyTypeId;
    }

    public void setCurrencyTypeId(int currencyTypeId) {
        this.currencyTypeId = currencyTypeId;
    }

    public String getProviderExtraData() {
        return providerExtraData;
    }

    public void setProviderExtraData(String providerExtraData) {
        try {
            this.providerExtraData = providerExtraData;
            if (this.providerExtraData != null) {
                this.providerExtraDataMap = JSONUtils.jsonToMap(this.providerExtraData, String.class, Object.class);
            }
        } catch (Exception e) {
            LogUtils.SYS.error(e.getMessage(), e);
        }
    }

    public String getProviderBOAccount() {
        return providerBOAccount;
    }

    public void setProviderBOAccount(String providerBOAccount) {
        this.providerBOAccount = providerBOAccount;
    }

    public BigDecimal getExposure() {
        return exposure;
    }

    public void setExposure(BigDecimal exposure) {
        this.exposure = exposure;
    }

    public long getBonusTurnoverId() {
        return bonusTurnoverId;
    }

    public void setBonusTurnoverId(long bonusTurnoverId) {
        this.bonusTurnoverId = bonusTurnoverId;
    }

    public long getLastCallGetBalanceTimeMillis() {
        return lastCallGetBalanceTimeMillis;
    }

    public void setLastCallGetBalanceTimeMillis(long lastCallGetBalanceTimeMillis) {
        this.lastCallGetBalanceTimeMillis = lastCallGetBalanceTimeMillis;
    }

    public Map<String, ?> getProviderExtraDataMap() {
        return providerExtraDataMap;
    }

    public void setProviderExtraDataMap(Map<String, ?> providerExtraDataMap) {
        this.providerExtraDataMap = providerExtraDataMap;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountProvider that = (AccountProvider) o;
        return websiteType == that.websiteType &&
                providerId == that.providerId &&
                Objects.equals(userId, that.userId) &&
                bonusTurnoverId == that.bonusTurnoverId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, websiteType, providerId, bonusTurnoverId);
    }

//    public static AccountProviderBuilder builder() {
//        return new AccountProviderBuilder();
//    }

//    public static class AccountProviderBuilder {
//
//        private String userId;
//        private int websiteType;
//        private int providerId;
//        private String providerAccount;
//        private String providerPassword;
//        private BigDecimal providerBalance;
//        private Timestamp providerCreateTime;
//        private Timestamp providerUpdateTime;
//        private int currencyTypeId;
//        private String providerExtraData;
//        private String providerBOAccount;
//        private BigDecimal exposure;
//        private long bonusTurnoverId;
//        private long lastCallGetBalanceTimeMillis;
//        private Map<String, ?> providerExtraDataMap;
//        private Provider provider;
//
//        public AccountProviderBuilder userId(String userId) {
//            this.userId = userId;
//            return this;
//        }
//
//        public AccountProviderBuilder websiteType(int websiteType) {
//            this.websiteType = websiteType;
//            return this;
//        }
//
//        public AccountProviderBuilder providerId(int providerId) {
//            this.providerId = providerId;
//            return this;
//        }
//
//        public AccountProviderBuilder providerAccount(String providerAccount) {
//            this.providerAccount = providerAccount;
//            return this;
//        }
//
//        public AccountProviderBuilder providerPassword(String providerPassword) {
//            this.providerPassword = providerPassword;
//            return this;
//        }
//
//        public AccountProviderBuilder providerBalance(BigDecimal providerBalance) {
//            this.providerBalance = providerBalance;
//            return this;
//        }
//
//        public AccountProviderBuilder providerCreateTime(Timestamp providerCreateTime) {
//            this.providerCreateTime = providerCreateTime;
//            return this;
//        }
//
//        public AccountProviderBuilder providerUpdateTime(Timestamp providerUpdateTime) {
//            this.providerUpdateTime = providerUpdateTime;
//            return this;
//        }
//
//        public AccountProviderBuilder currencyTypeId(int currencyTypeId) {
//            this.currencyTypeId = currencyTypeId;
//            return this;
//        }
//
//        public AccountProviderBuilder providerExtraData(String providerExtraData) {
//            this.providerExtraData = providerExtraData;
//            return this;
//        }
//
//        public AccountProviderBuilder providerBOAccount(String providerBOAccount) {
//            this.providerBOAccount = providerBOAccount;
//            return this;
//        }
//
//        public AccountProviderBuilder exposure(BigDecimal exposure) {
//            this.exposure = exposure;
//            return this;
//        }
//
//        public AccountProviderBuilder bonusTurnoverId(long bonusTurnoverId) {
//            this.bonusTurnoverId = bonusTurnoverId;
//            return this;
//        }
//
//        public AccountProviderBuilder lastCallGetBalanceTimeMillis(long lastCallGetBalanceTimeMillis) {
//            this.lastCallGetBalanceTimeMillis = lastCallGetBalanceTimeMillis;
//            return this;
//        }
//
//        public AccountProviderBuilder providerExtraDataMap(Map<String, ?> providerExtraDataMap) {
//            this.providerExtraDataMap = providerExtraDataMap;
//            return this;
//        }
//
//        public AccountProviderBuilder provider(Provider provider) {
//            this.provider = provider;
//            return this;
//        }
//
//        public AccountProvider build() {
//            AccountProvider account = new AccountProvider();
//            account.setUserId(this.userId);
//            account.setWebsiteType(this.websiteType);
//            account.setProviderId(this.providerId);
//            account.setProviderAccount(this.providerAccount);
//            account.setProviderPassword(this.providerPassword);
//            account.setProviderBalance(this.providerBalance);
//            account.setProviderCreateTime(this.providerCreateTime);
//            account.setProviderUpdateTime(this.providerUpdateTime);
//            account.setCurrencyTypeId(this.currencyTypeId);
//            account.setProviderExtraData(this.providerExtraData);
//            account.setProviderBOAccount(this.providerBOAccount);
//            account.setExposure(this.exposure);
//            account.setBonusTurnoverId(this.bonusTurnoverId);
//            account.setLastCallGetBalanceTimeMillis(this.lastCallGetBalanceTimeMillis);
//            account.setProviderExtraDataMap(this.providerExtraDataMap);
//            account.setProvider(this.provider);
//            return account;
//        }
//    }
}
