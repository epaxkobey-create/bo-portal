package com.nv.commons.bo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.ip2location.IPResult;
import com.nv.commons.cache.AccountCache;
import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.cache.MailTemplateCache;
import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountStatusType;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.ContactType;
import com.nv.commons.constants.CountryType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.EncodeType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.MailTemplateType;
import com.nv.commons.constants.MessageDigestType;
import com.nv.commons.constants.OTPType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountContactInfo;
import com.nv.commons.dto.AccountDocument;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.MailTemplate;
import com.nv.commons.dto.RegisterRequest;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.CountryLookup;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PlayResponsiblyUtils;
import com.nv.commons.utils.Validator;
import com.nv.module.engagement.constant.EngagementPurpose;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;
import com.nv.player.controller.validator.BaseRegisterValidator;
import com.nv.player.controller.validator.RegisterValidator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Luke Chi
 */
public class UserBO {

	private static AccountContactInfo createAccountContactInfo(String userId, int websiteType, String content,
		String creator, int contactType) {
		AccountContactInfo info = new AccountContactInfo();
		info.setUserId(userId);
		info.setWebsiteType(websiteType);
		info.setContent(content);
		info.setCreator(creator);
		info.setContactType(contactType);
		return info;
	}

	public static void register(RegisterRequest registerRequest)
		throws Exception {
		registerInternal(registerRequest);
	}

	public static void registerFromBO(RegisterRequest registerRequest)
		throws Exception {
		registerInternal(registerRequest);
	}

	public static void registerInternal(RegisterRequest registerRequest)
		throws Exception {

		RegisterValidator validator = BaseRegisterValidator.getInstance(registerRequest);
		validator.validate();

		WebSiteType webSiteType = registerRequest.getWebSiteType();
		String realIp = registerRequest.getRealIp();
		String email = registerRequest.getEmail();
		String userId = registerRequest.getUserId();
		int currencyTypeId = registerRequest.getCurrencyTypeId();

		List<AccountDocument> applyDocumentList = registerRequest.getApplyDocumentList();

		/*
		 */
		Account account = prepareAccountForRegister(
			registerRequest,
			webSiteType, userId,
			email, realIp);

		List<AccountContactInfo> accountContactInfoList = new ArrayList<>();

		//Account Document
		if (email != null) {
			accountContactInfoList.add(
				createAccountContactInfo(userId, webSiteType.unique(), email, userId, ContactType.Email.unique()));
		}

		AccountBO.registerNewUser(account, accountContactInfoList);

		if (applyDocumentList != null && !applyDocumentList.isEmpty()) {
			// MEMO: 先只允許一個文件
			AccountDocumentBO.apply(userId, webSiteType, applyDocumentList.getFirst());
		}

		boolean isRegisterWithPRSettings = PlayResponsiblyUtils.isPlayResponsiblySettingsRequiredWhenRegister(
			webSiteType, currencyTypeId);

		if (isRegisterWithPRSettings) {
			// apply limits
			applyPlayResponsiblyLimitsWhenRegister(registerRequest, userId, realIp);
			// apply reality check
			applyPlayResponsiblyRealityCheckWhenRegister(registerRequest, userId, realIp);
			// apply account review reminder
			applyPlayResponsiblyAccountReviewReminderWhenRegister(webSiteType, userId, realIp);
		}

		String walletResult = SeamlessWalletApiService.getInstance().createWallet(
			AccountUtils.getUserKey(webSiteType, userId), BigDecimal.ZERO);

		JsonNode data = JSONUtils.toJsonNode(walletResult);

		if (data.get("status").asInt() != 200) {
			LogUtils.SYS.error("createWallet failed: {}", walletResult);
		}
	}

	private static void applyPlayResponsiblyLimitsWhenRegister(RegisterRequest registerRequest,
		String userId, String realIp) throws Exception {

		// apply wagerLimits
		List<AccountPlayResponsiblySetting> wagerLimits =
			Optional.ofNullable(registerRequest.getWagerLimits()).orElseGet(ArrayList::new);
		AccountPlayResponsiblySettingBO.update(userId, AccountPlayResponsiblyType.WAGER_LIMITS,
			wagerLimits, userId, realIp, false, true);

		List<AccountPlayResponsiblySetting> lossLimits =
			Optional.ofNullable(registerRequest.getLossLimits()).orElseGet(ArrayList::new);
		AccountPlayResponsiblySettingBO.update(userId, AccountPlayResponsiblyType.LOSS_LIMITS,
			lossLimits, userId, realIp, false, true);

		List<AccountPlayResponsiblySetting> depositLimitsSettings =
			Optional.ofNullable(registerRequest.getDepositLimits()).orElseGet(ArrayList::new);

		Set<Integer> existingPeriodTypes =
			depositLimitsSettings.stream()
				.map(AccountPlayResponsiblySetting::getPeriodType)
				.collect(Collectors.toSet());

			Arrays.stream(AccountPlayResponsiblyPeriodType.values())
				.map(AccountPlayResponsiblyPeriodType::unique)
				.filter(periodType -> !existingPeriodTypes.contains(periodType))
				.map(periodType -> {
					AccountPlayResponsiblySetting setting = new AccountPlayResponsiblySetting();
					setting.setUserId(userId);
					setting.setWebsiteType(registerRequest.getWebSiteType().unique());
					setting.setType(AccountPlayResponsiblyType.DEPOSIT_LIMITS.unique());
					setting.setPeriodType(periodType);
					setting.setNewValue(String.valueOf(AccountPlayResponsiblyType.DEPOSIT_LIMITS.getDefaultValue()));
					return setting;
				})
				.forEach(depositLimitsSettings::add);

		AccountPlayResponsiblySettingBO.update(userId, AccountPlayResponsiblyType.DEPOSIT_LIMITS,
			depositLimitsSettings, userId, realIp, false, true);
	}

	private static void applyPlayResponsiblyRealityCheckWhenRegister(RegisterRequest registerRequest,
		String userId, String realIp) throws Exception {

		if (registerRequest.getRealityCheck() == null || registerRequest.getRealityCheck().isEmpty()) {

			List<AccountPlayResponsiblySetting> realityChecks = new ArrayList<>();

			realityChecks.add(AccountPlayResponsiblySettingCache.getInstance()
				.getPlayResponsiblyOrDefault(registerRequest.getWebSiteType(), userId,
					AccountPlayResponsiblyType.REALITY_CHECK, AccountPlayResponsiblyPeriodType.DAILY));

			registerRequest.setRealityCheck(realityChecks);
		}

		AccountPlayResponsiblySettingBO.update(userId, AccountPlayResponsiblyType.REALITY_CHECK,
			registerRequest.getRealityCheck(), userId, realIp, false, true);
	}

	private static void applyPlayResponsiblyAccountReviewReminderWhenRegister(WebSiteType webSiteType,
		String userId, String realIp) throws Exception {

		var prList = new ArrayList<AccountPlayResponsiblySetting>();
		var accountReviewReminder = AccountPlayResponsiblySettingBO.getPlayResponsiblyFromCacheOrDB(
			userId, webSiteType, AccountPlayResponsiblyType.ACCOUNT_REVIEW_REMINDER).getFirst();
		prList.add(accountReviewReminder);

		AccountPlayResponsiblySettingBO.update(userId, AccountPlayResponsiblyType.ACCOUNT_REVIEW_REMINDER,
			prList, userId, realIp, false, true);
	}

	@NotNull
	private static Account prepareAccountForRegister(
		RegisterRequest registerRequest,
		WebSiteType webSiteType, String userId,
		String email,
		String realIp) {

		int currencyTypeId = registerRequest.getCurrencyTypeId();

		final CurrencyType currencyType = CurrencyType.getInstance(currencyTypeId);

		String countryCallingCode = registerRequest.getCountryCallingCode();

		CountryType countryType = countryCallingCode == null ?
			WebsiteCurrencySettingCache.getInstance().getDefaultCountryType(webSiteType, currencyType) :
			CountryType.getInstanceByCallingCode(countryCallingCode);

		String password = registerRequest.getPassword();
		String userName = registerRequest.getUserName();
		String legalFirstName = registerRequest.getLegalFirstName();
		String legalLastName = registerRequest.getLegalLastName();
		String dateOfBirth = registerRequest.getDateOfBirth();
		String affiliate = registerRequest.getAffiliateDomain();
		int gender = registerRequest.getGender();
		int marital = registerRequest.getMarital();

		AccountStatusType accountStatusType = registerRequest.getAccountStatusType();

		Account account = new Account();

		account.setWebsiteType(webSiteType.unique());
		account.setUserId(userId);
		account.setUserName(userName);
		account.setLegalFirstName(legalFirstName);
		account.setLegalLastName(legalLastName);
		account.setPassword(MessageDigestType.SHA1.encrypt(password, EncodeType.Hex));

		account.setCurrencyTypeId(currencyTypeId);
		account.setBalance(BigDecimal.ZERO);
		account.setCountryType(countryType.unique());
		account.setCallingCode(countryCallingCode);
		account.setEmail(email);
		account.setAffiliate(affiliate);

		if (StringUtils.isNotBlank(dateOfBirth)) {
			account.setBirthday(DateUtils.toTimestamp(dateOfBirth, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy));
		}

		if (gender != -1) {
			account.setGender(gender);
		}

		if (marital != -1) {
			account.setMarital(marital);
		}

		account.setAllowGameType(GameType.ALL);
		// member level , AccountVipSetting 的 id
		account.setVipLevel(1);

		account.setAutoUpgradeVip(1);

		if (accountStatusType != null) {
			account.setStatus(accountStatusType.unique());
		} else {
			account.setStatus(AccountStatusType.ACTIVE.unique()); // default 0
		}

		account.setMinForceServe(null);
		account.setMinDepositAmount(null);
		// allow null
		account.setLoginFail(0);

		account.setSignUpIp(realIp);

		IPResult ipResult = CountryLookup.getInstance().getIPResult(realIp);
		if (null != ipResult) {
			account.setSignUpCity(ipResult.getCity());
			account.setSignUpState(ipResult.getRegion());
			account.setSignUpCountry(ipResult.getCountryShort());
		}

		// set viewedPlayResponsibly
		if (registerRequest.isFromFrontend()) {
			account.setViewedPlayResponsibly(BinaryStatusType.ACTIVE.unique());
		} else {
			account.setViewedPlayResponsibly(BinaryStatusType.INACTIVE.unique());
		}

		return account;
	}

	public static void validateRegisterRequestByValidatorBeforeRegister(RegisterRequest registerRequest)
		throws Exception {
		RegisterValidator validator = BaseRegisterValidator.getInstance(registerRequest);
		validator.validateBeforeRegister();
	}

	public static Account checkAccountStatus(String userId, WebSiteType webSiteType, LangMessage langMessage) {

		Account account = AccountCache.getInstance().getAccount(webSiteType.unique(), userId);

		if (account == null) {
			return null;
		}

		boolean unlockAccount =
			webSiteType.getWebsiteInfo().getForgotPwdUnlockAccount() == BinaryStatusType.ACTIVE.unique();
		AccountUtils.checkCanForgotPwd(unlockAccount, account);

		String forgetPasswordSendTimeLimit = WebsiteSystemSettingCache.getInstance()
			.getValueByKey(webSiteType.unique(), account.getCurrencyTypeId(),
				WebsiteSystemSettingType.FORGET_PASSWORD_SEND_TIME_LIMIT.unique());

		if (forgetPasswordSendTimeLimit != null) {
			long remainMillisecond = AccountBO.getForgetPasswordSendRemainTime(webSiteType, account.getUserId(),
				forgetPasswordSendTimeLimit);

			if (remainMillisecond > 0) {
				String message = AccountBO.getForgetPasswordSendTimeLimitMessage(langMessage, remainMillisecond);
				throw new Deviation().setI18N("fs.error.message", message);
			}
		}

		return account;
	}

	public static void forgotPassword(Account account, ContactType contactType,
		String urlPrefixForImage, WebSiteType webSiteType, LanguageType languageType)
		throws Exception {

		boolean isEmailContactType = contactType == ContactType.Email;

		boolean unlockAccount =
			webSiteType.getWebsiteInfo().getForgotPwdUnlockAccount() == BinaryStatusType.ACTIVE.unique();

		AccountUtils.checkCanForgotPwd(unlockAccount, account);

		String newPassword = AccountUtils.generatePlayerPassword();

		// MEMO: only Status Locked need change to Active
		unlockAccount = AccountCache.getInstance().getAccount(account.getUserKey()).getStatus()
						== AccountStatusType.LOCKED.unique();

		int count = AccountBO.changePassword(MessageDigestType.SHA1.encrypt(newPassword, EncodeType.Hex),
			account.getUserId(), webSiteType, BinaryStatusType.ACTIVE.unique(), unlockAccount);

		if (count > 0) {
			String code = account.getEmail();
			OTPType otpType = OTPType.FORGOT_PASSWORD_EMAIL;
			AccountBO.forgetPasswordSuccess(account, otpType, code, unlockAccount);
		}

		CurrencyType currencyType = CurrencyType.getInstance(account.getCurrencyTypeId());

		EngagementPurpose purpose = EngagementPurpose.FORGOT_PASSWORD_BY_PLAYER;

		if (isEmailContactType) {

			MailTemplate mailTemplate = MailTemplateCache.getInstance()
				.get(webSiteType, currencyType, languageType, MailTemplateType.PASSWORD);

			String subject = mailTemplate.getTitle();

			var brandName = WebsiteSystemSettingCache.getInstance().getValueByKey(webSiteType.unique(),
				currencyType.unique(), WebsiteSystemSettingType.BRAND_NAME.unique());

			if (StringUtils.isEmpty(brandName)) {
				brandName = SystemConstants.DEFAULT_BRAND_NAME;
			}

			MailTemplateType.PASSWORD.sendMail(subject, account, urlPrefixForImage, purpose, languageType,
				brandName, urlPrefixForImage, newPassword);
		}
	}

	public static boolean changePassword(String oldPassword, String newPassword, WebSiteType webSiteType,
		Account account, AccountUpdateType accountUpdateType,
		String updater, String updaterIp
	) throws Exception {

		if (!Validator.isValidatedPlayerStrictPassword(newPassword)) {
			throw new Deviation("msg.error.password.player.isNotValidated");
		}

		if (oldPassword != null) {
			if (!account.getPassword().equals(MessageDigestType.SHA1.encrypt(oldPassword, EncodeType.Hex))) {
				throw new Deviation("msg.error.validation.oldPassIncorrect");
			}
			if (oldPassword.equals(newPassword)) {
				throw new Deviation("msg.error.validation.newPassSameAsOldPass");
			}
		}

		// save
		final String encryptedPassword = MessageDigestType.SHA1.encrypt(newPassword, EncodeType.Hex);

		final int updateCount = AccountBO
			.changePassword(encryptedPassword, account.getUserId(), webSiteType, BinaryStatusType.INACTIVE.unique(),
				false);
		if (updateCount > 0) {

			AccountUpdateLog accountUpdateLog = AccountUtils
				.getAccountUpdateLog(account.getUserId(), account.getWebsiteType(),
					accountUpdateType,
					new UpdateRecord("-", "-", "Update Password"), updater, updaterIp,
					account.getCurrencyTypeId());

			AccountUpdateLogBO.insert(accountUpdateLog);

			return true;
		} else {
			return false;
		}
	}

}