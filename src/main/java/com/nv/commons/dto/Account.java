package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nv.commons.annotation.Column;
import com.nv.commons.cache.AccountProviderCache;
import com.nv.commons.cache.PlayerAccountContactInfoLocalCache;
import com.nv.commons.cache.key.AccountContactInfoKey;
import com.nv.commons.cache.key.AccountProviderKey;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.ContactType;
import com.nv.commons.constants.CountryType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.LogUtils;

public class Account {

	/*
	 *
	 */
	
	@Column(name = "user_id", maxLength = 50)
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	/*
	 *  Testing
	 * */
	private String KYCFrontPhoto;

	public String getKYCFrontPhoto() {
		return KYCFrontPhoto;
	}

	public void setKYCFrontPhoto(String KYCFrontPhoto) {
		this.KYCFrontPhoto = KYCFrontPhoto;
	}

	
	private String showUserId;

	@JsonIgnore
	private String userKey;

	@JsonIgnore
	private String[] userKeyStrs = null;

	@Column(name = "website_type")
	private int websiteType;

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	
	@Column(name = "affiliate")
	private String affiliate;

	public String getAffiliate() {
		return affiliate;
	}

	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}

	
	@Column(name = "legal_first_name")
	private String legalFirstName;

	public String getLegalFirstName() {
		return legalFirstName;
	}

	public void setLegalFirstName(String legalFirstName) {
		this.legalFirstName = legalFirstName;
	}

	
	@Column(name = "legal_last_name")
	private String legalLastName;

	public String getLegalLastName() {
		return legalLastName;
	}

	public void setLegalLastName(String legalLastName) {
		this.legalLastName = legalLastName;
	}

	
	@Column(name = "user_name")
	private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	
	private String userNameSpecial;

	/*
	 * 註冊時 填電話 要選 calling code 國碼 , 此 country_type 是由國碼判斷而來非從 ip (CountryLookUp)
	 */
	
	@Column(name = "country_type")
	private Integer countryType;

	// MEMO: for 大量用 InsertAccountUtils 匯入 使用者
	//		if (phoneNumber.startsWith("0")) {
	//			phoneNumber = phoneNumber.substring(1);
	//		}
	
	@Column(name = "phone_number")
	private String phoneNumber;

	
	private String callingCode;

	
	@Column(name = "phone_number2")
	private String phoneNumber2;

	
	private String callingCode2;

	
	@Column(name = "phone_number3")
	private String phoneNumber3;

	
	private String callingCode3;

	
	@Column(name = "email")
	private String email;

	
	@Column(name = "occupation")
	private String occupation;

	
	@Column(name = "address")
	private String address;

	
	@Column(name = "birthday")
	private Timestamp birthday;

	
	private String birthdayStr;

	// 複合值
	@Deprecated
	
	@Column(name = "CONTACT_VERIFIED")
	private int contactVerified;

	
	@Column(name = "vip_level")
	private int vipLevel = 1;

	
	private String vipLevelName;

	// "0=Manual,
	// 1=Automatic"
	
	@Column(name = "auto_upgrade_vip")
	private int autoUpgradeVip;

	
//	@Column(name = "balance")
	private BigDecimal balance = BigDecimal.ZERO;

	
	@Column(name = "currency_type_id")
	private int currencyTypeId;

	
	private String currencyTypeName;

	@Column(name = "password")
	private String password;

	// "是否須重新設定密碼.
	// 0:不用
	// 1:要"
	@Column(name = "reset_password")
	private int resetPassword;

	// used in AccountBO.syncAccountInPlayerCacheWithDB force Player Cache update
	// first login user
	private boolean firstLogin = true;

	@JsonIgnore
	private boolean loginFailed = false;

	public void setLoginFailed(boolean loginFailed) {
		this.loginFailed = loginFailed;
	}

	public boolean isLoginFailed() {
		return loginFailed;
	}

	@Column(name = "bio_token")
	private String bioToken;

	
	@Column(name = "status")
	private int status;

	@Column(name = "login_fail")
	private int loginFail;

	
	@Column(name = "sign_up_time")
	private Timestamp signUpTime;

	
	private String signUpTimeStr = "";

	
	@Column(name = "sign_up_ip")
	private String signUpIp;

	
	@Column(name = "sign_up_country")
	private String signUpCountry;

	
	@Column(name = "sign_up_state")
	private String signUpState;

	
	@Column(name = "sign_up_city")
	private String signUpCity;

	
	@Column(name = "login_time")
	private Timestamp loginTime;

	
	private String loginTimeStr = "";

	
	@Column(name = "login_ip")
	private String loginIp;

	@Column(name = "server_id")
	private String serverId;

	@Column(name = "session_id")
	private String sessionId;

	
	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "favorite_game")
	private String favoriteGame;

	@Column(name = "first_deposit")
	private BigDecimal firstDeposit;

	
	@Column(name = "first_deposit_time")
	private Timestamp firstDepositTime;

	
	private String firstDepositTimeStr = "";

	
	@Column(name = "last_deposit_time")
	private Timestamp lastDepositTime;

	
	private String lastDepositTimeStr = "";

	
	@Column(name = "first_withdrawal_time")
	private Timestamp firstWithdrawalTime;

	
	private String firstWithdrawalTimeStr = "";

	
	@Column(name = "last_withdrawal_time")
	private Timestamp lastWithdrawalTime;

	
	private String lastWithdrawalTimeStr = "";

	
	@Column(name = "first_adjustment_time")
	private Timestamp firstAdjustmentTime;

	
	private String firstAdjustmentTimeStr = "";

	
	@Column(name = "last_adjustment_time")
	private Timestamp lastAdjustmentTime;

	
	private String lastAdjustmentTimeStr = "";

	
	@Column(name = "first_bonus_time")
	private Timestamp firstBonusTime;

	
	private String firstBonusTimeStr = "";

	
	@Column(name = "last_bonus_time")
	private Timestamp lastBonusTime;

	
	private String lastBonusTimeStr = "";

	@Column(name = "use_first_deposit_bonus")
	private int useFirstDepositBonus;

	
	@Column(name = "deposit_amount")
	private BigDecimal depositAmount;

	//	@Column(name = "withdrawal_count")
	//	private int withdrawalCount;
	//
	//	
	//	@Column(name = "withdrawal_amount")
	//	private BigDecimal withdrawalAmount;

	//	
	//	@Column(name = "bonus_amount")
	//	private BigDecimal bonusAmount;

	//	
	//	@Column(name = "profit_lose")
	//	private BigDecimal profitLose;

	
	@Column(name = "first_bet_time")
	private Timestamp firstBetTime;

	
	private String firstBetTimeStr = "";

	
	@Column(name = "last_bet_time")
	private Timestamp lastBetTime;

	
	private String lastBetTimeStr = "";

	// ref: enum DeviceType
	@Column(name = "device_type")
	private int deviceType;

	@Column(name = "platform_type")
	private int platformType;

	
	@Column(name = "min_force_serve")
	private BigDecimal minForceServe;

	
	@Column(name = "min_deposit_amount")
	private Integer minDepositAmount;

	//		@Column(name = "allow_recive_bonus")

	@Column(name = "allow_game_type")
	private int allowGameType;

	@Column(name = "UPDATED_ATTRIBUTE")
	private int updatedAttribute;

	
	@Column(name = "summary_convertion_point")
	private BigDecimal summaryConvertionPoint;

	@Column(name = "affiliate_id")
	private long affiliateId;

	public long getAffiliateId() {
		return affiliateId;
	}

	public void setAffiliateId(long affiliateId) {
		this.affiliateId = affiliateId;
	}

	
	private String affiliateName;

	@Column(name = "use_kyc_bonus")
	private int useKycBonus;

	@Column(name = "use_kyc_bonus_document")
	private String useKycBonusDocument;

	@Column(name = "use_kyc_bonus_personal_info")
	private int useKycBonusPersonalInfo;

	@Column(name = "last_open_provider")
	private int lastOpenProvider;

	@Column(name = "affiliate_link_seq")
	private Integer affiliateLinkSeq;

	@Column(name = "first_bet")
	private BigDecimal firstBet;

	@Column(name = "last_bet")
	private BigDecimal lastBet;

	@Column(name = "last_deposit")
	private BigDecimal lastDeposit;

	// for affiliate
	@Column(name = "key_word")
	private String keyWord;

	
	@Column(name = "user_channel_type")
	private int userChannelType;

	
	@Column(name = "AUTO_VERIFICATION")
	private int autoVerification = 0;

	
	private int gender = -1;

	
	private int marital = -1;

	
	@Column(name = "AUTO_VERIFICATION_AMOUNT")
	private BigDecimal autoVerificationAmount;

	//	//	//	
	//	private List<AccountBonusTurnover> allBonusTurnover;
	/*
	 * for ajax get balance, vip level, status
	 */
	private String playerInfoJson;

	
	@Column(name = "ALLOW_FORCE_SERVE")
	private String allowForceServe;

	@Column(name = "VIEWED_PLAY_RESPONSIBLY")
	private int viewedPlayResponsibly;

//	@JsonIgnore
//	private List<AccountInGroups> accountGroups = Collections.emptyList();

	public boolean allowPlayerNameSetting = false;

	public int kycDocumentStatus;

	//	@JsonIgnore
	//	private Map<Integer, AccountProvider> accountProviders = new HashMap<>();

	
//	private AccountStats accountStats;

	//	private Map<AccountBonusTurnoverStatusType, List<AccountBonusTurnover>> bonusTurnoverMap = new ConcurrentHashMap<>();

	public String getUserKey() {
		if (this.userKey == null) {
			this.userKey = AccountUtils.getUserKey(WebSiteType.getInstance(this.websiteType), this.userId);
		}
		return this.userKey;
	}

	public String[] getUserKeyStrs() {
		if (this.userKeyStrs == null) {
			this.userKeyStrs = new String[] {getUserId(), String.valueOf(getWebsiteType())};
		}
		return Arrays.copyOf(this.userKeyStrs, this.userKeyStrs.length, String[].class);
	}

	//	public String[] getUserKeyRevereStrs() {
	//		if (this.userKeyRevereStrs == null) {
	//			this.userKeyRevereStrs = new String[] {String.valueOf(getWebsiteType()), getUserId()};
	//		}
	//		return Arrays.copyOf(this.userKeyRevereStrs, this.userKeyRevereStrs.length, String[].class);
	//	}

//	@JsonIgnore
//	public String getUserNameSpecial() {
//
//		userNameSpecial = userName;
//
//		return userNameSpecial;
//	}

	public int getCountryType() {
		return countryType;
	}

	public void setCountryType(int countryType) {
		this.countryType = countryType;
	}

	private String getCallingCodePart(String defaultCallingCode, String content) {
		return AccountUtils.getCallingCodeFromPhoneNumber(content, defaultCallingCode);
	}

	private String getPhoneNumberPart(String content) {
		return AccountUtils.getPhoneNumberWithoutCallingCode(content);
	}

	public String getPhoneNumberWithCallingCode() {
		return AccountUtils.getNewFormatPhoneNumber(getPhoneNumber(), getCallingCode());
	}

	public String getPhoneNumber() {
		if (phoneNumber == null) {
			if (accountContactInfoMap != null) {
//				String contactPhoneNumber = accountContactInfoMap
//					.get(new AccountContactInfoKey(ContactType.Phone.unique(), 1));
//
//				contactPhoneNumber = getPhoneNumberPart(contactPhoneNumber);
//				if (contactPhoneNumber != null && contactPhoneNumber.startsWith("0")) {
//					contactPhoneNumber = contactPhoneNumber.substring(1);
//				}
//				phoneNumber = contactPhoneNumber;
			} else {
				//有些 BO功能使用Account不會有websiteType跟userId, 或是只會有一個.
				//不完整的userKey 一定取不到資料, 直接回傳null
				if (websiteType > 0 && userId != null) {
//					String contactPhoneNumber = PlayerAccountContactInfoLocalCache.getInstance()
//						.getAccountContactInfo(getUserKey(),
//							new AccountContactInfoKey(ContactType.Phone.unique(), 1));
//
//					contactPhoneNumber = getPhoneNumberPart(contactPhoneNumber);
//					if (contactPhoneNumber != null && contactPhoneNumber.startsWith("0")) {
//						contactPhoneNumber = contactPhoneNumber.substring(1);
//					}
//					return contactPhoneNumber;
				}
			}
		}
		return phoneNumber;
	}

	public String getCallingCode() {
		if (callingCode == null) {
			CountryType countryTypeEnum = CountryType.getInstance(getCountryType());
			String defaultCallingCode = (countryTypeEnum != null) ? countryTypeEnum.getCallingCode() : null;
			if (accountContactInfoMap != null) {
//				String contactPhoneNumber = accountContactInfoMap
//					.get(new AccountContactInfoKey(ContactType.Phone.unique(), 1));
//
//				callingCode = getCallingCodePart(defaultCallingCode, contactPhoneNumber);
			} else {
				//有些 BO功能使用Account不會有websiteType跟userId, 或是只會有一個.
				//不完整的userKey 一定取不到資料, 直接回傳null
//				if (websiteType > 0 && userId != null) {
//					String contactPhoneNumber = PlayerAccountContactInfoLocalCache.getInstance()
//						.getAccountContactInfo(getUserKey(),
//							new AccountContactInfoKey(ContactType.Phone.unique(), 1));
//
//					return getCallingCodePart(defaultCallingCode, contactPhoneNumber);
//				}
			}
		}
		return callingCode;
	}

//	public String getPhoneNumber2WithCallingCode() {
//		return AccountUtils.getNewFormatPhoneNumber(getPhoneNumber2(), getCallingCode2());
//	}

//	public String getPhoneNumber2() {
//		if (phoneNumber2 == null) {
//			if (accountContactInfoMap != null) {
//				String contactPhoneNumber = accountContactInfoMap
//					.get(new AccountContactInfoKey(ContactType.Phone.unique(), 2));
//				contactPhoneNumber = getPhoneNumberPart(contactPhoneNumber);
//				if (contactPhoneNumber != null && contactPhoneNumber.startsWith("0")) {
//					contactPhoneNumber = contactPhoneNumber.substring(1);
//				}
//				phoneNumber2 = contactPhoneNumber;
//			} else {
//				//有些 BO功能使用Account不會有websiteType跟userId, 或是只會有一個.
//				//不完整的userKey 一定取不到資料, 直接回傳null
//				if (websiteType > 0 && userId != null) {
//					String contactPhoneNumber = PlayerAccountContactInfoLocalCache.getInstance()
//						.getAccountContactInfo(getUserKey(),
//							new AccountContactInfoKey(ContactType.Phone.unique(), 2));
//					contactPhoneNumber = getPhoneNumberPart(contactPhoneNumber);
//					if (contactPhoneNumber != null && contactPhoneNumber.startsWith("0")) {
//						contactPhoneNumber = contactPhoneNumber.substring(1);
//					}
//					return contactPhoneNumber;
//				}
//			}
//		}
//		return phoneNumber2;
//	}

//	public String getCallingCode2() {
//		if (callingCode2 == null) {
//
//			String defaultCallingCode = CountryType.getInstance(getCountryType()).getCallingCode();
//			if (accountContactInfoMap != null) {
//				String contactPhoneNumber = accountContactInfoMap
//					.get(new AccountContactInfoKey(ContactType.Phone.unique(), 2));
//
//				callingCode2 = getCallingCodePart(defaultCallingCode, contactPhoneNumber);
//			} else {
//				//有些 BO功能使用Account不會有websiteType跟userId, 或是只會有一個.
//				//不完整的userKey 一定取不到資料, 直接回傳null
//				if (websiteType > 0 && userId != null) {
//					String contactPhoneNumber = PlayerAccountContactInfoLocalCache.getInstance()
//						.getAccountContactInfo(getUserKey(),
//							new AccountContactInfoKey(ContactType.Phone.unique(), 2));
//
//					return getCallingCodePart(defaultCallingCode, contactPhoneNumber);
//				}
//			}
//		}
//		return callingCode2;
//	}

//	public String getPhoneNumber3WithCallingCode() {
//		return AccountUtils.getNewFormatPhoneNumber(getPhoneNumber3(), getCallingCode3());
//	}

//	public String getPhoneNumber3() {
//		if (phoneNumber3 == null) {
//			//BO才會有 accountContactInfoMap, web 改去cache拿
//			if (accountContactInfoMap != null) {
//				String contactPhoneNumber = accountContactInfoMap
//					.get(new AccountContactInfoKey(ContactType.Phone.unique(), 3));
//				contactPhoneNumber = getPhoneNumberPart(contactPhoneNumber);
//				if (contactPhoneNumber != null && contactPhoneNumber.startsWith("0")) {
//					contactPhoneNumber = contactPhoneNumber.substring(1);
//				}
//				phoneNumber3 = contactPhoneNumber;
//			} else {
//				//有些 BO功能使用Account不會有websiteType跟userId, 或是只會有一個.
//				//不完整的userKey 一定取不到資料, 直接回傳null
//				if (websiteType > 0 && userId != null) {
//					String contactPhoneNumber = PlayerAccountContactInfoLocalCache.getInstance()
//						.getAccountContactInfo(getUserKey(),
//							new AccountContactInfoKey(ContactType.Phone.unique(), 3));
//					contactPhoneNumber = getPhoneNumberPart(contactPhoneNumber);
//					if (contactPhoneNumber != null && contactPhoneNumber.startsWith("0")) {
//						contactPhoneNumber = contactPhoneNumber.substring(1);
//					}
//					return contactPhoneNumber;
//				}
//			}
//		}
//		return phoneNumber3;
//	}

//	public String getCallingCode3() {
//		if (callingCode3 == null) {
//
//			String defaultCallingCode = CountryType.getInstance(getCountryType()).getCallingCode();
//			if (accountContactInfoMap != null) {
//				String contactPhoneNumber = accountContactInfoMap
//					.get(new AccountContactInfoKey(ContactType.Phone.unique(), 3));
//
//				callingCode3 = getCallingCodePart(defaultCallingCode, contactPhoneNumber);
//			} else {
//				//有些 BO功能使用Account不會有websiteType跟userId, 或是只會有一個.
//				//不完整的userKey 一定取不到資料, 直接回傳null
//				if (websiteType > 0 && userId != null) {
//					String contactPhoneNumber = PlayerAccountContactInfoLocalCache.getInstance()
//						.getAccountContactInfo(getUserKey(),
//							new AccountContactInfoKey(ContactType.Phone.unique(), 3));
//
//					return getCallingCodePart(defaultCallingCode, contactPhoneNumber);
//				}
//			}
//		}
//		return callingCode3;
//	}

	public String getEmail() {
		if (email == null) {
			if (accountContactInfoMap != null) {
				email = accountContactInfoMap.get(new AccountContactInfoKey(ContactType.Email.unique(), 1));
			} else {
				//有些 BO功能使用Account不會有websiteType跟userId, 或是只會有一個.
				//不完整的userKey 一定取不到資料, 直接回傳null
				if (websiteType > 0 && userId != null) {
					return PlayerAccountContactInfoLocalCache.getInstance().getAccountContactInfo(getUserKey(),
						new AccountContactInfoKey(ContactType.Email.unique(), 1));
				}
			}
		}
		return email;
	}

	public String getAddress() {
		if (address == null) {
			if (accountContactInfoMap != null) {
//				address = accountContactInfoMap.get(new AccountContactInfoKey(ContactType.Address.unique(), 1));
			} else {
				if (websiteType > 0 && userId != null) {
//					return PlayerAccountContactInfoLocalCache.getInstance().getAccountContactInfo(getUserKey(),
//						new AccountContactInfoKey(ContactType.Address.unique(), 1));
				}
			}
		}
		return address;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING,
		pattern = FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy,
		timezone = "GMT+8")
	public Timestamp getBirthday() {
		return birthday;
	}

	public void setBirthday(Timestamp birthday) {
		this.birthday = birthday;
		if (null != birthday) {
			birthdayStr = FormatUtils.dateFormat(birthday, FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd);
		}
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
		this.currencyTypeName = CurrencyType.getInstance(currencyTypeId).name();
	}

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	public Timestamp getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Timestamp loginTime) {
		this.loginTime = loginTime;
		if (null != loginTime) {
			this.loginTimeStr = FormatUtils.dateFormat(loginTime);
		}
	}

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setFirstDepositTime(Timestamp firstDepositTime) {
		this.firstDepositTime = firstDepositTime;
		if (null != firstDepositTime) {
			this.firstDepositTimeStr = FormatUtils.dateFormat(firstDepositTime);
		}
	}

	public void setLastBetTime(Timestamp lastBetTime) {
		this.lastBetTime = lastBetTime;
		if (null != lastBetTime) {
			this.lastBetTimeStr = FormatUtils.dateFormat(lastBetTime);
		}
	}

	public void setLastDepositTime(Timestamp lastDepositTime) {
		this.lastDepositTime = lastDepositTime;
		if (null != lastDepositTime) {
			this.lastDepositTimeStr = FormatUtils.dateFormat(lastDepositTime);
		}
	}

	public void setFirstWithdrawalTime(Timestamp firstWithdrawalTime) {
		this.firstWithdrawalTime = firstWithdrawalTime;
		if (null != firstWithdrawalTime) {
			this.firstWithdrawalTimeStr = FormatUtils.dateFormat(firstWithdrawalTime);
		}
	}

	public void setLastWithdrawalTime(Timestamp lastWithdrawalTime) {
		this.lastWithdrawalTime = lastWithdrawalTime;
		if (null != lastWithdrawalTime) {
			this.lastWithdrawalTimeStr = FormatUtils.dateFormat(lastWithdrawalTime);
		}
	}

	public void setFirstAdjustmentTime(Timestamp firstAdjustmentTime) {
		this.firstAdjustmentTime = firstAdjustmentTime;
		if (null != firstAdjustmentTime) {
			this.firstAdjustmentTimeStr = FormatUtils.dateFormat(firstAdjustmentTime);
		}
	}

	public void setLastAdjustmentTime(Timestamp lastAdjustmentTime) {
		this.lastAdjustmentTime = lastAdjustmentTime;
		if (null != lastAdjustmentTime) {
			this.lastAdjustmentTimeStr = FormatUtils.dateFormat(lastAdjustmentTime);
		}
	}

	public void setFirstBonusTime(Timestamp firstBonusTime) {
		this.firstBonusTime = firstBonusTime;
		if (null != firstBonusTime) {
			this.firstBonusTimeStr = FormatUtils.dateFormat(firstBonusTime);
		}
	}

	public void setLastBonusTime(Timestamp lastBonusTime) {
		this.lastBonusTime = lastBonusTime;
		if (null != lastBonusTime) {
			this.lastBonusTimeStr = FormatUtils.dateFormat(lastBonusTime);
		}
	}

	public Timestamp getFirstDepositTime() {
		return firstDepositTime;
	}

	public String getFirstDepositTimeStr() {
		return firstDepositTimeStr;
	}

	public void setFirstDepositTimeStr(String firstDepositTimeStr) {
		this.firstDepositTimeStr = firstDepositTimeStr;
	}

	public Timestamp getLastDepositTime() {
		return lastDepositTime;
	}

	public String getLastDepositTimeStr() {
		return lastDepositTimeStr;
	}

	public void setLastDepositTimeStr(String lastDepositTimeStr) {
		this.lastDepositTimeStr = lastDepositTimeStr;
	}

	public Timestamp getFirstWithdrawalTime() {
		return firstWithdrawalTime;
	}

	public String getFirstWithdrawalTimeStr() {
		return firstWithdrawalTimeStr;
	}

	public void setFirstWithdrawalTimeStr(String firstWithdrawalTimeStr) {
		this.firstWithdrawalTimeStr = firstWithdrawalTimeStr;
	}

	public Timestamp getLastWithdrawalTime() {
		return lastWithdrawalTime;
	}

	public String getLastWithdrawalTimeStr() {
		return lastWithdrawalTimeStr;
	}

	public void setLastWithdrawalTimeStr(String lastWithdrawalTimeStr) {
		this.lastWithdrawalTimeStr = lastWithdrawalTimeStr;
	}

	public Timestamp getFirstAdjustmentTime() {
		return firstAdjustmentTime;
	}

	public String getFirstAdjustmentTimeStr() {
		return firstAdjustmentTimeStr;
	}

	public void setFirstAdjustmentTimeStr(String firstAdjustmentTimeStr) {
		this.firstAdjustmentTimeStr = firstAdjustmentTimeStr;
	}

	public Timestamp getLastAdjustmentTime() {
		return lastAdjustmentTime;
	}

	public String getLastAdjustmentTimeStr() {
		return lastAdjustmentTimeStr;
	}

	public void setLastAdjustmentTimeStr(String lastAdjustmentTimeStr) {
		this.lastAdjustmentTimeStr = lastAdjustmentTimeStr;
	}

	public Timestamp getFirstBonusTime() {
		return firstBonusTime;
	}

	public String getFirstBonusTimeStr() {
		return firstBonusTimeStr;
	}

	public void setFirstBonusTimeStr(String firstBonusTimeStr) {
		this.firstBonusTimeStr = firstBonusTimeStr;
	}

	public Timestamp getLastBonusTime() {
		return lastBonusTime;
	}

	public String getLastBonusTimeStr() {
		return lastBonusTimeStr;
	}

	public void setLastBonusTimeStr(String lastBonusTimeStr) {
		this.lastBonusTimeStr = lastBonusTimeStr;
	}

	public Timestamp getFirstBetTime() {
		return firstBetTime;
	}

	public String getFirstBetTimeStr() {
		return firstBetTimeStr;
	}

	public void setFirstBetTimeStr(String firstBetTimeStr) {
		this.firstBetTimeStr = firstBetTimeStr;
	}

	public Timestamp getLastBetTime() {
		return lastBetTime;
	}

	public String getLastBetTimeStr() {
		return lastBetTimeStr;
	}

	public void setLastBetTimeStr(String lastBetTimeStr) {
		this.lastBetTimeStr = lastBetTimeStr;
	}

	
	private int[] allowGameTypeDetails;

	
	public int[] getAllowGameTypeDetails() {
		GameType[] gameTypeValues = GameType.values();
		allowGameTypeDetails = new int[gameTypeValues.length];
		for (int i = 0; i < gameTypeValues.length; i++) {
			if ((allowGameType & gameTypeValues[i].unique()) == gameTypeValues[i].unique()) {
				allowGameTypeDetails[i] = gameTypeValues[i].unique();
			}
		}
		return allowGameTypeDetails;
	}

	/**
	 *
	 */
//	@JsonIgnore
//	private LoadingCache<String, List<AccountInGroups>> accountGroupsLoadingCache = null;

	//	public Set<Integer> getAccountGroups() {
	//		return getAccountInGroupsData().stream().map(AccountInGroups::getGroupId).collect(Collectors.toSet());
	//	}

//	public void setAccountGroupsForBO(List<AccountInGroups> accountGroups) {
//		this.accountGroups = accountGroups;
//	}

	public AccountProvider getAccountProvider(Integer providerID) {

		//		final AccountBonusTurnoverCache cache = AccountBonusTurnoverCache.getInstance();

//		long bonusTurnoverId = -1;
		/*
		long bonusTurnoverId = AccountBonusTurnoverUtil
			.getEffectiveBonusTurnoverId(providerID, WebSiteType.getInstance(websiteType), () -> {
				try {
					return cache.getEnabledBonusWallet(getUserKey());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		*/

		AccountProviderKey key = new AccountProviderKey(websiteType, providerID, userId);

		AccountProvider accountProviderInCache = AccountProviderCache.getInstance().getAccountProvider(key);

		/*
		if (bonusTurnoverId == BonusWalletType.MAIN.unique() && (accountProviderInCache != null
			&& accountProviderInCache.getBonusTurnoverId() != BonusWalletType.MAIN.unique())) {

			// TODO debug log，追蹤用，解決會移除
			LogUtils.bonus.info("[ tracking AccountProvider information ] bonusTurnoverId is MAIN,"
				+ " but bonusTurnoverId of accountProviderInCache is {}", accountProviderInCache.getBonusTurnoverId());
		}
		 */

		return accountProviderInCache;
	}

//	public Set<Integer> getAccountProviders(long bonusTurnoverId) {
//		return AccountProviderCache.getInstance().getAccountProviderSet(userId, websiteType)
//			.stream()
//			.map(AccountProvider::getProviderId)
//			.collect(Collectors.toSet());
//	}

	/*
	 * MEMO: accountProviderList is for UI display in BO
	 */
	
	private List<AccountProvider> accountProviderList;

	
//	private List<Object> accountGroupList;

	public List<Integer> getUseKycBonusDocumentList() {
		try {
			if (useKycBonusDocument == null) {
				return new ArrayList<>();
			} else {
				List<Integer> useKycBonusDocumentList = Stream.of(useKycBonusDocument.split(",")).map(Integer::valueOf)
					.collect(
						Collectors.toList());
				if (useKycBonusDocumentList.size() == 1) {
					DocumentType documentType = DocumentType.getInstance(useKycBonusDocumentList.getFirst());
					if (documentType == null) {
						return DocumentType.getDocumentTypesUnique(useKycBonusDocumentList.getFirst());
					}
				}
				return useKycBonusDocumentList;
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	public void setFirstBetTime(Timestamp firstBetTime) {
		this.firstBetTime = firstBetTime;
		if (null != firstBetTime) {
			this.firstBetTimeStr = FormatUtils.dateFormat(firstBetTime);
		}
	}

	//	
	//	private List<AccountContactInfo> accountContactInfoList;

	@JsonDeserialize(keyUsing = AccountContactInfoKey.KeyDeserializer.class)
	private Map<AccountContactInfoKey, String> accountContactInfoMap;

	@JsonIgnore
	public List<AccountContactInfo> getAccountContactInfoList() {
		String userKey = websiteType > 0 && userId != null ? getUserKey() : "";
		return PlayerAccountContactInfoLocalCache.getInstance().get(userKey);
	}

	public void setAccountContactInfoListForJsonView(List<AccountContactInfo> accountContactInfoList) {
		setAccountContactInfoMap(accountContactInfoList);
	}

	private void setAccountContactInfoMap(List<AccountContactInfo> accountContactInfoList) {
		if (accountContactInfoMap == null) {
			accountContactInfoMap = new HashMap<>();
		}

		if (accountContactInfoList != null) {
			accountContactInfoList.sort(Comparator.comparing(AccountContactInfo::getContentNo));
			for (AccountContactInfo accountContactInfo : accountContactInfoList) {
				accountContactInfoMap.put(new AccountContactInfoKey(accountContactInfo.getContactType(),
					accountContactInfo.getContentNo()), accountContactInfo.getContent());
			}
		}
	}

	public void setAccountContactInfoList(List<AccountContactInfo> accountContactInfoList) {
		PlayerAccountContactInfoLocalCache.getInstance().put(getUserKey(), accountContactInfoList);
	}

	private AtomicInteger depositPendingCount;

	private AtomicInteger withdrawalPendingCount;

	//	private List<MoneyTransaction> withdrawalPendingList;

	//	private List<MoneyTransaction> depositPendingList;

	//	public List<MoneyTransaction> getDepositPendingList() {
	//		if (depositPendingList == null) {
	//			return Collections.emptyList();
	//		}
	//		return depositPendingList;
	//	}

	//	public void setDepositPendingList(List<MoneyTransaction> depositPendingList) {
	//		this.depositPendingList = depositPendingList;
	//	}

	//	public List<MoneyTransaction> getWithdrawalPendingList() {
	//		if (withdrawalPendingList == null) {
	//			return Collections.emptyList();
	//		}
	//		return withdrawalPendingList;
	//	}

	//	public void setWithdrawalPendingList(List<MoneyTransaction> withdrawalPendingList) {
	//		this.withdrawalPendingList = withdrawalPendingList;
	//	}

//	private void clearContactData() {
//		accountContactInfoMap = null;
//		phoneNumber = null;
//		phoneNumber2 = null;
//		phoneNumber3 = null;
//		email = null;
//	}

	//	public Map<AccountBonusTurnoverStatusType, List<AccountBonusTurnover>> getBonusTurnoverMap() {
	//		return bonusTurnoverMap;
	//	}

	//	public void setBonusTurnoverMap(
	//		Map<AccountBonusTurnoverStatusType, List<AccountBonusTurnover>> bonusTurnoverMap) {
	//		this.bonusTurnoverMap = bonusTurnoverMap;
	//	}

	//	public AccountBonusTurnover getEnabledBonusTurnover() throws Exception {
	//		return getUniqueBonusTurnover(accountBonusTurnover ->
	//			accountBonusTurnover.getStatus() == AccountBonusTurnoverStatusType.ACTIVE.unique()
	//				|| accountBonusTurnover.getStatus() == AccountBonusTurnoverStatusType.COMPLETE.unique());
	//	}

	//	public AccountBonusTurnover getUniqueBonusTurnover(Predicate<AccountBonusTurnover> predicate) throws Exception {
	//
	//		List<AccountBonusTurnover> accountBonusTurnoverList = bonusTurnoverMap.values()
	//			.stream()
	//			.flatMap(Collection::stream)
	//			.filter(predicate)
	//			.collect(Collectors.toList());
	//
	//		if (CollectionUtils.isEmpty(accountBonusTurnoverList)) {
	//			return null;
	//		}
	//
	//		if (accountBonusTurnoverList.size() > 1) {
	//			String message = "more than one bonus wallet is turned on, website:" + websiteType + "userId:" + userId;
	//			LogUtils.SYS.error(message);
	//			throw new Exception(message);
	//		}
	//
	//		return accountBonusTurnoverList.get(0);
	//	}

//	@JsonIgnore
//	public int getPersonalInfoTypeId() {
//		int personalInfoTypeId = 0;
//		for (PersonalInfoType personalInfoType : PersonalInfoType.VALUES) {
//			if (PersonalInfoType.getInstance(personalInfoType.unique()).checkPersonalInfoExist(this)) {
//				personalInfoTypeId += personalInfoType.unique();
//			}
//		}
//		return personalInfoTypeId;
//	}

	//	private List<AccountDocument> accountDocumentList;

	//	public List<AccountDocument> getAccountDocumentList() {
	//		if (accountDocumentList == null) {
	//			return Collections.emptyList();
	//		}
	//		return accountDocumentList;
	//	}

	//	public void setAccountDocumentList(List<AccountDocument> accountDocumentList) {
	//		this.accountDocumentList = accountDocumentList;
	//	}

	//	private List<AccountDocument> bankDocumentList;

	//	public List<AccountDocument> getBankDocumentList() {
	//		if (bankDocumentList == null) {
	//			return Collections.emptyList();
	//		}
	//		return bankDocumentList;
	//	}

	//	public void setBankDocumentList(List<AccountDocument> bankDocumentList) {
	//		this.bankDocumentList = bankDocumentList;
	//	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Account account = (Account) o;
		return websiteType == account.websiteType && Objects.equals(userId, account.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, websiteType);
	}

//	public String getShowUserId() {
//		if (allowPlayerNameSetting) {
//			return userId;
//		} else {
//			int showLength = userId.length() > 6 ? 4 : 2;
//			return StringUtil.mask(userId, 0, userId.length() - showLength, '*');
//		}
//	}

	public String getFullName() {
		return legalFirstName + " " + legalLastName;
	}

	public KycPersonalInfo kycPersonalInfo;

	@JsonProperty("isSessionExpired")
	public boolean isSessionExpired;

	private RealityCheckReminderData realityCheckReminderData;

	public RealityCheckReminderData getRealityCheckReminderData() {
		return realityCheckReminderData;
	}

	public void setRealityCheckReminderData(RealityCheckReminderData realityCheckReminderData) {
		this.realityCheckReminderData = realityCheckReminderData;
	}

	// Additional missing getters and setters
	public String getCurrencyTypeName() {
		return currencyTypeName;
	}

	public void setCurrencyTypeName(String currencyTypeName) {
		this.currencyTypeName = currencyTypeName;
	}

	public String getAffiliateName() {
		return affiliateName;
	}

	public void setAffiliateName(String affiliateName) {
		this.affiliateName = affiliateName;
	}

	public String getBirthdayStr() {
		return birthdayStr;
	}

	public void setBirthdayStr(String birthdayStr) {
		this.birthdayStr = birthdayStr;
	}

	public int getContactVerified() {
		return contactVerified;
	}

	public void setContactVerified(int contactVerified) {
		this.contactVerified = contactVerified;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public String getVipLevelName() {
		return vipLevelName;
	}

	public void setVipLevelName(String vipLevelName) {
		this.vipLevelName = vipLevelName;
	}

	public int getAutoUpgradeVip() {
		return autoUpgradeVip;
	}

	public void setAutoUpgradeVip(int autoUpgradeVip) {
		this.autoUpgradeVip = autoUpgradeVip;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getResetPassword() {
		return resetPassword;
	}

	public void setResetPassword(int resetPassword) {
		this.resetPassword = resetPassword;
	}

	public boolean isFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(boolean firstLogin) {
		this.firstLogin = firstLogin;
	}

	public String getBioToken() {
		return bioToken;
	}

	public void setBioToken(String bioToken) {
		this.bioToken = bioToken;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getLoginFail() {
		return loginFail;
	}

	public void setLoginFail(int loginFail) {
		this.loginFail = loginFail;
	}

	public String getSignUpIp() {
		return signUpIp;
	}

	public void setSignUpIp(String signUpIp) {
		this.signUpIp = signUpIp;
	}

	public String getSignUpCountry() {
		return signUpCountry;
	}

	public void setSignUpCountry(String signUpCountry) {
		this.signUpCountry = signUpCountry;
	}

	public String getSignUpState() {
		return signUpState;
	}

	public void setSignUpState(String signUpState) {
		this.signUpState = signUpState;
	}

	public String getSignUpCity() {
		return signUpCity;
	}

	public void setSignUpCity(String signUpCity) {
		this.signUpCity = signUpCity;
	}

	public Timestamp getSignUpTime() {
		return signUpTime;
	}

	public void setSignUpTime(Timestamp signUpTime) {
		this.signUpTime = signUpTime;
		if (null != signUpTime) {
			this.signUpTimeStr = FormatUtils.dateFormat(signUpTime);
		}
	}

	public String getSignUpTimeStr() {
		return signUpTimeStr;
	}

	public void setSignUpTimeStr(String signUpTimeStr) {
		this.signUpTimeStr = signUpTimeStr;
	}

	public String getLoginTimeStr() {
		return loginTimeStr;
	}

	public void setLoginTimeStr(String loginTimeStr) {
		this.loginTimeStr = loginTimeStr;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getFavoriteGame() {
		return favoriteGame;
	}

	public void setFavoriteGame(String favoriteGame) {
		this.favoriteGame = favoriteGame;
	}

	public BigDecimal getFirstDeposit() {
		return firstDeposit;
	}

	public void setFirstDeposit(BigDecimal firstDeposit) {
		this.firstDeposit = firstDeposit;
	}

	public int getUseFirstDepositBonus() {
		return useFirstDepositBonus;
	}

	public void setUseFirstDepositBonus(int useFirstDepositBonus) {
		this.useFirstDepositBonus = useFirstDepositBonus;
	}

	public BigDecimal getDepositAmount() {
		return depositAmount;
	}

	public void setDepositAmount(BigDecimal depositAmount) {
		this.depositAmount = depositAmount;
	}

	public int getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	public int getPlatformType() {
		return platformType;
	}

	public void setPlatformType(int platformType) {
		this.platformType = platformType;
	}

	public BigDecimal getMinForceServe() {
		return minForceServe;
	}

	public void setMinForceServe(BigDecimal minForceServe) {
		this.minForceServe = minForceServe;
	}

	public Integer getMinDepositAmount() {
		return minDepositAmount;
	}

	public void setMinDepositAmount(Integer minDepositAmount) {
		this.minDepositAmount = minDepositAmount;
	}

	public int getAllowGameType() {
		return allowGameType;
	}

	public void setAllowGameType(int allowGameType) {
		this.allowGameType = allowGameType;
	}

	public int getUpdatedAttribute() {
		return updatedAttribute;
	}

	public void setUpdatedAttribute(int updatedAttribute) {
		this.updatedAttribute = updatedAttribute;
	}

	public BigDecimal getSummaryConvertionPoint() {
		return summaryConvertionPoint;
	}

	public void setSummaryConvertionPoint(BigDecimal summaryConvertionPoint) {
		this.summaryConvertionPoint = summaryConvertionPoint;
	}

	public int getUseKycBonus() {
		return useKycBonus;
	}

	public void setUseKycBonus(int useKycBonus) {
		this.useKycBonus = useKycBonus;
	}

	public String getUseKycBonusDocument() {
		return useKycBonusDocument;
	}

	public void setUseKycBonusDocument(String useKycBonusDocument) {
		this.useKycBonusDocument = useKycBonusDocument;
	}

	public int getUseKycBonusPersonalInfo() {
		return useKycBonusPersonalInfo;
	}

	public void setUseKycBonusPersonalInfo(int useKycBonusPersonalInfo) {
		this.useKycBonusPersonalInfo = useKycBonusPersonalInfo;
	}

	public int getLastOpenProvider() {
		return lastOpenProvider;
	}

	public void setLastOpenProvider(int lastOpenProvider) {
		this.lastOpenProvider = lastOpenProvider;
	}

	public Integer getAffiliateLinkSeq() {
		return affiliateLinkSeq;
	}

	public void setAffiliateLinkSeq(Integer affiliateLinkSeq) {
		this.affiliateLinkSeq = affiliateLinkSeq;
	}

	public BigDecimal getFirstBet() {
		return firstBet;
	}

	public void setFirstBet(BigDecimal firstBet) {
		this.firstBet = firstBet;
	}

	public BigDecimal getLastBet() {
		return lastBet;
	}

	public void setLastBet(BigDecimal lastBet) {
		this.lastBet = lastBet;
	}

	public BigDecimal getLastDeposit() {
		return lastDeposit;
	}

	public void setLastDeposit(BigDecimal lastDeposit) {
		this.lastDeposit = lastDeposit;
	}

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public int getUserChannelType() {
		return userChannelType;
	}

	public void setUserChannelType(int userChannelType) {
		this.userChannelType = userChannelType;
	}

	public int getAutoVerification() {
		return autoVerification;
	}

	public void setAutoVerification(int autoVerification) {
		this.autoVerification = autoVerification;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getMarital() {
		return marital;
	}

	public void setMarital(int marital) {
		this.marital = marital;
	}

	public BigDecimal getAutoVerificationAmount() {
		return autoVerificationAmount;
	}

	public void setAutoVerificationAmount(BigDecimal autoVerificationAmount) {
		this.autoVerificationAmount = autoVerificationAmount;
	}

	public String getAllowForceServe() {
		return allowForceServe;
	}

	public void setAllowForceServe(String allowForceServe) {
		this.allowForceServe = allowForceServe;
	}

	public boolean isAllowPlayerNameSetting() {
		return allowPlayerNameSetting;
	}

	public void setAllowPlayerNameSetting(boolean allowPlayerNameSetting) {
		this.allowPlayerNameSetting = allowPlayerNameSetting;
	}

	public int getKycDocumentStatus() {
		return kycDocumentStatus;
	}

	public void setKycDocumentStatus(int kycDocumentStatus) {
		this.kycDocumentStatus = kycDocumentStatus;
	}

//	public AccountStats getAccountStats() {
//		return accountStats;
//	}

//	public void setAccountStats(AccountStats accountStats) {
//		this.accountStats = accountStats;
//	}

	public List<AccountProvider> getAccountProviderList() {
		return accountProviderList;
	}

	public void setAccountProviderList(List<AccountProvider> accountProviderList) {
		this.accountProviderList = accountProviderList;
	}

//	public List<Object> getAccountGroupList() {
//		return accountGroupList;
//	}

	public void setAccountGroupList(List<Object> accountGroupList) {
//		this.accountGroupList = accountGroupList;
	}

	public AtomicInteger getDepositPendingCount() {
		return depositPendingCount;
	}

	public void setDepositPendingCount(AtomicInteger depositPendingCount) {
		this.depositPendingCount = depositPendingCount;
	}

	public AtomicInteger getWithdrawalPendingCount() {
		return withdrawalPendingCount;
	}

	public void setWithdrawalPendingCount(AtomicInteger withdrawalPendingCount) {
		this.withdrawalPendingCount = withdrawalPendingCount;
	}

	public KycPersonalInfo getKycPersonalInfo() {
		return kycPersonalInfo;
	}

	public void setKycPersonalInfo(KycPersonalInfo kycPersonalInfo) {
		this.kycPersonalInfo = kycPersonalInfo;
	}

	public boolean isSessionExpired() {
		return isSessionExpired;
	}

	public void setSessionExpired(boolean sessionExpired) {
		isSessionExpired = sessionExpired;
	}

	// More missing getters and setters
	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setCallingCode(String callingCode) {
		this.callingCode = callingCode;
	}

	public void setCallingCode2(String callingCode2) {
		this.callingCode2 = callingCode2;
	}

	public void setPhoneNumber2(String phoneNumber2) {
		this.phoneNumber2 = phoneNumber2;
	}

	public void setCallingCode3(String callingCode3) {
		this.callingCode3 = callingCode3;
	}

	public void setPhoneNumber3(String phoneNumber3) {
		this.phoneNumber3 = phoneNumber3;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public int getViewedPlayResponsibly() {
		return viewedPlayResponsibly;
	}

	public void setViewedPlayResponsibly(int viewedPlayResponsibly) {
		this.viewedPlayResponsibly = viewedPlayResponsibly;
	}

	@JsonProperty("hasViewedPlayResponsibly")
	public boolean hasViewedPlayResponsibly() {
		return this.viewedPlayResponsibly == BinaryStatusType.ACTIVE.unique();
	}
}
