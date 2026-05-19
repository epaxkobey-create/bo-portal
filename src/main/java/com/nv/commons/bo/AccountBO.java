package com.nv.commons.bo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import com.google.common.collect.Sets;
import com.nv.commons.cache.AccountCache;
import com.nv.commons.cache.AccountContactInfoCache;
import com.nv.commons.cache.AccountProviderCache;
import com.nv.commons.cache.OTPRecordCache;
import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.ProviderProxyCache;
import com.nv.commons.constants.AccountRemarkType;
import com.nv.commons.constants.AccountStatusType;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CloseAutoVerifyWithdrawalType;
import com.nv.commons.constants.ContactType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.InternalErrorCodeType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.OTPType;
import com.nv.commons.constants.RegisterType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.UpdatedAttributeType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountContactInfoDAO;
import com.nv.commons.dao.AccountDAO;
import com.nv.commons.dao.AccountDocumentDAO;
import com.nv.commons.dao.AccountRemarkDAO;
import com.nv.commons.dao.AccountStatsDAO;
import com.nv.commons.dao.AffiliateDomainDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountAttach;
import com.nv.commons.dto.AccountBank;
import com.nv.commons.dto.AccountCard;
import com.nv.commons.dto.AccountContactInfo;
import com.nv.commons.dto.AccountDocument;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.AccountRemark;
import com.nv.commons.dto.AccountRequest;
import com.nv.commons.dto.AccountStats;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.AffiliateDomain;
import com.nv.commons.dto.KycPersonalInfo;
import com.nv.commons.dto.Manager;
import com.nv.commons.dto.OTPRecord;
import com.nv.commons.dto.PageResult;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.RegisterRequest;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.exceptions.InternalErrorException;
import com.nv.commons.manager.GlobalThreadPool;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.provider.proxy.ProviderProxy;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.Validator;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PlayerCacheHelper;
import com.nv.module.backendapi.cache.PlayerLocalCache;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 * @author Luke Chi
 */
public class AccountBO {

	public static void registerNewUser(Account account, List<AccountContactInfo> accountContactInfoList) {
		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			// 1. check account
			WebSiteType webSiteType = WebSiteType.getInstance(account.getWebsiteType());
			if (webSiteType == null) {
				throw new Deviation("WebsiteType is invalid");
			}
			String userId = account.getUserId();

			if (AccountCache.getInstance().getAccount(webSiteType.unique(), userId) != null) {
				throw new Deviation("msg.error.account.userId.isExisted");
			}

			long accountId = AccountDAO.getAccountID(conn);

			AccountDAO.save(conn, account, accountId);

			// 2. check contact info
			String accountContactInfoJson = "";
			if (!CollectionUtils.isEmpty(accountContactInfoList)) {

				boolean isNeedCheckUsed = accountContactInfoList.stream().anyMatch(
					o -> Arrays.asList(ContactType.Email.unique()
							//							,
							//							ContactType.Phone.unique()
						)
						.contains(o.getContactType()));

				if (isNeedCheckUsed) {
					int checkResult = AccountContactInfoDAO.checkIsAlreadyRegistered(conn,
						account.getPhoneNumberWithCallingCode(),
						account.getEmail(), account.getWebsiteType());

					if (checkResult != 0) {
						if (checkResult == 1) {
							throw new Deviation("msg.error.account.phone.alreadyUsed");
						} else if (checkResult == 2) {
							throw new Deviation("msg.error.account.email.alreadyUsed");
						} else {
							throw new Deviation("msg.error.register.isNotValidated");
						}
					}
				}

				for (AccountContactInfo contact : accountContactInfoList) {
					ContactType contactType = ContactType.getInstanceOf(contact.getContactType());

					AccountContactInfoDAO.checkDuplicateThenAdd(conn, contact,
						"msg.error.account." + contactType.getName() + ".alreadyUsed");
				}

				accountContactInfoJson = JSONUtils.toJsonString(accountContactInfoList);
			}

			conn.commit();

			if (!CollectionUtils.isEmpty(accountContactInfoList)) {
				Object[][] accountContactInfos = accountContactInfoList
					.stream()
					.map(accountContactInfo -> new Object[] {webSiteType.unique(), accountContactInfo.getContactType(),
						accountContactInfo.getContentNo(), userId})
					.toArray(Object[][]::new);
				AccountContactInfoDAO.refreshUpdateTime(conn, accountContactInfos);
				conn.commit();
			}

			LogUtils.register.info(JSONUtils.getJSONString(
				"Action", "create account",
				"Time", String.valueOf(new Timestamp(System.currentTimeMillis())),
				"AccountInfo", JSONUtils.toJsonString(account),
				"AccountContactInfo", accountContactInfoJson));

			AccountCache.getInstance().update();
			AccountContactInfoCache.getInstance().update();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			if (e instanceof Deviation) {
				throw (Deviation) e;
			}
			throw new InternalErrorException(InternalErrorCodeType.REGISTER_NEW_USER_ERROR, e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
	}

	public static void resetUpdateAttribute(
		final Map<String, PlayerCacheHelper.UpdatedAttributeRecord> updatedAttributeRecordMap) {
		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			for (PlayerCacheHelper.UpdatedAttributeRecord updatedAttributeRecord : updatedAttributeRecordMap.values()) {

				for (Integer updatedAttribute : updatedAttributeRecord.getSumOfUpdatedAttributes()) {

					AccountDAO
						.resetUpdateAttribute(conn, updatedAttributeRecord.getUserIdWebSiteId(), updatedAttribute);

					conn.commit();
				}
			}
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
	}

	/*
	 * Server init
	 */
	public static void clearWebSessionAndServerId() {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);
			int count = AccountDAO.clearWebSessionAndServerId(conn);

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
	}

	public static String searchMember(AccountRequest accountRequest) {

		try {
			// 有email，先查詢accountInfo取得唯一的userId，使用userId查詢account
			if (accountRequest.hasContactInfo()) {

				HashSet<String> userIdSetByContactInfo = new HashSet<>();

				for (Map.Entry<ContactType, String> entry : accountRequest.getContactInfoCondition().entrySet()) {

					ContactType contactType = entry.getKey();

					String content = entry.getValue();
					if (content != null) {

						List<String> userIdByContactInfo = DbExecutor.query(conn ->
							AccountContactInfoDAO.getUserIdsByWildCardSearchEmail(conn,
								accountRequest.getWebSiteType(), contactType, content));

						userIdSetByContactInfo.addAll(userIdByContactInfo);

						// 根據聯絡資訊查詢時不應查到多個userId
						if (userIdByContactInfo.isEmpty()) {
							return JSONUtils.EMPTY_JSON_ARRAY_STRING;
						}
					}
				}

				Set<String> userIds = Optional.ofNullable(accountRequest.getUserIds())
					.map(userIdWithComma -> userIdWithComma.split(","))
					.map(Sets::newHashSet)
					.orElse(userIdSetByContactInfo);

				userIdSetByContactInfo.retainAll(userIds);

				if (!userIds.isEmpty() && userIdSetByContactInfo.isEmpty()) {
					return JSONUtils.EMPTY_JSON_ARRAY_STRING;
				}

				accountRequest.setUserIds(String.join(",", userIdSetByContactInfo));
			}

			return mergeMemberJson(accountRequest);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		}
	}

	public static boolean createMemberAccount(Manager manager, LangMessage lang, String userId,
		String password, String confirmPassword, String email,
		String loginIp, AccountStatusType accountStatusType)
		throws Exception {

		WebSiteType websiteType = manager.getWebsiteTypeObj();
		String affiliateDomainString;
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			AffiliateDomain affiliateDomainDto = AffiliateDomainDAO.getDefault(conn, websiteType);
			affiliateDomainString = affiliateDomainDto.getDomain();

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		if (email == null || email.isEmpty() || !Validator.isValidatedEmail(email)) {
			throw new Deviation().setI18N("fs.parameter.validation", "email");
		}
		String username = email.toLowerCase().substring(0, email.indexOf("@"));

		RegisterRequest registerRequest = RegisterRequest.builder()
			.webSiteType(websiteType)
			.languageType(LanguageType.getInstance(lang.getLang()))
			.userId(userId)
			.userName(username)
			.password(password)
			.confirmPassword(confirmPassword != null ? confirmPassword : password)
			.currencyTypeId(CurrencyType.EUR.unique())
			.email(email)
			.realIp(loginIp)
			.contactTypeId(-1)
			.accountStatusType(accountStatusType)
			.registerType(RegisterType.ACCOUNT.unique())
			.isFromFrontend(false)
			.affiliateDomain(affiliateDomainString)
			.build();

		UserBO.registerFromBO(registerRequest);

		return true;
	}

	private static String mergeMemberJson(AccountRequest accountRequest) throws Exception {

		String memberJson = DbExecutor.query(conn ->
			AccountDAO.getMemberJsonByMultiCondition(conn, accountRequest));

		WebSiteType webSiteType = accountRequest.getWebSiteType();
		ObjectMapper objectMapper = JSONUtils.getObjectMapper();
		JsonNode memberListNode = objectMapper.readTree(memberJson);
		Set<String> userIds = new HashSet<>(memberListNode.findValuesAsText("userId"));

		Set<Integer> contactTypes = accountRequest.getContactInfoCondition().keySet().stream()
			.map(ContactType::unique)
			.collect(Collectors.toSet());

		Map<String, Map<ContactType, List<AccountContactInfo>>> contactInfoByUserIdMap =
			DbExecutor.query(conn ->
				AccountContactInfoDAO.getContactInfoMapByUserIds(conn, webSiteType, userIds, contactTypes));

		List<ObjectNode> newMemberNodeList = StreamSupport.stream(memberListNode.spliterator(), false)
			.map(node -> {

				String userId = node.get("userId").asText();

				ObjectNode member = (ObjectNode) node;

				Map<ContactType, List<AccountContactInfo>> contactInfoMap = contactInfoByUserIdMap.getOrDefault(userId,
					new LinkedHashMap<>());

				contactTypes.forEach(eachTypeInt -> {

					ContactType eachContactType = ContactType.getInstanceOf(eachTypeInt);
					String contactName = Optional.ofNullable(eachContactType)
						.map(ContactType::getName)
						.orElse("");

					if (contactInfoMap.get(eachContactType) == null) {

						JsonGenerateProcessor processor = jGenerator -> {
							jGenerator.writeNullField(contactName);
							jGenerator.writeNumberField("verified", 0);
						};

						member.putRawValue(contactName, new RawValue(JSONUtils.getJSONString(processor)));
					} else {
						List<AccountContactInfo> contactInfos = contactInfoMap.getOrDefault(eachContactType,
							new ArrayList<>());

						contactInfos.stream()
							.filter(contactInfo -> contactInfo.getContentNo() == 1)
							.findFirst()
							.ifPresent(contactInfo -> {

								Map<String, Object> contentMap = new LinkedHashMap<>();

								if (ContactType.Email.equals(eachContactType)) {

									if (accountRequest.isEnableShowEmail()) {
										contentMap.put(contactName, contactInfo.getContent());
										contentMap.put("verified", contactInfo.getVerifiedType());
										member.putPOJO(contactName, contentMap);
									} else {
										member.put(contactName, "");
									}
								}
							});
					}
				});

				// 使用總餘額排序時，總餘額會在sql查詢階段就取得，此處不需重複查詢
				//				if (!AccountSortType.TOTAL_BALANCE.getSortCondition().equals(accountRequest.getSortCondition())) {
				try {
					BigDecimal currentBalance = SeamlessWalletApiService.getInstance()
						.getBalance(
							AccountUtils.getUserKey(
								member.get("webSiteType").asInt(),
								member.get("userId").asText()
							)
						);

					BigDecimal finalBalance = currentBalance != null
						? currentBalance.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
						: BigDecimal.ZERO;

					member.put("totalBalance", finalBalance);

				} catch (Exception e) {
					LogUtils.SYS.error(e.getMessage(), e);
					member.putNull("totalBalance");
				}
				//				}

				return member;

			}).collect(Collectors.toList());

		return JSONUtils.toJsonString(newMemberNodeList);
	}

	public static AccountAttach getProfileOverview(String userId, WebSiteType webSiteType, int accountCurrency) {

		final boolean enableViewIp = true;

		Connection conn = null;
		AccountAttach account;
		List<AccountContactInfo> accountContactList;
		List<AccountDocument> accountDocumentList;
		List<AccountRemark> accountRemarkList;
		List<AccountBank> accountBankList;
		AccountStats accountStats;
		AccountCard accountCard;

		try {
			conn = DBPool.getReadConnection();
			account = AccountDAO.getProfileOverview(conn, userId, webSiteType, accountCurrency);
			if (account == null) {
				return null;
			}


			accountContactList = AccountContactInfoDAO.findAccountContactDataByUserId(conn, userId, webSiteType);
			accountDocumentList = AccountDocumentDAO.findDocumentByUserId(conn, userId, webSiteType);

			//			List<Integer> remarkTypeFilter = Arrays.stream(AccountRemarkType.values()).map(AccountRemarkType::unique)
			//				.collect(Collectors.toList());

			accountRemarkList = AccountRemarkDAO.getAllRemarkType(conn, webSiteType.unique(), userId);

			/// TODO: 在AccountStatus表整理好前, 先直接從交易表拉資料
			accountStats = AccountStatsDAO.getStatsByUserIdFromTransaction(conn, userId, webSiteType);

			accountBankList = AccountBankBO.getAccountBankByUserId(userId, webSiteType.unique(), false);
			accountCard = AccountCardBO.findFirstActiveCardByUserId(account);


			List<Provider> allProvider = ProviderCache.getInstance().getProvider(webSiteType);
			List<AccountProvider> accountProviderList = getAccountProvider(allProvider, userId, webSiteType,
				accountCurrency);

			account.setAccountProviderList(accountProviderList);

			account.setAccountContactInfoListForJsonView(accountContactList);

			if (!enableViewIp) {
				account.setLoginIp(null);
				account.setLoginTime(null);
				account.setLoginTimeStr(null);
			}

			account.setAccountDocumentList(accountDocumentList);

			accountRemarkList = accountRemarkList.stream()
				.peek(remark -> remark.setRemark(StringEscapeUtils.unescapeHtml4(remark.getRemark()))).toList();
			account.setAccountRemarkList(accountRemarkList);

			if (accountBankList != null) {
				account.setAccountBank(accountBankList);
			}
			if (accountCard != null) {
				account.setAccountCard(accountCard);
			}

			account.setAccountStats(accountStats);

		} catch (Exception e) {
			LogUtils.SYS.error("AccountBO.getProfileOverview error:" + e.getMessage(), e);
			return null;
		} finally {
			DbUtils.close(conn);
		}
		return account;
	}

	public static AccountStats getAccountStats(String userId, WebSiteType webSiteType) {
		try {
			// TODO: 在AccountStatus表整理好前, 先直接從交易表拉資料
			return DbExecutor.query(conn ->
				AccountStatsDAO.getStatsByUserIdFromTransaction(conn, userId, webSiteType));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		}
	}

	public static String getProfileOverviewWithJGenerator(String userId, WebSiteType webSiteType, int accountCurrency) {
		AccountAttach accountAttach = getProfileOverview(userId, webSiteType, accountCurrency);
		if (accountAttach == null) {
			LogUtils.managerBO.warn(
				MessageFormat.format(
					"AccountAttach object is null, userId: {0}, webSiteType: {1}, accountCurrency: {2}",
					userId, webSiteType, accountCurrency));
			return null;
		} else {
			LogUtils.managerBO.info(MessageFormat.format(
				"AccountAttach object get successfully, userId: {0}, webSiteType: {1}, accountCurrency: {2}",
				userId, webSiteType, accountCurrency));

		}
		String resultString = null;

		try {
			resultString = JSONUtils.getJSONString(jGenerator -> {
				jGenerator.writeStringField("userId", accountAttach.getUserId());
				jGenerator.writeStringField("affiliate", accountAttach.getAffiliate());
				jGenerator.writeStringField("userName", accountAttach.getUserName());
				jGenerator.writeStringField("legalFirstName", accountAttach.getLegalFirstName());
				jGenerator.writeStringField("legalLastName", accountAttach.getLegalLastName());
				jGenerator.writeNumberField("countryType", accountAttach.getCountryType());
				jGenerator.writeStringField("phoneNumber", accountAttach.getPhoneNumber());
				jGenerator.writeStringField("callingCode", accountAttach.getCallingCode());
				jGenerator.writeStringField("address", accountAttach.getAddress());
				jGenerator.writeStringField("email", accountAttach.getEmail());
				jGenerator.writeStringField("birthday", accountAttach.getBirthdayStr());
				jGenerator.writeStringField("birthdayStr", accountAttach.getBirthdayStr());

				jGenerator.writeNumberField("vipLevel", accountAttach.getVipLevel());
				jGenerator.writeStringField("vipLevelName", accountAttach.getVipLevelName());
				jGenerator.writeStringField("occupation", accountAttach.getOccupation());

				BigDecimal balance = SeamlessWalletApiService.getInstance().getBalance(accountAttach.getUserKey());
				jGenerator.writeNumberField("balance", balance);

				jGenerator.writeNumberField("currencyTypeId", accountAttach.getCurrencyTypeId());
				jGenerator.writeNumberField("status", accountAttach.getStatus());
				if (accountAttach.getAccountRemarkList() != null) {
					for (AccountRemark accountRemark : accountAttach.getAccountRemarkList()) {
						AccountRemarkType remarkType = AccountRemarkType.getInstance(accountRemark.getRemarkType());
						jGenerator.writeStringField(remarkType.name(), accountRemark.getRemark());
						jGenerator.writeStringField(remarkType.name() + "UpdateTime",
							FormatUtils.dateFormat(accountRemark.getUpdateTime(),
								FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd_HHmmss));
						jGenerator.writeStringField(remarkType.name() + "Updater", accountRemark.getUpdater());
					}
				}
				if (accountAttach.getSignUpTime() != null) {
					jGenerator.writeNumberField("signUpTime", accountAttach.getSignUpTime().getTime());
				} else {
					jGenerator.writeNullField("signUpTime");
				}
				jGenerator.writeStringField("signUpTimeStr", accountAttach.getSignUpTimeStr());
				jGenerator.writeStringField("signUpIp", accountAttach.getSignUpIp());
				jGenerator.writeStringField("signUpCountry", accountAttach.getSignUpCountry());
				jGenerator.writeStringField("signUpState", accountAttach.getSignUpState());
				jGenerator.writeStringField("signUpCity", accountAttach.getSignUpCity());
				if (accountAttach.getLoginTime() != null) {
					jGenerator.writeNumberField("loginTime", accountAttach.getLoginTime().getTime());
				} else {
					jGenerator.writeNullField("loginTime");
				}
				jGenerator.writeStringField("loginTimeStr", accountAttach.getLoginTimeStr());
				jGenerator.writeStringField("loginIp", accountAttach.getLoginIp());
				if (accountAttach.getCreateTime() != null) {
					jGenerator.writeNumberField("createTime", accountAttach.getCreateTime().getTime());
				} else {
					jGenerator.writeNullField("createTime");
				}
				if (accountAttach.getFirstDepositTime() != null) {
					jGenerator.writeNumberField("firstDepositTime", accountAttach.getFirstDepositTime().getTime());
				} else {
					jGenerator.writeNullField("firstDepositTime");
				}
				jGenerator.writeStringField("firstDepositTimeStr", accountAttach.getFirstDepositTimeStr());
				if (accountAttach.getLastDepositTime() != null) {
					jGenerator.writeNumberField("lastDepositTime", accountAttach.getLastDepositTime().getTime());
				} else {
					jGenerator.writeNullField("lastDepositTime");
				}
				jGenerator.writeStringField("lastDepositTimeStr", accountAttach.getLastDepositTimeStr());
				if (accountAttach.getFirstWithdrawalTime() != null) {
					jGenerator.writeNumberField("firstWithdrawalTime",
						accountAttach.getFirstWithdrawalTime().getTime());
				} else {
					jGenerator.writeNullField("firstWithdrawalTime");
				}
				jGenerator.writeStringField("firstWithdrawalTimeStr", accountAttach.getFirstWithdrawalTimeStr());
				if (accountAttach.getLastWithdrawalTime() != null) {
					jGenerator.writeNumberField("lastWithdrawalTime", accountAttach.getLastWithdrawalTime().getTime());
				} else {
					jGenerator.writeNullField("lastWithdrawalTime");
				}
				jGenerator.writeStringField("lastWithdrawalTimeStr", accountAttach.getLastWithdrawalTimeStr());
				if (accountAttach.getFirstAdjustmentTime() != null) {
					jGenerator.writeNumberField("firstAdjustmentTime",
						accountAttach.getFirstAdjustmentTime().getTime());
				} else {
					jGenerator.writeNullField("firstAdjustmentTime");
				}
				jGenerator.writeStringField("firstAdjustmentTimeStr", accountAttach.getFirstAdjustmentTimeStr());
				if (accountAttach.getLastAdjustmentTime() != null) {
					jGenerator.writeNumberField("lastAdjustmentTime", accountAttach.getLastAdjustmentTime().getTime());
				} else {
					jGenerator.writeNullField("lastAdjustmentTime");
				}
				jGenerator.writeStringField("lastAdjustmentTimeStr", accountAttach.getLastAdjustmentTimeStr());
				if (accountAttach.getFirstBonusTime() != null) {
					jGenerator.writeNumberField("firstBonusTime", accountAttach.getFirstBonusTime().getTime());
				} else {
					jGenerator.writeNullField("firstBonusTime");
				}
				jGenerator.writeStringField("firstBonusTimeStr", accountAttach.getFirstBonusTimeStr());
				if (accountAttach.getLastBonusTime() != null) {
					jGenerator.writeNumberField("lastBonusTime", accountAttach.getLastBonusTime().getTime());
				} else {
					jGenerator.writeNullField("lastBonusTime");
				}
				jGenerator.writeStringField("lastBonusTimeStr", accountAttach.getLastBonusTimeStr());
				if (accountAttach.getDepositAmount() != null) {
					jGenerator.writeNumberField("depositAmount", accountAttach.getDepositAmount());
				} else {
					jGenerator.writeNullField("depositAmount");
				}
				if (accountAttach.getFirstBetTime() != null) {
					jGenerator.writeNumberField("firstBetTime", accountAttach.getFirstBetTime().getTime());
				} else {
					jGenerator.writeNullField("firstBetTime");
				}
				jGenerator.writeStringField("firstBetTimeStr", accountAttach.getFirstBetTimeStr());
				if (accountAttach.getLastBetTime() != null) {
					jGenerator.writeNumberField("lastBetTime", accountAttach.getLastBetTime().getTime());
				} else {
					jGenerator.writeNullField("lastBetTime");
				}
				jGenerator.writeStringField("lastBetTimeStr", accountAttach.getLastBetTimeStr());
				if (accountAttach.getMinDepositAmount() != null) {
					jGenerator.writeNumberField("minDepositAmount", accountAttach.getMinDepositAmount());
				} else {
					jGenerator.writeNullField("minDepositAmount");
				}
				if (accountAttach.getSummaryConvertionPoint() != null) {
					jGenerator.writeNumberField("summaryConvertionPoint", accountAttach.getSummaryConvertionPoint());
				} else {
					jGenerator.writeNullField("summaryConvertionPoint");
				}
				jGenerator.writeStringField("affiliateName", accountAttach.getAffiliateName());
				jGenerator.writeNumberField("userChannelType", accountAttach.getUserChannelType());

				jGenerator.writeNumberField("gender", accountAttach.getGender());
				jGenerator.writeNumberField("marital", accountAttach.getMarital());

				jGenerator.writeObjectFieldStart("accountStats");
				jGenerator.writeNumberField("depositCount", 0);

				jGenerator.writeNullField("depositAmount");

				jGenerator.writeNumberField("withdrawalCount", 0);

				jGenerator.writeNullField("withdrawalAmount");

				jGenerator.writeNumberField("adjustmentCount", 0);

				jGenerator.writeNullField("adjustmentAmount");

				jGenerator.writeNumberField("bonusCount", 0);

				jGenerator.writeNullField("bonusAmount");

				jGenerator.writeNullField("profitLoss");

				jGenerator.writeNullField("turnover");

				jGenerator.writeNullField("transferIn");

				jGenerator.writeNullField("transferOut");

				jGenerator.writeNullField("pointToBalance");

				jGenerator.writeNullField("recycleBalance");

				jGenerator.writeEndObject();

				jGenerator.writeArrayFieldStart("accountProviderList");
				for (AccountProvider accountProvider : accountAttach.getAccountProviderList()) {
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("providerId", accountProvider.getProviderId());
					jGenerator.writeStringField("providerAccount", accountProvider.getProviderAccount());
					if (accountProvider.getProviderBalance() != null) {
						jGenerator.writeNumberField("providerBalance", accountProvider.getProviderBalance());
					} else {
						jGenerator.writeNullField("providerBalance");
					}
					if (accountProvider.getProviderCreateTime() != null) {
						jGenerator.writeNumberField("providerCreateTime",
							accountProvider.getProviderCreateTime().getTime());
					} else {
						jGenerator.writeNullField("providerCreateTime");
					}
					jGenerator.writeStringField("providerExtraData", accountProvider.getProviderExtraData());
					jGenerator.writeStringField("providerBOAccount", accountProvider.getProviderBOAccount());
					if (accountProvider.getExposure() != null) {
						jGenerator.writeNumberField("exposure", accountProvider.getExposure());
					} else {
						jGenerator.writeNullField("exposure");
					}
					jGenerator.writeNumberField("bonusTurnoverId", accountProvider.getBonusTurnoverId());
					Provider provider = accountProvider.getProvider();
					jGenerator.writeObjectFieldStart("provider");
					jGenerator.writeStringField("systemCode", provider.getSystemCode());
					jGenerator.writeStringField("providerName", provider.getProviderName());
					jGenerator.writeEndObject();
					jGenerator.writeEndObject();
				}
				jGenerator.writeEndArray();

				jGenerator.writeArrayFieldStart("accountGroupList");

				jGenerator.writeEndArray();

				if (accountAttach.getAccountBonusProviderList() == null) {
					jGenerator.writeNullField("accountBonusProviderList");
				} else {
					jGenerator.writeArrayFieldStart("accountBonusProviderList");
					for (AccountProvider accountProvider : accountAttach.getAccountBonusProviderList()) {
						jGenerator.writeStartObject();
						jGenerator.writeNumberField("providerId", accountProvider.getProviderId());
						jGenerator.writeStringField("providerAccount", accountProvider.getProviderAccount());
						if (accountProvider.getProviderBalance() != null) {
							jGenerator.writeNumberField("providerBalance", accountProvider.getProviderBalance());
						} else {
							jGenerator.writeNullField("providerBalance");
						}
						if (accountProvider.getProviderCreateTime() != null) {
							jGenerator.writeNumberField("providerCreateTime",
								accountProvider.getProviderCreateTime().getTime());
						} else {
							jGenerator.writeNullField("providerCreateTime");
						}
						jGenerator.writeStringField("providerExtraData", accountProvider.getProviderExtraData());
						jGenerator.writeStringField("providerBOAccount", accountProvider.getProviderBOAccount());
						if (accountProvider.getExposure() != null) {
							jGenerator.writeNumberField("exposure", accountProvider.getExposure());
						} else {
							jGenerator.writeNullField("exposure");
						}
						jGenerator.writeNumberField("bonusTurnoverId", accountProvider.getBonusTurnoverId());
						Provider provider = accountProvider.getProvider();
						jGenerator.writeObjectFieldStart("provider");
						jGenerator.writeStringField("systemCode", provider.getSystemCode());
						jGenerator.writeStringField("providerName", provider.getProviderName());
						jGenerator.writeEndObject();
						jGenerator.writeEndObject();
					}
					jGenerator.writeEndArray();
				}

				if (accountAttach.getWalletBalance() != null) {
					jGenerator.writeNumberField("walletBalance", accountAttach.getWalletBalance());
				} else {
					jGenerator.writeNullField("walletBalance");
				}

				jGenerator.writeStringField("bonusCode", accountAttach.getBonusCode());

				AccountDocument sumsubAccountDocument = accountAttach.getAccountDocumentList().stream().findFirst()
					.orElse(null);

				if (sumsubAccountDocument != null) {
					KycPersonalInfo kycPersonalInfo = KycPersonalInfoBO.find(sumsubAccountDocument.getId(), userId,
						webSiteType);

					if (kycPersonalInfo != null) {
						jGenerator.writeFieldName("sumsubAccountDocument");
						jGenerator.writeStartObject();
						jGenerator.writeNumberField("id", sumsubAccountDocument.getId());
						DocumentType documentType = DocumentType.getInstance(sumsubAccountDocument.getDocumentType());
						if (documentType != null) {
							jGenerator.writeStringField("documentType", documentType.getFullName(null));
						}
						jGenerator.writeStringField("documentNo", kycPersonalInfo.getDocumentNo());
						jGenerator.writeStringField("fullName", kycPersonalInfo.getFullName());
						if (kycPersonalInfo.getDob() != null) {
							jGenerator.writeNumberField("dob", kycPersonalInfo.getDob().getTime());
						}
						if (sumsubAccountDocument.getExpiredDate() != null) {
							jGenerator.writeNumberField("expiryDate", sumsubAccountDocument.getExpiredDate().getTime());
						}
						jGenerator.writeStringField("street", kycPersonalInfo.getStreet());
						jGenerator.writeStringField("city", kycPersonalInfo.getCity());
						jGenerator.writeStringField("postalCode", kycPersonalInfo.getPostalCode());
						jGenerator.writeNumberField("accountDocumentStatusType",
							AccountDocumentBO.getKycDocumentStatus(sumsubAccountDocument.getStatus()).unique());
						jGenerator.writeStringField("approveRemark", sumsubAccountDocument.getApprovedRemark());
						jGenerator.writeEndObject();
					}
				}

				if (accountAttach.getAccountBank() == null) {
					jGenerator.writeNullField("accountBank");
				} else {
					List<AccountBank> accountBanks = accountAttach.getAccountBank();
					jGenerator.writeArrayFieldStart("accountBank");
					for(AccountBank bankDetail: accountBanks){
						jGenerator.writeStartObject();
						jGenerator.writeStringField("bankName", bankDetail.getBankName());
						jGenerator.writeStringField("accountNumber", bankDetail.getBankAccNumber());
						jGenerator.writeStringField("id",String.valueOf(bankDetail.getId()));
						jGenerator.writeEndObject();

					}
					jGenerator.writeEndArray();
				}
				if (accountAttach.getAccountCard() == null) {
					jGenerator.writeNullField("accountCard");
				} else {
					AccountCard accountCard = accountAttach.getAccountCard();
					jGenerator.writeFieldName("accountCard");
					jGenerator.writeStartObject();
					jGenerator.writeStringField("bankName", accountCard.getBankName());
					jGenerator.writeStringField("cardBrand", accountCard.getCardSchemeType());
					jGenerator.writeStringField("cardNumber", accountCard.getCardNo());
					jGenerator.writeStringField("expiryDate", accountCard.getExpMonthYear());
					jGenerator.writeStringField("cardholderName", accountCard.getCardholderName());
					jGenerator.writeEndObject();
				}

				AccountStats accountStats = accountAttach.getAccountStats();
				BigDecimal depositAmount = BigDecimal.ZERO;
				BigDecimal withdrawalAmount = BigDecimal.ZERO;
				jGenerator.writeObjectFieldStart("accountStats");
				jGenerator.writeNumberField("depositCount", accountStats.getDepositCount());
				if (accountStats.getDepositAmount() != null) {
					depositAmount = accountStats.getDepositAmount();
					jGenerator.writeNumberField("depositAmount", depositAmount);
				} else {
					jGenerator.writeNullField("depositAmount");
				}
				jGenerator.writeNumberField("withdrawalCount", accountStats.getWithdrawalCount());
				if (accountStats.getWithdrawalAmount() != null) {
					withdrawalAmount = accountStats.getWithdrawalAmount();
					jGenerator.writeNumberField("withdrawalAmount", withdrawalAmount);
				} else {
					jGenerator.writeNullField("withdrawalAmount");
				}
				jGenerator.writeNumberField("netDepositAmount", depositAmount.subtract(withdrawalAmount));
				jGenerator.writeNumberField("adjustmentCount", accountStats.getAdjustmentCount());
				if (accountStats.getAdjustmentAmount() != null) {
					jGenerator.writeNumberField("adjustmentAmount", accountStats.getAdjustmentAmount());
				} else {
					jGenerator.writeNullField("adjustmentAmount");
				}
				jGenerator.writeNumberField("bonusCount", accountStats.getBonusCount());
				if (accountStats.getBonusAmount() != null) {
					jGenerator.writeNumberField("bonusAmount", accountStats.getBonusAmount());
				} else {
					jGenerator.writeNullField("bonusAmount");
				}
				if (accountStats.getProfitLoss() != null) {
					jGenerator.writeNumberField("profitLoss", accountStats.getProfitLoss());
				} else {
					jGenerator.writeNullField("profitLoss");
				}
				if (accountStats.getTurnover() != null) {
					jGenerator.writeNumberField("turnover", accountStats.getTurnover());
				} else {
					jGenerator.writeNullField("turnover");
				}
				if (accountStats.getTransferIn() != null) {
					jGenerator.writeNumberField("transferIn", accountStats.getTransferIn());
				} else {
					jGenerator.writeNullField("transferIn");
				}
				if (accountStats.getTransferOut() != null) {
					jGenerator.writeNumberField("transferOut", accountStats.getTransferOut());
				} else {
					jGenerator.writeNullField("transferOut");
				}
				if (accountStats.getPointToBalance() != null) {
					jGenerator.writeNumberField("pointToBalance", accountStats.getPointToBalance());
				} else {
					jGenerator.writeNullField("pointToBalance");
				}
				if (accountStats.getRecycleBalance() != null) {
					jGenerator.writeNumberField("recycleBalance", accountStats.getRecycleBalance());
				} else {
					jGenerator.writeNullField("recycleBalance");
				}
				jGenerator.writeEndObject();
			});
		} catch (Exception e) {
			LogUtils.managerBO.error("AccountBO.getProfileOverviewWithJGenerator error:" + e.getMessage(), e);
		}

		return resultString;
	}

	public static List<AccountProvider> getAccountProvider(List<Provider> allProvider, String userId,
		WebSiteType webSiteType, int accountCurrency) {

		List<AccountProvider> accountProviderList = new ArrayList<>();
		Map<Integer, AccountProvider> accountProviderMap = AccountProviderCache.getInstance()
			.getAccountProviderSet(userId, webSiteType.unique())
			.stream()
			.collect(
				Collectors.toMap(
					AccountProvider::getProviderId,
					accountProvider -> accountProvider
				)
			);
		for (Provider provider : allProvider) {

			AccountProvider accountProvider = accountProviderMap.get(provider.getId());

			if (null == accountProvider) {
				accountProvider = new AccountProvider();

			} else if (StringUtils.isNotBlank(accountProvider.getProviderAccount())) {
				try {
					ProviderProxy proxy = ProviderProxyCache.getInstance().
						getProviderProxy(webSiteType, accountProvider.getProviderId(),
							CurrencyType.getInstance(accountCurrency));

					String providerBOAccount = proxy.getProviderBOAccount(accountProvider);
					accountProvider.setProviderBOAccount(providerBOAccount);

				} catch (Exception e) {
					LogUtils.SYS.error(e.getMessage(), e);
				}
			}

			WebsiteProvider websiteProvider = ProviderCache.getInstance()
				.getWebsiteProvider(webSiteType, provider.getId());

			Provider temProvider = new Provider();
			temProvider.setProviderName(websiteProvider.getDisplayName());
			temProvider.setSystemCode(provider.getSystemCode());

			accountProvider.setProvider(temProvider);
			accountProviderList.add(accountProvider);
		}
		return accountProviderList;
	}

	public static boolean updateStatus(WebSiteType webSiteType, Account account, int status, Manager manager,
		String updaterIp) {

		boolean resetLoginFail = status != AccountStatusType.LOCKED.unique();

		Connection conn = null;
		boolean updateResult = false;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			int result = AccountDAO.updateStatus(conn, account.getUserId(), webSiteType.unique(), status,
				resetLoginFail);

			if (result > 0) {
				updateResult = true;
			}

			conn.commit();

		} catch (Deviation e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		if (updateResult) {
			AccountUpdateLog updateLog = new AccountUpdateLog();
			updateLog.setUserId(account.getUserId());
			updateLog.setWebsiteType(account.getWebsiteType());
			updateLog.setUpdater(manager.getUserId());
			updateLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			updateLog.setUpdaterIp(updaterIp);
			updateLog.setLogType(AccountUpdateType.STATUS.unique());
			updateLog.setRecords(JSONUtils.toJsonString(
				new UpdateRecord(String.valueOf(account.getStatus()), String.valueOf(status),
					"Player Status Change From " + AccountStatusType.getInstanceOf(account.getStatus()).name()
						+ " to " + AccountStatusType.getInstanceOf(status).name())));
			updateLog.setCurrencyTypeId(account.getCurrencyTypeId());
			updateLog.setLogTypeStr(AccountUpdateType.STATUS.getName());
			AccountUpdateLogBO.insert(updateLog);

			CloseAutoVerifyWithdrawalBO.verifyPlayerWithdrawalStatus(webSiteType.unique(), account.getCurrencyTypeId(),
				account.getUserId(),
				AccountUpdateType.STATUS.getName(),
				CloseAutoVerifyWithdrawalType.ACCOUNTSTATUS);
		}

		return updateResult;
	}

	public static List<AccountContactInfo> getAccountContactInfos(String userId, WebSiteType webSiteType) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return AccountContactInfoDAO.findAccountContactDataByUserId(conn, userId, webSiteType);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation("failed to get account contact info");
		} finally {
			DbUtils.close(conn);
		}
	}

	public static Account getAccountByUserId(String userId, WebSiteType webSiteType) {
		Connection conn = null;
		Account account = null;
		try {
			conn = DBPool.getReadConnection();
			account = AccountDAO.getAccountByUserId(conn, userId, webSiteType);

			if (account != null) {
				account.setAccountContactInfoList(
					AccountContactInfoDAO.
						findAccountContactDataByUserId(conn, userId, webSiteType));
			}

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
		return account;
	}

	public static String searchUserIdByCurrency(WebSiteType webSiteType, String userId,
		Set<Integer> accessCurrencySet, List<String> selectUserIdList) {

		return AccountBO.searchUserId(webSiteType, userId, accessCurrencySet, selectUserIdList,
			150, 1, "USER_ID",
			DBOrderType.ASC);
	}

	public static String searchUserId(WebSiteType webSiteType, String userId, Set<Integer> accessCurrencySet,
		List<String> selectUserId, int pageSize, int pageNumber, String sortCondition,
		DBOrderType orderType) {

		if (null == selectUserId) {
			selectUserId = new ArrayList<>();
		}

		List<String> list;

		try {
			PageInfo pageInfo = new PageInfo();
			pageInfo.setPageNumber(pageNumber);
			pageInfo.setPageSize(pageSize + selectUserId.size());

			PageResult<Account> data = DbExecutor.query(conn ->
				AccountDAO.getAccountByUserId(conn,
					webSiteType, userId, accessCurrencySet, pageInfo, sortCondition,
					orderType));

			List<String> finalSelectUserId = selectUserId;
			list = data.getResultList().stream()
				.filter(account -> !finalSelectUserId.contains(account.getUserId()))
				.sorted(Comparator
					.comparing(Account::getUserId, Comparator.comparingInt(String::length)))
				.map(Account::getUserId)
				.limit(pageSize)
				.collect(Collectors.toList());
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return JSONUtils.EMPTY_JSON_ARRAY_STRING;
		}
		return JSONUtils.toJsonString(list);
	}

	public static int changePassword(String password, String userID, WebSiteType websiteType, int isForgotPassword,
		boolean unlockAccount) {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			int updateCount = AccountDAO
				.changePassword(conn, password, userID, websiteType, isForgotPassword, unlockAccount);

			conn.commit();

			AccountCache.getInstance().update();

			return updateCount;
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw new InternalErrorException(InternalErrorCodeType.CHANGE_PASSWORD_ERROR, e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
	}

	public static String getRegisterUsers(WebSiteType webSiteType, Timestamp startDate, Timestamp endDate, int currency,
		PageInfo pageInfo, String column, DBOrderType orderType,
		long affiliateId, LangMessage lang) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return AccountDAO.getRegisterUsers(conn, webSiteType, startDate, endDate, currency, pageInfo,
				column, orderType, affiliateId, lang);
		} catch (Exception e) {
			return JSONUtils.EMPTY_JSON_STRING;
		} finally {
			DbUtils.close(conn);
		}
	}

	public static int checkIsAlreadyRegistered(String phoneNumber, String email, int websiteType) {
		try (Connection conn = DBPool.getReadConnection()) {
			return AccountContactInfoDAO
				.checkIsAlreadyRegistered(conn, phoneNumber, email, websiteType);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return -1;
	}

	public static void checkUpdateAttribute(Account userInCache,
		Map<String, PlayerCacheHelper.UpdatedAttributeRecord> updatedAttributeRecordMap,
		List<String[]> accountBankUpdateList,
		List<String[]> accountContactUpdateList,
		List<String[]> multipleTransactionUpdateList,
		List<String[]> accountDocumentUpdateList
	) {

		final boolean firstLogin = userInCache.isFirstLogin();
		final int updateAttribute = userInCache.getUpdatedAttribute();


		if (firstLogin || (UpdatedAttributeType.ACCOUNT_BANK.unique() & updateAttribute) > 0) {
			accountBankUpdateList.add(userInCache.getUserKeyStrs());

			updateSumOfUpdatedAttributes(updatedAttributeRecordMap, userInCache,
				UpdatedAttributeType.ACCOUNT_BANK);
		}

		if (firstLogin || (UpdatedAttributeType.ACCOUNT_CONTACT.unique() & updateAttribute) > 0) {
			accountContactUpdateList.add(userInCache.getUserKeyStrs());

			updateSumOfUpdatedAttributes(updatedAttributeRecordMap, userInCache,
				UpdatedAttributeType.ACCOUNT_CONTACT);
		}

		if (firstLogin || (UpdatedAttributeType.MULTIPLE_TRANSACTION.unique() & updateAttribute) > 0) {

			multipleTransactionUpdateList.add(userInCache.getUserKeyStrs());

			updateSumOfUpdatedAttributes(updatedAttributeRecordMap, userInCache,
				UpdatedAttributeType.MULTIPLE_TRANSACTION);

		}

		if (firstLogin || (UpdatedAttributeType.ACCOUNT_DOCUMENT.unique() & updateAttribute) > 0) {
			accountDocumentUpdateList.add(userInCache.getUserKeyStrs());

			updateSumOfUpdatedAttributes(updatedAttributeRecordMap, userInCache,
				UpdatedAttributeType.ACCOUNT_DOCUMENT);
		}
	}

	public static void updateSumOfUpdatedAttributes(
		Map<String, PlayerCacheHelper.UpdatedAttributeRecord> updatedAttributeRecordMap, Account userInCache,
		UpdatedAttributeType attributeType) {

		updatedAttributeRecordMap
			.computeIfAbsent(userInCache.getUserKey(),
				k -> new PlayerCacheHelper.UpdatedAttributeRecord(userInCache.getUserKeyStrs()))
			.updateSumOfUpdatedAttributes(attributeType);
	}

	public static long getForgetPasswordSendRemainTime(WebSiteType webSiteType, String userId,
		String forgetPasswordSendTimeLimitMinute) {
		OTPRecord cacheOtpRecord = OTPRecordCache.getInstance().getLatestOTPRecordInMinutes(webSiteType,
			userId,
			new OTPType[] {OTPType.FORGOT_PASSWORD_EMAIL},
			Integer.parseInt(forgetPasswordSendTimeLimitMinute));

		if (cacheOtpRecord == null) {
			return 0;
		}

		long lastUseTimeToNow = System.currentTimeMillis() - cacheOtpRecord.getTime().getTime();

		long sendLimitTime = Long.parseLong(forgetPasswordSendTimeLimitMinute) * 1000 * 60;

		return sendLimitTime - lastUseTimeToNow;
	}

	public static void forgetPasswordSuccess(Account account, OTPType otpType, String code, boolean unlockAccount)
		throws Exception {

		AccountUpdateLog accountUpdateLog = AccountUtils.getAccountUpdateLog(account.getUserId(),
			account.getWebsiteType(), AccountUpdateType.UPDATE_PASSWORD,
			new UpdateRecord("", "", "Player Forgot Password"), "SYS", "0.0.0.0", account.getCurrencyTypeId());

		AccountUpdateLogBO.insert(accountUpdateLog);

		if (unlockAccount && account.getStatus() == AccountStatusType.LOCKED.unique()) {
			GlobalThreadPool.execute(() ->
				AccountUpdateLogBO.insert(
					AccountUtils.getAccountUpdateLog(account.getUserId(), account.getWebsiteType(),
						AccountUpdateType.STATUS, new UpdateRecord(String.valueOf(AccountStatusType.LOCKED.unique()),
							String.valueOf(AccountStatusType.ACTIVE.unique()), "Player Forgot Password"), "SYS",
						"0.0.0.0",
						account.getCurrencyTypeId())));
		}

		OTPRecord otpRecord = new OTPRecord();
		otpRecord.setUserId(account.getUserId());
		otpRecord.setWebsiteType(account.getWebsiteType());
		otpRecord.setOtpType(otpType.unique());
		otpRecord.setCode(code);
		otpRecord.setTime(new Timestamp(System.currentTimeMillis()));

		OTPRecordBO.insert(otpRecord);
	}

	public static String getForgetPasswordSendTimeLimitMessage(LangMessage langMessage, long remainMillisecond) {
		long remainMinute = TimeUnit.MILLISECONDS.toMinutes(remainMillisecond);

		if (remainMinute == 0) {
			remainMinute = 1;
		}

		return langMessage.get("msg.forgot.password.sendTimeLimit",
			new String[] {String.valueOf(remainMinute)});
	}

	public static Account getByUserId(String userId, WebSiteType webSiteType) {
		try (Connection conn = DBPool.getReadConnection()) {
			return AccountDAO.getAccountByUserId(conn, userId, webSiteType);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		}
	}

	public static Account getByEmail(String email, WebSiteType webSiteType) throws Exception {
		try {
			return AccountCache.getInstance().getAccount(webSiteType.unique(), email);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		}
	}

	public static boolean updateFavoriteGame(int webSiteType, String userId, String favoriteGames) {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			int count = AccountDAO.updateFavoriteGame(conn, favoriteGames, webSiteType, userId);
			conn.commit();

			return 1 == count;
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
		return false;
	}

	public static void updateViewedPlayResponsiblyToTrue(int webSiteType, String userId) throws Exception {
		DbExecutor.update(conn ->
			AccountDAO.updateViewedPlayResponsibly(conn, userId, webSiteType, BinaryStatusType.ACTIVE.unique())
		);

		AccountCache.getInstance().update();

		String userKey = AccountUtils.getUserKey(webSiteType, userId);
		Account accountInLocalCache = PlayerLocalCache.getInstance().get(userKey);
		if (accountInLocalCache != null && !accountInLocalCache.hasViewedPlayResponsibly()) {
			accountInLocalCache.setViewedPlayResponsibly(BinaryStatusType.ACTIVE.unique());
			PlayerLocalCache.getInstance().put(userKey, accountInLocalCache);
		}
	}

	public static KycPersonalInfo getKycPersonalInfo(WebSiteType webSiteType, String userId) throws Exception {

		List<AccountDocument> accountDocumentList = AccountDocumentBO.findAccountDocuments(userId, webSiteType);
		Optional<AccountDocument> accountDocument = accountDocumentList.stream()
			.filter(doc -> doc.getDocumentType() == DocumentType.SUMSUB_KYC.unique())
			.max(Comparator.comparing(AccountDocument::getId));

		return accountDocument.map(document -> KycPersonalInfoBO.find(document.getId(), userId, webSiteType))
			.orElse(null);
	}
}
