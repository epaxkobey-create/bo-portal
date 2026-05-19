package com.nv.commons.bo;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.cache.AccountCache;
import com.nv.commons.cache.AccountContactInfoCache;
import com.nv.commons.cache.AccountContactInfoVerificationCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.ContactType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.UpdatedAttributeType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dao.AccountContactInfoDAO;
import com.nv.commons.dao.AccountContactInfoVerificationDAO;
import com.nv.commons.dao.AccountDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountContactInfo;
import com.nv.commons.dto.AccountContactInfoVerification;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

public class AccountContactInfoBO {

	public static boolean updateAsVerifiedByPlayer(int webSiteType, String userId, ContactType contactType,
		String contactInfo) {
		Connection conn = null;
		WebSiteType webSite = WebSiteType.getInstance(webSiteType);
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			int result = AccountContactInfoDAO.updateAsVerifiedByPlayer(conn, webSiteType, userId,
				contactType, contactInfo);

			if (result == 0) {
				throw new Exception(
					"Verified Fail ! WebsiteType:" + webSiteType + ", userId: " + userId + ", ContactInfo: "
					+ contactInfo);
			}

			int updateCount = AccountDAO.updateUpdatedAttribute(conn, userId, webSite, UpdatedAttributeType.ACCOUNT_CONTACT);


			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		return true;
	}

	public static boolean updateAsVerifiedByBO(int webSiteType, String userId, String updater, ContactType contactType,
		int contentNo, String updaterIp, int currencyType) {
		Connection conn = null;
		WebSiteType webSite = WebSiteType.getInstance(webSiteType);


		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			if (!AccountContactInfoDAO
				.updateAsVerifiedByBO(conn, webSiteType, userId, contactType, contentNo, updater)) {

				throw new Exception("Verified Fail ! WebsiteType:" + webSiteType + ", userId: " + userId);
			}
			conn.commit();

			Object[][] contactInfoKey = {
				{webSiteType, contactType.unique(), contentNo, userId}};

			AccountContactInfoDAO.refreshUpdateTime(conn, contactInfoKey);

			int updateCount = AccountDAO.updateUpdatedAttribute(conn, userId, webSite, UpdatedAttributeType.ACCOUNT_CONTACT);

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		AccountUpdateLog updateLog = new AccountUpdateLog();
		updateLog.setUserId(userId);
		updateLog.setWebsiteType(webSiteType);
		updateLog.setLogType(AccountUpdateType.EMAIL_STATUS.unique());
		updateLog.setRecords(JSONUtils.toJsonString(new UpdateRecord("Unverified", "Verified",
			"Email verification successful"
		)));
		updateLog.setUpdater(updater);
		updateLog.setUpdaterIp(updaterIp);
		updateLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		updateLog.setCurrencyTypeId(currencyType);
		updateLog.setLogTypeStr(AccountUpdateType.EMAIL_STATUS.getName());

		AccountUpdateLogBO.insert(updateLog);

		return true;
	}

	public static String getVerifiedDataList(String userId, WebSiteType webSiteType) throws Exception {
		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;
		try (Connection conn = DBPool.getReadConnection()) {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			jGenerator.writeArrayFieldStart("verifiedList");

			List<AccountContactInfo> verifiedList = AccountContactInfoDAO
				.findAccountContactDataByUserId(conn, userId, webSiteType);

			if (!CollectionUtils.isEmpty(verifiedList)) {
				for (AccountContactInfo verifiedData : verifiedList) {
					ContactType contactType = ContactType.getInstanceOf(verifiedData.getContactType());
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("cotactType", contactType.unique());
					jGenerator.writeStringField("cotactTypeName", contactType.getName());
					jGenerator.writeNumberField("updateType", contactType.getAccountUpdateType().unique());
					jGenerator.writeNumberField("verified", verifiedData.getVerifiedType());
					boolean isAllowLogin = verifiedData.getContentNo() == 1
										   && verifiedData.getVerifiedType() == BinaryStatusType.ACTIVE.unique();
					jGenerator.writeNumberField("allowLogin", BooleanUtils.toInteger(isAllowLogin));
					jGenerator.writeNumberField("contentNo", verifiedData.getContentNo());
					jGenerator.writeStringField("updater", verifiedData.getUpdater());
					jGenerator.writeStringField("updateTime", FormatUtils.dateFormat(verifiedData.getUpdateTime()));
					jGenerator.writeEndObject();
				}
			}

			jGenerator.writeEndArray();
			jGenerator.writeEndObject();
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	public static String createVerificationThenReturnVerifyCode(WebSiteType webSiteType, String userId,
		ContactType contactType, CurrencyType currencyType, String contactInfo) {
		Connection conn = null;
		String verifyCode;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			Timestamp now = new Timestamp(System.currentTimeMillis());

			if (contactType != ContactType.Email) {
				throw new Exception("Invalid ContactType! WebsiteType:" + webSiteType.unique() + ", userId: " + userId +
									", ContactType: " + contactType.unique() + ", ContactInfo: " + contactInfo);
			}

			AccountContactInfo accountContactInfo = AccountContactInfoCache.getInstance().getContactInfo(
				webSiteType.unique(), userId, contactType.unique());
			if (accountContactInfo == null
				|| accountContactInfo.getIsDeleted() == BinaryStatusType.ACTIVE.unique()) {
				throw new Exception("No Contact Found! WebsiteType:" + webSiteType.unique() + ", userId: " + userId +
									", ContactType: " + contactType.unique() + ", ContactInfo: " + contactInfo);
			}

			AccountContactInfoVerification accountContactInfoVerification = AccountContactInfoVerificationCache.getInstance()
				.getContactInfoVerificationList(webSiteType.unique(), userId,
					accountContactInfo.getContactType(), accountContactInfo.getContent()).stream()
				.max(Comparator.comparing(AccountContactInfoVerification::getCreateTime))
				.orElse(null);

			if (accountContactInfoVerification != null) {
				// check email/link is verified, return exception
				if (accountContactInfo.isVerified() || accountContactInfoVerification.isVerified()) {
					throw new Deviation().setI18N("msg.error.account.verification.alreadyVerified");
				}
				// check request within 5 minutes, return exception
				long lastRequestTimeDiff = DateUtils.secondsBetween(accountContactInfoVerification.getUpdateTime(),
					now);
				if (lastRequestTimeDiff >= 0 && lastRequestTimeDiff < 300) {

					if (!"junit".equals(System.getProperty(SystemConstants.RUNTIME_ENV))) {
						throw new Deviation().setI18N("msg.error.account.verification.requestTooFrequent",
							String.valueOf((int) Math.ceil((300 - lastRequestTimeDiff) / 60.0))
						);
					}
				}
				// check haven't expired, return existing verifyCode
				if (DateUtils.secondsBetween(accountContactInfoVerification.getValidTo(), now) <= 0) {
					verifyCode = accountContactInfoVerification.getVerifyCode();
					accountContactInfoVerification.setUpdater(userId);
					AccountContactInfoVerificationDAO.update(conn, accountContactInfoVerification);
					conn.commit();
					AccountContactInfoVerificationCache.getInstance().update();
					return verifyCode;
				}
			}

			verifyCode = insertVerification(conn, accountContactInfo, currencyType);
			conn.commit();

			AccountContactInfoVerificationCache.getInstance().update();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			if (e instanceof Deviation deviation) {
				throw deviation;
			}
			return "";
		} finally {
			DbUtils.close(conn);
		}

		return verifyCode;
	}

	private static String insertVerification(Connection conn, AccountContactInfo accountContactInfo,
		CurrencyType currencyType) throws Exception {

		String verifyCode = UUID.randomUUID().toString();

		String valueByKey = WebsiteSystemSettingCache.getInstance().getValueByKey(
			accountContactInfo.getWebsiteType(), currencyType.unique(),
			WebsiteSystemSettingType.BT_USER_VERIFY_LINK_EXPIRY_IN_SECONDS.unique()
		);

		long expiryInSeconds = Long.parseLong(valueByKey);

		Timestamp now = new Timestamp(System.currentTimeMillis());
		Timestamp validTo = Timestamp.from(
			now.toInstant().plus(Duration.ofSeconds(
				expiryInSeconds
			))
		);

		AccountContactInfoVerification accountContactInfoVerification = accountContactInfo.toAccountContactInfoVerification();
		accountContactInfoVerification.setVerifyCode(verifyCode);
		accountContactInfoVerification.setValidFrom(now);
		accountContactInfoVerification.setValidTo(validTo);
		accountContactInfoVerification.setIsVerified(BinaryStatusType.INACTIVE.unique());
		accountContactInfoVerification.setCreator(accountContactInfo.getUserId());
		accountContactInfoVerification.setCreateTime(now);
		accountContactInfoVerification.setUpdater(accountContactInfo.getUserId());
		accountContactInfoVerification.setUpdateTime(now);

		int result = AccountContactInfoVerificationDAO.insert(conn, accountContactInfoVerification);

		if (result == 0) {
			throw new Exception(
				"Create Verification Failed! WebsiteType:" + accountContactInfo.getWebsiteType() + ", userId: "
				+ accountContactInfoVerification.getUserId()
				+ ", ContactType: " + accountContactInfoVerification.getContactType() + ", ContactInfo: "
				+ accountContactInfo.getContent());
		}

		return verifyCode;
	}

	public static void updateVerification(WebSiteType webSiteType, String verifyCode, String playerIp) {
		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			Timestamp now = new Timestamp(System.currentTimeMillis());

			AccountContactInfoVerification accountContactInfoVerification = AccountContactInfoVerificationCache.getInstance()
				.getContactInfoVerification(verifyCode);

			if (accountContactInfoVerification != null) {
				String userId = accountContactInfoVerification.getUserId();
				ContactType contactType = ContactType.getInstanceOf(accountContactInfoVerification.getContactType());
				String content = accountContactInfoVerification.getContent();

				AccountContactInfoVerification accountContactInfoVerificationLatest = AccountContactInfoVerificationCache.getInstance()
					.getContactInfoVerificationList(webSiteType.unique(), userId,
						contactType.unique(), content).stream()
					.max(Comparator.comparing(AccountContactInfoVerification::getCreateTime))
					.orElse(null);

				if (!verifyCode.equals(accountContactInfoVerificationLatest.getVerifyCode())) {
					throw new Deviation().setI18N("msg.error.account.verification.isNotValidated");
				}

				AccountContactInfo accountContactInfo = AccountContactInfoCache.getInstance()
					.getContactInfo(webSiteType.unique(), userId, contactType.unique());
				if (accountContactInfo == null
					|| accountContactInfo.getIsDeleted() == BinaryStatusType.ACTIVE.unique()) {
					throw new Exception(
						"No Contact Found! WebsiteType:" + webSiteType.unique() + ", userId: " + userId +
							", ContactType: " + contactType.unique() + ", ContactInfo: " + content);
				}

				// check email is not same, return exception
				if (!accountContactInfoVerification.getContent().equalsIgnoreCase(accountContactInfo.getContent())) {
					throw new Deviation().setI18N("msg.error.account.verification.isNotValidated");
				}
				// check email/link is verified, return exception
				if (accountContactInfo.isVerified() || accountContactInfoVerification.isVerified()) {
					throw new Deviation().setI18N("msg.error.account.verification.linkVerified");
				}
				// check link is expired, return exception
				if (DateUtils.secondsBetween(accountContactInfoVerification.getValidTo(), now) >= 0) {
					throw new Deviation().setI18N("msg.error.account.verification.isExpired");
				}

				// valid
				int updateCount = AccountContactInfoVerificationDAO.updateAsVerified(conn,
					accountContactInfoVerification);

				if (updateCount != 0) {
					boolean isUpdateAsVerifiedByPlayer = updateAsVerifiedByPlayer(webSiteType.unique(), userId,
						contactType, content);

					if (isUpdateAsVerifiedByPlayer) {
						Account account = AccountCache.getInstance().getAccount(webSiteType.unique(), userId);

						AccountUpdateLog accountUpdateLog = AccountUtils.getAccountUpdateLog(userId,
							webSiteType.unique(), AccountUpdateType.EMAIL_STATUS,
							new UpdateRecord("Unverified", "Verified", "Player's Email Verified"), userId,
							playerIp, account.getCurrencyTypeId());

						AccountUpdateLogBO.insert(accountUpdateLog);
					}
				}
			} else {
				throw new Deviation().setI18N("msg.error.account.verification.isNotValidated");
			}

			conn.commit();

			AccountContactInfoVerificationCache.getInstance().update();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			if (e instanceof Deviation deviation) {
				throw deviation;
			}
		} finally {
			DbUtils.close(conn);
		}
	}
}
