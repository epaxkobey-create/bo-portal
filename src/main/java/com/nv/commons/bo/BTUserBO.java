package com.nv.commons.bo;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.nv.commons.cache.AccountCache;
import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.cache.AccountProviderCache;
import com.nv.commons.cache.MailTemplateCache;
import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountStatusType;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.BetRuleType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.ContactType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.EncodeType;
import com.nv.commons.constants.KycDocumentStatusType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.MailTemplateType;
import com.nv.commons.constants.MessageDigestType;
import com.nv.commons.constants.PaymentRuleType;
import com.nv.commons.constants.ProviderStatusType;
import com.nv.commons.constants.RealityCheckType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dao.AffiliateDomainDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountContactInfo;
import com.nv.commons.dto.AccountDocument;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.AccountStats;
import com.nv.commons.dto.AccountTracker;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.KycPersonalInfo;
import com.nv.commons.dto.LoginRequest;
import com.nv.commons.dto.MailTemplate;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.RealityCheckReminderData;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.exceptions.AccountLockException;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.system.Setting;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.BigDecimalUtils;
import com.nv.commons.utils.CountryLookup;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ProviderUtils;
import com.nv.module.backendapi.bo.BackendApiBO;
import com.nv.module.backendapi.cache.PlayerLocalCache;
import com.nv.module.backendapi.dto.TokenRes;
import com.nv.module.engagement.constant.EngagementPurpose;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class BTUserBO {

	// for servlet呼叫
	public static Account accountLogin(LoginRequest loginRequest, String sessionId, HttpSession session)
		throws Deviation {

		try {
			String email = loginRequest.getEmail();
			if (email != null) {
				email = email.toLowerCase();
			}

			Account account = AccountBO.getByEmail(email, loginRequest.getWebSiteType());
			if (account == null) {
				LogUtils.SYS.warn("Login attempt failed - account not found for email: {}", email);
				throw new Deviation().setI18N("msg.error.account.loginFailed");
			}

			final WebSiteType webSiteType = WebSiteType.getInstance(account.getWebsiteType());
			final String userId = account.getUserId();

			// confirm user is not lock, inactive, suspend
			AccountUtils.checkAllowLoginStatus(account);

			if (Setting.CHECK_PASSWORD) {
				String password = loginRequest.getPassword();
				// 檢查密碼
				String encryptPassword = MessageDigestType.SHA1.encrypt(password, EncodeType.Hex);
				// 密碼不相等
				if (!account.getPassword().equals(encryptPassword)) {
					// 更新錯誤次數
					account.setLoginFail(account.getLoginFail() + 1);

					if (account.getLoginFail() >= SystemConstants.LOGIN_FAILURE_LIMIT) {
						// 超過錯誤次數
						PlayerBO.loginFailureThenLock(account);
						// lock account and return message
						AccountStatusType currentAccountStatusType = AccountStatusType
							.getInstanceOf(account.getStatus());

						account.setStatus(AccountStatusType.LOCKED.unique());

						AccountUpdateLog accountUpdateLog = AccountUtils.getAccountUpdateLog(userId,
							loginRequest.getWebSiteType().unique(), AccountUpdateType.STATUS,
							new UpdateRecord(String.valueOf(currentAccountStatusType.unique()),
								String.valueOf(AccountStatusType.LOCKED.unique()),
								"Player Status Change From " + currentAccountStatusType.name()
								+ " to " + AccountStatusType.LOCKED.name() + " due to login failure"),
							userId, loginRequest.getLoginIp(), account.getCurrencyTypeId());

						AccountUpdateLogBO.insert(accountUpdateLog);
					} else {
						PlayerBO.addLoginFailure(account);
					}

					if (AccountStatusType.LOCKED.unique() == account.getStatus()) {
						throw new AccountLockException().setI18N("msg.error.account.isLocked");
					}
					account.setLoginFailed(true);
				}
			}

			if (account.isLoginFailed()) {
				// 往前拋的錯誤次數不會大於限制錯誤次數
				int loginFailTimes = Math.min(account.getLoginFail(), SystemConstants.LOGIN_FAILURE_LIMIT);
				throw new Deviation().setI18N("fs.error.account.failCount", String.valueOf(loginFailTimes));
			}

			try {
				String userKey = AccountUtils.getUserKey(webSiteType, userId);

				BigDecimal walletBalance = SeamlessWalletApiService.getInstance().getBalance(userKey);

				account.setBalance(walletBalance);

			} catch (Exception e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}

			account.setServerId(SystemInfo.getInstance().getServerID());
			account.setSessionId(loginRequest.getSessionId());
			account.setLoginIp(loginRequest.getLoginIp());
			account.setDeviceType(loginRequest.getDeviceType().unique());
			account.setPlatformType(loginRequest.getPlatformType().unique());

			List<AccountContactInfo> accountContactInfos = AccountBO.getAccountContactInfos(userId, webSiteType);
			account.setAccountContactInfoList(accountContactInfos);

			// Retrieve and set KYC document information
			retrieveAndSetKycDocument(account, userId, webSiteType);

			// update user login information
			account.setFirstLogin(account.getLoginTime() == null);
			PlayerBO.updateLastLogin(account);

			// Reality Check Time and Prompt
//			AccountPlayResponsiblySetting realityCheck = AccountPlayResponsiblySettingBO.getPlayResponsiblyFromCacheOrDB(
//				userId, webSiteType, AccountPlayResponsiblyType.REALITY_CHECK).getFirst();
//			RealityCheckType realityCheckType = RealityCheckType.getInstanceOf(realityCheck.getCurrentValue());
//
//			RealityCheckReminderData realityCheckReminderData = new RealityCheckReminderData(account.getLoginTime(),
//				realityCheckType.unique());
//			account.setRealityCheckReminderData(realityCheckReminderData);

			PlayerLocalCache.getInstance().add(account, sessionId, session);

			// Prepare and save account tracker for login event
			String affiliateDomain;
			try (Connection conn = DBPool.getReadConnection()) {
				affiliateDomain = AffiliateDomainDAO.getDefault(conn, webSiteType).getDomain();
			} catch (Exception e) {
				LogUtils.SYS.error(e.getMessage(), e);
				throw new Deviation("fail to get affiliate domain");
			}
			final String userCountry = CountryLookup.getInstance().getCountry(loginRequest.getLoginIp());

			prepareAndSaveAccountTracker(loginRequest, account, affiliateDomain, userCountry);

			AccountCache.getInstance().update();

			return account;
		} catch (Deviation ex) {
			throw ex;
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation().setI18N("msg.error.account.loginFailed");
		}
	}

	/**
	 * Retrieve and set KYC document information for the account.
	 * Finds the latest SUMSUB_KYC document and associated personal information.
	 *
	 * @param account the account to set KYC information for
	 * @param userId the user ID
	 * @param webSiteType the website type
	 */
	private static void retrieveAndSetKycDocument(Account account, String userId, WebSiteType webSiteType) {
		AccountDocument accountDocument =
			AccountDocumentBO.findAccountDocuments(userId, webSiteType)
			.stream()
			.filter(doc -> doc.getDocumentType() == DocumentType.SUMSUB_KYC.unique())
			.max(Comparator.comparing(AccountDocument::getId))
			.orElse(null);

		if (accountDocument != null) {
			KycPersonalInfo kycPersonalInfo = KycPersonalInfoBO.find(accountDocument.getId(), userId, webSiteType);
			account.setKycPersonalInfo(kycPersonalInfo);
		}
	}

	/**
	 * Prepare and persist account tracker for login event.
	 * Creates an AccountTracker with login information and saves it to the database.
	 *
	 * @param loginRequest the login request containing user agent, IP, and other login details
	 * @param account the logged in account
	 * @param affiliateDomain the affiliate domain
	 * @param userCountry the user's country based on IP
	 */
	private static void prepareAndSaveAccountTracker(LoginRequest loginRequest, Account account,
		String affiliateDomain, String userCountry) {
		AccountTracker accountTracker = AccountUtils.prepareAccountTracker(
			loginRequest.getUserAgent(),
			loginRequest.getWebSiteType(),
			account,
			affiliateDomain,
			loginRequest.getLoginIp(),
			userCountry,
			loginRequest.getIpTracker(),
			loginRequest.getLoginType(),
			loginRequest.getIsFirstLogin(),
			loginRequest.getBrowserHash(),
			loginRequest.getDeviceHash(),
			loginRequest.getCookieSessionHash()
		);

		AccountTrackerBO.addAccountTracker(accountTracker);
	}

	public static String getPlayerInfo(Account playerInCache, boolean forceCallApi, boolean isLogin) {

		String userId = playerInCache.getUserId();
		WebSiteType webSiteType = WebSiteType.getInstance(playerInCache.getWebsiteType());

		final List<Integer> favoriteGameList;
		if (StringUtils.isNotEmpty(playerInCache.getFavoriteGame())) {
			favoriteGameList = (List<Integer>) JSONUtils.jsonToObject(playerInCache.getFavoriteGame(), List.class);
		} else {
			favoriteGameList = Collections.emptyList();
		}

		JsonGenerateProcessor processor = jGenerator -> {

			if (isLogin) {
				TokenRes tokenRes = BackendApiBO.handleIssueNewToken(playerInCache);
				jGenerator.writeStringField("accessToken", tokenRes.getAccessToken());
				jGenerator.writeStringField("refreshToken", tokenRes.getRefreshToken());
			}

			jGenerator.writeStringField("userId", playerInCache.getUserId());
			jGenerator.writeStringField("userName", playerInCache.getUserName());
			jGenerator.writeStringField("email", playerInCache.getEmail());
			jGenerator.writeNumberField("currencyTypeId", playerInCache.getCurrencyTypeId());

			if (CollectionUtils.isNotEmpty(favoriteGameList)) {
				jGenerator.writeFieldName("favoriteGameIds");
				jGenerator.writeRawValue(JSONUtils.toJsonString(favoriteGameList));
			}

			BigDecimal balance = SeamlessWalletApiService.getInstance().getBalance(playerInCache.getUserKey());
			jGenerator.writeNumberField("balance", balance);

			jGenerator.writeNumberField("accountStatus", playerInCache.getStatus());
			jGenerator.writeBooleanField("isResetPassword", playerInCache.getResetPassword() == 1);
			if (null != playerInCache.getSignUpTime()) {
				jGenerator.writeNumberField("signUpTimestamp", playerInCache.getSignUpTime().getTime());
			}
			if (null != playerInCache.getUpdateTime()) {
				jGenerator.writeNumberField("updateTimestamp", playerInCache.getUpdateTime().getTime());
			}
			if (null != playerInCache.getLastDepositTime()) {
				jGenerator.writeNumberField("lastDepositTimestamp", playerInCache.getLastDepositTime().getTime());
			}
			if (null != playerInCache.getLastWithdrawalTime()) {
				jGenerator.writeNumberField("lastWithdrawalTimestamp", playerInCache.getLastWithdrawalTime().getTime());
			}
			if (null != playerInCache.getLoginTime()) {
				jGenerator.writeNumberField("lastLoginTimestamp", playerInCache.getLoginTime().getTime());
			}
			if (null != playerInCache.getFirstDepositTime()) {
				JSONUtils.checkNumberAndSet(jGenerator, "firstDepositTimestamp",
					playerInCache.getFirstDepositTime().getTime());
			}
			jGenerator.writeBooleanField("isFirstTimeLogin", playerInCache.isFirstLogin());

			jGenerator.writeBooleanField("alreadyUseKycBonusDocument",
				!playerInCache.getUseKycBonusDocumentList().isEmpty());

			int contactVerified = BinaryStatusType.INACTIVE.unique();
			Optional<AccountContactInfo> accountContactInfoResult = playerInCache.getAccountContactInfoList().stream()
				.filter(info -> info.getContactType() == ContactType.Email.unique()
								&& info.getIsDeleted() == BinaryStatusType.INACTIVE.unique())
				.findFirst();

			if (accountContactInfoResult.isPresent()) {
				AccountContactInfo accountContactInfo = accountContactInfoResult.get();
				contactVerified = accountContactInfo.isVerified()
					? BinaryStatusType.ACTIVE.unique()
					: BinaryStatusType.INACTIVE.unique();
			}

			KycDocumentStatusType kycDocumentStatus = KycDocumentStatusType.UNVERIFIED;
			String kycDocumentRemark = "";

			AccountDocument accountDocument = AccountDocumentBO
				.findAccountDocuments(playerInCache.getUserId(), webSiteType)
				.stream()
				.filter(doc -> doc.getDocumentType() == DocumentType.SUMSUB_KYC.unique())
				// TODO: find newest 1 record?
				.max(Comparator.comparing(AccountDocument::getId))
				.orElse(null);
			KycPersonalInfo kycPersonalInfo = null;

			if (accountDocument != null) {
				kycDocumentStatus = accountDocument.getKycDocumentStatus();
				if (kycDocumentStatus == KycDocumentStatusType.VERIFIED) {
					kycPersonalInfo = KycPersonalInfoBO.find(accountDocument.getId(), playerInCache.getUserId(),
						webSiteType);
				}
				if (kycDocumentStatus == KycDocumentStatusType.FAILED) {
					kycDocumentRemark = accountDocument.getApprovedRemark();
				}
			}

			// MEMO: when kycDocumentStatus is Verified, but kycPersonalInfo is null, return
			// as Verifying
			kycDocumentStatus = kycDocumentStatus == KycDocumentStatusType.VERIFIED && kycPersonalInfo == null
				? KycDocumentStatusType.VERIFYING
				: kycDocumentStatus;

			jGenerator.writeStringField("userFullName",
				kycDocumentStatus == KycDocumentStatusType.VERIFIED
					? StringUtils.trimToEmpty(kycPersonalInfo.getFullName())
					: playerInCache.getUserId());

			jGenerator.writeNumberField("contactVerifiedStatus", contactVerified);
			jGenerator.writeNumberField("kycDocumentStatus", kycDocumentStatus.unique());
			jGenerator.writeBooleanField("verificationStatus",
				contactVerified == BinaryStatusType.ACTIVE.unique()
				&& kycDocumentStatus == KycDocumentStatusType.VERIFIED);

			jGenerator.writeStringField("kycDocumentRemark", kycDocumentRemark);

			jGenerator.writeObjectField("kycPersonalInfo", kycPersonalInfo);

			BigDecimal totalMainProviderBalance = BigDecimal.ZERO;
			Set<AccountProvider> accountProviders = AccountProviderCache.getInstance()
				.getAccountProviderSet(playerInCache.getUserId(), webSiteType.unique());

			jGenerator.writeArrayFieldStart("providerExtraData");
			for (AccountProvider accountProvider : accountProviders) {
				try {
					int providerId = accountProvider.getProviderId();
					Provider provider = ProviderCache.getInstance().getProvider(providerId);
					WebsiteProvider websiteProvider = ProviderCache.getInstance()
						.getWebsiteProvider(webSiteType, providerId);

					int status = websiteProvider.getStatus();
					if (provider.getStatus() != ProviderStatusType.ACTIVE.unique()) {
						status = provider.getStatus();
					}

					// for provider inactive
					if (status == ProviderStatusType.INACTIVE.unique()) {
						continue;
					}

					BigDecimal providerBalance = ProviderUtils
						.getProviderBalance(forceCallApi, accountProvider);

					totalMainProviderBalance = BigDecimalUtils.add(totalMainProviderBalance, providerBalance);

					jGenerator.writeStartObject();
					jGenerator.writeNumberField("providerId", providerId);
					jGenerator.writeStringField("providerName", websiteProvider.getDisplayName());
					jGenerator.writeNumberField("status", status);

					if (accountProvider.getProviderExtraData() != null) {
						Map<String, ?> dataMap = accountProvider.getProviderExtraDataMap();
						jGenerator.writeObjectField("dataMap", dataMap);
					}

					if (accountProvider.getExposure() != null) {
						jGenerator.writeStringField("exposure", accountProvider.getExposure().toPlainString());
					}

					jGenerator.writeEndObject();

				} catch (Exception ignore) {
					// TODO: why ignore ?
				}
			}
			jGenerator.writeEndArray();
			jGenerator.writeNumberField("totalMainProviderBalance", totalMainProviderBalance);

			jGenerator.writeNumberField("unreadMessageCount", 0);

			jGenerator.writeObjectField("hasViewedPlayResponsibly", playerInCache.hasViewedPlayResponsibly());

			getAnnualReminderData(jGenerator, userId, webSiteType, playerInCache.getSignUpTime());

			getAccountReviewReminderInfo(jGenerator, userId, webSiteType, playerInCache.getSignUpTime());
		};

		return JSONUtils.getJSONString(processor);
	}

	public static String getProfile(Account playerInCache, WebSiteType webSiteType) {

		String userId = playerInCache.getUserId();

		JsonGenerateProcessor processor = jGenerator -> {

			jGenerator.writeStringField("userId", playerInCache.getUserId());
			jGenerator.writeStringField("userName", playerInCache.getUserName());
			jGenerator.writeStringField("email", playerInCache.getEmail());
			jGenerator.writeNumberField("signUpTimestamp", playerInCache.getSignUpTime().getTime());

			int contactVerified = BinaryStatusType.INACTIVE.unique();
			Optional<AccountContactInfo> accountContactInfoResult = playerInCache.getAccountContactInfoList().stream()
				.filter(info -> info.getContactType() == ContactType.Email.unique()
								&& info.getIsDeleted() == BinaryStatusType.INACTIVE.unique())
				.findFirst();

			if (accountContactInfoResult.isPresent()) {
				AccountContactInfo accountContactInfo = accountContactInfoResult.get();
				contactVerified = accountContactInfo.isVerified()
					? BinaryStatusType.ACTIVE.unique()
					: BinaryStatusType.INACTIVE.unique();
			}

			KycDocumentStatusType kycDocumentStatus = KycDocumentStatusType.UNVERIFIED;
			String kycDocumentRemark = "";

			AccountDocument accountDocument = AccountDocumentBO
				.findAccountDocuments(playerInCache.getUserId(), webSiteType)
				.stream()
				.filter(doc -> doc.getDocumentType() == DocumentType.SUMSUB_KYC.unique())
				// TODO: find newest 1 record?
				.max(Comparator.comparing(AccountDocument::getId))
				.orElse(null);
			KycPersonalInfo kycPersonalInfo = null;

			if (accountDocument != null) {
				kycDocumentStatus = accountDocument.getKycDocumentStatus();
				if (kycDocumentStatus == KycDocumentStatusType.VERIFIED) {
					kycPersonalInfo = KycPersonalInfoBO.find(accountDocument.getId(), playerInCache.getUserId(),
						webSiteType);
				}
				if (kycDocumentStatus == KycDocumentStatusType.FAILED) {
					kycDocumentRemark = accountDocument.getApprovedRemark();
				}
			}

			// MEMO: when kycDocumentStatus is Verified, but kycPersonalInfo is null, return
			// as Verifying
			kycDocumentStatus = kycDocumentStatus == KycDocumentStatusType.VERIFIED && kycPersonalInfo == null
				? KycDocumentStatusType.VERIFYING
				: kycDocumentStatus;

			jGenerator.writeStringField("userFullName",
				kycDocumentStatus == KycDocumentStatusType.VERIFIED
					? StringUtils.trimToEmpty(kycPersonalInfo.getFullName())
					: playerInCache.getUserId());

			jGenerator.writeNumberField("contactVerifiedStatus", contactVerified);
			jGenerator.writeNumberField("kycDocumentStatus", kycDocumentStatus.unique());
			jGenerator.writeBooleanField("verificationStatus",
				contactVerified == BinaryStatusType.ACTIVE.unique()
				&& kycDocumentStatus == KycDocumentStatusType.VERIFIED);

			jGenerator.writeStringField("kycDocumentRemark", kycDocumentRemark);
			jGenerator.writeObjectField("kycPersonalInfo", kycPersonalInfo);

			jGenerator.writeBooleanField("isFirstTimeDeposit", playerInCache.getFirstDepositTime() == null);

			jGenerator.writeBooleanField("isSessionExpired", playerInCache.isSessionExpired());

			jGenerator.writeNumberField("currencyTypeId", playerInCache.getCurrencyTypeId());

			BigDecimal balance = SeamlessWalletApiService.getInstance().getBalance(playerInCache.getUserKey());
			jGenerator.writeNumberField("balance", balance);

			jGenerator.writeNumberField("accountStatus", playerInCache.getStatus());
			jGenerator.writeBooleanField("isResetPassword", playerInCache.getResetPassword() == 1);
			jGenerator.writeBooleanField("alreadyUseKycBonusDocument",
				!playerInCache.getUseKycBonusDocumentList().isEmpty());

			BigDecimal totalMainProviderBalance = BigDecimal.ZERO;
			Set<AccountProvider> accountProviders = AccountProviderCache.getInstance()
				.getAccountProviderSet(playerInCache.getUserId(), webSiteType.unique());

			jGenerator.writeArrayFieldStart("providerExtraData");
			for (AccountProvider accountProvider : accountProviders) {
				try {
					int providerId = accountProvider.getProviderId();
					Provider provider = ProviderCache.getInstance().getProvider(providerId);
					WebsiteProvider websiteProvider = ProviderCache.getInstance()
						.getWebsiteProvider(webSiteType, providerId);

					int status = websiteProvider.getStatus();
					if (provider.getStatus() != ProviderStatusType.ACTIVE.unique()) {
						status = provider.getStatus();
					}

					// for provider inactive
					if (status == ProviderStatusType.INACTIVE.unique()) {
						continue;
					}

					BigDecimal providerBalance = ProviderUtils.getProviderBalance(false, accountProvider);

					totalMainProviderBalance = BigDecimalUtils.add(totalMainProviderBalance, providerBalance);

					jGenerator.writeStartObject();
					jGenerator.writeNumberField("providerId", providerId);
					jGenerator.writeStringField("providerName", websiteProvider.getDisplayName());
					jGenerator.writeNumberField("status", status);

					if (accountProvider.getProviderExtraData() != null) {
						Map<String, ?> dataMap = accountProvider.getProviderExtraDataMap();
						jGenerator.writeObjectField("dataMap", dataMap);
					}

					if (accountProvider.getExposure() != null) {
						jGenerator.writeStringField("exposure", accountProvider.getExposure().toPlainString());
					}

					jGenerator.writeEndObject();

				} catch (Exception ignore) {
					// TODO: why ignore ?
				}
			}
			jGenerator.writeEndArray();
			jGenerator.writeNumberField("totalMainProviderBalance", totalMainProviderBalance);

			jGenerator.writeNumberField("unreadMessageCount", 0);

			jGenerator.writeObjectField("hasViewedPlayResponsibly", playerInCache.hasViewedPlayResponsibly());
		};

		return JSONUtils.getJSONString(processor);
	}

	private static void getAnnualReminderData(JsonGenerator jGenerator, String userId, WebSiteType webSiteType,
		Timestamp accountSignUpTime) throws IOException {

		int signUpYear = accountSignUpTime.toLocalDateTime().getYear();
		int currentYear = DateTimeBuilder.localDateTime().toLocalDateTime().getYear();

		if (signUpYear == currentYear) {
			return;
		}

		var annualReminderValue = AccountPlayResponsiblySettingCache.getInstance()
			.getPlayResponsiblyOrDefault(webSiteType, userId,
				AccountPlayResponsiblyType.ANNUAL_REMINDER, AccountPlayResponsiblyPeriodType.DAILY)
			.getCurrentValue();

		if (Integer.parseInt(annualReminderValue) == BinaryStatusType.INACTIVE.unique()) {

			boolean isWagerLimitSet = AccountPlayResponsiblySettingBO.isLimitsSet(
				userId, webSiteType, AccountPlayResponsiblyType.WAGER_LIMITS, false);
			boolean isLossLimitSet = AccountPlayResponsiblySettingBO.isLimitsSet(
				userId, webSiteType, AccountPlayResponsiblyType.LOSS_LIMITS, false);
			boolean isDepositLimitSet = AccountPlayResponsiblySettingBO.isLimitsSet(
				userId, webSiteType, AccountPlayResponsiblyType.DEPOSIT_LIMITS, false);

			if (isWagerLimitSet && isLossLimitSet && isDepositLimitSet) {
				return;
			}

			jGenerator.writeObjectFieldStart("annualReminderData");
			jGenerator.writeBooleanField("isWagerLimitSet", isWagerLimitSet);
			jGenerator.writeBooleanField("isLossLimitSet", isLossLimitSet);
			jGenerator.writeBooleanField("isDepositLimitSet", isDepositLimitSet);
			jGenerator.writeEndObject();
		}
	}

	private static void getAccountReviewReminderInfo(JsonGenerator jGenerator, String userId, WebSiteType webSiteType,
		Timestamp accountSignUpTime) throws IOException {

		Timestamp now = new Timestamp(System.currentTimeMillis());
		Timestamp effectiveTime = null;

		var accountReviewReminder = AccountPlayResponsiblySettingBO.getPlayResponsiblyFromCacheOrDB(
			userId, webSiteType, AccountPlayResponsiblyType.ACCOUNT_REVIEW_REMINDER).getFirst();

		// Check and set effective time before processing
		if (accountReviewReminder.getEffectiveTime() == null) {
			effectiveTime = Timestamp.valueOf(
				accountSignUpTime.toLocalDateTime()
					.plusMonths(Integer.parseInt(accountReviewReminder.getCurrentValue())));
		} else {
			effectiveTime = accountReviewReminder.getEffectiveTime();
		}

		// trim effective time to 00:00
		effectiveTime = Timestamp.valueOf(effectiveTime.toLocalDateTime().toLocalDate().atStartOfDay());

		if (now.after(effectiveTime)) {
			var months = accountReviewReminder.getCurrentValue();
			jGenerator.writeNumberField("accountReviewReminderMonths", Integer.parseInt(months));
		}
	}

	public static String getAccountStatsSummary(Account playerInCache, WebSiteType webSiteType) {

		JsonGenerateProcessor processor = jGenerator -> {

			BigDecimal deposit = BigDecimal.ZERO;
			BigDecimal withdrawal = BigDecimal.ZERO;
			BigDecimal adjustment = BigDecimal.ZERO;
			BigDecimal turnover = BigDecimal.ZERO;
			BigDecimal profit = BigDecimal.ZERO;

			AccountStats accountStats = AccountBO.getAccountStats(playerInCache.getUserId(), webSiteType);

			if (accountStats != null) {
				deposit = accountStats.getDepositAmount();
				withdrawal = accountStats.getWithdrawalAmount();
				adjustment = accountStats.getAdjustmentAmount();
				turnover = accountStats.getTurnover();
				profit = accountStats.getProfitLoss();
			}

			jGenerator.writeNumberField("deposit", deposit);
			jGenerator.writeNumberField("withdrawal", withdrawal);
			jGenerator.writeNumberField("netDeposit", deposit.subtract(withdrawal));
			jGenerator.writeNumberField("adjustment", adjustment);
			jGenerator.writeNumberField("turnover", turnover);
			jGenerator.writeNumberField("profit", profit);
		};

		return JSONUtils.getJSONString(processor);
	}

	public static void verifyContact(Account playerInCache, WebSiteType webSiteType, ContactType contactType,
		LanguageType languageType) {

		if (contactType == null) {
			throw new Deviation("type is not supported");
		}
		final CurrencyType currencyType = CurrencyType.getInstance(playerInCache.getCurrencyTypeId());

		String contactInfo;

		if (contactType == ContactType.Email) {
			contactInfo = playerInCache.getEmail();

			String verifyCode = AccountContactInfoBO.createVerificationThenReturnVerifyCode(webSiteType,
				playerInCache.getUserId(),
				contactType, currencyType, contactInfo);

			if (verifyCode.isEmpty()) {
				throw new Deviation().setI18N("msg.error.account.verification.createFailed");
			}

			EngagementPurpose purpose = EngagementPurpose.VERIFICATION_BY_PLAYER;

			MailTemplate mailTemplate = MailTemplateCache.getInstance()
				.get(webSiteType, currencyType, languageType, MailTemplateType.VERIFICATION);

			String subject = mailTemplate.getTitle();

			String affiliateDomain;
			try (Connection conn = DBPool.getReadConnection()) {
				affiliateDomain = AffiliateDomainDAO.getDefault(conn, webSiteType).getDomain();
			} catch (Exception e) {
				LogUtils.SYS.error(e.getMessage(), e);
				throw new Deviation("fail to get affiliate domain");
			}
			var brandName = WebsiteSystemSettingCache.getInstance().getValueByKey(webSiteType.unique(),
				currencyType.unique(), WebsiteSystemSettingType.BRAND_NAME.unique());
			if(StringUtils.isEmpty(brandName)) {brandName=SystemConstants.DEFAULT_BRAND_NAME;}

			String verifyLink = "https://" + affiliateDomain + "/verify-account?verify-code=" + verifyCode;
			MailTemplateType.VERIFICATION.sendMail(subject, playerInCache, affiliateDomain, purpose, languageType,
				brandName, affiliateDomain, verifyLink);
		}
	}

	public static void verifyAccount(WebSiteType webSiteType, String verifyCode, String playerIp) throws Exception {
		AccountContactInfoBO.updateVerification(webSiteType, verifyCode, playerIp);
	}

	public static void changePassword(Account playerInCache, String currentPassword,
		String newPassword, WebSiteType webSiteType, String playerIp) throws Exception {

		if (!UserBO.changePassword(currentPassword, newPassword, webSiteType, playerInCache,
			AccountUpdateType.UPDATE_PASSWORD,
			playerInCache.getUserId(), playerIp)) {
			throw new Deviation("msg.error.account.updateFailed");
		}
	}

	public static String checkPlayerLimitStatus(Account playerInCache, LangMessage langMessage) throws Exception {

		SeamlessWalletApiService singleWalletApi = SeamlessWalletApiService.getInstance();

		String result = singleWalletApi.checkPlayerLimitStatus(playerInCache.getUserKey());

		if (result.isEmpty()) {
			throw new Deviation(langMessage.get("check limit status error"));
		}

		JsonNode data = JSONUtils.toJsonNode(result);

		if (data.get("status").asInt() != 200
			|| !data.has("data")
			|| !data.get("data").isArray()
			|| data.get("data").size() != 1) {
			throw new Deviation(langMessage.get("check limit status error"));
		}

		JsonNode dataLimitStatus = data.get("data").get(0);

		if (!dataLimitStatus.has("limitStatus")) {
			throw new Deviation(langMessage.get("check limit status error"));
		}

		JsonNode limitStatus = dataLimitStatus.get("limitStatus");

		if (limitStatus.isObject()) {
			BetRuleType ruleType = BetRuleType.valueOf(limitStatus.get("ruleType").asText());
			AccountPlayResponsiblyType type = ruleType.getAccountPlayResponsiblyType();
			AccountPlayResponsiblyPeriodType periodType = ruleType.getAccountPlayResponsiblyPeriodType();
			BigDecimal maxLimitAmount = BigDecimal.valueOf(limitStatus.get("maxLimitAmount").asDouble());
			BigDecimal currentLimitAmount = BigDecimal.valueOf(limitStatus.get("currentLimitAmount").asDouble());
			BigDecimal remainLimitAmount = BigDecimal.valueOf(limitStatus.get("remainLimitAmount").asDouble());

			JsonGenerateProcessor processor = jGenerator -> {

				jGenerator.writeNumberField("type", type.unique());
				jGenerator.writeNumberField("periodType", periodType.unique());
				jGenerator.writeNumberField("maxLimitAmount", maxLimitAmount);
				jGenerator.writeNumberField("currentLimitAmount", currentLimitAmount);
				jGenerator.writeNumberField("remainLimitAmount", remainLimitAmount);
			};

			return JSONUtils.getJSONString(processor);
		}

		return null;
	}

	public static String getPaymentLimitUsageInfo(Account playerInCache, String typeName, String objectName)
		throws Exception {

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {

			var ruleUsage = SeamlessWalletApiService.getInstance()
				.getPaymentRuleAndUsage(playerInCache.getUserKey(), null);

			if (!StringUtils.isEmpty(typeName)) {
				jGenerator.writeStringField("type", "DEPOSIT_LIMIT_USAGE_UPDATE");
			}

			writePaymentLimitUsageInfo(jGenerator, ruleUsage, objectName, PaymentRuleType.DAILY_DEPOSIT_LIMIT,
				PaymentRuleType.WEEKLY_DEPOSIT_LIMIT, PaymentRuleType.MONTHLY_DEPOSIT_LIMIT);
		};

		return JSONUtils.getJSONString(processor);
	}

	private static void writePaymentLimitUsageInfo(JsonGenerator jGenerator,
		Map<PaymentRuleType, Map<String, BigDecimal>> ruleUsage, String objectName,
		PaymentRuleType dailyRuleType, PaymentRuleType weeklyRuleType, PaymentRuleType monthlyRuleType)
		throws IOException {

		if (!StringUtils.isEmpty(objectName)) {
			jGenerator.writeObjectFieldStart(objectName);
		}

		var dailyRule = ruleUsage.get(dailyRuleType);
		var weeklyRule = ruleUsage.get(weeklyRuleType);
		var monthlyRule = ruleUsage.get(monthlyRuleType);

		jGenerator.writeNumberField("dailyUsage",
			dailyRule == null || dailyRule.isEmpty() ?
				BigDecimal.ZERO.doubleValue() : dailyRule.get("usage").doubleValue());
		jGenerator.writeNumberField("dailyLimit",
			dailyRule == null || dailyRule.isEmpty() ?
				BigDecimal.ZERO.doubleValue() : dailyRule.get("limit").doubleValue());

		jGenerator.writeNumberField("weeklyUsage",
			weeklyRule == null || weeklyRule.isEmpty() ?
				BigDecimal.ZERO.doubleValue() : weeklyRule.get("usage").doubleValue());
		jGenerator.writeNumberField("weeklyLimit",
			weeklyRule == null || weeklyRule.isEmpty() ?
				BigDecimal.ZERO.doubleValue() : weeklyRule.get("limit").doubleValue());

		jGenerator.writeNumberField("monthlyUsage",
			monthlyRule == null || monthlyRule.isEmpty() ?
				BigDecimal.ZERO.doubleValue() : monthlyRule.get("usage").doubleValue());
		jGenerator.writeNumberField("monthlyLimit",
			monthlyRule == null || monthlyRule.isEmpty() ?
				BigDecimal.ZERO.doubleValue() : monthlyRule.get("limit").doubleValue());

		if (!StringUtils.isEmpty(objectName)) {
			jGenerator.writeEndObject();
		}
	}

	public static String getRealityCheckReminderInfo(Account playerInCache, String typeName) throws Exception {

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {

			BigDecimal winLoss = SeamlessWalletApiService.getInstance()
				.getSessionWinLoss(playerInCache.getUserKey(),playerInCache.getLoginTime());

			if (!StringUtils.isEmpty(typeName)) {
				jGenerator.writeStringField("type", typeName);
			}

			writeRealityCheckReminderInfo(jGenerator, playerInCache, winLoss);
		};

		return JSONUtils.getJSONString(processor);
	}

	private static void writeRealityCheckReminderInfo(JsonGenerator jGenerator, Account playerInCache, BigDecimal winLoss)
		throws IOException {

		var reminderData = playerInCache.getRealityCheckReminderData();
		int realActiveTime = reminderData.getRealActiveTime();
		int interval = reminderData.getIntervalInMinutes();

		if (realActiveTime <= 0 || realActiveTime < interval) {
			return;
		}

		jGenerator.writeNumberField("activeTime", realActiveTime);
		jGenerator.writeNumberField("sessionStartTime", playerInCache.getLoginTime().getTime());
		jGenerator.writeNumberField("sessionAmount", winLoss.doubleValue());
		jGenerator.writeNumberField("interval", interval);
	}

	public static String getGameSessionUsageInfo( String typeName, BigDecimal limitSnapshot, BigDecimal usage) throws Exception {
		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			if (!StringUtils.isEmpty(typeName)) {
				jGenerator.writeStringField("type", typeName);
			}
			jGenerator.writeNumberField("maxLimitAmount",limitSnapshot);
			jGenerator.writeNumberField("currentLimitAmount", usage);
		};

		return JSONUtils.getJSONString(processor);
	}
}
